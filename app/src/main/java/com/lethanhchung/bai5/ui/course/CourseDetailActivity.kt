package com.lethanhchung.bai5.ui.course

import android.content.Intent
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.load
import com.lethanhchung.bai5.ui.common.BaseActivity

class CourseDetailActivity : BaseActivity() {
    private var player: ExoPlayer? = null
    private var trailerDialog: AlertDialog? = null
    private val courseId get() = intent.getStringExtra("COURSE_ID") ?: ""
    override fun onResume() { super.onResume(); reload() }
    private fun reload(): Unit = task {
        body.removeAllViews()
        button("Quay lại") { finish() }
        button("Tải lại") { reload() }
        val course = api.getCourse(courseId)
        val categories = api.getCategories()
        heading(course.title)
        body.addView(ImageView(this).apply {
            contentDescription = "Ảnh bìa"; scaleType = ImageView.ScaleType.CENTER_CROP
            load(course.thumbnail) { error(android.R.drawable.ic_menu_report_image) }
        }, LinearLayout.LayoutParams(-1, (220 * resources.displayMetrics.density).toInt()))
        label(categories.find { it.id == course.categoryId }?.name ?: "Danh mục không còn tồn tại", 20f)
        label("Giảng viên: ${course.instructor}\nHọc phí: ${money(course.price)}\nThời lượng: ${course.duration} giờ\nNgày tạo: ${course.createdAt}")
        label(course.description)
        button("▶ Xem trailer") { play(course.trailerUrl) }
        button("Sửa khóa học") { startActivity(Intent(this, CourseFormActivity::class.java).putExtra("COURSE_ID", course.id)) }
        button("Xóa khóa học") {
            AlertDialog.Builder(this).setTitle("Xóa khóa học?")
                .setMessage("Bạn có chắc muốn xóa “${course.title}”?")
                .setNegativeButton("Hủy", null).setPositiveButton("Xóa") { _, _ ->
                    task { api.deleteCourse(course.id); finish() }
                }.show()
        }
    }
    private fun play(url: String) {
        val view = PlayerView(this)
        val playback = ExoPlayer.Builder(this).build()
        player = playback; view.player = playback
        trailerDialog = AlertDialog.Builder(this).setTitle("Trailer khóa học").setView(view)
            .setPositiveButton("Đóng", null).create().also {
                it.setOnDismissListener { releasePlayer() }; it.show()
                view.layoutParams = view.layoutParams.apply { height = (240 * resources.displayMetrics.density).toInt() }
            }
        playback.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                trailerDialog?.dismiss()
                status.text = "Không phát được trailer. Kiểm tra kết nối và URL video MP4 trực tiếp."
            }
        })
        playback.setMediaItem(MediaItem.fromUri(url)); playback.prepare(); playback.playWhenReady = true
    }
    private fun releasePlayer() { player?.release(); player = null }
    override fun onStop() { trailerDialog?.dismiss(); releasePlayer(); super.onStop() }
}
