package com.example.data.model

import androidx.annotation.Keep

@Keep
data class NexusUser(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String? = null,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Keep
data class AdminAnnouncement(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val priority: String = "info", // "info", "warning", "update", "urgent"
    val timestamp: Long = System.currentTimeMillis(),
    val active: Boolean = true,
    val authorEmail: String = ""
)

@Keep
data class CloudUserData(
    val favorites: List<String> = emptyList(),
    val readLater: List<String> = emptyList(),
    val history: List<ReadingHistoryEntry> = emptyList(),
    val readChapters: Map<String, List<Int>> = emptyMap(),
    val lastSynced: Long = System.currentTimeMillis()
)

sealed class AuthResult {
    data class Success(val user: NexusUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
    object Loading : AuthResult()
}
