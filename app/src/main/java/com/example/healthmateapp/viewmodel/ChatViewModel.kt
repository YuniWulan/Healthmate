package com.example.healthmateapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.healthmateapp.screens.ChatMessage
import com.google.ai.client.generativeai.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // ✅ Initialize Gemini AI Model
//    private val generativeModel = GenerativeModel(
//        modelName = "gemini-1.5-flash", // or "gemini-pro"
//        apiKey = "AIzaSyASYm0eNoExq2EJjOEKWk2RUxSFv1yCMAA"   // ⚠️ Replace with your actual API key
//    )

    private val chatHistory = mutableListOf<com.google.ai.client.generativeai.type.Content>()

    companion object {
        private const val TAG = "ChatViewModel"

        // System prompt to guide the AI
//        private const val SYSTEM_PROMPT = ""
    }

    init {
        // Add system prompt to chat history
        chatHistory.add(
            content(role = "user") {
//                text(SYSTEM_PROMPT)
            }
        )
    }

    /**
     * Send a message to Gemini AI and get response
     */
//    fun sendMessage(userMessage: String) {
//        if (userMessage.isBlank()) return
//
//        viewModelScope.launch {
//            try {
//                // Add user message to UI
//                val userChatMessage = ChatMessage(
//                    text = userMessage,
//                    isFromUser = true
//                )
//                _messages.value = _messages.value + userChatMessage
//
//                // Show loading
//                _isLoading.value = true
//
//                // Add user message to chat history
//                chatHistory.add(
//                    content(role = "user") {
//                        text(userMessage)
//                    }
//                )
//
//                Log.d(TAG, "📤 Sending message to Gemini: $userMessage")
//
//                // Start chat with history
//                val chat = generativeModel.startChat(history = chatHistory.toList())
//
//                // Get AI response
//                val response = chat.sendMessage(userMessage)
//                val aiResponseText = response.text ?: "I'm sorry, I couldn't generate a response."
//
//                Log.d(TAG, "📥 Received response from Gemini: $aiResponseText")
//
//                // Add AI response to chat history
//                chatHistory.add(
//                    content(role = "model") {
//                        text(aiResponseText)
//                    }
//                )
//
//                // Add AI response to UI
//                val aiChatMessage = ChatMessage(
//                    text = aiResponseText,
//                    isFromUser = false
//                )
//                _messages.value = _messages.value + aiChatMessage
//
//            } catch (e: Exception) {
//                Log.e(TAG, "❌ Error communicating with Gemini", e)
//
//                // Add error message
//                val errorMessage = ChatMessage(
//                    text = "I'm having trouble connecting right now. Please try again in a moment. 🔄",
//                    isFromUser = false
//                )
//                _messages.value = _messages.value + errorMessage
//            } finally {
//                _isLoading.value = false
//            }
//        }
//    }

    /**
     * Send message with health context (for integration with health metrics)
     */
    fun sendMessageWithHealthContext(
        userMessage: String,
        healthContext: String
    ) {
        val contextualMessage = """
            Health Context:
            $healthContext
            
            User Question:
            $userMessage
        """.trimIndent()

//        sendMessage(contextualMessage)
    }

    /**
     * Clear chat history
     */
    fun clearChat() {
        _messages.value = emptyList()
        chatHistory.clear()

        // Re-add system prompt
        chatHistory.add(
            content(role = "user") {
//                text(SYSTEM_PROMPT)
            }
        )

        Log.d(TAG, "🗑️ Chat cleared")
    }

    /**
     * Get suggested questions based on health data
     */
    fun getSuggestedQuestions(): List<String> {
        return listOf(
            "What are my medications for today?",
            "How do I manage high blood pressure?",
            "What are healthy blood glucose levels?",
            "Give me tips for better sleep",
            "What foods should I eat for heart health?",
            "How much water should I drink daily?",
            "What exercises are good for beginners?"
        )
    }
}
