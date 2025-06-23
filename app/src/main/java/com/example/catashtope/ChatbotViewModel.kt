package com.example.catashtope

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig

@HiltViewModel
class ChatbotViewModel @Inject constructor() : ViewModel() {
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    // Add loading state
    private val _isThinking = MutableStateFlow(false)
    val isThinking: StateFlow<Boolean> = _isThinking.asStateFlow()

    // Maintain chat history for context
    private val chatHistory = mutableListOf<com.google.firebase.ai.type.Content>()

    init {
        // Add Chika's welcome message at the start
        val welcomeText = "Halo! Saya Chika, asisten perjalanan dan cuaca kamu. Ada yang bisa Chika bantu hari ini?"
        val welcomeMessage = ChatMessage(
            id = 0,
            text = welcomeText,
            isFromUser = false
        )
        _messages.value = listOf(welcomeMessage)
        chatHistory.add(
            com.google.firebase.ai.type.content("model") {
                text(welcomeText)
            }
        )
    }

    fun onEvent(event: ChatbotEvent) {
        when (event) {
            is ChatbotEvent.SendMessage -> {
                val userMessage = ChatMessage(
                    id = _messages.value.size,
                    text = event.message,
                    isFromUser = true
                )
                _messages.value = _messages.value + userMessage

                // Add user message to chat history
                chatHistory.add(
                    com.google.firebase.ai.type.content("user") {
                        text(event.message)
                    }
                )

                // Launch coroutine to get AI response
                viewModelScope.launch {
                    _isThinking.value = true
                    val reply = generateContent()
                    val botMessage = ChatMessage(
                        id = _messages.value.size,
                        text = reply,
                        isFromUser = false
                    )
                    _messages.value = _messages.value + botMessage

                    // Add bot message to chat history
                    chatHistory.add(
                        com.google.firebase.ai.type.content("model") {
                            text(reply)
                        }
                    )
                    _isThinking.value = false
                }
            }
        }
    }

    // Now generateContent uses the chatHistory for context
    private suspend fun generateContent(): String {
        val config = generationConfig {
            responseMimeType = "text/plain"
        }

        val systemInstruction = content {
            text(
                "Nama kamu adalah Chika, asisten perjalanan dan cuaca digital pintar. " +
                        "Tugas kamu adalah membantu pengguna merencanakan perjalanan dengan aman, " +
                        "memberikan informasi prakiraan cuaca, dan menyarankan destinasi wisata. " +
                        "Jawab dengan bahasa Indonesia secara jelas, ramah, dan akurat. " +
                        "Jika kamu tidak tahu jawabannya, sarankan pengguna untuk mencari informasi tambahan di sumber resmi seperti BMKG atau situs wisata lokal. " +
                        "Fokus pada pertanyaan yang berhubungan dengan cuaca, bencana alam, dan destinasi liburan."
            )
        }

        val model = Firebase.ai.generativeModel(
            modelName = "gemini-2.0-flash",
            generationConfig = config,
            systemInstruction = systemInstruction
        )

        val chat = model.startChat(
            history = chatHistory // Pass the conversation history
        )
        // The last user message is already in chatHistory, so just get response
        val response = chat.sendMessage(chatHistory.last())
        return response.text ?: "Maaf, Chika belum bisa menjawab pertanyaan itu."
    }
}