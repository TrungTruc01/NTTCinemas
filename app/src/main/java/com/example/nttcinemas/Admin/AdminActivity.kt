package com.example.nttcinemas.Admin

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.nttcinemas.R
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth

@Suppress("DEPRECATION")
class AdminActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var adminTitle: TextView
    private var isQuanly: Boolean = false // Xác định xem đây có phải tài khoản quản lý hay không

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Nhận giá trị isQuanly từ Intent
        isQuanly = intent.getBooleanExtra("isQuanly", false)

        // Khởi tạo DrawerLayout và NavigationView
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        adminTitle = findViewById(R.id.adminTitle) // Liên kết với TextView adminTitle
        val hamburgerMenu: ImageView = findViewById(R.id.hamburgerMenu)

        // Mở Navigation Drawer khi nhấn vào hamburgerMenu
        hamburgerMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Thiết lập Navigation với NavHostFragment
        val navController = findNavController(R.id.adminNavHostFragment)
        navigationView.setupWithNavController(navController)

        // Cập nhật giao diện dựa trên loại tài khoản
        setupAdminUI(navController)

        // Xử lý khi chọn item trong NavigationView
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_banner -> navController.navigate(R.id.nav_banner)
                R.id.nav_film -> navController.navigate(R.id.nav_film)
                R.id.nav_statistics -> navController.navigate(R.id.nav_statistics)
                R.id.nav_users -> navController.navigate(R.id.nav_users)
                R.id.nav_logout -> handleLogout() // Xử lý đăng xuất
            }
            drawerLayout.closeDrawer(GravityCompat.START) // Đóng Drawer sau khi chọn
            true
        }
    }

    // Cập nhật giao diện Admin hoặc Quản lý
    private fun setupAdminUI(navController: androidx.navigation.NavController) {
        val navHeaderView = navigationView.getHeaderView(0)
        val navHeaderTextView: TextView = navHeaderView.findViewById(R.id.headerTextView)

        if (isQuanly) {
            // Tài khoản quản lý
            navHeaderTextView.text = "Quản lý"
            adminTitle.text = "Quản lý" // Cập nhật adminTitle

            // Ẩn các mục không cần thiết
            val navMenu = navigationView.menu
            navMenu.findItem(R.id.nav_banner).isVisible = false
            navMenu.findItem(R.id.nav_film).isVisible = false
            navMenu.findItem(R.id.nav_users).isVisible = false

            // Chỉ giữ lại mục Thống kê và Đăng xuất
            navMenu.findItem(R.id.nav_statistics).isVisible = true
            navMenu.findItem(R.id.nav_logout).isVisible = true

            // Mặc định điều hướng đến Thống kê
            navController.navigate(R.id.nav_statistics)
        } else {
            // Tài khoản Admin đầy đủ
            navHeaderTextView.text = "Admin"
            adminTitle.text = "Admin" // Cập nhật adminTitle
        }
    }

    // Hàm xử lý đăng xuất
    private fun handleLogout() {
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    // Xử lý Back Button để đóng Drawer nếu đang mở
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
