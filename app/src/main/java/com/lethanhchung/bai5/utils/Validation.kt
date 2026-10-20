package com.lethanhchung.bai5.utils

import com.lethanhchung.bai5.data.model.Course

object Validation {
    fun validateCourse(course: Course, categoryIds: Set<String>): String? = when {
        course.title.isBlank() || course.description.isBlank() || course.instructor.isBlank() -> "Vui lòng nhập đủ trường bắt buộc"
        !course.price.isFinite() || course.price < 0 -> "Học phí phải là số không âm"
        course.duration <= 0 -> "Thời lượng phải lớn hơn 0"
        course.categoryId !in categoryIds -> "Danh mục không hợp lệ"
        !validUrl(course.thumbnail) -> "URL ảnh phải là địa chỉ HTTPS hợp lệ"
        !validUrl(course.trailerUrl) -> "URL trailer phải là địa chỉ HTTPS hợp lệ"
        runCatching { java.time.LocalDate.parse(course.createdAt) }.isFailure -> "Ngày tạo phải có dạng yyyy-MM-dd"
        else -> null
    }
    private fun validUrl(value: String): Boolean = runCatching {
        val uri = java.net.URI(value)
        uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null
    }.getOrDefault(false)
}
