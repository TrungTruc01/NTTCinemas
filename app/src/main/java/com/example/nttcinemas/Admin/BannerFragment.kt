package com.example.nttcinemas.Admin

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nttcinemas.Models.Banner
import com.example.nttcinemas.R
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*

@Suppress("DEPRECATION")
class BannerFragment : Fragment(R.layout.fragment_banner) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BannerAdapter
    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage
    private val bannerList = mutableListOf<Banner>()
    private var imageUri: Uri? = null
    private lateinit var bannerImageView: ImageView // ImageView trong dialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recyclerViewBanner)
        val btnAddBanner: View = view.findViewById(R.id.btnAddBanner)

        // Kết nối Firebase
        database = FirebaseDatabase.getInstance().getReference("Banners")
        storage = FirebaseStorage.getInstance()

        // Load dữ liệu từ Firebase
        loadBannersFromFirebase()

        // Thiết lập Adapter
        adapter = BannerAdapter(bannerList,
            onEditClick = { banner -> showBannerDialog("Sửa Banner", banner) },
            onDeleteClick = { banner -> confirmDeleteBanner(banner) }
        )
        recyclerView.adapter = adapter

        // Xử lý thêm banner
        btnAddBanner.setOnClickListener {
            showBannerDialog("Thêm Banner", null)
        }
    }

    private fun loadBannersFromFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            @SuppressLint("NotifyDataSetChanged")
            override fun onDataChange(snapshot: DataSnapshot) {
                bannerList.clear()
                for (child in snapshot.children) {
                    val banner = child.getValue(Banner::class.java)
                    banner?.let { bannerList.add(it) }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showBannerDialog(title: String, banner: Banner?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_edit_banner, null)
        val edtName = dialogView.findViewById<EditText>(R.id.edtBannerName)
        bannerImageView = dialogView.findViewById(R.id.bannerImageView)
        val btnChooseImage = dialogView.findViewById<Button>(R.id.btnChooseImage)

        imageUri = null // Reset image URI
        banner?.let {
            edtName.setText(it.name)
            Glide.with(this).load(it.image).into(bannerImageView)
        }

        btnChooseImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        val dialog = AlertDialog.Builder(context)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Lưu", null)
            .setNegativeButton("Hủy", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = edtName.text.toString().trim()


                if (name.isNotEmpty()) {
                    generateAutoIncrementId { id ->
                        if (imageUri != null) {
                            uploadImageToFirebaseStorage(imageUri!!, name) { imageUrl ->
                                saveBannerToFirebase(id, imageUrl, name)
                                dialog.dismiss()
                            }
                        } else if (banner != null) {
                            saveBannerToFirebase(banner.id, banner.image, name)
                            dialog.dismiss()
                        } else {
                            Toast.makeText(context, "Vui lòng chọn ảnh", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            bannerImageView.setImageURI(imageUri) // Hiển thị ảnh đã chọn
            Toast.makeText(context, "Ảnh đã chọn", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebaseStorage(imageUri: Uri, name: String, onComplete: (String) -> Unit) {
        val storageRef = storage.getReference("banner/${name.replace(" ", "_")}.jpg")
        storageRef.putFile(imageUri)
            .addOnSuccessListener {
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    onComplete(uri.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Lỗi upload ảnh", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveBannerToFirebase(id: String, imageUrl: String, name: String) {
        val currentDate = getCurrentDateTime()

        val banner = Banner(
            id = id,
            image = imageUrl,
            name = name,
            dateAdded = currentDate
        )

        database.child(id).setValue(banner)
            .addOnSuccessListener {
                Toast.makeText(context, "Lưu thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Lưu thất bại", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generateAutoIncrementId(onComplete: (String) -> Unit) {
        database.orderByKey().limitToLast(1).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lastId = snapshot.children.firstOrNull()?.key?.toIntOrNull() ?: -1
                val newId = (lastId + 1).toString()
                onComplete(newId)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Lỗi tạo ID", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getCurrentDateTime(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun confirmDeleteBanner(banner: Banner) {
        val dialog = AlertDialog.Builder(context)
            .setTitle("Xác nhận xóa")
            .setMessage("Bạn có chắc chắn muốn xóa banner '${banner.name}'?")
            .setPositiveButton("Xóa") { _, _ -> deleteBanner(banner) }
            .setNegativeButton("Hủy", null)
            .create()
        dialog.show()
    }

    private fun deleteBanner(banner: Banner) {
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(banner.image)

        storageReference.delete()
            .addOnSuccessListener {
                database.child(banner.id).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Xóa thành công!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Xóa dữ liệu thất bại!", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Xóa ảnh thất bại!", Toast.LENGTH_SHORT).show()
            }
    }
}
