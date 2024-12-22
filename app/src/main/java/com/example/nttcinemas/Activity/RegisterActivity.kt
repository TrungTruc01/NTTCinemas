package com.example.nttcinemas.Activity

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.nttcinemas.R
import com.example.nttcinemas.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private val firestore = FirebaseFirestore.getInstance() // Firestore instance
    private var isPasswordVisible = false // Trạng thái hiển thị mật khẩu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Thiết lập sự kiện hiển thị/ẩn mật khẩu
        setupPasswordToggle()

        // Thiết lập Spinner Giới tính
        setupGenderSpinner()

        // Thiết lập DatePicker cho Ngày sinh
        setupDatePicker()

        // Xử lý nút Đăng Ký
        binding.btnRegister.setOnClickListener {
            val fullName = binding.edtFullName.text.toString().trim()
            val phoneNumber = binding.edtPhoneNumber.text.toString().trim()
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()
            val birthday = binding.edtBirthday.text.toString().trim()
            val gender = binding.spinnerGender.selectedItem?.toString()

            // Kiểm tra các trường bắt buộc
            when {
                fullName.isEmpty() -> showToast("Họ tên không được bỏ trống")
                phoneNumber.isEmpty() -> showToast("Số điện thoại không được bỏ trống")
                email.isEmpty() -> showToast("Email không được bỏ trống")
                password.isEmpty() -> showToast("Mật khẩu không được bỏ trống")
                birthday.isEmpty() -> showToast("Vui lòng chọn ngày sinh")
                gender.isNullOrEmpty() -> showToast("Vui lòng chọn giới tính")
                !binding.checkboxPolicy1.isChecked -> showToast("Vui lòng đồng ý với chính sách xử lý dữ liệu cá nhân")
                !binding.checkboxPolicy2.isChecked -> showToast("Vui lòng xác nhận thông tin là chính xác")
                !binding.checkboxPolicy3.isChecked -> showToast("Vui lòng đồng ý với Điều Khoản Sử Dụng")
                else -> {
                    // Tất cả thông tin hợp lệ, tiến hành đăng ký
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                saveUserToFirestore(fullName, phoneNumber, email, birthday, gender)
                            } else {
                                showToast("Đăng ký thất bại: ${task.exception?.localizedMessage}")
                            }
                        }
                }
            }
        }

        // Xử lý nút Quay Lại
        binding.btnBack.setOnClickListener {
            finish() // Quay lại màn hình trước đó
        }
    }

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
                            0, 0, R.drawable.ic_eye_black, 0
                        ) // Thay biểu tượng con mắt không gạch
                    } else {
                        // Ẩn mật khẩu
                        binding.edtPassword.inputType =
                            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                        binding.edtPassword.setCompoundDrawablesWithIntrinsicBounds(
                            0, 0, R.drawable.ic_eye_slash_black, 0
                        ) // Thay biểu tượng con mắt có gạch
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

    private fun setupGenderSpinner() {
        val genders = listOf("Nam", "Nữ", "Khác")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, genders)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = adapter
    }

    private fun setupDatePicker() {
        binding.edtBirthday.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, selectedYear, selectedMonth, selectedDay ->
                    val formattedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                    binding.edtBirthday.setText(formattedDate)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }
    }

    private fun saveUserToFirestore(
        fullName: String,
        phoneNumber: String,
        email: String,
        birthday: String,
        gender: String
    ) {
        // Lấy tất cả tài liệu trong collection để tìm ID lớn nhất
        firestore.collection("Users").get().addOnSuccessListener { querySnapshot ->
            // Lấy danh sách ID hiện tại, chuyển thành số nguyên nếu hợp lệ
            val currentIds = querySnapshot.documents.mapNotNull { document ->
                document.id.toIntOrNull()
            }
            // Tính ID tiếp theo dựa trên giá trị lớn nhất hiện tại
            val nextId = (currentIds.maxOrNull() ?: -1) + 1
            val userId = nextId.toString() // Tạo ID mới là số nguyên tiếp theo

            val user = mapOf(
                "fullName" to fullName,
                "phoneNumber" to phoneNumber,
                "email" to email,
                "birthday" to birthday,
                "gender" to gender
            )

            // Lưu tài liệu vào Firestore với ID được tính toán
            firestore.collection("Users").document(userId).set(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Đăng ký thành công và đã lưu dữ liệu với ID $userId")
                    finish()
                } else {
                    showToast("Lưu dữ liệu thất bại: ${task.exception?.localizedMessage}")
                }
            }
        }.addOnFailureListener { exception ->
            showToast("Không thể lấy dữ liệu người dùng: ${exception.localizedMessage}")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
