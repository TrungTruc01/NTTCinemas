@file:Suppress("DEPRECATION")

package com.example.nttcinemas.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.nttcinemas.Adapter.CategoryEachFilmAdapter
import com.example.nttcinemas.Adapter.CastListAdapter
import com.example.nttcinemas.Models.Film
import com.example.nttcinemas.databinding.ActivityFilmDetailBinding
import eightbitlab.com.blurview.RenderScriptBlur

class FilmDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilmDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilmDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        setupFilmDetails()
    }

    @SuppressLint("SetTextI18n")
    private fun setupFilmDetails() {
        val film: Film = intent.getParcelableExtra("object")!!

        // Hiển thị Poster
        Glide.with(this)
            .load(film.poster)
            .apply(RequestOptions().transform(CenterCrop(), GranularRoundedCorners(0f, 0f, 50f, 50f)))
            .into(binding.filmPoster)

        // Hiển thị thông tin phim
        binding.filmTitle.text = film.title
        binding.filmDescription.text = film.description
        binding.ageRating.text = "Độ tuổi: ${film.ageRating}"
        binding.directorName.text = "Đạo diễn: ${film.director}"
        binding.castLabel.text = "Diễn viên: ${film.cast.joinToString(", ")}"
        binding.genreList.adapter = CategoryEachFilmAdapter(film.genre)
        binding.genreList.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        // Nút Quay Lại
        binding.backButton.setOnClickListener {
            finish()
        }

        // Nút Đặt Vé
        binding.buyTicketButton.setOnClickListener {
            val intent = Intent(this, SeatListActivity::class.java)
            intent.putExtra("film", film)
            startActivity(intent)
        }

        // Hiệu ứng Blur
        val radius = 10f
        val decorView = window.decorView
        val rootView = decorView.findViewById<ViewGroup>(android.R.id.content)
        val background = decorView.background

        binding.blurView.setupWith(rootView, RenderScriptBlur(this))
            .setFrameClearDrawable(background)
            .setBlurRadius(radius)
        binding.blurView.outlineProvider = ViewOutlineProvider.BACKGROUND
        binding.blurView.clipToOutline = true
    }
}
