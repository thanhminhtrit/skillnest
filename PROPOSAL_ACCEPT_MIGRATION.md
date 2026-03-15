# ⚠️ QUAN TRỌNG: THAY ĐỔI PROPOSAL ACCEPT FLOW

## 🔴 DEPRECATED ENDPOINT

Endpoint sau đây đã bị **DEPRECATED** và sẽ bị xóa trong tương lai:

```
❌ POST /api/proposals/{proposalId}/accept
```

**Response khi gọi endpoint này:**
```json
{
  "statusCode": 410,
  "message": "This endpoint is deprecated. Please use POST /api/payments/proposals/{proposalId}/accept for secure payment flow with escrow protection.",
  "data": null
}
```

---

## ✅ ENDPOINT MỚI (BẮT BUỘC DÙNG)

Tất cả proposal **PHẢI** được accept qua payment flow để đảm bảo an toàn:

```
✅ POST /api/payments/proposals/{proposalId}/accept
```

---

## 🎯 LÝ DO THAY ĐỔI

### Vấn đề với endpoint cũ:
- ❌ Không có escrow payment protection
- ❌ Client có thể accept rồi không trả tiền
- ❌ Student không được bảo vệ
- ❌ Không có platform fee
- ❌ Không có audit trail cho thanh toán

### Ưu điểm của payment flow mới:
- ✅ **Escrow protection**: Tiền được giữ an toàn
- ✅ **Platform fee 8%**: Duy trì hoạt động nền tảng
- ✅ **Bank transfer verification**: Admin verify trước khi tạo contract
- ✅ **Full audit trail**: Mọi transaction được ghi nhận
- ✅ **Refund policy**: Hoàn tiền 100% nếu hủy

---

## 📊 SO SÁNH 2 FLOW

### ❌ OLD FLOW (Deprecated):
```
Accept Proposal → Contract Created → Work → Complete → ???
```
**Vấn đề:** Không có đảm bảo thanh toán!

### ✅ NEW FLOW (Required):
```
Accept Proposal → Payment Request → Bank Transfer → Admin Verify 
→ Contract Created (with Escrow) → Work → Complete → Release Payment
```
**An toàn:** Tiền được ký quỹ, cả 2 bên được bảo vệ!

---

## 🔄 MIGRATION GUIDE

### Nếu bạn đang dùng endpoint cũ:

**Cũ:**
```bash
POST /api/proposals/1/accept
Authorization: Bearer {TOKEN}
```

**Mới:**
```bash
POST /api/payments/proposals/1/accept
Authorization: Bearer {TOKEN}
```

**Chỉ thay đổi đường dẫn từ `/api/proposals/` sang `/api/payments/proposals/`**

---

## 📝 UPDATED WORKFLOW

### Bước 8: Client xem proposals
```bash
GET /api/proposals/project/1
```

### Bước 9: Accept proposal QUA PAYMENT (BẮT BUỘC)
```bash
POST /api/payments/proposals/1/accept
Authorization: Bearer {TOKEN_CLIENT}
```

**Response:**
```json
{
  "statusCode": 201,
  "message": "Payment request created. Please complete bank transfer.",
  "data": {
    "paymentRequestId": 1,
    "totalAmount": 19440000,
    "platformFee": 1440000,
    "bankTransferInfo": {
      "bankName": "Vietcombank",
      "accountNumber": "1234567890",
      "accountName": "SKILLNEST PLATFORM",
      "transferContent": "SKILLNEST PAY1 CLIENT",
      "amount": 19440000
    }
  }
}
```

### Bước 10: Client chuyển khoản
```
Bank Transfer: 19,440,000 VND
```

### Bước 11: Admin verify payment
```bash
POST /api/payments/1/verify
Authorization: Bearer {TOKEN_ADMIN}
```

**Response:**
```json
{
  "statusCode": 200,
  "message": "Payment verified and contract created successfully",
  "data": {
    "contractId": 1,
    "escrowTransaction": {
      "amount": 19440000,
      "status": "COMPLETED"
    }
  }
}
```

---

## ⚠️ LƯU Ý QUAN TRỌNG

1. **Không thể accept proposal mà không thanh toán** - Đây là business requirement bắt buộc
2. **Tất cả contract phải có escrow deposit** - Bảo vệ cả client và student
3. **Platform fee 8%** - Client trả thêm, student nhận đủ
4. **Admin verify required** - Chống fraud và scam

---

## 🚀 ACTION ITEMS

- [ ] Cập nhật tất cả documentation
- [ ] Thông báo cho FE team về endpoint mới
- [ ] Update Postman collection
- [ ] Test payment flow end-to-end
- [ ] Prepare admin training cho verify payment

---

**Effective Date:** March 11, 2026  
**Deprecation Complete:** June 11, 2026 (3 tháng sau)

---

Nếu có câu hỏi, liên hệ Backend Team! 🎯

