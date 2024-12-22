package com.example.nttcinemas.Activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.nttcinemas.Adapter.ViewPagerAdapter
import com.example.nttcinemas.Models.Ticket
import com.example.nttcinemas.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyTicketsActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var emptyMessage: TextView
    private lateinit var db: FirebaseFirestore
    private lateinit var ivBack: ImageView
    private var upcomingTickets = mutableListOf<Ticket>()
    private var watchedTickets = mutableListOf<Ticket>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_tickets)

        // Ánh xạ các thành phần
        db = FirebaseFirestore.getInstance()
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        progressBar = findViewById(R.id.progressBar)
        emptyMessage = findViewById(R.id.emptyMessage)
        ivBack = findViewById(R.id.ivBack)

        // Tải danh sách vé
        loadTickets()

        ivBack.setOnClickListener {
            finish() // Quay lại màn hình trước
        }
    }
    private fun loadTickets() {
        showLoading(true)

        // Lấy email người dùng hiện tại từ Firebase Auth
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: run {
            showError("Không thể lấy thông tin email người dùng!")
            showLoading(false)
            return
        }

        // Lấy vé sắp xem từ Firestore theo userEmail
        db.collection("tickets")
            .whereEqualTo("userEmail", userEmail) // Chỉ lấy vé có email người dùng hiện tại
            .whereEqualTo("status", "upcoming") // Trạng thái "upcoming"
            .get()
            .addOnSuccessListener { result ->
                upcomingTickets = result.documents.mapNotNull { document ->
                    val ticket = document.toObject(Ticket::class.java)
                    ticket?.copy(id = document.id) // Gán ID tài liệu Firestore
                }.toMutableList()

                // Tiếp tục tải vé đã xem
                loadWatchedTickets(userEmail)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                showError("Lỗi khi tải vé sắp xem!")
                showLoading(false) // Dừng hiển thị tải nếu có lỗi
            }
    }

    private fun loadWatchedTickets(userEmail: String) {
        // Lấy vé có trạng thái "watched" từ Firestore theo userEmail
        db.collection("tickets")
            .whereEqualTo("userEmail", userEmail) // Chỉ lấy vé có email người dùng hiện tại
            .whereEqualTo("status", "watched") // Trạng thái "watched"
            .get()
            .addOnSuccessListener { result ->
                watchedTickets = result.documents.mapNotNull { document ->
                    val ticket = document.toObject(Ticket::class.java)
                    ticket?.copy(id = document.id) // Gán ID tài liệu Firestore
                }.toMutableList()

                // Thiết lập giao diện
                setupViewPager()
                showLoading(false)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                showError("Lỗi khi tải vé đã xem!")
                showLoading(false) // Dừng hiển thị tải nếu có lỗi
            }
    }
    private fun setupViewPager() {
        val pages = listOf(upcomingTickets, watchedTickets)

        // Kiểm tra dữ liệu rỗng
        if (pages.all { it.isEmpty() }) {
            emptyMessage.visibility = View.VISIBLE
            viewPager.visibility = View.GONE
            tabLayout.visibility = View.GONE
        } else {
            emptyMessage.visibility = View.GONE
            viewPager.visibility = View.VISIBLE
            tabLayout.visibility = View.VISIBLE

            // Thiết lập adapter cho ViewPager
            viewPager.adapter = ViewPagerAdapter(pages) { ticket ->
                openTicketDetails(ticket)
            }

            // Thiết lập TabLayout cho ViewPager
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = if (position == 0) "Phim sắp xem" else "Phim đã xem"
            }.attach()
        }
    }

    private fun openTicketDetails(ticket: Ticket) {
        // Mở chi tiết vé
        val intent = Intent(this, TicketDetailActivity::class.java)
        intent.putExtra("ticket", ticket)
        startActivity(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        // Hiển thị hoặc ẩn ProgressBar
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        showLoading(false)
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
