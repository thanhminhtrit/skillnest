# 🔄 THAY ĐỔI LOGIC THANH TOÁN - CHỈ THU PHÍ NỀN TẢNG

## ✅ ĐÃ THAY ĐỔI THÀNH CÔNG

Logic thanh toán đã được sửa đổi theo yêu cầu của bạn.

---

## 📊 SO SÁNH LOGIC CŨ VÀ MỚI

### ❌ **LOGIC CŨ (Đã bỏ)**
```
Client phải trả = proposedPrice + (proposedPrice × 8%)
```

**Ví dụ:** 
- Student đề xuất: 18,000,000 VNĐ
- Phí nền tảng 8%: 1,440,000 VNĐ
- **Client phải trả: 19,440,000 VNĐ**

---

### ✅ **LOGIC MỚI (Hiện tại)**
```
Client chỉ phải trả = budgetMax × 8%
```

**Ví dụ:**
- Project budgetMax: 15,000,000 VNĐ
- Phí nền tảng 8%: 1,200,000 VNĐ
- **Client chỉ phải trả: 1,200,000 VNĐ** (để duy trì nền tảng)
- Student sẽ nhận: 18,000,000 VNĐ (từ client thanh toán sau)

---

## 🎯 MỤC ĐÍCH CỦA THAY ĐỔI

1. **Client chỉ trả phí nền tảng trước** để admin/manager duyệt proposal và tạo contract
2. **Phí nền tảng = 8% của budgetMax** (ngân sách tối đa mà client đặt khi tạo project)
3. **Student nhận tiền từ client sau** khi hoàn thành công việc (không qua nền tảng)

---

## 📝 CHI TIẾT THAY ĐỔI TRONG CODE

### File đã sửa: `PaymentServiceImpl.java`

**Thay đổi chính trong method `acceptProposalWithPayment()`:**

```java
// 4. Calculate amounts - CHỈ LẤY 8% PHÍ DUY TRÌ NỀN TẢNG TỪ BUDGETMAX
BigDecimal budgetMax = project.getBudgetMax();
if (budgetMax == null || budgetMax.compareTo(BigDecimal.ZERO) <= 0) {
    throw new BadRequestException("Project budgetMax is not set or invalid");
}

// Chỉ tính phí nền tảng 8% từ budgetMax
BigDecimal platformFee = budgetMax.multiply(platformFeePercent).divide(HUNDRED, 2, RoundingMode.HALF_UP);
BigDecimal totalAmount = platformFee; // Client chỉ phải trả phí nền tảng
BigDecimal studentAmount = proposal.getProposedPrice(); // Student nhận đúng số tiền đã đề xuất
```

---

## 📋 VÍ DỤ CỤ THỂ

### **Ví dụ 1: Project với budgetMax = 15,000,000 VNĐ**

**Input:**
- Client tạo project với `budgetMax: 15,000,000 VNĐ`
- Student đề xuất `proposedPrice: 18,000,000 VNĐ`
- Platform fee: 8%

**Tính toán:**
```
platformFee = 15,000,000 × 8% = 1,200,000 VNĐ
totalAmount = 1,200,000 VNĐ (client phải trả)
studentAmount = 18,000,000 VNĐ (student sẽ nhận)
```

**API Response:**
```json
{
  "statusCode": 201,
  "message": "Payment request created. Please complete bank transfer.",
  "data": {
    "paymentRequestId": 1,
    "proposalId": 1,
    "projectId": 1,
    "clientId": 1,
    "studentId": 2,
    "agreedBudget": 18000000,
    "platformFee": 1200000,
    "totalAmount": 1200000,
    "status": "PENDING_PAYMENT",
    "bankTransferInfo": {
      "bankName": "Vietcombank",
      "accountNumber": "1234567890",
      "accountName": "SKILLNEST PLATFORM",
      "transferContent": "SKILLNEST PAY1",
      "amount": 1200000
    },
    "createdAt": "2026-03-11T11:00:00",
    "expiresAt": "2026-03-13T11:00:00"
  }
}
```

---

### **Ví dụ 2: Project với budgetMax = 20,000,000 VNĐ**

**Input:**
- Client tạo project với `budgetMax: 20,000,000 VNĐ`
- Student đề xuất `proposedPrice: 19,500,000 VNĐ`
- Platform fee: 8%

**Tính toán:**
```
platformFee = 20,000,000 × 8% = 1,600,000 VNĐ
totalAmount = 1,600,000 VNĐ (client phải trả)
studentAmount = 19,500,000 VNĐ (student sẽ nhận)
```

**Client chỉ phải chuyển: 1,600,000 VNĐ**

---

### **Ví dụ 3: Project với budgetMax = 10,000,000 VNĐ**

**Input:**
- Client tạo project với `budgetMax: 10,000,000 VNĐ`
- Student đề xuất `proposedPrice: 9,000,000 VNĐ`
- Platform fee: 8%

**Tính toán:**
```
platformFee = 10,000,000 × 8% = 800,000 VNĐ
totalAmount = 800,000 VNĐ (client phải trả)
studentAmount = 9,000,000 VNĐ (student sẽ nhận)
```

**Client chỉ phải chuyển: 800,000 VNĐ**

---

## 🔐 VALIDATION MỚI

Code giờ sẽ kiểm tra `budgetMax` phải hợp lệ:

```java
if (budgetMax == null || budgetMax.compareTo(BigDecimal.ZERO) <= 0) {
    throw new BadRequestException("Project budgetMax is not set or invalid");
}
```

**Nghĩa là:**
- Project PHẢI có `budgetMax` được set
- `budgetMax` phải > 0
- Nếu không, API sẽ trả lỗi: "Project budgetMax is not set or invalid"

---

## 🔄 FLOW HOẠT ĐỘNG MỚI

### **Bước 1: Client tạo project**
```http
POST /api/projects
{
  "title": "Cần tìm Backend Developer",
  "description": "...",
  "budgetMin": 10000000,
  "budgetMax": 15000000,  ← Số này sẽ dùng để tính phí
  "projectType": "FIXED_PRICE"
}
```

### **Bước 2: Student gửi proposal**
```http
POST /api/proposals
{
  "projectId": 1,
  "proposedPrice": 18000000,  ← Student đề xuất giá
  "coverLetter": "..."
}
```

### **Bước 3: Client accept proposal**
```http
POST /api/payments/proposals/1/accept
Authorization: Bearer {TOKEN_CLIENT}
```

**Response:**
```json
{
  "statusCode": 201,
  "data": {
    "platformFee": 1200000,      ← 8% của budgetMax (15,000,000)
    "totalAmount": 1200000,       ← Client chỉ trả số này
    "studentAmount": 18000000,    ← Student sẽ nhận từ client
    "bankTransferInfo": {
      "amount": 1200000           ← Client chuyển 1.2 triệu
    },
    "message": "Please transfer 1200000 VND (8% platform maintenance fee) to proceed..."
  }
}
```

### **Bước 4: Client chuyển khoản phí nền tảng**
Client chuyển **1,200,000 VNĐ** vào tài khoản nền tảng

### **Bước 5: Admin/Manager verify payment**
```http
POST /api/payments/{paymentRequestId}/verify
Authorization: Bearer {TOKEN_ADMIN}
```

→ Contract được tạo tự động

### **Bước 6: Client thanh toán trực tiếp cho Student**
Client thanh toán **18,000,000 VNĐ** trực tiếp cho student (ngoài nền tảng)

---

## 📊 BẢNG TÍNH PHÍ NHANH

| budgetMax | Platform Fee 8% | Client phải trả |
|-----------|-----------------|-----------------|
| 5,000,000 | 400,000        | 400,000         |
| 10,000,000| 800,000        | 800,000         |
| 15,000,000| 1,200,000      | 1,200,000       |
| 20,000,000| 1,600,000      | 1,600,000       |
| 25,000,000| 2,000,000      | 2,000,000       |
| 30,000,000| 2,400,000      | 2,400,000       |
| 50,000,000| 4,000,000      | 4,000,000       |

---

## ⚠️ LƯU Ý QUAN TRỌNG

### 1. **budgetMax BẮT BUỘC phải có giá trị**
Khi client tạo project, PHẢI set `budgetMax` > 0, nếu không sẽ không thể accept proposal.

### 2. **Phí tính trên budgetMax, không phải proposedPrice**
Dù student đề xuất giá cao hay thấp, phí nền tảng luôn tính dựa trên `budgetMax` của project.

### 3. **Student nhận tiền trực tiếp từ client**
Nền tảng chỉ thu phí duy trì, không giữ tiền của student. Client thanh toán cho student bên ngoài hệ thống.

### 4. **Message trong response đã thay đổi**
Message mới: 
```
"Please transfer {platformFee} VND (8% platform maintenance fee) to proceed. 
Contract will be created after payment verification."
```

### 5. **QR Code sinh ra với số tiền = platformFee**
QR code chỉ hiển thị số tiền phí nền tảng (VD: 1,200,000 VNĐ), không phải tổng budget.

---

## 🧪 TEST API

### **Test Case 1: Accept proposal thành công**

**Request:**
```http
POST http://4.193.192.105:8080/api/payments/proposals/1/accept
Authorization: Bearer {TOKEN_CLIENT}
```

**Expected Response:**
```json
{
  "statusCode": 201,
  "message": "Payment request created. Please complete bank transfer.",
  "data": {
    "platformFee": 1200000,
    "totalAmount": 1200000,
    "studentAmount": 18000000,
    "bankTransferInfo": {
      "amount": 1200000
    }
  }
}
```

---

### **Test Case 2: Project không có budgetMax**

**Scenario:** Project có `budgetMax = null`

**Expected Response:**
```json
{
  "statusCode": 400,
  "message": "Project budgetMax is not set or invalid"
}
```

---

### **Test Case 3: budgetMax = 0**

**Scenario:** Project có `budgetMax = 0`

**Expected Response:**
```json
{
  "statusCode": 400,
  "message": "Project budgetMax is not set or invalid"
}
```

---

## ✅ CHECKLIST THAY ĐỔI

- ✅ Logic tính toán đã thay đổi: chỉ lấy 8% từ budgetMax
- ✅ Validation budgetMax đã được thêm vào
- ✅ totalAmount = platformFee (client chỉ trả phí)
- ✅ studentAmount = proposedPrice (student nhận đúng số đề xuất)
- ✅ QR code sinh ra với số tiền = platformFee
- ✅ Message thông báo đã được cập nhật
- ✅ Log ghi nhận thay đổi
- ✅ Code không có lỗi biên dịch

---

## 🚀 DEPLOYMENT

Sau khi thay đổi này, bạn cần:

1. **Rebuild project:**
```cmd
mvn clean install
```

2. **Restart application:**
```cmd
mvn spring-boot:run
```

3. **Test API với data mới**

4. **Cập nhật documentation** cho team Frontend biết thay đổi

---

## 📞 HỖ TRỢ

Nếu có vấn đề gì, kiểm tra:

1. ✅ Project có `budgetMax` được set chưa?
2. ✅ `budgetMax > 0` chưa?
3. ✅ Platform fee percent = 8% trong `application.yml`?
4. ✅ Database có cột `budget_max` trong bảng `projects`?

---

**Thay đổi đã hoàn tất! 🎉**

