package com.lethanhchung.bai5.ui.common

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.lethanhchung.bai5.data.remote.RetrofitClient
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.text.NumberFormat
import java.util.Locale

abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var body: LinearLayout
    protected lateinit var status: TextView
    protected lateinit var progress: ProgressBar
    protected val api get() = RetrofitClient.api(this)
    protected var busy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24, 16, 24, 16) }
        root.setBackgroundColor(Color.rgb(246, 248, 252))
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(24 + bars.left, 16 + bars.top, 24 + bars.right, 16 + bars.bottom)
            insets
        }
        setContentView(root)
        progress = ProgressBar(this).apply { visibility = View.GONE }
        status = TextView(this).apply { setPadding(8, 8, 8, 8); setTextColor(Color.rgb(130, 45, 35)) }
        root.addView(progress)
        root.addView(status)
        body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(body) }, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    protected fun heading(value: String) = label(value, 26f)
    protected fun label(value: String, size: Float = 16f, parent: LinearLayout = body): TextView =
        TextView(this).apply {
            text = value; textSize = size; setTextColor(Color.rgb(24, 38, 64)); setPadding(8, 12, 8, 12)
            parent.addView(this)
        }
    protected fun button(value: String, parent: LinearLayout = body, action: () -> Unit): Button =
        Button(this).apply { text = value; parent.addView(this); setOnClickListener { if (!busy) action() } }
    protected fun field(hintText: String, value: String = "", parent: LinearLayout = body): EditText =
        EditText(this).apply {
            hint = hintText; setText(value); minHeight = 56; parent.addView(this)
        }
    protected fun card(parent: LinearLayout = body): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(16, 16, 16, 16)
            setBackgroundColor(Color.WHITE)
            parent.addView(this, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 10, 0, 10) })
        }
    protected fun money(value: Double): String = NumberFormat.getCurrencyInstance(Locale("vi", "VN")).format(value)
    protected fun task(action: suspend () -> Unit) {
        if (busy) return
        busy = true; progress.visibility = View.VISIBLE; status.text = "Đang xử lý…"
        lifecycleScope.launch {
            try { action(); status.text = "" }
            catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                status.text = when (e) {
                    is HttpException -> "API trả lỗi ${e.code()}. Kiểm tra URL/resource rồi thử lại."
                    is java.io.IOException -> "Không thể kết nối. Kiểm tra Internet rồi thử lại."
                    else -> e.message ?: "Có lỗi xảy ra. Vui lòng thử lại."
                }
            } finally { busy = false; progress.visibility = View.GONE }
        }
    }
}
