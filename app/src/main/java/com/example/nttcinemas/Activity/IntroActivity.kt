package com.example.nttcinemas.Activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.nttcinemas.databinding.ActivityIntroBinding
import com.example.nttcinemas.R
class IntroActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ẩn thanh trạng thái để full màn hình
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // Thiết lập video
        setupVideo()
    }

    private fun setupVideo() {
        // Đường dẫn video trong res/raw
        val videoUri = Uri.parse("android.resource://${packageName}/${R.raw.video_intro}")
        binding.videoView.setVideoURI(videoUri)

        // Bắt đầu phát video
        binding.videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = false // Không lặp lại video
            binding.videoView.start()
        }

        // Xử lý sự kiện khi video kết thúc
        binding.videoView.setOnCompletionListener {
            // Chuyển sang MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish() // Kết thúc IntroActivity
        }
    }
}
