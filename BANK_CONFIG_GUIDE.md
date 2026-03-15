# 🏦 HƯỚNG DẪN CẤU HÌNH THÔNG TIN NGÂN HÀNG

## ✅ CÂU TRẢ LỜI: **HOÀN TOÀN CÓ THỂ CHỈNH SỬA!**

Các giá trị trong `bankTransferInfo` của API response hoàn toàn có thể thay đổi theo ý muốn của bạn.

---

## 📍 VỊ TRÍ CẤU HÌNH

File: `src/main/resources/application.yml`

```yaml
payment:
  platform-fee-percent: 8
  bank:
    name: Vietcombank              # Tên ngân hàng
    account-number: 1234567890     # Số tài khoản
    account-name: SKILLNEST PLATFORM  # Tên chủ tài khoản
    code: VCB                      # Mã ngân hàng
```

---

## 🔧 CÁC TRƯỜNG CÓ THỂ CHỈNH SỬA

### 1. **`name`** - Tên Ngân Hàng
- **Hiện tại:** `Vietcombank`
- **Có thể đổi thành:** 
  - `Techcombank`
  - `VPBank`
  - `BIDV`
  - `MB Bank`
  - Bất kỳ tên ngân hàng nào

**Ví dụ:**
```yaml
name: Techcombank
```

---

### 2. **`account-number`** - Số Tài Khoản
- **Hiện tại:** `1234567890`
- **Có thể đổi thành:** Số tài khoản thật của bạn

**Ví dụ:**
```yaml
account-number: 9876543210123
```

---

### 3. **`account-name`** - Tên Chủ Tài Khoản
- **Hiện tại:** `SKILLNEST PLATFORM`
- **Có thể đổi thành:** Tên công ty hoặc tên cá nhân của bạn

**Ví dụ:**
```yaml
account-name: CONG TY TNHH SKILLNEST
```
hoặc
```yaml
account-name: NGUYEN VAN A
```

---

### 4. **`code`** - Mã Ngân Hàng
- **Hiện tại:** `VCB`
- **Có thể đổi thành:** Mã ngân hàng tương ứng

**Danh sách mã ngân hàng phổ biến:**
- `VCB` - Vietcombank
- `TCB` - Techcombank
- `VPB` - VPBank
- `BIDV` - BIDV
- `MB` - MB Bank
- `ACB` - ACB
- `VIB` - VIB
- `TPB` - TPBank

**Ví dụ:**
```yaml
code: TCB
```

---

### 5. **`platform-fee-percent`** - Phí Nền Tảng (%)
- **Hiện tại:** `8` (tức 8%)
- **Có thể đổi thành:** Bất kỳ số nào bạn muốn

**Ví dụ:**
```yaml
platform-fee-percent: 10  # Phí 10%
```
hoặc
```yaml
platform-fee-percent: 5   # Phí 5%
```

---

## 💡 VÍ DỤ THAY ĐỔI HOÀN CHỈNH

### **Ví dụ 1: Đổi sang Techcombank**
```yaml
payment:
  platform-fee-percent: 8
  bank:
    name: Techcombank
    account-number: 19036765340018
    account-name: CONG TY TNHH SKILLNEST
    code: TCB
```

**Kết quả API sẽ trả về:**
```json
"bankTransferInfo": {
  "bankName": "Techcombank",
  "accountNumber": "19036765340018",
  "accountName": "CONG TY TNHH SKILLNEST",
  "transferContent": "SKILLNEST PAY1 ALICE",
  "amount": 19440000
}
```

---

### **Ví dụ 2: Đổi sang VPBank với phí 10%**
```yaml
payment:
  platform-fee-percent: 10
  bank:
    name: VPBank
    account-number: 123456789012
    account-name: NGUYEN VAN A
    code: VPB
```

**Kết quả API sẽ trả về:**
```json
"bankTransferInfo": {
  "bankName": "VPBank",
  "accountNumber": "123456789012",
  "accountName": "NGUYEN VAN A",
  "transferContent": "SKILLNEST PAY1 ALICE",
  "amount": 19800000
}
```
*Lưu ý: `amount` sẽ cao hơn vì phí tăng từ 8% lên 10%*

---

### **Ví dụ 3: Đổi sang MB Bank**
```yaml
payment:
  platform-fee-percent: 5
  bank:
    name: MB Bank
    account-number: 0123456789
    account-name: TRAN THI B
    code: MB
```

**Kết quả API sẽ trả về:**
```json
"bankTransferInfo": {
  "bankName": "MB Bank",
  "accountNumber": "0123456789",
  "accountName": "TRAN THI B",
  "transferContent": "SKILLNEST PAY1 ALICE",
  "amount": 18900000
}
```
*Lưu ý: `amount` sẽ thấp hơn vì phí giảm từ 8% xuống 5%*

---

## 🔄 CÁCH ÁP DỤNG THAY ĐỔI

### **Bước 1:** Mở file cấu hình
```
D:\FPT_U\HK8\EXE2\skillnest\src\main\resources\application.yml
```

### **Bước 2:** Chỉnh sửa các giá trị theo ý muốn
```yaml
payment:
  platform-fee-percent: 10  # Đổi phí
  bank:
    name: Techcombank       # Đổi tên ngân hàng
    account-number: YOUR_ACCOUNT_NUMBER  # Đổi số TK
    account-name: YOUR_NAME              # Đổi tên chủ TK
    code: TCB               # Đổi mã ngân hàng
```

### **Bước 3:** Lưu file

### **Bước 4:** Restart ứng dụng
```cmd
# Nếu đang chạy, dừng lại (Ctrl + C)
# Sau đó chạy lại:
mvn spring-boot:run
```

### **Bước 5:** Test API
Gọi lại API accept proposal và kiểm tra response:
```bash
POST http://4.193.192.105:8080/api/payments/proposals/1/accept
```

Response sẽ có thông tin ngân hàng mới!

---

## 🔐 CẤU HÌNH CHO PRODUCTION

Nếu deploy lên server production, bạn có thể tạo file riêng:

**File:** `application-prod.yml`
```yaml
payment:
  platform-fee-percent: 8
  bank:
    name: Vietcombank
    account-number: YOUR_REAL_ACCOUNT
    account-name: YOUR_COMPANY_NAME
    code: VCB
```

Sau đó chạy với profile production:
```cmd
java -jar skillnest.jar --spring.profiles.active=prod
```

---

## 📊 CÔNG THỨC TÍNH TOÁN

Khi thay đổi `platform-fee-percent`, số tiền sẽ thay đổi theo công thức:

```
platformFee = agreedBudget × (platform-fee-percent / 100)
totalAmount = agreedBudget + platformFee
```

**Ví dụ với budget 18,000,000 VNĐ:**

| Phí (%) | Platform Fee | Total Amount |
|---------|--------------|--------------|
| 5%      | 900,000      | 18,900,000   |
| 8%      | 1,440,000    | 19,440,000   |
| 10%     | 1,800,000    | 19,800,000   |
| 15%     | 2,700,000    | 20,700,000   |

---

## ⚠️ LƯU Ý QUAN TRỌNG

1. **Số tài khoản:** Nhập đúng số tài khoản thật để nhận tiền
2. **Tên chủ tài khoản:** Nên viết HOA và không dấu (theo chuẩn ngân hàng)
3. **Mã ngân hàng:** Phải khớp với tên ngân hàng
4. **Phí nền tảng:** Nên cân nhắc mức phí hợp lý (5-15%)
5. **Restart app:** Sau khi đổi phải restart ứng dụng để áp dụng thay đổi

---

## ✅ KẾT LUẬN

**Trả lời câu hỏi của bạn:**

> "những giá trị trong bankTransferInfo tôi hoàn toàn có thể chỉnh được phải không"

**→ ĐÚNG VẬY! Hoàn toàn có thể chỉnh sửa mọi thứ trong `bankTransferInfo`:**

✅ Tên ngân hàng  
✅ Số tài khoản  
✅ Tên chủ tài khoản  
✅ Mã ngân hàng  
✅ Phí nền tảng (%)

Tất cả đều có thể thay đổi dễ dàng trong file `application.yml`!

---

**Chúc bạn cấu hình thành công! 🎉**

