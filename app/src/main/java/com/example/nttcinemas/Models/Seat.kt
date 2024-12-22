package com.example.nttcinemas.Models

data class Seat(
    val type: SeatType,
    val name: String,
    var status: SeatStatus,
    var isSelected: Boolean = false
) {
    enum class SeatType {
        NORMAL, VIP, DOUBLE
    }

    enum class SeatStatus {
        AVAILABLE, UNAVAILABLE, SELECTED
    }
}
