package com.lethanhchung.bai5.data.remote

import com.lethanhchung.bai5.data.model.Category
import com.lethanhchung.bai5.data.model.Course
import retrofit2.http.*

interface MockApiService {
    @GET("categories") suspend fun getCategories(): List<Category>
    @POST("categories") suspend fun createCategory(@Body category: Category): Category
    @PUT("categories/{id}") suspend fun updateCategory(@Path("id") id: String, @Body category: Category): Category
    @DELETE("categories/{id}") suspend fun deleteCategory(@Path("id") id: String)
    @GET("courses") suspend fun getCourses(): List<Course>
    @GET("courses/{id}") suspend fun getCourse(@Path("id") id: String): Course
    @POST("courses") suspend fun createCourse(@Body course: Course): Course
    @PUT("courses/{id}") suspend fun updateCourse(@Path("id") id: String, @Body course: Course): Course
    @DELETE("courses/{id}") suspend fun deleteCourse(@Path("id") id: String)
}
