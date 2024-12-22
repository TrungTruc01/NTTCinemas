package com.example.nttcinemas.Admin

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.nttcinemas.R
import com.google.firebase.firestore.FirebaseFirestore

class UsersFragment : Fragment() {

    private lateinit var lvUsers: ListView
    private lateinit var lvAdmins: ListView

    private val usersList = mutableListOf<Pair<String, String>>() // Email, Name
    private val adminsList = mutableListOf<Pair<String, String>>() // Email, Name

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_users, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lvUsers = view.findViewById(R.id.lvUsers)
        lvAdmins = view.findViewById(R.id.lvAdmins)

        loadUsers()
        loadAdmins()
    }

    private fun loadUsers() {
        firestore.collection("Users")
            .get()
            .addOnSuccessListener { querySnapshot ->
                usersList.clear()
                for (document in querySnapshot) {
                    val email = document.getString("email") ?: ""
                    val name = document.getString("fullName") ?: "Không có tên"
                    usersList.add(Pair(maskEmail(email), name))
                }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_2,
                    android.R.id.text1,
                    usersList.map { "${it.second}\n${it.first}" }
                )
                lvUsers.adapter = adapter
            }
            .addOnFailureListener {
                Toast.makeText(context, "Lỗi tải danh sách người dùng", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadAdmins() {
        firestore.collection("Quanly")
            .get()
            .addOnSuccessListener { querySnapshot ->
                adminsList.clear()
                for (document in querySnapshot) {
                    val email = document.getString("email") ?: ""
                    val name = document.getString("name") ?: "Không có tên"
                    adminsList.add(Pair(email, name))
                }
                val adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_list_item_2,
                    android.R.id.text1,
                    adminsList.map { "${it.second}\n${it.first}" }
                )
                lvAdmins.adapter = adapter

                lvAdmins.setOnItemClickListener { _, _, position, _ ->
                    showResetPasswordDialog(adminsList[position].first)
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Lỗi tải danh sách quản lý", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showResetPasswordDialog(email: String) {
        val input = EditText(context).apply {
            hint = "Nhập mật khẩu mới"
        }

        AlertDialog.Builder(context)
            .setTitle("Đổi mật khẩu")
            .setMessage("Thay đổi mật khẩu cho quản lý: $email")
            .setView(input)
            .setPositiveButton("Đổi") { _, _ ->
                val newPassword = input.text.toString()
                if (newPassword.isNotEmpty()) {
                    resetAdminPassword(email, newPassword)
                } else {
                    Toast.makeText(context, "Mật khẩu không được để trống", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun resetAdminPassword(email: String, newPassword: String) {
        firestore.collection("Quanly")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val docId = document.id
                    firestore.collection("Quanly").document(docId)
                        .update("password", newPassword)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Lỗi khi đổi mật khẩu", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Lỗi khi tìm tài khoản quản lý", Toast.LENGTH_SHORT).show()
            }
    }

    private fun maskEmail(email: String): String {
        val atIndex = email.indexOf('@')
        return if (atIndex > 1) {
            email.replaceRange(1 until atIndex, "*".repeat(atIndex - 1))
        } else email
    }
}
