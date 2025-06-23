package com.example.catashtope

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ChatbotViewModel @Inject constructor() : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    fun onEvent(event: ChatbotEvent) {
        when (event) {
            is ChatbotEvent.SendMessage -> {
                val newMessage = ChatMessage(
                    id = _messages.value.size,
                    text = event.message,
                    isFromUser = true
                )
                _messages.value = _messages.value + newMessage
                // Here you would typically process the message and generate a response
                generateBotResponse(event.message)
            }
        }
    }

    private fun generateBotResponse(userMessage: String) {
        val response = when {
            userMessage.contains("hello", ignoreCase = true) ->
                "Hello! How can I help you today?"
            userMessage.contains("help", ignoreCase = true) ->
                "I can help you with information about natural disasters, emergency procedures, and safety guidelines. What would you like to know?"
            userMessage.contains("disaster", ignoreCase = true) ->
                "I can provide information about various types of natural disasters including earthquakes, floods, hurricanes, and more. Which one would you like to learn about?"
            userMessage.contains("emergency", ignoreCase = true) ->
                "Here are some general emergency procedures: 1. Stay calm 2. Call emergency services 3. Follow evacuation procedures if necessary. Would you like more specific information?"
            else ->
                "I understand you're asking about \"$userMessage\". Could you please provide more details about what you'd like to know?"
        }

        val botMessage = ChatMessage(
            id = _messages.value.size,
            text = response,
            isFromUser = false
        )
        _messages.value = _messages.value + botMessage
    }
}
