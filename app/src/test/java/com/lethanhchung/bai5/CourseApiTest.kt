package com.lethanhchung.bai5

import com.google.gson.JsonParser
import com.lethanhchung.bai5.data.model.Course
import com.lethanhchung.bai5.data.remote.MockApiService
import com.lethanhchung.bai5.data.remote.RetrofitClient
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CourseApiTest {
    @Test fun postSendsFieldsWithoutIdAndReadsServerGeneratedId() = runBlocking {
        MockWebServer().use { server ->
            val course = Course(title = "Kotlin", description = "Mobile", categoryId = "3",
                instructor = "Chung", price = 500000.0, duration = 30,
                thumbnail = "https://example.org/a.jpg", trailerUrl = "https://example.org/a.mp4",
                createdAt = "2026-10-20")
            val json = JsonParser.parseString(RetrofitClient.gson.toJson(course)).asJsonObject
            json.addProperty("id", "42")
            server.enqueue(MockResponse().setBody(json.toString()))
            val api = Retrofit.Builder().baseUrl(server.url("/api/v1/"))
                .addConverterFactory(GsonConverterFactory.create(RetrofitClient.gson)).build()
                .create(MockApiService::class.java)
            val saved = api.createCourse(course)
            assertEquals("42", saved.id)
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/v1/courses", request.path)
            val body = JsonParser.parseString(request.body.readUtf8()).asJsonObject
            assertFalse(body.has("id"))
            assertEquals("3", body["categoryId"].asString)
            assertEquals(500000.0, body["price"].asDouble, 0.0)
        }
    }
}
