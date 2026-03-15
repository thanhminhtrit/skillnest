package com.exe202.skillnest.enums;

public enum TransactionType {
    ESCROW_DEPOSIT,   // Client payment deposited to platform
    PAYOUT,           // Platform pays student
    REFUND,           // Platform refunds client
    PLATFORM_FEE      // Platform keeps commission (8%)
}

