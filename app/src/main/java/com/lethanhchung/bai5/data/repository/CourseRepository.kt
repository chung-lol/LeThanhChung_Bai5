package com.lethanhchung.bai5.data.repository

import com.lethanhchung.bai5.data.model.Course
import com.lethanhchung.bai5.data.remote.MockApiService

class CourseRepository(private val api: MockApiService) {
    suspend fun getAll() = api.getCourses()
    suspend fun getById(id: String) = api.getCourse(id)
    suspend fun create(course: Course) = api.createCourse(course)
    suspend fun update(id: String, course: Course) = api.updateCourse(id, course)
    suspend fun delete(id: String) = api.deleteCourse(id)
}
