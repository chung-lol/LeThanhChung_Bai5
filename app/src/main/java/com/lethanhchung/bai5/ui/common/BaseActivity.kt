package com.lethanhchung.bai5.ui.common

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
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
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.textfield.TextInputEditText

abstract class BaseActivity : AppCompatActivity() {
    protected lateinit var body: LinearLayout
    protected lateinit var status: TextView
    protected lateinit var progress: ProgressBar
    protected val api get() = RetrofitClient.api(this)
    protected var busy = false
    protected fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.setBackgroundColor(Color.rgb(246, 248, 252))
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            view.setPadding(dp(20) + bars.left, dp(12) + bars.top, dp(20) + bars.right,
                dp(12) + maxOf(bars.bottom, keyboard.bottom))
            insets
        }
        setContentView(root)
        progress = ProgressBar(this).apply { visibility = View.GONE }
        status = TextView(this).apply {
            setPadding(dp(4), dp(4), dp(4), dp(4)); setTextColor(Color.rgb(130, 45, 35))
            accessibilityLiveRegion = View.ACCESSIBILITY_LIVE_REGION_POLITE
        }
        root.addView(progress)
        root.addView(status)
        body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(body) }, LinearLayout.LayoutParams(-1, 0, 1f))
    }

    protected fun heading(value: String) = label(value, 28f).apply { setTypeface(typeface, Typeface.BOLD) }
    protected fun label(value: String, size: Float = 16f, parent: LinearLayout = body): TextView =
        TextView(this).apply {
            text = value; textSize = size; setTextColor(Color.rgb(24, 38, 64)); setPadding(dp(4), dp(8), dp(4), dp(8))
            parent.addView(this)
        }
    protected fun button(value: String, parent: LinearLayout = body, action: () -> Unit): Button =
        MaterialButton(this).apply {
            text = value; isAllCaps = false; cornerRadius = dp(14); minHeight = dp(48)
            parent.addView(this, LinearLayout.LayoutParams(-1, -2))
            setOnClickListener { if (!busy) action() }
        }
    protected fun field(hintText: String, value: String = "", parent: LinearLayout = body): EditText =
        TextInputEditText(this).apply {
            setText(value); minHeight = dp(56); textSize = 16f
            val wrapper = TextInputLayout(this@BaseActivity).apply {
                hint = hintText
                boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
                setBoxCornerRadii(dp(12).toFloat(), dp(12).toFloat(), dp(12).toFloat(), dp(12).toFloat())
            }
            wrapper.addView(this, LinearLayout.LayoutParams(-1, -2))
            parent.addView(wrapper, LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, dp(8), 0, dp(4)) })
        }
    protected fun card(parent: LinearLayout = body): LinearLayout =
        LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16))
            background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = dp(20).toFloat() }
            elevation = dp(2).toFloat()
            parent.addView(this, LinearLayout.LayoutParams(-1, -2).apply { setMargins(dp(2), dp(10), dp(2), dp(10)) })
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
