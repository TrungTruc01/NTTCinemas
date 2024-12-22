package com.example.nttcinemas.Admin

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nttcinemas.Adapter.FilmAdapter
import com.example.nttcinemas.Models.Film
import com.example.nttcinemas.R
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.*

@Suppress("DEPRECATION")
class FilmFragment : Fragment() {

    private lateinit var tabLayout: TabLayout
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddFilm: Button
    private lateinit var file: Button
    private lateinit var adapter: FilmAdapter

    private lateinit var database: DatabaseReference
    private lateinit var storage: FirebaseStorage

    private var selectedTab: String = "NowPlaying"
    private val filmList = mutableListOf<Film>()
    private var imageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_film, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabLayout = view.findViewById(R.id.tabLayout)
        recyclerView = view.findViewById(R.id.recyclerViewBanner)
        btnAddFilm = view.findViewById(R.id.btnAddBanner)
        file = view.findViewById(R.id.file)

        database = FirebaseDatabase.getInstance().getReference("Items")
        storage = FirebaseStorage.getInstance()

        setupTabs()
        loadDataFromFirebase()

        file.setOnClickListener {
            uploadFilmsFromJsonFile()
        }

        btnAddFilm.setOnClickListener {
            showAddFilmDialog(null)
        }
    }

    private fun setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Đang Chiếu"))
        tabLayout.addTab(tabLayout.newTab().setText("Sắp Chiếu"))
        tabLayout.addTab(tabLayout.newTab().setText("Đặc Biệt"))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                selectedTab = when (tab?.position) {
                    0 -> "NowPlaying"
                    1 -> "Upcoming"
                    2 -> "Featured"
                    else -> "NowPlaying"
                }
                loadDataFromFirebase()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun loadDataFromFirebase() {
        database.child(selectedTab).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                filmList.clear()
                for (child in snapshot.children) {
                    val film = child.getValue(Film::class.java)
                    film?.let { filmList.add(it) }
                }
                adapter = FilmAdapter(
                    filmList,
                    onEditClick = { selectedFilm ->
                        showAddFilmDialog(selectedFilm)
                    },
                    onDeleteClick = { selectedFilm ->
                        showDeleteConfirmation(selectedFilm)
                    }
                )
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun uploadFilmsFromJsonFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/json"
        startActivityForResult(intent, 200)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 200 && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                readJsonAndUploadToFirebase(uri)
            }
        } else if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            imageUri = data?.data
            Toast.makeText(context, "Ảnh đã chọn", Toast.LENGTH_SHORT).show()
        }
    }

    private fun readJsonAndUploadToFirebase(fileUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val inputStream = requireContext().contentResolver.openInputStream(fileUri)
                val jsonString = inputStream?.bufferedReader()?.use { it.readText() }

                if (jsonString.isNullOrEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "File JSON rỗng", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val gson = Gson()

                try {
                    // Đọc JSON là mảng
                    val films = gson.fromJson(jsonString, Array<Film>::class.java)
                    films.forEach { film ->
                        saveFilmToFirebase(film)
                    }
                } catch (e: Exception) {
                    // Nếu JSON không phải mảng, xử lý như đối tượng
                    val film = gson.fromJson(jsonString, Film::class.java)
                    saveFilmToFirebase(film)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Tải dữ liệu từ JSON thành công", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Lỗi đọc file JSON: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun saveFilmToFirebase(film: Film) {
        database.child(selectedTab).child(film.title!!).setValue(film)
            .addOnSuccessListener {
                Toast.makeText(context, "Thêm phim thành công", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Lỗi thêm phim", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDeleteConfirmation(film: Film) {
        AlertDialog.Builder(context)
            .setTitle("Xác nhận")
            .setMessage("Bạn có chắc chắn muốn xóa phim này không?")
            .setPositiveButton("Xóa") { _, _ ->
                database.child(selectedTab).child(film.title!!).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Xóa phim thành công", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Lỗi khi xóa phim", Toast.LENGTH_SHORT).show()
                    }
            }
            .setNegativeButton("Hủy", null)
            .create()
            .show()
    }

    private fun showAddFilmDialog(film: Film?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_film, null)

        val imgPoster = dialogView.findViewById<ImageView>(R.id.imgPoster)
        val btnChooseImage = dialogView.findViewById<Button>(R.id.btnChooseImage)
        val edtTitle = dialogView.findViewById<EditText>(R.id.edtTitle)
        val edtDescription = dialogView.findViewById<EditText>(R.id.edtDescription)
        val edtTime = dialogView.findViewById<EditText>(R.id.edtTime)
        val edtReleaseDate = dialogView.findViewById<EditText>(R.id.edtReleaseDate)
        val edtPrice = dialogView.findViewById<EditText>(R.id.edtPrice)
        val edtGenre = dialogView.findViewById<EditText>(R.id.edtGenre)
        val edtAgeRating = dialogView.findViewById<EditText>(R.id.edtAgeRating)
        val edtDirector = dialogView.findViewById<EditText>(R.id.edtDirector)
        val edtCast = dialogView.findViewById<EditText>(R.id.edtCast)
        val edtLanguage = dialogView.findViewById<EditText>(R.id.edtLanguage)
        imageUri = null
        if (film != null) {
            edtTitle.setText(film.title)
            edtDescription.setText(film.description)
            edtTime.setText(film.time)
            edtReleaseDate.setText(film.releaseDate)
            edtPrice.setText(film.price.toString())
            edtGenre.setText(film.genre.joinToString(", "))
            edtAgeRating.setText(film.ageRating)
            edtDirector.setText(film.director)
            edtCast.setText(film.cast.joinToString(", "))
            edtLanguage.setText(film.language)
        }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnChooseImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 100)
        }

        dialogView.findViewById<Button>(R.id.btnSaveFilm).setOnClickListener {
            val title = edtTitle.text.toString().trim()
            if (title.isEmpty() || (imageUri == null && film == null)) {
                Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val uploadUri = imageUri ?: Uri.parse(film?.poster)
            uploadPosterToFirebaseStorage(uploadUri!!, title) { posterUrl ->
                val updatedFilm = Film(
                    title = title,
                    description = edtDescription.text.toString().trim(),
                    poster = posterUrl,
                    time = edtTime.text.toString().trim(),
                    releaseDate = edtReleaseDate.text.toString().trim(),
                    price = edtPrice.text.toString().toDoubleOrNull() ?: 0.0,
                    genre = edtGenre.text.toString().split(",").map { it.trim() },
                    ageRating = edtAgeRating.text.toString().trim(),
                    director = edtDirector.text.toString().trim(),
                    cast = edtCast.text.toString().split(",").map { it.trim() },
                    language = edtLanguage.text.toString().trim()
                )
                saveFilmToFirebase(updatedFilm)
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun uploadPosterToFirebaseStorage(uri: Uri, title: String, onComplete: (String) -> Unit) {
        val folder = when (selectedTab) {
            "NowPlaying" -> "dang_chieu"
            "Upcoming" -> "sap_chieu"
            "Featured" -> "dac_biet"
            else -> "other"
        }
        val fileName = title.replace(" ", "_") + ".jpg"
        val storageRef = storage.getReference("$folder/$fileName")

        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Log.d("FirebaseStorage", "Tệp tồn tại: $uri")
            onComplete(uri.toString())
        }.addOnFailureListener { exception ->
            Log.e("FirebaseStorage", "Lỗi tải tệp: ${exception.message}")
            Toast.makeText(context, "Không thể tải tệp: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
