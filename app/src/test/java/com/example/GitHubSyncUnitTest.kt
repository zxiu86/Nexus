package com.example

import com.example.data.network.GitHubNetworkModule
import org.junit.Assert.*
import org.junit.Test

class GitHubSyncUnitTest {

    @Test
    fun testGitHubDefaultConfig() {
        assertEquals("zxiu86", GitHubNetworkModule.DEFAULT_REPO_OWNER)
        assertEquals("Data", GitHubNetworkModule.DEFAULT_REPO_NAME)
        assertEquals("main", GitHubNetworkModule.DEFAULT_BRANCH)
    }

    @Test
    fun testVersionMatches201() {
        assertEquals("2.0.1", com.example.util.AppVersionConfig.VERSION_NAME)
        assertEquals(201, com.example.util.AppVersionConfig.VERSION_CODE)
    }
}
