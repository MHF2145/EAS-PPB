// file: WeatherDao.kt
package com.example.catashtope

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {

    // Fungsi untuk memasukkan atau memperbarui data.
    // OnConflictStrategy.REPLACE berarti jika data dengan primary key yang sama dimasukkan,
    // data lama akan diganti dengan yang baru.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(weatherResponse: WeatherResponse)

    // Fungsi untuk mengambil data. Menggunakan Flow agar UI otomatis update
    // setiap kali ada perubahan data di tabel ini.
    @Query("SELECT * FROM weather_data ORDER BY id DESC LIMIT 1")
    fun getLatestWeather(): Flow<WeatherResponse?>


    @Query("SELECT * FROM weather_data")
    fun getAllWeatherData(): Flow<List<WeatherResponse>>

}