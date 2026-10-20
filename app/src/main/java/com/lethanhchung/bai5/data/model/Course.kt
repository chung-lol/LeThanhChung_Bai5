package com.lethanhchung.bai5.data.model

data class Course(
    val id: String = "", val title: String, val description: String, val categoryId: String,
    val instructor: String, val price: Double, val duration: Int, val thumbnail: String,
    val trailerUrl: String, val createdAt: String
)
