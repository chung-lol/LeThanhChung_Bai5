package com.lethanhchung.bai5.ui.category

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.lethanhchung.bai5.data.model.Category
import com.lethanhchung.bai5.data.repository.CategoryRepository
import com.lethanhchung.bai5.ui.common.BaseActivity

class CategoryManagementActivity : BaseActivity() {
    private lateinit var results: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        heading("Quản lý danh mục")
        button("Quay lại") { finish() }
        button("+ Thêm danh mục") { edit(null) }
        button("Tải lại") { task { refresh() } }
        results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; body.addView(this) }
        task { refresh() }
    }
    private suspend fun refresh() {
        val categories = api.getCategories()
        results.removeAllViews()
        if (categories.isEmpty()) label("Chưa có danh mục. Hãy thêm danh mục đầu tiên.", parent = results)
        categories.forEach { category ->
            val row = card(results)
            label(category.name, 21f, row); label(category.description, parent = row)
            button("Sửa", row) { edit(category) }
            button("Xóa", row) {
                AlertDialog.Builder(this).setTitle("Xóa danh mục?")
                    .setMessage("Xóa “${category.name}”? Chỉ được xóa nếu không có khóa học.")
                    .setNegativeButton("Hủy", null).setPositiveButton("Xóa") { _, _ ->
                        task {
                            CategoryRepository(api).delete(category.id); refresh()
                        }
                    }.show()
            }
        }
    }
    private fun edit(category: Category?) {
        val container = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32, 8, 32, 8) }
        val name = field("Tên danh mục *", category?.name ?: "", container)
        val description = field("Mô tả *", category?.description ?: "", container)
        val dialog = AlertDialog.Builder(this).setTitle(if (category == null) "Thêm danh mục" else "Sửa danh mục")
            .setView(container).setNegativeButton("Hủy", null).setPositiveButton("Lưu", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (name.text.isBlank()) { name.error = "Nhập tên danh mục"; return@setOnClickListener }
                if (description.text.isBlank()) { description.error = "Nhập mô tả"; return@setOnClickListener }
                val value = Category(category?.id ?: "", name.text.toString().trim(), description.text.toString().trim())
                task {
                    if (category == null) api.createCategory(value) else api.updateCategory(category.id, value)
                    dialog.dismiss(); refresh()
                }
            }
        }
        dialog.show()
    }
}
