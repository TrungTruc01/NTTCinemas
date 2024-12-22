package com.example.nttcinemas.Models

import android.os.Parcel
import android.os.Parcelable

data class Film(
    var title: String? = null, // Tên phim
    var description: String? = null, // Mô tả phim
    var poster: String? = null, // Đường dẫn hình ảnh Poster (URL hoặc resource ID)
    var time: String? = null, // Thời gian chiếu (định dạng giờ:phút)
    var trailer: String? = null, // Đường dẫn Trailer (URL)
    var releaseDate: String? = null, // Ngày phát hành (ví dụ: "08 tháng 12, 2024")
    var price: Double = 0.0, // Giá vé (1 vé)
    var genre: List<String> = listOf(), // Thể loại phim (danh sách thể loại)
    var ageRating: String? = null, // Độ tuổi đánh giá (ví dụ: "+13", "18+")
    var director: String? = null, // Tên đạo diễn
    var cast: List<String> = listOf(), // Danh sách diễn viên
    var language: String? = null // Ngôn ngữ và phụ đề (ví dụ: "Tiếng Việt, Phụ đề Anh")
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readDouble(),
        parcel.createStringArrayList() ?: listOf(),
        parcel.readString(),
        parcel.readString(),
        parcel.createStringArrayList() ?: listOf(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(poster)
        parcel.writeString(time)
        parcel.writeString(trailer)
        parcel.writeString(releaseDate)
        parcel.writeDouble(price)
        parcel.writeStringList(genre)
        parcel.writeString(ageRating)
        parcel.writeString(director)
        parcel.writeStringList(cast)
        parcel.writeString(language)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Film> {
        override fun createFromParcel(parcel: Parcel): Film = Film(parcel)
        override fun newArray(size: Int): Array<Film?> = arrayOfNulls(size)
    }
}
