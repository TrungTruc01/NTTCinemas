package com.example.nttcinemas.Activity

import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.nttcinemas.R
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import kotlin.reflect.KMutableProperty0

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var ivToggleCurrentPassword: ImageView
    private lateinit var ivToggleNewPassword: ImageView
    private lateinit var ivToggleConfirmPassword: ImageView
    private lateinit var btnUpdatePassword: Button
    private lateinit var btnBack: ImageView
    private var isCurrentPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_password)

        // Ánh xạ view
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        ivToggleCurrentPassword = findViewById(R.id.ivToggleCurrentPassword)
        ivToggleNewPassword = findViewById(R.id.ivToggleNewPassword)
        ivToggleConfirmPassword = findViewById(R.id.ivToggleConfirmPassword)
        btnUpdatePassword = findViewById(R.id.btnUpdatePassword)
        btnBack = findViewById(R.id.btnBack)

        // Thiết lập toggle mật khẩu
        setupPasswordToggle(ivToggleCurrentPassword, etCurrentPassword, ::isCurrentPasswordVisible)
        setupPasswordToggle(ivToggleNewPassword, etNewPassword, ::isNewPasswordVisible)
        setupPasswordToggle(ivToggleConfirmPassword, etConfirmPassword, ::isConfirmPasswordVisible)

        // Xử lý nút trở về
        btnBack.setOnClickListener { finish() }

        // Xử lý cập nhật mật khẩu
        btnUpdatePassword.setOnClickListener {
            val currentPassword = etCurrentPassword.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (validateInput(currentPassword, newPassword, confirmPassword)) {
                updatePassword(currentPassword, newPassword)
            }
        }
    }

    private fun setupPasswordToggle(imageView: ImageView, editText: EditText, passwordVisible: KMutableProperty0<Boolean>) {
        imageView.setOnClickListener {
            passwordVisible.set(!passwordVisible.get())
            if (passwordVisible.get()) {
                editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                imageView.setImageResource(R.drawable.ic_eye_black)
            } else {
                editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                imageView.setImageResource(R.drawable.ic_eye_slash_black)
            }
            editText.setSelection(editText.text.length)
        }
    }

    private fun validateInput(currentPassword: String, newPassword: String, confirmPassword: String): Boolean {
        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showToast("Vui lòng nhập đầy đủ thông tin")
            return false
        }

        if (newPassword != confirmPassword) {
            showToast("Mật khẩu mới không khớp")
            return false
        }

        if (newPassword.length < 6) {
            showToast("Mật khẩu mới phải ít nhất 6 ký tự")
            return false
        }

        return true
    }

    private fun updatePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser ?: return
        val email = user.email ?: return

        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).addOnCompleteListener { reauthTask ->
            if (reauthTask.isSuccessful) {
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        showToast("Mật khẩu đã được cập nhật thành công")
                        finish()
                    } else {
                        showToast("Lỗi khi cập nhật mật khẩu: ${updateTask.exception?.message}")
                    }
                }
            } else {
                showToast("Mật khẩu hiện tại không chính xác")
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
