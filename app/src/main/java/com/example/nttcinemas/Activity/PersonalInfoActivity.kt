package com.example.nttcinemas.Activity

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.nttcinemas.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PersonalInfoActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var tvAccountEmail: TextView
    private lateinit var etFullName: EditText
    private lateinit var tvBirthdate: TextView
    private lateinit var spinnerGender: Spinner
    private lateinit var btnUpdateInfo: Button
    private lateinit var tvDeleteAccount: TextView
    private lateinit var btnBack: ImageView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_info)

        // Khởi tạo Firebase Auth và Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Liên kết các view trong giao diện
        tvAccountEmail = findViewById(R.id.tvAccountEmail)
        etFullName = findViewById(R.id.etFullName)
        tvBirthdate = findViewById(R.id.tvBirthdate)
        spinnerGender = findViewById(R.id.spinnerGender)
        btnUpdateInfo = findViewById(R.id.btnUpdateInfo)
        tvDeleteAccount = findViewById(R.id.tvDeleteAccount)
        btnBack = findViewById(R.id.btnBack)

        // Gán dữ liệu Spinner Giới tính
        setupGenderSpinner()

        // Tải thông tin người dùng
        loadUserProfile()

        // Xử lý nút Cập nhật thông tin
        btnUpdateInfo.setOnClickListener { updateUserProfile() }

        // Xử lý nút Xoá tài khoản
        tvDeleteAccount.setOnClickListener { deleteUserAccount() }

        // Xử lý nút Quay lại
        btnBack.setOnClickListener { finish() }
    }

    private fun setupGenderSpinner() {
        val genders = arrayOf("Nam", "Nữ", "Khác")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerGender.adapter = adapter
    }

    private fun loadUserProfile() {
        // Lấy email từ Firebase Auth
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: run {
            Toast.makeText(this, "Không tìm thấy email người dùng.", Toast.LENGTH_SHORT).show()
            return
        }

        // Tìm tài liệu người dùng trong Firestore dựa trên email
        firestore.collection("Users").whereEqualTo("email", userEmail).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Lấy tài liệu đầu tiên
                    val document = querySnapshot.documents.first()
                    val fullName = document.getString("fullName") ?: "Người dùng"
                    val email = document.getString("email") ?: "Không có email"
                    val birthdate = document.getString("birthday") ?: "Không rõ"
                    val gender = document.getString("gender") ?: "Khác"

                    // Cập nhật giao diện
                    tvAccountEmail.text = email
                    etFullName.setText(fullName)
                    tvBirthdate.text = birthdate

                    // Cập nhật giới tính trong Spinner
                    val genderPosition = (spinnerGender.adapter as ArrayAdapter<String>).getPosition(gender)
                    spinnerGender.setSelection(genderPosition)
                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Lỗi khi tải thông tin: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun updateUserProfile() {
        // Lấy UID của người dùng hiện tại từ Firebase Auth
        val authUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        // Tìm tài liệu Firestore liên kết với UID này
        firestore.collection("Users").whereEqualTo("authUserId", authUserId).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Lấy ID tài liệu Firestore của người dùng đăng nhập
                    val document = querySnapshot.documents.first()
                    val userId = document.id

                    val fullName = etFullName.text.toString()
                    val selectedGender = spinnerGender.selectedItem.toString()

                    if (fullName.isEmpty()) {
                        Toast.makeText(this, "Họ tên không được để trống", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    val updates = mapOf(
                        "fullName" to fullName,
                        "gender" to selectedGender
                    )

                    // Cập nhật thông tin người dùng trong Firestore
                    firestore.collection("Users").document(userId).update(updates)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this, "Cập nhật thất bại: ${error.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin người dùng.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Không thể lấy dữ liệu người dùng: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun deleteUserAccount() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("Users").document(userId).delete()
            .addOnSuccessListener {
                auth.currentUser?.delete()?.addOnSuccessListener {
                    Toast.makeText(this, "Tài khoản đã bị xoá", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }?.addOnFailureListener { error ->
                    Toast.makeText(this, "Lỗi khi xoá tài khoản: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Lỗi khi xoá thông tin người dùng: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
