package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey val id: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val bio: String,
    val photoUrl: String,
    val isOnline: Boolean,
    val lastSeenStr: String,
    val isBlocked: Boolean = false
) {
    val fullName: String get() = if (lastName.isEmpty()) firstName else "$firstName $lastName"
}

@Entity(tableName = "chats")
data class Chat(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: String, // "PRIVATE", "SECRET", "GROUP", "CHANNEL", "COMMUNITY"
    val photoUrl: String,
    val memberCount: Int = 0,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val parentCommunityId: String? = null // For groups/channels under a Community
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val chatId: String,
    val senderId: String,
    val senderName: String,
    val senderPhotoUrl: String = "",
    val text: String,
    val timestamp: Long,
    val isSent: Boolean = true,
    val isRead: Boolean = false,
    val isSecret: Boolean = false,
    val attachmentName: String = "", // E.g., Dark_Mode_Tokens_v2.fig
    val attachmentSize: String = "", // E.g., 2.4 MB
    val attachmentType: String = "", // "IMAGE", "FILE", "VOICE", ""
    val attachmentUrl: String = "",
    val reactionsStr: String = "",  // Comma-separated reactions: "👍,❤️"
    val replyToId: String = "",
    val replyToText: String = "",
    val isScheduled: Boolean = false,
    val isE2EEncrypted: Boolean = false
)

@Entity(tableName = "communities")
data class Community(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val bannerUrl: String,
    val photoUrl: String,
    val subscriberCount: Int = 0,
    val isPublic: Boolean = true
)

@Entity(tableName = "stories")
data class Story(
    @PrimaryKey val id: String,
    val userId: String,
    val username: String,
    val avatarUrl: String,
    val imageUrl: String,
    val caption: String,
    val timestamp: Long,
    val isViewed: Boolean = false,
    val isArchived: Boolean = false
)

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey val id: String,
    val amount: String,
    val label: String, // "Creator Premium Subscription", "Nogram Token Transfer"
    val timestamp: Long,
    val isSuccess: Boolean = true,
    val isIncoming: Boolean = false // Green incoming vs red outgoing
)
