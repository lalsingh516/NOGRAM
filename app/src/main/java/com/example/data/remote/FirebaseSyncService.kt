package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object FirebaseSyncService {
    private const val TAG = "FirebaseSyncService"
    private const val PREFS_NAME = "NogramFirebasePrefs"
    private const val KEY_DB_URL = "firebase_db_url"
    private const val KEY_API_KEY = "firebase_api_key"
    private const val KEY_SYNC_ENABLED = "firebase_sync_enabled"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    fun getDbUrl(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_DB_URL, "https://nogramoffical-default-rtdb.asia-southeast1.firebasedatabase.app") ?: "https://nogramoffical-default-rtdb.asia-southeast1.firebasedatabase.app"
    }

    fun getApiKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_API_KEY, "AIzaSyCb_R9j7MsUemFzLLBnmPPGFPNMKFrCMlQ") ?: "AIzaSyCb_R9j7MsUemFzLLBnmPPGFPNMKFrCMlQ"
    }

    fun isSyncEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_SYNC_ENABLED, true)
    }

    fun saveConfig(context: Context, url: String, key: String, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_DB_URL, url.trim())
            .putString(KEY_API_KEY, key.trim())
            .putBoolean(KEY_SYNC_ENABLED, enabled)
            .apply()
    }

    fun isConnected(context: Context): Boolean {
        val url = getDbUrl(context)
        return url.isNotEmpty() && url.startsWith("http")
    }

    private fun buildUrl(baseUrl: String, path: String, key: String): String {
        val cleanUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        val suffix = if (key.isNotEmpty()) "?auth=$key" else ""
        return "$cleanUrl$path.json$suffix"
    }

    // ==========================================
    // ENTITY SERIALIZERS
    // ==========================================

    private fun serializeChat(chat: Chat): JSONObject = JSONObject().apply {
        put("id", chat.id)
        put("title", chat.title)
        put("description", chat.description)
        put("type", chat.type)
        put("photoUrl", chat.photoUrl)
        put("memberCount", chat.memberCount)
        put("unreadCount", chat.unreadCount)
        put("isPinned", chat.isPinned)
        put("isMuted", chat.isMuted)
        put("parentCommunityId", chat.parentCommunityId)
    }

    private fun deserializeChat(json: JSONObject): Chat = Chat(
        id = json.getString("id"),
        title = json.getString("title"),
        description = json.optString("description", ""),
        type = json.optString("type", "PRIVATE"),
        photoUrl = json.optString("photoUrl", ""),
        memberCount = json.optInt("memberCount", 0),
        unreadCount = json.optInt("unreadCount", 0),
        isPinned = json.optBoolean("isPinned", false),
        isMuted = json.optBoolean("isMuted", false),
        parentCommunityId = if (json.isNull("parentCommunityId")) null else json.optString("parentCommunityId", null)
    )

    private fun serializeMessage(message: Message): JSONObject = JSONObject().apply {
        put("id", message.id)
        put("chatId", message.chatId)
        put("senderId", message.senderId)
        put("senderName", message.senderName)
        put("senderPhotoUrl", message.senderPhotoUrl)
        put("text", message.text)
        put("timestamp", message.timestamp)
        put("isSent", message.isSent)
        put("isRead", message.isRead)
        put("isSecret", message.isSecret)
        put("attachmentName", message.attachmentName)
        put("attachmentSize", message.attachmentSize)
        put("attachmentType", message.attachmentType)
        put("attachmentUrl", message.attachmentUrl)
        put("reactionsStr", message.reactionsStr)
        put("replyToId", message.replyToId)
        put("replyToText", message.replyToText)
        put("isScheduled", message.isScheduled)
        put("isE2EEncrypted", message.isE2EEncrypted)
    }

    private fun deserializeMessage(json: JSONObject): Message = Message(
        id = json.getString("id"),
        chatId = json.getString("chatId"),
        senderId = json.optString("senderId", ""),
        senderName = json.optString("senderName", ""),
        senderPhotoUrl = json.optString("senderPhotoUrl", ""),
        text = json.optString("text", ""),
        timestamp = json.optLong("timestamp", System.currentTimeMillis()),
        isSent = json.optBoolean("isSent", true),
        isRead = json.optBoolean("isRead", false),
        isSecret = json.optBoolean("isSecret", false),
        attachmentName = json.optString("attachmentName", ""),
        attachmentSize = json.optString("attachmentSize", ""),
        attachmentType = json.optString("attachmentType", ""),
        attachmentUrl = json.optString("attachmentUrl", ""),
        reactionsStr = json.optString("reactionsStr", ""),
        replyToId = json.optString("replyToId", ""),
        replyToText = json.optString("replyToText", ""),
        isScheduled = json.optBoolean("isScheduled", false),
        isE2EEncrypted = json.optBoolean("isE2EEncrypted", false)
    )

    private fun serializeContact(contact: Contact): JSONObject = JSONObject().apply {
        put("id", contact.id)
        put("username", contact.username)
        put("firstName", contact.firstName)
        put("lastName", contact.lastName)
        put("bio", contact.bio)
        put("photoUrl", contact.photoUrl)
        put("isOnline", contact.isOnline)
        put("lastSeenStr", contact.lastSeenStr)
        put("isBlocked", contact.isBlocked)
    }

    private fun deserializeContact(json: JSONObject): Contact = Contact(
        id = json.getString("id"),
        username = json.getString("username"),
        firstName = json.getString("firstName"),
        lastName = json.optString("lastName", ""),
        bio = json.optString("bio", ""),
        photoUrl = json.optString("photoUrl", ""),
        isOnline = json.optBoolean("isOnline", false),
        lastSeenStr = json.optString("lastSeenStr", "recently"),
        isBlocked = json.optBoolean("isBlocked", false)
    )

    private fun serializeCommunity(community: Community): JSONObject = JSONObject().apply {
        put("id", community.id)
        put("name", community.name)
        put("description", community.description)
        put("bannerUrl", community.bannerUrl)
        put("photoUrl", community.photoUrl)
        put("subscriberCount", community.subscriberCount)
        put("isPublic", community.isPublic)
    }

    private fun deserializeCommunity(json: JSONObject): Community = Community(
        id = json.getString("id"),
        name = json.getString("name"),
        description = json.optString("description", ""),
        bannerUrl = json.optString("bannerUrl", ""),
        photoUrl = json.optString("photoUrl", ""),
        subscriberCount = json.optInt("subscriberCount", 0),
        isPublic = json.optBoolean("isPublic", true)
    )

    private fun serializeStory(story: Story): JSONObject = JSONObject().apply {
        put("id", story.id)
        put("userId", story.userId)
        put("username", story.username)
        put("avatarUrl", story.avatarUrl)
        put("imageUrl", story.imageUrl)
        put("caption", story.caption)
        put("timestamp", story.timestamp)
        put("isViewed", story.isViewed)
        put("isArchived", story.isArchived)
    }

    private fun deserializeStory(json: JSONObject): Story = Story(
        id = json.getString("id"),
        userId = json.optString("userId", ""),
        username = json.optString("username", ""),
        avatarUrl = json.optString("avatarUrl", ""),
        imageUrl = json.optString("imageUrl", ""),
        caption = json.optString("caption", ""),
        timestamp = json.optLong("timestamp", System.currentTimeMillis()),
        isViewed = json.optBoolean("isViewed", false),
        isArchived = json.optBoolean("isArchived", false)
    )

    private fun serializeWalletTransaction(tx: WalletTransaction): JSONObject = JSONObject().apply {
        put("id", tx.id)
        put("amount", tx.amount)
        put("label", tx.label)
        put("timestamp", tx.timestamp)
        put("isSuccess", tx.isSuccess)
        put("isIncoming", tx.isIncoming)
    }

    private fun deserializeWalletTransaction(json: JSONObject): WalletTransaction = WalletTransaction(
        id = json.getString("id"),
        amount = json.optString("amount", "0.00"),
        label = json.optString("label", ""),
        timestamp = json.optLong("timestamp", System.currentTimeMillis()),
        isSuccess = json.optBoolean("isSuccess", true),
        isIncoming = json.optBoolean("isIncoming", false)
    )

    // ==========================================
    // PUSH DATA TO FIREBASE (UPLOAD)
    // ==========================================

    suspend fun uploadAllData(
        context: Context,
        contacts: List<Contact>,
        chats: List<Chat>,
        messages: List<Message>,
        communities: List<Community>,
        stories: List<Story>,
        walletTransactions: List<WalletTransaction>
    ): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getDbUrl(context)
        val apiKey = getApiKey(context)
        if (baseUrl.isEmpty() || !baseUrl.startsWith("http")) return@withContext false

        try {
            // 1. Pack contacts
            val contactsObj = JSONObject()
            contacts.forEach { contactsObj.put(it.id, serializeContact(it)) }
            uploadJson(baseUrl, "contacts", contactsObj.toString(), apiKey)

            // 2. Pack chats
            val chatsObj = JSONObject()
            chats.forEach { chatsObj.put(it.id, serializeChat(it)) }
            uploadJson(baseUrl, "chats", chatsObj.toString(), apiKey)

            // 3. Pack messages (grouped by chat id for quick updates)
            // Firebase layout: messages -> {chatId} -> {messageId} -> message_payload
            val messagesGrouped = JSONObject()
            messages.forEach { msg ->
                var chatGroup = messagesGrouped.optJSONObject(msg.chatId)
                if (chatGroup == null) {
                    chatGroup = JSONObject()
                    messagesGrouped.put(msg.chatId, chatGroup)
                }
                chatGroup.put(msg.id, serializeMessage(msg))
            }
            uploadJson(baseUrl, "messages", messagesGrouped.toString(), apiKey)

            // 4. Pack communities
            val communitiesObj = JSONObject()
            communities.forEach { communitiesObj.put(it.id, serializeCommunity(it)) }
            uploadJson(baseUrl, "communities", communitiesObj.toString(), apiKey)

            // 5. Pack stories
            val storiesObj = JSONObject()
            stories.forEach { storiesObj.put(it.id, serializeStory(it)) }
            uploadJson(baseUrl, "stories", storiesObj.toString(), apiKey)

            // 6. Pack wallet transactions
            val walletObj = JSONObject()
            walletTransactions.forEach { walletObj.put(it.id, serializeWalletTransaction(it)) }
            uploadJson(baseUrl, "wallet_transactions", walletObj.toString(), apiKey)

            Log.d(TAG, "Successfully uploaded all tables to Firebase cloud!")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading to Firebase: ${e.localizedMessage}", e)
            false
        }
    }

    private fun uploadJson(baseUrl: String, nodePath: String, jsonStr: String, apiKey: String): Boolean {
        val url = buildUrl(baseUrl, nodePath, apiKey)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonStr.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .put(body)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e(TAG, "Failed putting node $nodePath response code: ${response.code}")
                return false
            }
            return true
        }
    }

    // ==========================================
    // PULL DATA FROM FIREBASE (MERGE/DOWNLOAD)
    // ==========================================

    suspend fun downloadAllDataAndMerge(
        context: Context,
        onContact: suspend (List<Contact>) -> Unit,
        onChat: suspend (List<Chat>) -> Unit,
        onMessage: suspend (List<Message>) -> Unit,
        onCommunity: suspend (List<Community>) -> Unit,
        onStory: suspend (List<Story>) -> Unit,
        onWalletTx: suspend (List<WalletTransaction>) -> Unit
    ): Boolean = withContext(Dispatchers.IO) {
        val baseUrl = getDbUrl(context)
        val apiKey = getApiKey(context)
        if (baseUrl.isEmpty() || !baseUrl.startsWith("http")) return@withContext false

        try {
            // 1. Fetch contacts
            fetchNode(baseUrl, "contacts", apiKey)?.let { jsonStr ->
                val list = mutableListOf<Contact>()
                val parentJson = JSONObject(jsonStr)
                val keys = parentJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val itemJson = parentJson.optJSONObject(key)
                    if (itemJson != null) {
                        list.add(deserializeContact(itemJson))
                    }
                }
                if (list.isNotEmpty()) onContact(list)
            }

            // 2. Fetch chats
            fetchNode(baseUrl, "chats", apiKey)?.let { jsonStr ->
                val list = mutableListOf<Chat>()
                val parentJson = JSONObject(jsonStr)
                val keys = parentJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val itemJson = parentJson.optJSONObject(key)
                    if (itemJson != null) {
                        list.add(deserializeChat(itemJson))
                    }
                }
                if (list.isNotEmpty()) onChat(list)
            }

            // 3. Fetch messages (grouped)
            fetchNode(baseUrl, "messages", apiKey)?.let { jsonStr ->
                val list = mutableListOf<Message>()
                val parentJson = JSONObject(jsonStr)
                val chatKeys = parentJson.keys()
                while (chatKeys.hasNext()) {
                    val chatId = chatKeys.next()
                    val chatMsgsJson = parentJson.optJSONObject(chatId)
                    if (chatMsgsJson != null) {
                        val msgKeys = chatMsgsJson.keys()
                        while (msgKeys.hasNext()) {
                            val msgId = msgKeys.next()
                            val msgJson = chatMsgsJson.optJSONObject(msgId)
                            if (msgJson != null) {
                                list.add(deserializeMessage(msgJson))
                            }
                        }
                    }
                }
                if (list.isNotEmpty()) onMessage(list)
            }

            // 4. Fetch communities
            fetchNode(baseUrl, "communities", apiKey)?.let { jsonStr ->
                val list = mutableListOf<Community>()
                val parentJson = JSONObject(jsonStr)
                val keys = parentJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val itemJson = parentJson.optJSONObject(key)
                    if (itemJson != null) {
                        list.add(deserializeCommunity(itemJson))
                    }
                }
                if (list.isNotEmpty()) onCommunity(list)
            }

            // 5. Fetch stories
            fetchNode(baseUrl, "stories", apiKey)?.let { jsonStr ->
                val list = mutableListOf<Story>()
                val parentJson = JSONObject(jsonStr)
                val keys = parentJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val itemJson = parentJson.optJSONObject(key)
                    if (itemJson != null) {
                        list.add(deserializeStory(itemJson))
                    }
                }
                if (list.isNotEmpty()) onStory(list)
            }

            // 6. Fetch wallet txs
            fetchNode(baseUrl, "wallet_transactions", apiKey)?.let { jsonStr ->
                val list = mutableListOf<WalletTransaction>()
                val parentJson = JSONObject(jsonStr)
                val keys = parentJson.keys()
                while (keys.hasNext()) {
                    val key = keys.next()
                    val itemJson = parentJson.optJSONObject(key)
                    if (itemJson != null) {
                        list.add(deserializeWalletTransaction(itemJson))
                    }
                }
                if (list.isNotEmpty()) onWalletTx(list)
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error downloading/merging from Cloud: ${e.localizedMessage}", e)
            false
        }
    }

    private fun fetchNode(baseUrl: String, nodePath: String, apiKey: String): String? {
        val url = buildUrl(baseUrl, nodePath, apiKey)
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "Fetch non-success code $nodePath : ${response.code}")
                    return null
                }
                val bodyStr = response.body?.string() ?: ""
                if (bodyStr.trim() == "null" || bodyStr.trim().isEmpty()) return null
                return bodyStr
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception fetching node $nodePath", e)
            return null
        }
    }

    // ==========================================
    // INSTANT INDIVIDUAL CLOUD SYNC ACTIONS
    // ==========================================

    suspend fun syncSingleChat(context: Context, chat: Chat) = withContext(Dispatchers.IO) {
        if (!isSyncEnabled(context)) return@withContext
        val baseUrl = getDbUrl(context)
        val apiKey = getApiKey(context)
        if (baseUrl.isEmpty() || !baseUrl.startsWith("http")) return@withContext

        try {
            val json = serializeChat(chat)
            uploadJson(baseUrl, "chats/${chat.id}", json.toString(), apiKey)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing single chat", e)
        }
    }

    suspend fun syncSingleMessage(context: Context, message: Message) = withContext(Dispatchers.IO) {
        if (!isSyncEnabled(context)) return@withContext
        val baseUrl = getDbUrl(context)
        val apiKey = getApiKey(context)
        if (baseUrl.isEmpty() || !baseUrl.startsWith("http")) return@withContext

        try {
            val json = serializeMessage(message)
            uploadJson(baseUrl, "messages/${message.chatId}/${message.id}", json.toString(), apiKey)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing single message", e)
        }
    }

    suspend fun syncSingleWalletTx(context: Context, tx: WalletTransaction) = withContext(Dispatchers.IO) {
        if (!isSyncEnabled(context)) return@withContext
        val baseUrl = getDbUrl(context)
        val apiKey = getApiKey(context)
        if (baseUrl.isEmpty() || !baseUrl.startsWith("http")) return@withContext

        try {
            val json = serializeWalletTransaction(tx)
            uploadJson(baseUrl, "wallet_transactions/${tx.id}", json.toString(), apiKey)
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing single wallet tx", e)
        }
    }
}
