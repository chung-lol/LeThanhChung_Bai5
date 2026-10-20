package com.lethanhchung.bai5.ui.course

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import coil.load
import com.lethanhchung.bai5.data.model.*
import com.lethanhchung.bai5.data.remote.RetrofitClient
import com.lethanhchung.bai5.ui.category.CategoryManagementActivity
import com.lethanhchung.bai5.ui.common.BaseActivity

class MainActivity : BaseActivity() {
    private var courses = emptyList<Course>()
    private var categories = emptyList<Category>()
    private lateinit var results: LinearLayout
    private lateinit var search: EditText
    private lateinit var filter: Spinner
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        heading("Khám phá khóa học")
        label("Học hôm nay • Vững bước ngày mai")
        button("+ Thêm khóa học") { startActivity(Intent(this, CourseFormActivity::class.java)) }
        button("Quản lý danh mục") { startActivity(Intent(this, CategoryManagementActivity::class.java)) }
        button("Cấu hình API") { configure() }
        button("Tải lại") { reload() }
        search = field("Tìm theo tên khóa học").apply { doAfterTextChanged { render() } }
        filter = Spinner(this).also { body.addView(it) }
        filter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) = render()
            override fun onNothingSelected(p: AdapterView<*>?) = Unit
        }
        results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; body.addView(this) }
    }
    override fun onResume() { super.onResume(); reload() }
    private fun reload(): Unit = task {
        val selectedId = categories.getOrNull(filter.selectedItemPosition - 1)?.id
        val newCategories = api.getCategories()
        val newCourses = api.getCourses()
        categories = newCategories; courses = newCourses
        filter.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Tất cả danh mục") + categories.map { it.name })
        filter.setSelection(categories.indexOfFirst { it.id == selectedId }.let { if (it < 0) 0 else it + 1 })
        render()
    }
    private fun render() {
        if (!::results.isInitialized) return
        results.removeAllViews()
        val categoryId = categories.getOrNull(filter.selectedItemPosition - 1)?.id
        val shown = courses.filter {
            it.title.contains(search.text.toString().trim(), ignoreCase = true) &&
                (categoryId == null || it.categoryId == categoryId)
        }
        label("${shown.size} khóa học", parent = results)
        if (shown.isEmpty()) label("Chưa có khóa học phù hợp. Thêm khóa học hoặc đổi bộ lọc.", parent = results)
        shown.forEach { course ->
            val row = card(results)
            row.addView(ImageView(this).apply {
                contentDescription = "Ảnh bìa ${course.title}"; scaleType = ImageView.ScaleType.CENTER_CROP
                load(course.thumbnail) { crossfade(true); error(android.R.drawable.ic_menu_report_image) }
            }, LinearLayout.LayoutParams(-1, (180 * resources.displayMetrics.density).toInt()))
            label(course.title, 21f, row)
            label("${course.instructor}\n${categories.find { it.id == course.categoryId }?.name ?: "Danh mục không còn tồn tại"}\n${money(course.price)}", parent = row)
            button("Xem chi tiết", row) {
                startActivity(Intent(this, CourseDetailActivity::class.java).putExtra("COURSE_ID", course.id))
            }
        }
    }
    private fun configure() {
        val input = EditText(this).apply { setText(RetrofitClient.baseUrl(this@MainActivity)); hint = "https://….mockapi.io/api/v1/" }
        val dialog = AlertDialog.Builder(this).setTitle("Base URL MockAPI").setView(input)
            .setNegativeButton("Hủy", null).setPositiveButton("Lưu", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                try {
                    RetrofitClient.configure(this, input.text.toString())
                    courses = emptyList(); categories = emptyList(); render()
                    dialog.dismiss(); reload()
                } catch (e: Exception) { input.error = e.message }
            }
        }
        dialog.show()
    }
}
