package com.example.nttcinemas.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nttcinemas.Admin.AdminActivity
import com.example.nttcinemas.R
import com.example.nttcinemas.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance() // Firestore instance
    private var isPasswordVisible = false // Trạng thái hiển thị mật khẩu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Xử lý nút hiển thị/ẩn mật khẩu
        setupPasswordToggle()

        // Điều hướng đến màn hình đăng ký
        binding.btnSignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // Xử lý đăng nhập
        binding.btnLogin.setOnClickListener {
            val emailOrPhone = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            // Kiểm tra thông tin nhập
            when {
                emailOrPhone.isEmpty() -> showToast("Vui lòng nhập email hoặc tên đăng nhập")
                password.isEmpty() -> showToast("Vui lòng nhập mật khẩu")
                else -> checkCredentials(emailOrPhone, password) // Kiểm tra thông tin đăng nhập
            }
        }

        // Xử lý nút quay lại
        binding.btnBack.setOnClickListener {
            finish() // Kết thúc Activity và quay lại màn hình trước đó
        }

        // Điều hướng đến quên mật khẩu
        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    // Hàm xử lý hiển thị/ẩn mật khẩu
    @SuppressLint("ClickableViewAccessibility")
    private fun setupPasswordToggle() {
        binding.edtPassword.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.edtPassword.right - binding.edtPassword.compoundDrawables[2].bounds.width())) {
                    // Toggle trạng thái hiển thị mật khẩu
                    isPasswordVisible = !isPasswordVisible
                    if (isPasswordVisible) {
                        // Hiển thị mật khẩu
                        binding.edtPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                        binding.edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.ic_eye_white, 0
                        )
                    } else {
                        // Ẩn mật khẩu
                        binding.edtPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        binding.edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.ic_eye_slash_white, 0
                        )
                    }
                    // Đặt lại con trỏ
                    binding.edtPassword.setSelection(binding.edtPassword.text.length)
                    true
                } else {
                    false
                }
            } else {
                false
            }
        }
    }

    // Kiểm tra thông tin đăng nhập Admin hoặc Quản lý
    private fun checkCredentials(username: String, password: String) {
        firestore.collection("Admin").document("admin").get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val storedUsername = document.getString("username")
                    val storedPassword = document.getString("password")

                    if (username == storedUsername && password == storedPassword) {
                        navigateToAdminActivity(isQuanly = false) // Đăng nhập với tài khoản Admin
                    } else {
                        checkQuanlyCredentials(username, password) // Nếu không phải Admin, kiểm tra Quản lý
                    }
                } else {
                    checkQuanlyCredentials(username, password)
                }
            }
            .addOnFailureListener { error ->
                showToast("Lỗi khi kiểm tra tài khoản: ${error.localizedMessage}")
            }
    }

    // Kiểm tra thông tin đăng nhập Quản lý
    private fun checkQuanlyCredentials(username: String, password: String) {
        firestore.collection("Quanly").whereEqualTo("qlname", username).get()
            .addOnSuccessListener { querySnapshot ->
                val document = querySnapshot.documents.firstOrNull()
                if (document != null && document.getString("password") == password) {
                    navigateToAdminActivity(isQuanly = true) // Đăng nhập với tài khoản Quản lý
                } else {
                    loginWithEmailAndPassword(username, password) // Nếu không phải Quản lý, kiểm tra tài khoản Firebase Authentication
                }
            }
            .addOnFailureListener {
                showToast("Lỗi khi kiểm tra tài khoản Quản lý: ${it.localizedMessage}")
            }
    }

    // Đăng nhập với Firebase Authentication
    private fun loginWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid
                    if (userId != null) {
                        navigateToMainActivity(userId)
                    }
                } else {
                    handleLoginError(task.exception?.localizedMessage)
                }
            }
    }

    // Xử lý lỗi đăng nhập
    private fun handleLoginError(exceptionMessage: String?) {
        val exception = exceptionMessage ?: "Đăng nhập thất bại"
        when {
            exception.contains("There is no user record", ignoreCase = true) -> {
                showToast("Tài khoản không tồn tại")
            }
            exception.contains("password is invalid", ignoreCase = true) -> {
                showToast("Sai mật khẩu")
            }
            else -> {
                showToast("Đăng nhập thất bại: $exception")
            }
        }
    }

    // Điều hướng đến AdminActivity
    private fun navigateToAdminActivity(isQuanly: Boolean) {
        val intent = Intent(this, AdminActivity::class.java)
        intent.putExtra("isQuanly", isQuanly)
        startActivity(intent)
        finish()
    }

    // Điều hướng đến MainActivity
    private fun navigateToMainActivity(userId: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("userId", userId)
        startActivity(intent)
        finish()
    }

    // Hàm hiển thị thông báo Toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
