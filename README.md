# EAS-PPB

# Catashtope 🌦️✈️

**Aplikasi Cuaca & Perencana Perjalanan Berbasis Android**

[![Release](https://img.shields.io/badge/Release-v0.2.0-blue)](https://github.com/MHF2145/PPB-Assignment-2025/releases/tag/v0.2.0)
[![License](https://img.shields.io/badge/License-MIT-green)](LICENSE)

## 📱 Gambaran Umum
Catashtope adalah aplikasi Android yang menyediakan:
- Informasi cuaca real-time
- Perencanaan perjalanan terintegrasi
- Asisten virtual berbasis AI (Chatbot "Chika")
- Rekomendasi destinasi wisata

Dibangun dengan:
- Jetpack Compose
- Firebase Gemini
- Room Database
- Retrofit

## ✨ Fitur Utama
| Fitur | Deskripsi |
|-------|-----------|
| ☁️ **Cuaca Real-Time** | Prediksi cuaca berdasarkan lokasi dengan auto-update |
| 📝 **To-Do List** | Manajemen rencana perjalanan dengan penyimpanan lokal |
| 🤖 **Chatbot Chika** | Asisten AI untuk konsultasi cuaca dan destinasi |
| 🌄 **Gambar Destinasi** | Tampilan landscape otomatis dari Pexels API |
| 🔄 **Riwayat Cuaca** | Penyimpanan data cuaca historis dengan filter waktu |

## 🛠️ Teknologi
```kotlin
dependencies {
    // UI
    implementation("androidx.compose:compose-ui:1.6.0")
    
    // Database
    implementation("androidx.room:room-ktx:2.6.0")
    
    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    
    // AI
    implementation("com.google.firebase:firebase-ai:21.0.0")
}
