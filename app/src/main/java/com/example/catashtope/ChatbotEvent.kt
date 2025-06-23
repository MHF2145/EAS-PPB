package com.example.catashtope

sealed class ChatbotEvent {
    data class SendMessage(val message: String) : ChatbotEvent()
}
