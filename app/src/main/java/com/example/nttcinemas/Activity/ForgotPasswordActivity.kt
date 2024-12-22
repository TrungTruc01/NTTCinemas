package com.example.nttcinemas.Activity

import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nttcinemas.databinding.ActivityForgotPasswordBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding
    private lateinit var auth: FirebaseAuth
    private var storedVerificationId: String? = null
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        // Xử lý nút quay lại
        binding.btnBack.setOnClickListener {
            finish() // Kết thúc Activity và quay lại màn hình trước đó
        }
        binding.btnContinue.setOnClickListener {
            val input = binding.edtEmailOrPhone.text.toString().trim()

            if (input.isEmpty()) {
                showToast("Vui lòng nhập email hoặc số điện thoại")
                return@setOnClickListener
            }

            if (Patterns.EMAIL_ADDRESS.matcher(input).matches()) {
                // Gửi email khôi phục mật khẩu
                sendPasswordResetEmail(input)
            } else if (Patterns.PHONE.matcher(input).matches()) {
                // Gửi mã xác minh qua số điện thoại
                sendVerificationCodeToPhone(input)
            } else {
                showToast("Email hoặc số điện thoại không hợp lệ")
            }
            finish()
        }
    }

    private fun sendPasswordResetEmail(email: String) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Email khôi phục mật khẩu đã được gửi.")
                } else {
                    showToast("Không thể gửi email: ${task.exception?.message}")
                }
            }
    }

    private fun sendVerificationCodeToPhone(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    showToast("Xác minh thành công!")
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    showToast("Gửi mã thất bại: ${e.message}")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    showToast("Mã xác minh đã được gửi.")
                    storedVerificationId = verificationId
                    resendingToken = token
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
