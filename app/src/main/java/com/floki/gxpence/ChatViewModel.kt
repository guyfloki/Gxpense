package com.floki.gxpence

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.UUID

data class ChatMessage(val message: String, val isUser: Boolean, val sessionId: String)
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages
    private val sharedPreferences: SharedPreferences = getApplication<Application>().getSharedPreferences("ChatViewModel", Context.MODE_PRIVATE)
    private var databaseSessionId: String?
        get() = sharedPreferences.getString("sessionId", null)
        set(value) = sharedPreferences.edit().putString("sessionId", value).apply()

    private val sessionId: String
        get() = databaseSessionId ?: UUID.randomUUID().toString().also { databaseSessionId = it }

    private val dbHelper = DatabaseHelper(getApplication())

    init {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _chatMessages.value = dbHelper.getMessages()
                if (databaseSessionId == null && _chatMessages.value.isNotEmpty()) {
                    databaseSessionId = _chatMessages.value.firstOrNull()?.sessionId
                }
            }
        }
    }


    suspend fun sendMessage(message: String, username: String) {
        try {
            val chatMessage = ChatMessage(message, true, sessionId)
            _chatMessages.value = _chatMessages.value.plus(chatMessage)
            dbHelper.addMessage(chatMessage)

            val lambdaResponse = MainActivity().callLambda(message, sessionId, username)
            val jsonResponse = JSONObject(lambdaResponse)
            val assistantMessage = jsonResponse.getString("assistant_message")
            val assistantChatMessage = ChatMessage(assistantMessage, false, sessionId)
            _chatMessages.value = _chatMessages.value.plus(assistantChatMessage)
            dbHelper.addMessage(assistantChatMessage)
        } catch (e: Exception) {
            val errorMessage = "Internal error, please try again later."
            val errorChatMessage = ChatMessage(errorMessage, false, sessionId)
            _chatMessages.value = _chatMessages.value.plus(errorChatMessage)
            dbHelper.addMessage(errorChatMessage)
        }
    }


    fun clearChatMessages() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _chatMessages.value = emptyList()
                dbHelper.clearMessages()
                databaseSessionId = null
            }
        }
    }

}





