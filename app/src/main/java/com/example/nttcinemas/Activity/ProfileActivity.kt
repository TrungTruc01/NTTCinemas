package com.example.nttcinemas.Activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.nttcinemas.R
import com.example.nttcinemas.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@Suppress("DEPRECATION")
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var auth: FirebaseAuth
    private val storageRef = FirebaseStorage.getInstance().reference
    private val firestore = FirebaseFirestore.getInstance() // Firestore instance
    private val userId by lazy { auth.currentUser?.uid }
    private var selectedImageUri: Uri? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Load thông tin người dùng
        loadUserProfile()

        // Quay lại màn hình trước
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Chọn avatar từ thư viện
        binding.profileAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        // Đăng xuất
        binding.logout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Đăng xuất thành công!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.accountInfo.setOnClickListener {
            val intent = Intent(this, PersonalInfoActivity::class.java)
            startActivity(intent)
        }
        // Chuyển đến màn hình đổi mật khẩu
        binding.changePassword.setOnClickListener {
            val intent = Intent(this, ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        // Chuyển đến lịch sử giao dịch
        binding.transactionHistory.setOnClickListener {
            // TODO: Implement transaction history functionality
        }
    }

    @Deprecated("This method has been deprecated in favor of using the Activity Result API\n      which brings increased type safety via an {@link ActivityResultContract} and the prebuilt\n      contracts for common intents available in\n      {@link androidx.activity.result.contract.ActivityResultContracts}, provides hooks for\n      testing, and allow receiving results in separate, testable classes independent from your\n      activity. Use\n      {@link #registerForActivityResult(ActivityResultContract, ActivityResultCallback)}\n      with the appropriate {@link ActivityResultContract} and handling the result in the\n      {@link ActivityResultCallback#onActivityResult(Object) callback}.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let {
                uploadAvatarToFirebase(it)
            }
        }
    }

    private fun uploadAvatarToFirebase(uri: Uri) {
        val avatarRef = storageRef.child("avatars/$userId.jpg")
        avatarRef.putFile(uri)
            .addOnSuccessListener {
                avatarRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    userId?.let { uid ->
                        firestore.collection("Users").document(uid)
                            .update("avatarUrl", downloadUrl.toString()) // Cập nhật Firestore
                            .addOnSuccessListener {
                                Toast.makeText(this, "Cập nhật avatar thành công!", Toast.LENGTH_SHORT).show()
                                loadAvatar(downloadUrl.toString())
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Lỗi khi lưu avatar vào cơ sở dữ liệu.", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Lỗi khi tải lên avatar.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadUserProfile() {
        // Lấy email của người dùng từ Firebase Auth
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: run {
            Toast.makeText(this, "Không tìm thấy email người dùng.", Toast.LENGTH_SHORT).show()
            return
        }

        // Sử dụng email để tìm tài liệu trong Firestore
        firestore.collection("Users").whereEqualTo("email", userEmail).get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Lấy tài liệu đầu tiên
                    val document = querySnapshot.documents.first()
                    val userFirestoreId = document.id // Lấy ID tài liệu trong Firestore
                    val userName = document.getString("fullName") ?: "Người dùng"
                    val avatarUrl = document.getString("avatarUrl")

                    // Hiển thị thông tin người dùng
                    binding.profileName.text = userName

                    // Hiển thị avatar nếu có
                    if (!avatarUrl.isNullOrEmpty()) {
                        loadAvatar(avatarUrl)
                    } else {
                        binding.profileAvatar.setImageResource(R.drawable.ic_avatar1)
                    }

                    // Debug: In ID người dùng lấy được từ Firestore
                    println("User Firestore ID: $userFirestoreId")
                } else {
                    Toast.makeText(this, "Không tìm thấy thông tin người dùng trong Firestore.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { error ->
                Toast.makeText(this, "Lỗi khi tải thông tin người dùng: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadAvatar(avatarUrl: String) {
        Glide.with(this)
            .load(avatarUrl)
            .apply(RequestOptions.circleCropTransform()) // Bo tròn ảnh avatar
            .placeholder(R.drawable.ic_avatar) // Ảnh placeholder khi đang tải
            .error(R.drawable.ic_avatar) // Ảnh lỗi khi tải
            .into(binding.profileAvatar)
    }
}
