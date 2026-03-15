package com.exe202.skillnest.enums;

public enum PaymentStatus {
    PENDING_PAYMENT,  // Waiting for client to pay
    PAID,             // Payment verified by admin/manager
    CANCELLED,        // Payment cancelled before verification
    REFUNDED,         // Money returned to client
    RELEASED          // Money paid out to student
}

