package com.example.nttcinemas.Activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nttcinemas.Models.Ticket
import com.example.nttcinemas.R

@Suppress("DEPRECATION")
class TicketDetailActivity : AppCompatActivity() {

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ticket_detail)

        // Nhận dữ liệu từ Intent
        val ticket = intent.getParcelableExtra<Ticket>("ticket")

        // Khởi tạo các view
        val btnBack: ImageView = findViewById(R.id.btnBack)
        val ivPoster: ImageView = findViewById(R.id.ivPoster)
        val tvMovieName: TextView = findViewById(R.id.tvMovieName)
        val tvDateTime: TextView = findViewById(R.id.tvDateTime)
        val tvCinema: TextView = findViewById(R.id.tvCinema)
        val tvSeatInfo: TextView = findViewById(R.id.tvSeatInfo)
        val tvPrice: TextView = findViewById(R.id.tvPrice)
        val tvPaymentMethod: TextView = findViewById(R.id.tvPaymentMethod)
        val tvTotal: TextView = findViewById(R.id.tvTotal)

        // Nút quay lại
        btnBack.setOnClickListener { finish() }

        // Hiển thị thông tin vé
        ticket?.let {
            tvMovieName.text = it.movieTitle
            tvDateTime.text = it.dateTime
            tvCinema.text = "Rạp: ${it.cinema}"
            tvSeatInfo.text = "Ghế: ${it.seatInfo}" // Hiển thị thông tin ghế
            tvPrice.text = "Giá vé: ${it.price.toInt()} đ"
            tvPaymentMethod.text = "Phương thức thanh toán: ${it.paymentMethod ?: "Không xác định"}"
            tvTotal.text = "Tổng cộng: ${it.price.toInt()} đ"
            Glide.with(this)
                .load(it.posterUrl)               // Hiển thị ảnh từ URL
                .placeholder(R.drawable.ic_avatar) // Ảnh mặc định nếu URL trống
                .into(ivPoster)
        }
    }
}
