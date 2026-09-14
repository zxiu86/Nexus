#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <deque>
#include <thread>
#include <mutex>
#include <atomic>
#include <future>
#include <regex>
#include <algorithm>
#include <filesystem>
#include <chrono>
#include <iomanip>
#include <sstream>

#include <curl/curl.h>
#include <tesseract/baseapi.h>
#include <leptonica/allheaders.h>

namespace fs = std::filesystem;

// Structure for normalized watermark bounding box
struct NormalizedBoundingBox {
    float x{0.0f};
    float y{0.0f};
    float width{0.0f};
    float height{0.0f};
};

struct PageWatermark {
    int page_number{1};
    std::string original_text{"olympustaff.com"};
    std::string replacement_text{"تطبيق Nexus"};
    std::vector<NormalizedBoundingBox> boxes;
};

struct ChapterJob {
    std::string series_slug;
    int chapter_number{1};
    std::vector<std::string> image_urls;
    std::string output_path;
};

// Target keywords for detection (case-insensitive & whitespace-stripped)
const std::vector<std::string> TARGET_KEYWORDS = {
    "olympustaff.com",
    "olympustaff",
    "olympus",
    "olympus-scans",
    "olympusscan",
    "olympus staff",
    "olympustaf",
    "olympus.com"
};

// libcurl write callback to memory
static size_t WriteMemoryCallback(void* contents, size_t size, size_t nmemb, void* userp) {
    size_t realsize = size * nmemb;
    auto* mem = static_cast<std::vector<unsigned char>*>(userp);
    size_t oldSize = mem->size();
    mem->resize(oldSize + realsize);
    std::memcpy(mem->data() + oldSize, contents, realsize);
    return realsize;
}

// Download image into memory vector
bool DownloadImage(const std::string& url, std::vector<unsigned char>& outBuffer) {
    CURL* curl = curl_easy_init();
    if (!curl) return false;

    curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
    curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteMemoryCallback);
    curl_easy_setopt(curl, CURLOPT_WRITEDATA, &outBuffer);
    curl_easy_setopt(curl, CURLOPT_USERAGENT, "Nexus-Cleaner-Bot/1.0 (C++ OCR Engine)");
    curl_easy_setopt(curl, CURLOPT_TIMEOUT, 20L);
    curl_easy_setopt(curl, CURLOPT_FOLLOWLOCATION, 1L);
    curl_easy_setopt(curl, CURLOPT_SSL_VERIFYPEER, 0L);

    CURLcode res = curl_easy_perform(curl);
    long http_code = 0;
    curl_easy_getinfo(curl, CURLINFO_RESPONSE_CODE, &http_code);
    curl_easy_cleanup(curl);

    return (res == CURLE_OK && http_code == 200 && !outBuffer.empty());
}

// String cleanup for robust matching
std::string CleanText(const std::string& input) {
    std::string result;
    for (char c : input) {
        if (std::isalnum(static_cast<unsigned char>(c)) || c == '.') {
            result += static_cast<char>(std::tolower(static_cast<unsigned char>(c)));
        }
    }
    return result;
}

bool MatchesTargetWatermark(const std::string& text) {
    std::string cleaned = CleanText(text);
    if (cleaned.empty()) return false;
    for (const auto& kw : TARGET_KEYWORDS) {
        std::string cleanKw = CleanText(kw);
        if (cleaned.find(cleanKw) != std::string::npos || (cleanKw.find(cleaned) != std::string::npos && cleaned.length() >= 7)) {
            return true;
        }
    }
    return false;
}

// Thread-local Tesseract OCR analyzer
class OCRAnalyzer {
public:
    OCRAnalyzer() {
        tess_ = new tesseract::TessBaseAPI();
        if (tess_->Init(nullptr, "eng", tesseract::OEM_LSTM_ONLY)) {
            // Fallback to default OEM if LSTM not present
            tess_->Init(nullptr, "eng");
        }
        tess_->SetPageSegMode(tesseract::PSM_SPARSE_TEXT);
    }

    ~OCRAnalyzer() {
        if (tess_) {
            tess_->End();
            delete tess_;
        }
    }

    std::vector<NormalizedBoundingBox> AnalyzeImage(const std::vector<unsigned char>& imgBytes) {
        std::vector<NormalizedBoundingBox> detectedBoxes;
        if (imgBytes.empty()) return detectedBoxes;

        Pix* image = pixReadMem(imgBytes.data(), imgBytes.size());
        if (!image) return detectedBoxes;

        int imgWidth = pixGetWidth(image);
        int imgHeight = pixGetHeight(image);
        if (imgWidth <= 0 || imgHeight <= 0) {
            pixDestroy(&image);
            return detectedBoxes;
        }

        tess_->SetImage(image);
        tess_->Recognize(nullptr);

        tesseract::ResultIterator* ri = tess_->GetIterator();
        tesseract::PageIteratorLevel level = tesseract::RIL_TEXTLINE;

        if (ri != nullptr) {
            do {
                const char* lineText = ri->GetUTF8Text(level);
                if (lineText) {
                    std::string textStr(lineText);
                    delete[] lineText;

                    if (MatchesTargetWatermark(textStr)) {
                        int x1, y1, x2, y2;
                        ri->BoundingBox(level, &x1, &y1, &x2, &y2);

                        // Calculate padded bounding box
                        float boxW = static_cast<float>(x2 - x1);
                        float boxH = static_cast<float>(y2 - y1);
                        float padX = std::max(boxW * 0.12f, 10.0f);
                        float padY = std::max(boxH * 0.20f, 8.0f);

                        float left = std::max(0.0f, x1 - padX);
                        float top = std::max(0.0f, y1 - padY);
                        float right = std::min(static_cast<float>(imgWidth), x2 + padX);
                        float bottom = std::min(static_cast<float>(imgHeight), y2 + padY);

                        NormalizedBoundingBox norm;
                        norm.x = left / imgWidth;
                        norm.y = top / imgHeight;
                        norm.width = (right - left) / imgWidth;
                        norm.height = (bottom - top) / imgHeight;

                        // Clamp between 0.0 and 1.0
                        norm.x = std::clamp(norm.x, 0.0f, 1.0f);
                        norm.y = std::clamp(norm.y, 0.0f, 1.0f);
                        norm.width = std::clamp(norm.width, 0.01f, 1.0f);
                        norm.height = std::clamp(norm.height, 0.01f, 1.0f);

                        detectedBoxes.push_back(norm);
                    }
                }
            } while (ri->Next(level));
            delete ri;
        }

        tess_->Clear();
        pixDestroy(&image);
        return detectedBoxes;
    }

private:
    tesseract::TessBaseAPI* tess_{nullptr};
};

// Simple JSON Writer for coordinates output
void WriteCoordinatesJson(const ChapterJob& job, const std::vector<PageWatermark>& pages) {
    fs::create_directories(fs::path(job.output_path).parent_path());

    std::ofstream out(job.output_path);
    if (!out.is_open()) return;

    out << "{\n";
    out << "  \"series_slug\": \"" << job.series_slug << "\",\n";
    out << "  \"chapter_number\": " << job.chapter_number << ",\n";
    out << "  \"total_pages\": " << job.image_urls.size() << ",\n";
    out << "  \"version\": 1,\n";
    out << "  \"updated_at\": " << std::chrono::duration_cast<std::chrono::seconds>(std::chrono::system_clock::now().time_since_epoch()).count() << ",\n";
    out << "  \"pages\": [\n";

    for (size_t i = 0; i < pages.size(); ++i) {
        const auto& p = pages[i];
        out << "    {\n";
        out << "      \"page_number\": " << p.page_number << ",\n";
        out << "      \"original_text\": \"" << p.original_text << "\",\n";
        out << "      \"replacement_text\": \"" << p.replacement_text << "\",\n";
        out << "      \"boxes\": [\n";
        for (size_t b = 0; b < p.boxes.size(); ++b) {
            const auto& box = p.boxes[b];
            out << "        {\"x\": " << std::fixed << std::setprecision(4) << box.x
                << ", \"y\": " << box.y
                << ", \"width\": " << box.width
                << ", \"height\": " << box.height << "}";
            if (b + 1 < p.boxes.size()) out << ",";
            out << "\n";
        }
        out << "      ]\n";
        out << "    }";
        if (i + 1 < pages.size()) out << ",";
        out << "\n";
    }

    out << "  ]\n";
    out << "}\n";
    out.close();
}

// Parse Image URLs from chapter json file
std::vector<std::string> ExtractImageUrlsFromJson(const std::string& filePath) {
    std::vector<std::string> urls;
    std::ifstream file(filePath);
    if (!file.is_open()) return urls;

    std::string content((std::istreambuf_iterator<char>(file)), std::istreambuf_iterator<char>());
    file.close();

    // Regex match URLs (http/https ending in image extensions or generic http link in quotes)
    std::regex urlRegex("\"(https?:\\/\\/[^\"]+?\\.(?:jpg|jpeg|png|webp|avif)(?:\\?[^\"]*)?)\"", std::regex::icase);
    auto words_begin = std::sregex_iterator(content.begin(), content.end(), urlRegex);
    auto words_end = std::sregex_iterator();

    for (std::sregex_iterator i = words_begin; i != words_end; ++i) {
        std::smatch match = *i;
        urls.push_back(match[1].str());
    }

    // Fallback: match any "http..." inside "images" / "pages" arrays
    if (urls.empty()) {
        std::regex genericUrlRegex("\"(https?:\\/\\/[^\"]+)\"");
        auto g_begin = std::sregex_iterator(content.begin(), content.end(), genericUrlRegex);
        auto g_end = std::sregex_iterator();
        for (std::sregex_iterator i = g_begin; i != g_end; ++i) {
            std::smatch match = *i;
            std::string u = match[1].str();
            if (u.find("github.com") == std::string::npos && u.find("schema.org") == std::string::npos) {
                urls.push_back(u);
            }
        }
    }

    return urls;
}

int main(int argc, char* argv[]) {
    curl_global_init(CURL_GLOBAL_ALL);

    int max_chapters = 120;
    if (const char* env_limit = std::getenv("BATCH_LIMIT")) {
        try { max_chapters = std::stoi(env_limit); } catch (...) {}
    }
    if (argc > 1) {
        try { max_chapters = std::stoi(argv[1]); } catch (...) {}
    }

    std::cout << "========================================\n";
    std::cout << "🚀 Nexus Watermark Cleaner Bot (C++ High-Speed)\n";
    std::cout << "📦 Target Batch Limit: " << max_chapters << " chapters\n";
    std::cout << "========================================\n";

    fs::path data_dir = "data";
    fs::path coords_dir = "coordinates";

    if (!fs::exists(data_dir)) {
        std::cout << "⚠️ 'data' directory not found in current workspace. Exiting.\n";
        curl_global_cleanup();
        return 0;
    }

    fs::create_directories(coords_dir);

    std::vector<ChapterJob> pending_jobs;

    // Scan data directory for chapters
    for (const auto& series_entry : fs::directory_iterator(data_dir)) {
        if (!series_entry.is_directory()) continue;
        std::string series_slug = series_entry.path().filename().string();

        for (const auto& ch_entry : fs::directory_iterator(series_entry.path())) {
            if (!ch_entry.is_regular_file() || ch_entry.path().extension() != ".json") continue;

            std::string filename = ch_entry.path().stem().string();
            // Check if file represents a chapter (e.g. 1.json, 10.json, chapter_1.json)
            if (filename == "info" || filename == "series" || filename == "metadata") continue;

            int chNum = 1;
            try {
                std::string digits;
                for (char c : filename) { if (std::isdigit(c)) digits += c; }
                if (!digits.empty()) chNum = std::stoi(digits);
            } catch (...) {
                chNum = 1;
            }

            fs::path expected_coord_path = coords_dir / series_slug / (std::to_string(chNum) + ".json");
            if (fs::exists(expected_coord_path)) {
                continue; // Already processed!
            }

            std::vector<std::string> imageUrls = ExtractImageUrlsFromJson(ch_entry.path().string());
            if (imageUrls.empty()) continue;

            ChapterJob job;
            job.series_slug = series_slug;
            job.chapter_number = chNum;
            job.image_urls = imageUrls;
            job.output_path = expected_coord_path.string();

            pending_jobs.push_back(job);
            if (static_cast<int>(pending_jobs.size()) >= max_chapters) {
                break;
            }
        }
        if (static_cast<int>(pending_jobs.size()) >= max_chapters) {
            break;
        }
    }

    std::cout << "🔍 Found " << pending_jobs.size() << " chapters requiring watermark scanning & cleaning.\n";
    if (pending_jobs.empty()) {
        std::cout << "✅ All chapters in data/ are already up to date and cleaned!\n";
        curl_global_cleanup();
        return 0;
    }

    // Process with Multi-threaded Pool
    unsigned int num_threads = std::clamp(std::thread::hardware_concurrency(), 2u, 8u);
    std::cout << "⚡ Spawning " << num_threads << " high-performance C++ worker threads...\n";

    std::mutex queue_mutex;
    std::deque<ChapterJob> job_queue(pending_jobs.begin(), pending_jobs.end());
    std::atomic<int> completed_count{0};
    std::atomic<int> total_watermarks_found{0};

    std::vector<std::thread> workers;
    for (unsigned int t = 0; t < num_threads; ++t) {
        workers.emplace_back([&]() {
            OCRAnalyzer analyzer;

            while (true) {
                ChapterJob job;
                {
                    std::lock_guard<std::mutex> lock(queue_mutex);
                    if (job_queue.empty()) break;
                    job = job_queue.front();
                    job_queue.pop_front();
                }

                std::vector<PageWatermark> detectedPages;

                for (size_t pIdx = 0; pIdx < job.image_urls.size(); ++pIdx) {
                    std::vector<unsigned char> imgBytes;
                    if (DownloadImage(job.image_urls[pIdx], imgBytes)) {
                        auto boxes = analyzer.AnalyzeImage(imgBytes);
                        if (!boxes.empty()) {
                            PageWatermark pwm;
                            pwm.page_number = static_cast<int>(pIdx + 1);
                            pwm.boxes = boxes;
                            detectedPages.push_back(pwm);
                            total_watermarks_found += boxes.size();
                        }
                    }
                }

                WriteCoordinatesJson(job, detectedPages);
                int done = ++completed_count;
                std::cout << "[" << done << "/" << pending_jobs.size() << "] Processed "
                          << job.series_slug << " - Chapter " << job.chapter_number
                          << " (" << detectedPages.size() << " pages cleaned)\n";
            }
        });
    }

    for (auto& w : workers) {
        if (w.joinable()) w.join();
    }

    std::cout << "========================================\n";
    std::cout << "🎉 Completed processing " << completed_count << " chapters.\n";
    std::cout << "🎯 Total watermark boxes cleaned: " << total_watermarks_found << "\n";
    std::cout << "📁 Coordinates saved to: " << coords_dir.string() << "/\n";
    std::cout << "========================================\n";

    curl_global_cleanup();
    return 0;
}
