package com.example.nttcinemas.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nttcinemas.Models.Film
import com.example.nttcinemas.Models.Ticket
import com.example.nttcinemas.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class PaymentActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageView
    private lateinit var movieTitle: TextView
    private lateinit var moviePoster: ImageView
    private lateinit var movieTime: TextView
    private lateinit var movieDetails: TextView
    private lateinit var totalPayment: TextView
    private lateinit var confirmButton: Button

    private lateinit var paymentNapas: LinearLayout
    private lateinit var paymentCash: LinearLayout
    private lateinit var tickNapas: ImageView
    private lateinit var tickCash: ImageView

    private lateinit var ticketQuantity: TextView
    private lateinit var ticketTotal: TextView
    private lateinit var ticketDiscount: TextView

    private lateinit var db: FirebaseFirestore
    private val firebaseAuth = FirebaseAuth.getInstance()

    private var selectedSeats = listOf<String>()
    private var totalPrice = 0.0
    private var selectedPaymentMethod: String = ""

    // Liên kết Napas với các mức giá
    private val paymentLinks = mapOf(
        75000 to "https://pay.payos.vn/web/2b135fa6ff3c4668baf336216625fa59",
        80000 to "https://pay.payos.vn/web/5526785c67e24eac808bb7fdbbce7225",
        150000 to "https://pay.payos.vn/web/d3cb96a5f029498c85a8c840c08106f2",
        155000 to "https://pay.payos.vn/web/19b8c82f466840e2a0e481ec8eea7921",
        160000 to "https://pay.payos.vn/web/57cc9d153c7e4862a44f148cebaeafa4",
        225000 to "https://pay.payos.vn/web/cc67203489cf4dda8d99d6392e23ab23",
        240000 to "https://pay.payos.vn/web/9d588b0159cd449e8b9ef0111738ef6d",
        300000 to "https://pay.payos.vn/web/09d60db16a8e4f7b9db8fd68df0f92a1",
        320000 to "https://pay.payos.vn/web/1b8f3b4cfe434054a277e3cf6656f7a7",
        375000 to "https://pay.payos.vn/web/62c8ed1db2d94e90b39dd3e68c145066",
        400000 to "https://pay.payos.vn/web/f79b27b701e64ee096dfcf430e38c39c",
        450000 to "https://pay.payos.vn/web/ccbf627e7fad40e382396c747a1045ae",
        480000 to "https://pay.payos.vn/web/a164a7f5690849b9aa246f0d57c7e630",
        525000 to "https://pay.payos.vn/web/52e1d75ce5e446d98371987e93c771d8",
        560000 to "https://pay.payos.vn/web/31eb8eb7c5bc44fe94023034275c9d98",
        600000 to "https://pay.payos.vn/web/a15b3a9033aa493dbf73045f9d6e0b54",
        640000 to "https://pay.payos.vn/web/f6dc712f1fa449e785ad60ba0b3d74a0"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        db = FirebaseFirestore.getInstance()

        // Khởi tạo giao diện
        initViews()
        setupPaymentMethods()

        // Nút quay lại
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Nhận dữ liệu từ SeatListActivity
        val film = intent.getParcelableExtra<Film>("film")
        selectedSeats = intent.getStringArrayListExtra("selected_seats") ?: arrayListOf()
        val selectedDate = intent.getStringExtra("selected_date") ?: "Không rõ ngày"
        val selectedTime = intent.getStringExtra("selected_time") ?: "Không rõ giờ"
        totalPrice = intent.getDoubleExtra("total_price", 0.0)

        // Hiển thị chi tiết phim và ghế ngồi
        displayMovieDetails(film, selectedDate, selectedTime)

        // Xử lý thanh toán
        confirmButton.setOnClickListener {
            handlePaymentConfirmation(film)
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        movieTitle = findViewById(R.id.movieTitle)
        moviePoster = findViewById(R.id.moviePoster)
        movieTime = findViewById(R.id.movieTime)
        movieDetails = findViewById(R.id.movieDetails)
        totalPayment = findViewById(R.id.totalPayment)
        confirmButton = findViewById(R.id.confirmButton)

        paymentNapas = findViewById(R.id.Napas)
        paymentCash = findViewById(R.id.tienmat)
        tickNapas = findViewById(R.id.tickNapas)
        tickCash = findViewById(R.id.tickTienmat)

        ticketQuantity = findViewById(R.id.ticketQuantity)
        ticketTotal = findViewById(R.id.ticketTotal)
        ticketDiscount = findViewById(R.id.ticketDiscount)
    }

    private fun setupPaymentMethods() {
        paymentNapas.setOnClickListener { selectPaymentMethod("Napas") }
        paymentCash.setOnClickListener { selectPaymentMethod("Tiền mặt") }
    }

    private fun selectPaymentMethod(method: String) {
        selectedPaymentMethod = method
        tickNapas.visibility = View.GONE
        tickCash.visibility = View.GONE

        when (method) {
            "Napas" -> tickNapas.visibility = View.VISIBLE
            "Tiền mặt" -> tickCash.visibility = View.VISIBLE
        }
    }

    @SuppressLint("SetTextI18n")
    private fun displayMovieDetails(film: Film?, date: String, time: String) {
        movieTitle.text = film?.title ?: "Không xác định"
        movieTime.text = "$date | $time"
        movieDetails.text = "Ghế: ${selectedSeats.joinToString(", ")}"
        totalPayment.text = "Thành Tiền: ${totalPrice.toInt()} đ"

        film?.poster?.let {
            Glide.with(this).load(it).into(moviePoster)
        }

        ticketQuantity.text = selectedSeats.size.toString()
        ticketTotal.text = "${totalPrice.toInt()} đ"
        ticketDiscount.text = "0 đ" // Hiện tại không có giảm giá
    }

    private fun handlePaymentConfirmation(film: Film?) {
        if (selectedPaymentMethod.isEmpty()) {
            Toast.makeText(this, "Vui lòng chọn phương thức thanh toán!", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedPaymentMethod == "Napas") {
            val paymentUrl = paymentLinks[totalPrice.toInt()]
            if (paymentUrl != null) {
                openPaymentUrl(paymentUrl)
                saveTicketToFirestore(film) // Lưu vé vào Firestore
            } else {
                Toast.makeText(this, "Không tìm thấy liên kết thanh toán cho số tiền này.", Toast.LENGTH_SHORT).show()
            }
        } else if (selectedPaymentMethod == "Tiền mặt") {
            saveTicketToFirestore(film)
            Toast.makeText(this, "Bạn đã chọn thanh toán bằng tiền mặt!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openPaymentUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }

    private fun saveTicketToFirestore(film: Film?) {
        val user = firebaseAuth.currentUser ?: return

        val cinemas = listOf(
            "NTT Cinemas - Cinema 1",
            "NTT Cinemas - Cinema 2",
            "NTT Cinemas - Cinema 3",
            "NTT Cinemas - Cinema 4",
            "NTT Cinemas - Cinema 5",
            "NTT Cinemas - Cinema 6"
        )
        val randomCinema = cinemas.random()

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentDate = sdf.format(Date())

        val ticket = Ticket(
            movieTitle = film?.title ?: "Không xác định",
            dateTime = movieTime.text.toString(),
            cinema = randomCinema,
            seatInfo = selectedSeats.joinToString(", "),
            price = totalPrice,
            paymentMethod = selectedPaymentMethod,
            posterUrl = film?.poster ?: "",
            userEmail = user.email ?: "",
            bookingDate = currentDate
        )

        db.collection("tickets").get().addOnSuccessListener { querySnapshot ->
            val nextId = querySnapshot.size() // Lấy ID tiếp theo

            // Tạo ID mới
            val ticketId = nextId.toString()

            db.collection("tickets").document(ticketId).set(ticket.copy(id = ticketId))
                .addOnSuccessListener {
                    Toast.makeText(this, "Đặt vé thành công tại $randomCinema!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, TicketDetailActivity::class.java)
                    intent.putExtra("ticket", ticket.copy(id = ticketId))
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Lỗi khi lưu vé!", Toast.LENGTH_SHORT).show()
                }
        }.addOnFailureListener {
            Toast.makeText(this, "Không thể tạo ID cho vé!", Toast.LENGTH_SHORT).show()
        }
    }
}