package com.lethanhchung.bai5.data.repository

import com.lethanhchung.bai5.data.model.Category
import com.lethanhchung.bai5.data.remote.MockApiService

class CategoryRepository(private val api: MockApiService) {
    suspend fun getAll() = api.getCategories()
    suspend fun create(category: Category) = api.createCategory(category)
    suspend fun update(id: String, category: Category) = api.updateCategory(id, category)
    suspend fun delete(id: String) {
        require(api.getCourses().none { it.categoryId == id }) {
            "Không thể xóa danh mục đang có khóa học."
        }
        api.deleteCategory(id)
    }
}
