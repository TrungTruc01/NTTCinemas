package com.example.nttcinemas.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.LinearGradient
import android.graphics.Shader
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.nttcinemas.Adapter.FilmListAdapter
import com.example.nttcinemas.Adapter.SliderAdapter
import com.example.nttcinemas.Models.Film
import com.example.nttcinemas.Models.SliderItems
import com.example.nttcinemas.databinding.ActivityMainBinding
import com.google.firebase.database.*
import kotlin.math.abs
import com.example.nttcinemas.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.firestore.FirebaseFirestore

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var drawerLayout: DrawerLayout // Thêm DrawerLayout để quản lý menu điều hướng

    private val sliderHandler = Handler(Looper.getMainLooper())
    private val sliderRunnable = Runnable {
        binding.bannerSlider.currentItem = binding.bannerSlider.currentItem + 1
    }

    private val movieCategories = mutableMapOf<String, List<Film>>()
    private var currentTab = "NowPlaying" // Tab mặc định
    private var currentListener: ValueEventListener? = null
    private var currentRef: DatabaseReference? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = FirebaseFirestore.getInstance()
        // Thiết lập View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        updateAvatarAndName()
        // Firebase database
        database = FirebaseDatabase.getInstance()
        // Khởi tạo FirebaseAuth
        auth = FirebaseAuth.getInstance()

        // Cập nhật TextView trong menu_navigation
        updateMenuLoginText()
        // Đặt chế độ toàn màn hình
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // Thêm logic khởi tạo menu điều hướng
        drawerLayout = findViewById(R.id.drawerLayout)
        setupNavigationMenu()

        // Khởi tạo các thành phần UI khác
        initBanner()
        setupTabs()

        // Tab mặc định "Now Playing"
        currentTab = "NowPlaying"
        selectTab(
            binding.tabNowShowing,
            binding.tabNowShowingUnderline,
            listOf(
                binding.tabSpecial to binding.tabSpecialUnderline,
                binding.tabUpcoming to binding.tabUpcomingUnderline
            )
        )
        updateTabTextSize(binding.tabNowShowing, binding.tabSpecial, binding.tabUpcoming)
        displayMovies("NowPlaying")
        // Xử lý khi bấm vào avatar
        binding.avatarImage.setOnClickListener {
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
        setupMenuClickListeners()
        binding.ticketIcon.setOnClickListener {
            if (auth.currentUser == null) {
                // Chưa đăng nhập -> chuyển sang màn hình đăng nhập
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            } else {
                // Đã đăng nhập -> chuyển sang màn hình "Vé của tôi"
                val intent = Intent(this, MyTicketsActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun setupNavigationMenu() {
        // Xử lý mở menu khi nhấn nút hamburger
        binding.hamburgerMenu.setOnClickListener {
            toggleDrawerMenu()
        }

        // Đóng menu khi nhấn vào bên ngoài
        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {}
        })
    }

    private fun toggleDrawerMenu() {
        // Mở hoặc đóng menu điều hướng
        if (drawerLayout.isDrawerOpen(binding.navigationView)) {
            drawerLayout.closeDrawer(binding.navigationView)
        } else {
            drawerLayout.openDrawer(binding.navigationView)
        }
    }

    private fun setupTabs() {
        // Xử lý khi nhấn vào từng tab
        binding.tabNowShowing.setOnClickListener {
            disconnectCurrentTab()
            currentTab = "NowPlaying"
            selectTab(
                binding.tabNowShowing,
                binding.tabNowShowingUnderline,
                listOf(
                    binding.tabSpecial to binding.tabSpecialUnderline,
                    binding.tabUpcoming to binding.tabUpcomingUnderline
                )
            )
            displayMovies("NowPlaying")
            updateTabTextSize(binding.tabNowShowing, binding.tabSpecial, binding.tabUpcoming)
        }

        binding.tabSpecial.setOnClickListener {
            disconnectCurrentTab()
            currentTab = "Featured"
            selectTab(
                binding.tabSpecial,
                binding.tabSpecialUnderline,
                listOf(
                    binding.tabNowShowing to binding.tabNowShowingUnderline,
                    binding.tabUpcoming to binding.tabUpcomingUnderline
                )
            )
            displayMovies("Featured")
            updateTabTextSize(binding.tabSpecial, binding.tabNowShowing, binding.tabUpcoming)
        }

        binding.tabUpcoming.setOnClickListener {
            disconnectCurrentTab()
            currentTab = "Upcoming"
            selectTab(
                binding.tabUpcoming,
                binding.tabUpcomingUnderline,
                listOf(
                    binding.tabNowShowing to binding.tabNowShowingUnderline,
                    binding.tabSpecial to binding.tabSpecialUnderline
                )
            )
            displayMovies("Upcoming")
            updateTabTextSize(binding.tabUpcoming, binding.tabNowShowing, binding.tabSpecial)
        }
    }

    private fun updateTabTextSize(selected: TextView, unselected1: TextView, unselected2: TextView) {
        // Thay đổi kích thước chữ cho tab đang được chọn
        selected.textSize = 18f
        unselected1.textSize = 16f
        unselected2.textSize = 16f
    }

    private fun selectTab(
        selectedTab: TextView,
        selectedUnderline: View,
        otherTabs: List<Pair<TextView, View>>
    ) {
        // Reset trạng thái tất cả các tab
        otherTabs.forEach { (tab, underline) ->
            tab.paint.shader = null
            tab.setTextColor(resources.getColor(R.color.tab_inactive))
            underline.visibility = View.INVISIBLE
        }

        // Làm nổi bật tab được chọn
        applyGradientToText(selectedTab)
        selectedTab.setTextColor(resources.getColor(R.color.tab_active))
        selectedUnderline.visibility = View.VISIBLE
    }
    private fun applyGradientToText(textView: TextView) {
        val textPaint = textView.paint
        val width = textPaint.measureText(textView.text.toString())

        val shader = LinearGradient(
            0f, 0f, width, 0f,
            resources.getColor(R.color.gradient_start),
            resources.getColor(R.color.gradient_end),
            Shader.TileMode.CLAMP
        )

        textView.paint.shader = shader
    }
    private fun initBanner() {
        val myRef: DatabaseReference = database.getReference("Banners")
        binding.progressBarSlider.visibility = View.VISIBLE

        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val sliderItems = mutableListOf<SliderItems>()
                for (childSnapshot in snapshot.children) {
                    val item = childSnapshot.getValue(SliderItems::class.java)
                    if (item != null) {
                        sliderItems.add(item)
                    }
                }
                binding.progressBarSlider.visibility = View.GONE
                setupBannerSlider(sliderItems)
            }

            override fun onCancelled(error: DatabaseError) {
                    binding.progressBarSlider.visibility = View.GONE
            }
        })
    }
    private fun setupBannerSlider(sliderItems: MutableList<SliderItems>) {
        if (sliderItems.isEmpty()) return

        // Tăng danh sách để tạo hiệu ứng cuộn vô hạn
        val infiniteSliderItems = mutableListOf<SliderItems>().apply {
            addAll(sliderItems)
            addAll(sliderItems)
            addAll(sliderItems) // Tăng số lượng mục để cuộn liên tục
        }

        val adapter = SliderAdapter(infiniteSliderItems, binding.bannerSlider)
        binding.bannerSlider.adapter = adapter
        binding.bannerSlider.clipToPadding = false
        binding.bannerSlider.clipChildren = false
        binding.bannerSlider.offscreenPageLimit = 5
        binding.bannerSlider.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER

        val compositePageTransformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(40))
            addTransformer { page, position ->
                val r = 1 - abs(position)
                page.scaleY = 0.85f + r * 0.15f
            }
        }
        binding.bannerSlider.setPageTransformer(compositePageTransformer)

        setupDots(sliderItems.size)

        // Đặt vị trí ban đầu vào giữa danh sách để tạo hiệu ứng vô hạn
        val middlePosition = infiniteSliderItems.size / 3
        binding.bannerSlider.setCurrentItem(middlePosition, false)

        binding.bannerSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 3000)

                // Tính toán vị trí thật trong danh sách gốc
                val realPosition = position % sliderItems.size
                updateDots(realPosition)
            }
        })
    }
    private fun setupDots(count: Int) {
        binding.bannerDots.removeAllViews()
        val dots = Array(count) { ImageView(this) }
        for (i in dots.indices) {
            dots[i] = ImageView(this).apply {
                setImageResource(R.drawable.dot_inactive)
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(8, 0, 8, 0)
                layoutParams = params
            }
            binding.bannerDots.addView(dots[i])
        }
        if (dots.isNotEmpty()) dots[0].setImageResource(R.drawable.dot_active)
    }
    private fun updateDots(position: Int) {
        val dotsCount = binding.bannerDots.childCount
        for (i in 0 until dotsCount) {
            val imageView = binding.bannerDots.getChildAt(i) as ImageView
            if (i == position) {
                imageView.setImageResource(R.drawable.dot_active)
            } else {
                imageView.setImageResource(R.drawable.dot_inactive)
            }
        }
    }
    private fun displayMovies(category: String) {
        binding.progressBarTopMovies.visibility = View.VISIBLE

        disconnectCurrentTab()

        if (movieCategories.containsKey(category)) {
            val movies = movieCategories[category] ?: emptyList()
            setupMovieViewPager(movies)
            binding.progressBarTopMovies.visibility = View.GONE
            return
        }

        val myRef = database.getReference("Items").child(category)
        currentRef = myRef
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val movies = mutableListOf<Film>()
                snapshot.children.mapNotNullTo(movies) { it.getValue(Film::class.java) }
                movieCategories[category] = movies
                setupMovieViewPager(movies)
                binding.progressBarTopMovies.visibility = View.GONE
            }

            override fun onCancelled(error: DatabaseError) {
                binding.progressBarTopMovies.visibility = View.GONE
            }
        }
        currentListener = listener
        myRef.addListenerForSingleValueEvent(listener)
    }
    private fun disconnectCurrentTab() {
        currentRef?.removeEventListener(currentListener ?: return)
    }
    private fun setupMovieViewPager(items: List<Film>) {
        if (items.isEmpty()) return

        // Tăng thêm các mục để mô phỏng vòng lặp
        val infiniteItems = mutableListOf<Film>().apply {
            addAll(items)
            addAll(items)
            addAll(items) // Nhân đôi hoặc gấp ba danh sách để giả lập vòng lặp
        }

        val adapter = FilmListAdapter(ArrayList(infiniteItems))
        binding.movieViewPager.adapter = adapter

        val transformer = CompositePageTransformer().apply {
            addTransformer(MarginPageTransformer(5))
            addTransformer { page, position ->
                val absPosition = abs(position)
                page.scaleY = 0.85f + (1 - absPosition) * 0.15f
                page.scaleX = 0.85f + (1 - absPosition) * 0.15f
            }
        }
        binding.movieViewPager.setPageTransformer(transformer)
        binding.movieViewPager.clipToPadding = false
        binding.movieViewPager.clipChildren = false
        binding.movieViewPager.offscreenPageLimit = 3

        // Đặt vị trí ban đầu vào giữa danh sách
        val middlePosition = infiniteItems.size / 3
        binding.movieViewPager.setCurrentItem(middlePosition, false)

        binding.movieViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val realPosition = position % items.size
                updateMovieInfo(items[realPosition])
            }
        })

        // Cập nhật thông tin mục đầu tiên
        updateMovieInfo(items.first())
    }
    @SuppressLint("SetTextI18n")
    private fun updateMovieInfo(film: Film) {
        binding.movieTitle.text = film.title
        binding.movieRating.text = film.ageRating ?: "N/A"
        binding.movieDetails.text = "${film.time ?: "0 giờ"} | ${film.releaseDate}"
    }
    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }
    override fun onResume() {
        super.onResume()
        sliderHandler.postDelayed(sliderRunnable, 3000)
        refreshData()
    }
    private fun refreshData() {
        initBanner() // Tải lại banner
        displayMovies(currentTab) // Tải lại phim theo tab hiện tại
        updateAvatarAndName() // Cập nhật lại avatar và tên
    }
    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    override fun onBackPressed() {
        // Đóng menu nếu đang mở, nếu không thì thoát Activity
        if (drawerLayout.isDrawerOpen(binding.navigationView)) {
            drawerLayout.closeDrawer(binding.navigationView)
        } else {
            super.onBackPressed()
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
    @SuppressLint("SetTextI18n")
    private fun updateAvatarAndName() {
        val headerView = binding.navigationView.getHeaderView(0) // Header của NavigationView
        val avatarInMenu = headerView.findViewById<ImageView>(R.id.avatarImage) // Avatar trong NavigationView
        val nameInMenu = headerView.findViewById<TextView>(R.id.loginRegisterButton) // Tên người dùng trong NavigationView
        val avatarInMain = binding.avatarImage // Avatar trên giao diện chính
        val user = auth.currentUser

        if (user != null) {
            // Lấy avatar từ Firebase Storage
            val storageRef = FirebaseStorage.getInstance().reference.child("avatars/${user.uid}.jpg")

            storageRef.downloadUrl.addOnSuccessListener { uri ->
                // Hiển thị avatar trên NavigationView
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform()) // Cắt avatar thành hình tròn
                    .placeholder(R.drawable.ic_avatar) // Ảnh placeholder khi đang tải
                    .error(R.drawable.ic_avatar) // Ảnh lỗi khi tải
                    .into(avatarInMenu)

                // Hiển thị avatar trên giao diện chính
                Glide.with(this)
                    .load(uri)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_avatar)
                    .error(R.drawable.ic_avatar)
                    .into(avatarInMain)
            }.addOnFailureListener {
                // Hiển thị avatar mặc định nếu tải không thành công
                avatarInMenu.setImageResource(R.drawable.ic_avatar)
                avatarInMain.setImageResource(R.drawable.ic_avatar)
            }

            val firestoreRef = FirebaseFirestore.getInstance().collection("Users")

            // Lấy email từ Firebase Auth
            val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: run {
                nameInMenu.text = "Người dùng" // Nếu không có email, mặc định là "Người dùng"
                return
            }

            // Truy vấn Firestore để lấy dữ liệu người dùng
            firestoreRef.whereEqualTo("email", userEmail).get()
                .addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        // Lấy tài liệu đầu tiên
                        val document = querySnapshot.documents.first()
                        val userName = document.getString("fullName") ?: "Người dùng" // Nếu không có tên, mặc định là "Người dùng"
                        nameInMenu.text = userName
                    } else {
                        nameInMenu.text = "Người dùng" // Không tìm thấy tài liệu nào, mặc định là "Người dùng"
                    }
                }
                .addOnFailureListener {
                    nameInMenu.text = "Người dùng" // Nếu có lỗi khi truy vấn, mặc định là "Người dùng"
                }
        } else {
            // Hiển thị avatar và tên mặc định nếu người dùng chưa đăng nhập
            avatarInMenu.setImageResource(R.drawable.ic_avatar)
            avatarInMain.setImageResource(R.drawable.ic_avatar)
            nameInMenu.text = "Đăng nhập/Đăng ký"
        }
    }
}
