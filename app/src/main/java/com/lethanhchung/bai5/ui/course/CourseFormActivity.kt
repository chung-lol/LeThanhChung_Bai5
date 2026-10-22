package com.lethanhchung.bai5.ui.course

import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.core.widget.doAfterTextChanged
import coil.load
import com.lethanhchung.bai5.data.model.*
import com.lethanhchung.bai5.ui.common.BaseActivity
import com.lethanhchung.bai5.utils.Validation
import java.time.LocalDate

class CourseFormActivity : BaseActivity() {
    private val courseId get() = intent.getStringExtra("COURSE_ID")
    private var categories = emptyList<Category>()
    private var loaded = false
    private lateinit var titleField: EditText
    private lateinit var description: EditText
    private lateinit var instructor: EditText
    private lateinit var price: EditText
    private lateinit var duration: EditText
    private lateinit var thumbnail: EditText
    private lateinit var trailer: EditText
    private lateinit var createdAt: EditText
    private lateinit var category: Spinner
    private var draft: Bundle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        draft = savedInstanceState
        heading(if (courseId == null) "Thêm khóa học" else "Sửa khóa học")
        button("Quay lại") { finish() }
        button("Tải lại danh mục") {
            if (loaded) draft = captureDraft()
            load()
        }
        titleField = field("Tên khóa học *")
        description = field("Mô tả *").apply { minLines = 3; inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE }
        label("Danh mục *")
        category = Spinner(this).also { it.minimumHeight = dp(48); body.addView(it) }
        instructor = field("Giảng viên *")
        price = field("Học phí (VNĐ) *").apply { inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        duration = field("Thời lượng (giờ) *").apply { inputType = InputType.TYPE_CLASS_NUMBER }
        thumbnail = field("URL ảnh HTTPS *").apply { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI }
        val preview = ImageView(this).apply { contentDescription = "Xem trước ảnh"; scaleType = ImageView.ScaleType.CENTER_CROP }
        body.addView(preview, LinearLayout.LayoutParams(-1, (180 * resources.displayMetrics.density).toInt()))
        thumbnail.doAfterTextChanged { preview.load(it.toString().trim()) { error(android.R.drawable.ic_menu_report_image) } }
        trailer = field("URL trailer HTTPS (MP4 trực tiếp) *").apply { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI }
        createdAt = field("Ngày tạo (yyyy-MM-dd) *", LocalDate.now().toString())
        button("Lưu khóa học") { save() }
        load()
    }
    private fun fields() = listOf(titleField, description, instructor, price, duration, thumbnail, trailer, createdAt)
    private fun captureDraft() = Bundle().apply {
        fields().forEachIndexed { index, field -> putString("field$index", field.text.toString()) }
        putString("category", categories.getOrNull(category.selectedItemPosition - 1)?.id)
    }
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putAll(draft ?: captureDraft())
        super.onSaveInstanceState(outState)
    }
    private fun load() = task {
        loaded = false
        categories = api.getCategories()
        category.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Chọn danh mục…") + categories.map { it.name })
        val course = courseId?.let { api.getCourse(it) }
        if (course != null && draft == null) {
            listOf(course.title, course.description, course.instructor, course.price.toString(), course.duration.toString(),
                course.thumbnail, course.trailerUrl, course.createdAt).forEachIndexed { i, v -> fields()[i].setText(v) }
        }
        draft?.let { state -> fields().forEachIndexed { i, field -> field.setText(state.getString("field$i", field.text.toString())) } }
        val selected = if (draft != null) draft?.getString("category") else course?.categoryId
        val index = categories.indexOfFirst { it.id == selected }
        category.setSelection(if (index < 0) 0 else index + 1)
        draft = null
        loaded = true
        require(categories.isNotEmpty()) { "Chưa có danh mục. Quay lại và thêm danh mục trước." }
    }
    private fun save() {
        if (!loaded) { status.text = "Hãy tải dữ liệu thành công trước khi lưu."; return }
        fun EditText.value() = text.toString().trim()
        val course = Course(
            id = courseId ?: "", title = titleField.value(), description = description.value(),
            categoryId = categories.getOrNull(category.selectedItemPosition - 1)?.id ?: "",
            instructor = instructor.value(), price = price.value().toDoubleOrNull() ?: Double.NaN,
            duration = duration.value().toIntOrNull() ?: 0, thumbnail = thumbnail.value(),
            trailerUrl = trailer.value(), createdAt = createdAt.value()
        )
        Validation.validateCourse(course, categories.map { it.id }.toSet())?.let { status.text = it; return }
        task {
            val currentIds = api.getCategories().map { it.id }.toSet()
            require(course.categoryId in currentIds) { "Danh mục đã bị xóa. Hãy tải lại dữ liệu." }
            if (courseId == null) api.createCourse(course) else api.updateCourse(courseId!!, course)
            Toast.makeText(this, "Đã lưu khóa học", Toast.LENGTH_SHORT).show()
            setResult(RESULT_OK); finish()
        }
    }
}
