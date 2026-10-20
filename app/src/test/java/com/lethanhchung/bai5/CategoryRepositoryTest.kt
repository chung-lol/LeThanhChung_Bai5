package com.lethanhchung.bai5

import com.lethanhchung.bai5.data.remote.MockApiService
import com.lethanhchung.bai5.data.repository.CategoryRepository
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CategoryRepositoryTest {
    private fun repository(server: MockWebServer) = CategoryRepository(
        Retrofit.Builder().baseUrl(server.url("/api/v1/"))
            .addConverterFactory(GsonConverterFactory.create()).build().create(MockApiService::class.java))

    @Test fun neverDeletesCategoryWithCourses() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("""[{"id":"7","categoryId":"1"}]"""))
            val failure = runCatching { repository(server).delete("1") }.exceptionOrNull()
            assertTrue(failure is IllegalArgumentException)
            assertEquals(1, server.requestCount)
            assertEquals("/api/v1/courses", server.takeRequest().path)
        }
    }
    @Test fun checksCoursesBeforeDeletingEmptyCategory() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setBody("[]"))
            server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
            repository(server).delete("2")
            assertEquals("GET", server.takeRequest().method)
            val deletion = server.takeRequest()
            assertEquals("DELETE", deletion.method)
            assertEquals("/api/v1/categories/2", deletion.path)
        }
    }
    @Test fun networkFailureMustNotDeleteCategory() = runBlocking {
        MockWebServer().use { server ->
            server.enqueue(MockResponse().setResponseCode(500))
            assertTrue(runCatching { repository(server).delete("2") }.isFailure)
            assertEquals(1, server.requestCount)
        }
    }
}
