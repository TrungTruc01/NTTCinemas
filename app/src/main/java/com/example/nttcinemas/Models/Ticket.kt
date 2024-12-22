package com.example.nttcinemas.Models

import android.os.Parcel
import android.os.Parcelable

data class Ticket(
    var id: String = "",                // Mã vé
    val movieTitle: String = "",        // Tên phim
    val dateTime: String = "",          // Ngày giờ chiếu phim
    val cinema: String = "",            // Tên rạp chiếu
    val price: Double = 0.0,            // Giá vé
    val posterUrl: String = "",         // URL ảnh poster
    val status: String = "upcoming",    // Trạng thái vé: "upcoming" hoặc "watched"
    val userEmail: String = "",         // Email người dùng
    val seatInfo: String = "",          // Thông tin ghế ngồi
    val paymentMethod: String? = null,   // Phương thức thanh toán
    val bookingDate: String = "",
    val userId: String = ""
) : Parcelable {

    // Constructor đọc dữ liệu từ Parcel
    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readDouble(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString(),
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    // Ghi dữ liệu vào Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(movieTitle)
        parcel.writeString(dateTime)
        parcel.writeString(cinema)
        parcel.writeDouble(price)
        parcel.writeString(posterUrl)
        parcel.writeString(status)
        parcel.writeString(userEmail)
        parcel.writeString(seatInfo)
        parcel.writeString(paymentMethod)
        parcel.writeString(bookingDate)
        parcel.writeString(userId)
    }

    override fun describeContents(): Int = 0

    // Companion object để hỗ trợ Parcelable
    companion object CREATOR : Parcelable.Creator<Ticket> {
        override fun createFromParcel(parcel: Parcel): Ticket {
            return Ticket(parcel)
        }

        override fun newArray(size: Int): Array<Ticket?> {
            return arrayOfNulls(size)
        }
    }
}
