package com.lethanhchung.bai5.data.remote

import android.content.Context
import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    const val DEFAULT_URL = "https://6ad796ea95e98f36aef2ab6b.mockapi.io/api/v1/"
    private var service: MockApiService? = null
    // IDs are read from responses, but MockAPI owns their generation.
    internal val gson = GsonBuilder().addSerializationExclusionStrategy(object : ExclusionStrategy {
        override fun shouldSkipField(field: FieldAttributes) = field.name == "id"
        override fun shouldSkipClass(type: Class<*>) = false
    }).create()
    fun baseUrl(context: Context): String =
        context.getSharedPreferences("api", Context.MODE_PRIVATE).getString("url", DEFAULT_URL) ?: DEFAULT_URL

    fun configure(context: Context, url: String) {
        val normalized = url.trim().trimEnd('/') + "/"
        val uri = java.net.URI(normalized)
        require(uri.scheme == "https" && uri.host?.endsWith(".mockapi.io") == true &&
            uri.userInfo == null && uri.query == null && uri.fragment == null) {
            "Nhập URL HTTPS của project MockAPI, không kèm /courses hoặc /categories."
        }
        require(!uri.path.trimEnd('/').endsWith("/courses") &&
            !uri.path.trimEnd('/').endsWith("/categories")) { "Hãy nhập Base URL, không nhập URL resource." }
        context.getSharedPreferences("api", Context.MODE_PRIVATE).edit().putString("url", normalized).apply()
        service = null
    }

    fun api(context: Context): MockApiService {
        require(baseUrl(context).isNotBlank()) { "Chưa cấu hình MockAPI. Chọn Cấu hình API trên màn hình chính." }
        return service ?: Retrofit.Builder().baseUrl(baseUrl(context))
            .client(OkHttpClient.Builder().connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS).build())
            .addConverterFactory(GsonConverterFactory.create(gson)).build()
            .create(MockApiService::class.java).also { service = it }
    }
}
