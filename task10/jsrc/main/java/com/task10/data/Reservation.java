package com.task10.data;

public record Reservation(int tableNumber, String clientName, String phoneNumber, String date, String slotTimeStart, String slotTimeEnd) {
}
