package com.example.repository

import android.content.Context
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.ChatDao
import com.example.data.local.ContactDao
import com.example.data.local.MessageDao
import com.example.data.local.CommunityDao
import com.example.data.local.StoryDao
import com.example.data.local.WalletDao
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

class ChatRepository private constructor(private val context: Context) {

    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "nogram_local_db"
    ).build()

    val contactDao: ContactDao = database.contactDao()
    val chatDao: ChatDao = database.chatDao()
    val messageDao: MessageDao = database.messageDao()
    val communityDao: CommunityDao = database.communityDao()
    val storyDao: StoryDao = database.storyDao()
    val walletDao: WalletDao = database.walletDao()

    // Key management state (E2E Identity Keys)
    private val _fingerprint = MutableStateFlow("NOGRAM-E2EE-8409-1290-7731-0144-8891")
    val fingerprint: StateFlow<String> = _fingerprint

    private val _identityPublicKey = MutableStateFlow("MCowBQYDK2VwAyEAd5+n2VvB1Z98R/n+O4b8fX2a6S...")
    val identityPublicKey: StateFlow<String> = _identityPublicKey

    private val _preKeyStatus = MutableStateFlow("Active: Prekey refreshed (100 generated)")
    val preKeyStatus: StateFlow<String> = _preKeyStatus

    init {
        // Pre-populating dummy data is disabled per user request.
        // App will start fresh and authenticate / sync via Firebase.
    }

    suspend fun populateInitialData() {
        // 1. Check if we already have chats inside database
        val existingChats = database.chatDao().getAllChats()
        // Simple synchronous check
        val currentChats = withContext(Dispatchers.IO) {
            database.chatDao().getChatById("chat_design_alpha")
        }
        if (currentChats != null) return // Already populated!

        // 2. Insert robust contacts matching screens
        val contacts = listOf(
            Contact("usr_alice_freeman", "alice_freeman", "Alice", "Freeman", "Design is life.", "", true, "online"),
            Contact("usr_michael_chen", "michael_chen", "Michael", "Chen", "Building Nogram Android!", "", false, "last seen 10 minutes ago"),
            Contact("usr_sarah_jenkins", "sarah_jenkins", "Sarah", "Jenkins", "Admin | Lead Product Designer", "", false, "last seen recently"),
            Contact("usr_david_chen", "david_chen", "David", "Chen", "Quality & Perfection.", "", true, "online"),
            Contact("usr_sarah_connor", "sarah_connor", "Sarah", "Connor", "Live free or die trying.", "", false, "last seen recently"),
            Contact("usr_alex_chen", "alex_chen", "Alex", "Chen", "Working on UI elements...", "", false, "last seen 10 minutes ago"),
            Contact("usr_maria_garcia", "maria_garcia", "Maria", "Garcia", "Coffee & Code.", "", false, "last seen 1 hour ago"),
            Contact("usr_john_doe", "john_doe", "John", "Doe", "Secret agent E2E enthusiast.", "", true, "online"),
            Contact("usr_alice_anderson", "alice_anderson", "Alice", "Anderson", "Product Manager", "", true, "online"),
            Contact("usr_arthur_avery", "arthur_avery", "Arthur", "Avery", "Tech Lead", "", false, "last seen recently"),
            Contact("usr_bianca_bell", "bianca_bell", "Bianca", "Bell", "QA Specialist", "", false, "last seen 2 hours ago"),
            Contact("usr_benjamin_brooks", "benjamin_brooks", "Benjamin", "Brooks", "Backend Engineer", "", true, "online"),
            Contact("usr_catherine_cole", "catherine_cole", "Catherine", "Cole", "Database Master", "", false, "last seen yesterday"),
            Contact("usr_charlie_conway", "charlie_conway", "Charlie", "Conway", "Security Audit", "", false, "last seen within a week"),
            Contact("usr_david_diaz", "david_diaz", "David", "Diaz", "UI Designer", "", true, "online")
        )
        database.contactDao().insertAllContacts(contacts)

        // 3. Insert Chats matching screenshots
        val chats = listOf(
            Chat(
                id = "chat_design_alpha",
                title = "Design Team Alpha",
                description = "128 members, 12 online",
                type = "GROUP",
                photoUrl = "design_team_alpha",
                memberCount = 128,
                unreadCount = 3,
                isPinned = true
            ),
            Chat(
                id = "chat_alice_freeman",
                title = "Alice Freeman",
                description = "Typing...",
                type = "PRIVATE",
                photoUrl = "alice_freeman",
                unreadCount = 0,
                isPinned = true
            ),
            Chat(
                id = "chat_michael_chen",
                title = "Michael Chen",
                description = "Can we reschedule the meeting to Thursd...",
                type = "PRIVATE",
                photoUrl = "michael_chen",
                unreadCount = 0,
                isPinned = false
            ),
            Chat(
                id = "chat_announcements",
                title = "Announcements",
                description = "System maintenance scheduled for we...",
                type = "CHANNEL",
                photoUrl = "announcements",
                memberCount = 245000,
                unreadCount = 0,
                isPinned = false
            ),
            Chat(
                id = "chat_project_weekend",
                title = "Project Weekend",
                description = "You: Sounds like a plan! Let's do it.",
                type = "GROUP",
                photoUrl = "project_weekend",
                memberCount = 5,
                unreadCount = 0,
                isPinned = false
            ),
            Chat(
                id = "chat_john_doe",
                title = "John Doe",
                description = "online",
                type = "SECRET", // SECRET CHAT WITH E2E
                photoUrl = "john_doe",
                unreadCount = 0,
                isPinned = false
            )
        )
        database.chatDao().insertAllChats(chats)

        // 4. Populate Messages inside chats
        val messages = listOf(
            // Design Team Alpha
            Message(
                id = "msg1",
                chatId = "chat_design_alpha",
                senderId = "usr_sarah_jenkins",
                senderName = "Sarah Jenkins",
                text = "Hey team, just dropping the updated color tokens for the new dark mode implementation. Please review before the sync at 2 PM.",
                timestamp = System.currentTimeMillis() - 3600000 * 2 // 2 hours ago
            ),
            Message(
                id = "msg2",
                chatId = "chat_design_alpha",
                senderId = "usr_sarah_jenkins",
                senderName = "Sarah Jenkins",
                text = "Attachment File",
                timestamp = System.currentTimeMillis() - 3600000 * 2 + 1000,
                attachmentName = "Dark_Mode_Tokens_v2.fig",
                attachmentSize = "2.4 MB",
                attachmentType = "FILE"
            ),
            Message(
                id = "msg3",
                chatId = "chat_design_alpha",
                senderId = "usr_david_chen",
                senderName = "David Chen",
                text = "Looks solid to me. I noticed the `surface-container-highest` might be a bit too low contrast against the primary text in some contexts though. I'll add a comment in the file.",
                timestamp = System.currentTimeMillis() - 3600000 - 300000 // 1h 5m ago
            ),
            Message(
                id = "msg4",
                chatId = "chat_design_alpha",
                senderId = "me",
                senderName = "You",
                text = "I agree with David. Let's look at boosting that contrast ratio to at least 4.5:1 before we hand off to engineering.",
                timestamp = System.currentTimeMillis() - 1800000, // 30 mins ago
                isRead = true
            ),

            // John Doe Secret Chat
            Message(
                id = "msg_j1",
                chatId = "chat_john_doe",
                senderId = "usr_john_doe",
                senderName = "John Doe",
                text = "Hey! Are we still on for the design review meeting at 2 PM?",
                timestamp = System.currentTimeMillis() - 3600000,
                isSecret = true
            ),
            Message(
                id = "msg_j2",
                chatId = "chat_john_doe",
                senderId = "usr_john_doe",
                senderName = "John Doe",
                text = "I've got the latest mockups ready for the new dashboard.",
                timestamp = System.currentTimeMillis() - 3500000,
                isSecret = true
            ),
            Message(
                id = "msg_j3",
                chatId = "chat_john_doe",
                senderId = "me",
                senderName = "You",
                text = "Yes, absolutely. I'm looking forward to seeing them.",
                timestamp = System.currentTimeMillis() - 3400000,
                isRead = true,
                isSecret = true
            ),
            Message(
                id = "msg_j4",
                chatId = "chat_john_doe",
                senderId = "usr_john_doe",
                senderName = "John Doe",
                text = "dashboard_v2.png",
                timestamp = System.currentTimeMillis() - 3200000,
                isSecret = true,
                attachmentName = "dashboard_v2.png",
                attachmentSize = "184 KB",
                attachmentType = "IMAGE"
            ),
            Message(
                id = "msg_j5",
                chatId = "chat_john_doe",
                senderId = "usr_john_doe",
                senderName = "John Doe",
                text = "Here's a quick preview.",
                timestamp = System.currentTimeMillis() - 3100000,
                isSecret = true
            )
        )
        for (m in messages) {
            database.messageDao().insertMessage(m)
        }

        // 5. Populate Communities
        val communities = listOf(
            Community(
                id = "com_global_tech",
                name = "Global Tech Hub",
                description = "The largest community for developers, designers, and tech enthusiasts to share knowledge.",
                bannerUrl = "",
                photoUrl = "globe",
                subscriberCount = 245000,
                isPublic = true
            ),
            Community(
                id = "com_creative_arts",
                name = "Creative Arts Collective",
                description = "A space for digital artists, illustrators, and animators to showcase work and seek feedback.",
                bannerUrl = "",
                photoUrl = "palette",
                subscriberCount = 89000,
                isPublic = true
            ),
            Community(
                id = "com_outdoor",
                name = "Outdoor Adventures",
                description = "Connecting hikers, climbers, and nature lovers worldwide. Share trail reports.",
                bannerUrl = "",
                photoUrl = "mountain",
                subscriberCount = 1200000,
                isPublic = true
            ),
            Community(
                id = "com_python",
                name = "Python Developers Network",
                description = "Dedicated strictly to Python programming, from beginners learning to experts.",
                bannerUrl = "",
                photoUrl = "code",
                subscriberCount = 560000,
                isPublic = true
            )
        )
        database.communityDao().insertAllCommunities(communities)

        // 6. Populate Stories
        val stories = listOf(
            Story("story1", "usr_alice_freeman", "Alice Freeman", "", "", "New vector artwork workflow! 🎨", System.currentTimeMillis() - 7200000),
            Story("story2", "usr_michael_chen", "Michael Chen", "", "", "Kotlin Multiplatform is awesome!", System.currentTimeMillis() - 14400000)
        )
        database.storyDao().insertAllStories(stories)

        // 7. Populated Wallet Transactions
        val txs = listOf(
            WalletTransaction("tx1", "15.00", "Creator Premium Subscription", System.currentTimeMillis() - 86400000, true, false),
            WalletTransaction("tx2", "45.00", "Nogram Creator Monthly Payout", System.currentTimeMillis() - 172800000, true, true),
            WalletTransaction("tx3", "12.50", "User Reward Referral Claim", System.currentTimeMillis() - 259200000, true, true)
        )
        for (tx in txs) {
            database.walletDao().insertTransaction(tx)
        }
    }

    // Security & E2E encryption simulation
    fun generateNewSecretKeys() {
        // Mock Signal double ratchet rotation or master key derivation
        val randomCert = UUID.randomUUID().toString().uppercase().replace("-", "").take(16)
        _fingerprint.value = "NOGRAM-E2EE-${randomCert.take(4)}-${randomCert.substring(4, 8)}-${randomCert.substring(8, 12)}-${randomCert.takeLast(4)}"
        _identityPublicKey.value = "MCowBQYDK2VwAyEA${UUID.randomUUID().toString().replace("-", "").take(32)}..."
        _preKeyStatus.value = "Active: Prekey rotated key_id=${(100..999).random()}"
    }

    // Insert user created message
    suspend fun saveMessage(chatId: String, text: String, isSecret: Boolean = false, attachmentName: String = "", attachmentSize: String = "", attachmentType: String = "") {
        val uniqueId = "msg_" + System.currentTimeMillis()
        val newMessage = Message(
            id = uniqueId,
            chatId = chatId,
            senderId = "me",
            senderName = "You",
            text = text,
            timestamp = System.currentTimeMillis(),
            isSent = true,
            isRead = false,
            isSecret = isSecret,
            attachmentName = attachmentName,
            attachmentSize = attachmentSize,
            attachmentType = attachmentType
        )
        database.messageDao().insertMessage(newMessage)

        // Sync single message to Firebase in background
        com.example.data.remote.FirebaseSyncService.syncSingleMessage(context, newMessage)

        // Update last message preview inside Chat entry
        val currentChat = database.chatDao().getChatById(chatId)
        if (currentChat != null) {
            val previewText = if (attachmentName.isNotEmpty()) attachmentName else text
            val updatedChat = currentChat.copy(description = if (chatId == "chat_alice_freeman") "typing" else "You: $previewText")
            database.chatDao().insertChat(updatedChat)

            // Sync updated chat preview to Firebase in background
            com.example.data.remote.FirebaseSyncService.syncSingleChat(context, updatedChat)
        }
    }

    // Create custom Group/Community
    suspend fun createGroup(title: String, type: String = "GROUP") {
        val chatId = "chat_" + System.currentTimeMillis()
        val newChat = Chat(
            id = chatId,
            title = title,
            description = "No messages yet",
            type = type,
            photoUrl = "default_avatar",
            memberCount = 1,
            unreadCount = 0
        )
        database.chatDao().insertChat(newChat)

        // Sync newly created chat node to Firebase in background
        com.example.data.remote.FirebaseSyncService.syncSingleChat(context, newChat)
    }

    // ==========================================
    // DATA SYNC LOGIC
    // ==========================================

    suspend fun pushLocalCacheToFirebase(): Boolean = withContext(Dispatchers.IO) {
        try {
            val contacts = database.contactDao().getAllContacts().first()
            val chats = database.chatDao().getAllChats().first()
            val messages = database.messageDao().getAllMessages().first()
            val communities = database.communityDao().getAllCommunities().first()
            val stories = database.storyDao().getAllStories().first()
            val walletTxs = database.walletDao().getAllTransactions().first()

            com.example.data.remote.FirebaseSyncService.uploadAllData(
                context = context,
                contacts = contacts,
                chats = chats,
                messages = messages,
                communities = communities,
                stories = stories,
                walletTransactions = walletTxs
            )
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun pullFirebaseToLocalCache(): Boolean = withContext(Dispatchers.IO) {
        try {
            com.example.data.remote.FirebaseSyncService.downloadAllDataAndMerge(
                context = context,
                onContact = { list -> database.contactDao().insertAllContacts(list) },
                onChat = { list -> database.chatDao().insertAllChats(list) },
                onMessage = { list ->
                    for (m in list) {
                        database.messageDao().insertMessage(m)
                    }
                },
                onCommunity = { list -> database.communityDao().insertAllCommunities(list) },
                onStory = { list -> database.storyDao().insertAllStories(list) },
                onWalletTx = { list ->
                    for (tx in list) {
                        database.walletDao().insertTransaction(tx)
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun resetDatabaseLocalCache() = withContext(Dispatchers.IO) {
        database.clearAllTables()
    }

    companion object {
        @Volatile
        private var INSTANCE: ChatRepository? = null

        fun getInstance(context: Context): ChatRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = ChatRepository(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
