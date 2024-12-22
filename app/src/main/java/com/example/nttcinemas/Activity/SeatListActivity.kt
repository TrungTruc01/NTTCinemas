package com.example.nttcinemas.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ScaleGestureDetector
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nttcinemas.Adapter.DateAdapter
import com.example.nttcinemas.Adapter.SeatListAdapter
import com.example.nttcinemas.Adapter.TimeAdapter
import com.example.nttcinemas.Models.Seat
import com.example.nttcinemas.Models.Film
import com.example.nttcinemas.R
import com.example.nttcinemas.databinding.ActivitySeatListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.google.firebase.database.FirebaseDatabase

@Suppress("DEPRECATION")
class SeatListActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySeatListBinding
    private lateinit var film: Film
    private val firestore = FirebaseFirestore.getInstance() // Firestore instance
    private val seatList = mutableListOf<Seat>() // Danh sách ghế
    private var selectedSeats: MutableList<Seat> = mutableListOf()
    private var totalPrice: Double = 0.0
    private var selectedDate: String = ""
    private var selectedTime: String = ""
    private var scaleFactor = 1.0f
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private lateinit var drawerLayout: DrawerLayout
    private val realtimeDatabase = FirebaseDatabase.getInstance().reference
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySeatListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = FirebaseFirestore.getInstance()
        // Khởi tạo drawerLayout
        drawerLayout = findViewById(R.id.drawerLayout)
        // Nhận dữ liệu từ intent
        getIntentExtra()
        // Cấu hình giao diện
        setupUI()
        setupSeats()
        updateSeatsFromFirestore() // Cập nhật trạng thái ghế sau khi quay lại từ màn hình thanh toán

        // Cấu hình tính năng zoom
        setupZoomFeature()
        setupNavigationMenu()
        setupDateAndTime()
        setupMenuClickListeners()
        updateMenuLoginText()
        // Chế độ toàn màn hình
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun setupNavigationMenu() {
        binding.hamburgerMenu.setOnClickListener {
            toggleDrawerMenu()
        }

        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    private fun toggleDrawerMenu() {
        if (drawerLayout.isDrawerOpen(binding.navigationView)) {
            drawerLayout.closeDrawer(binding.navigationView)
        } else {
            drawerLayout.openDrawer(binding.navigationView)
        }
    }
    private fun setupMenuClickListeners() {
        // Tìm TextView "Đăng Nhập/Đăng Ký" từ NavigationView
        val headerView = binding.navigationView.getHeaderView(0) // Header của NavigationView
        val loginRegisterButton = headerView.findViewById<TextView>(R.id.loginRegisterButton)
        val thanhvien = headerView.findViewById<LinearLayout>(R.id.member)
        val trangchu = headerView.findViewById<LinearLayout>(R.id.home)
        val ticket = headerView.findViewById<LinearLayout>(R.id.ticket)
        val logout = headerView.findViewById<TextView>(R.id.logout)
        // Gắn sự kiện click cho TextView
        loginRegisterButton.setOnClickListener {
            // Mở Activity Đăng Nhập/Đăng Ký
            if (auth.currentUser != null) {
                // Đã đăng nhập -> Chuyển đến ProfileActivity
            } else {
                // Chưa đăng nhập -> Chuyển đến LoginActivity
                val intent = Intent(this, AuthenticationActivity::class.java)
                startActivity(intent)
            }
        }
        thanhvien.setOnClickListener {
            if (auth.currentUser != null) {
                // Đã đăng nhập -> Chuyển đến ProfileActivity
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
            } else {
                // Chưa đăng nhập -> Chuyển đến LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        trangchu.setOnClickListener {
            // Mở Activity Đăng Nhập/Đăng Ký
            startActivity(Intent(this, MainActivity::class.java))
        }
        ticket.setOnClickListener {
            if (auth.currentUser != null) {
                // Đã đăng nhập -> Chuyển đến ProfileActivity
                val intent = Intent(this, MyTicketsActivity::class.java)
                startActivity(intent)
            } else {
                // Chưa đăng nhập -> Chuyển đến LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            // Kiểm tra trạng thái đăng nhập
            FirebaseAuth.getInstance().addAuthStateListener { auth ->
                if (auth.currentUser == null) {
                    // Người dùng đã đăng xuất, chuyển sang LoginActivity
                    Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }

    }
    private fun updateMenuLoginText() {
        // Lấy email từ Firebase Auth
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: run {
            return
        }

        // Truy vấn Firestore để tìm tài liệu chứa email
        db.collection("Users").whereEqualTo("email", userEmail).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Lấy tài liệu đầu tiên
                    val document = querySnapshot.documents.first()
                    val userName = document.getString("fullName") ?: "Người dùng"

                    // Tìm NavigationView
                    val navigationView = binding.navigationView

                    // Lấy TextView trong headerLayout
                    val headerView = navigationView.getHeaderView(0)
                    val loginRegisterTextView: TextView = headerView.findViewById(R.id.loginRegisterButton)

                    // Cập nhật nội dung TextView
                    loginRegisterTextView.text = userName
                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Lỗi khi tải thông tin người dùng: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun getIntentExtra() {
        film = intent.getParcelableExtra("film")!!
    }

    private fun setupUI() {
        binding.movieTitle.text = film.title

        binding.btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        binding.bookTicketBtn.setOnClickListener {
            when {
                selectedDate.isEmpty() -> {
                    Toast.makeText(this, "Vui lòng chọn ngày chiếu!", Toast.LENGTH_SHORT).show()
                }
                selectedTime.isEmpty() -> {
                    Toast.makeText(this, "Vui lòng chọn giờ chiếu!", Toast.LENGTH_SHORT).show()
                }
                selectedSeats.isEmpty() -> {
                    Toast.makeText(this, "Vui lòng chọn ít nhất một ghế!", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    val intent = Intent(this, PaymentActivity::class.java)
                    intent.putExtra("film", film)
                    intent.putStringArrayListExtra("selected_seats", ArrayList(selectedSeats.map { it.name }))
                    intent.putExtra("selected_date", selectedDate)
                    intent.putExtra("selected_time", selectedTime)
                    intent.putExtra("total_price", totalPrice)
                    startActivity(intent)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged", "ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupSeats() {
        val gridLayoutManager = GridLayoutManager(this, 8)
        binding.seatRecyclerview.layoutManager = gridLayoutManager

        for (row in 'A'..'G') {
            for (col in 1..8) {
                val seatName = "$row$col"
                val seatType = when (row) {
                    in 'A'..'C' -> Seat.SeatType.NORMAL
                    in 'D'..'H' -> Seat.SeatType.VIP
                    else -> Seat.SeatType.NORMAL
                }
                seatList.add(Seat(seatType, seatName, Seat.SeatStatus.AVAILABLE))
            }
        }

        val seatAdapter = SeatListAdapter(seatList, this) { seat, isSelected ->
            if (seat.status == Seat.SeatStatus.UNAVAILABLE) {
                Toast.makeText(this, "Ghế này đã được đặt!", Toast.LENGTH_SHORT).show()
                return@SeatListAdapter
            }

            if (isSelected) {
                selectedSeats.add(seat)
                totalPrice += when (seat.type) {
                    Seat.SeatType.NORMAL -> 75000.0
                    Seat.SeatType.VIP -> 80000.0
                    Seat.SeatType.DOUBLE -> 200000.0
                }
            } else {
                selectedSeats.remove(seat)
                totalPrice -= when (seat.type) {
                    Seat.SeatType.NORMAL -> 75000.0
                    Seat.SeatType.VIP -> 80000.0
                    Seat.SeatType.DOUBLE -> 200000.0
                }
            }
            updateBottomUI()
        }

        binding.seatRecyclerview.adapter = seatAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateSeatsFromFirestore() {
        firestore.collection("tickets")
            .whereEqualTo("movieTitle", film.title)
            .get()
            .addOnSuccessListener { snapshot ->
                snapshot.documents.forEach { document ->
                    val seatInfo = document.getString("seatInfo") ?: ""
                    val bookedSeats = seatInfo.split(", ").map { it.trim() }
                    bookedSeats.forEach { seatName ->
                        seatList.find { it.name == seatName }?.status = Seat.SeatStatus.UNAVAILABLE
                    }
                }
                binding.seatRecyclerview.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi khi cập nhật trạng thái ghế từ Firestore!", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupDateAndTime() {
        // Lấy danh sách các ngày (7 ngày từ hôm nay)
        val dateList = List(7) {
            LocalDate.now().plusDays(it.toLong()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        }
        binding.dateRecyclerview.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.dateRecyclerview.adapter = DateAdapter(dateList) { selectedDate = it }

        // Tính toán giờ từ Realtime Database
        calculateApproximateTimesFromRealtimeDatabase { val timeList = List(15) {
            LocalTime.of(9 + it, 0).format(DateTimeFormatter.ofPattern("HH:mm"))
        }
            binding.TimeRecyclerview.layoutManager =
                LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            binding.TimeRecyclerview.adapter = TimeAdapter(timeList) { timeSelected ->
                handleTimeSelection(timeSelected)
            }
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun handleTimeSelection(timeSelected: String) {
        val currentDate = LocalDate.now()
        val currentTime = LocalTime.now()

        // Tách giờ bắt đầu từ chuỗi "startTime ~ endTime"
        val startTimeStr = timeSelected.split("~")[0].trim()
        val startTime = LocalTime.parse(startTimeStr, DateTimeFormatter.ofPattern("HH:mm"))

        // Kiểm tra nếu giờ đã qua
        if (selectedDate == currentDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) && startTime.isBefore(currentTime)) {
            Toast.makeText(this, "Giờ đã qua không thể đặt ghế!", Toast.LENGTH_SHORT).show()
        } else {
            selectedTime = timeSelected // Chọn giờ nếu hợp lệ
        }
    }

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateApproximateTimesFromRealtimeDatabase(onTimesReady: (List<String>) -> Unit) {
        realtimeDatabase.child("movies").child(film.title!!).child("times").get()
            .addOnSuccessListener { snapshot ->
                val timeList = snapshot.children.mapNotNull { data ->
                    val startTimeStr = data.key
                    val durationMinutes = data.getValue(Int::class.java) ?: 0
                    startTimeStr?.let {
                        val startTime = LocalTime.parse(it, DateTimeFormatter.ofPattern("HH:mm"))
                        val endTime = startTime.plusMinutes(durationMinutes.toLong())
                        "$startTime ~ $endTime"
                    }
                }
                onTimesReady(timeList)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Không thể tải giờ chiếu từ Realtime Database!", Toast.LENGTH_SHORT).show()
            }
    }

    @SuppressLint("SetTextI18n")
    private fun updateBottomUI() {
        binding.totalPrice.text = "Tổng: ${totalPrice.toInt()} đ"
        binding.totalSeatsSelected.text = "Số ghế: ${selectedSeats.size}"
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupZoomFeature() {
        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = scaleFactor.coerceIn(0.5f, 2.0f)
                binding.seatRecyclerview.scaleX = scaleFactor
                binding.seatRecyclerview.scaleY = scaleFactor
                binding.legendLayout.scaleX = scaleFactor
                binding.legendLayout.scaleY = scaleFactor
                return true
            }
        })

        binding.seatRecyclerview.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            true
        }
    }

}
