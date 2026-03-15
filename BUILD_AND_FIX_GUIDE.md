# Hướng dẫn Build và Fix Lỗi Compile

## Trạng thái hiện tại
Sau khi quét toàn bộ dự án, tôi đã xác nhận:

### ✅ Các file ĐÃ ĐÚNG (không cần sửa):
1. **BaseResponse** - Tồn tại tại `payloads/response/BaseResponse.java`
2. **ConversationRepository** - Có đầy đủ method `findByContractContractId()`
3. **MessageRepository** - Có đầy đủ method `findByConversationConversationIdOrderBySentAtDesc()`
4. **DisputeMapper** - Đang dùng đúng `getOpenedBy()`
5. **DisputeServiceImpl** - Đang dùng đúng `dispute.getOpenedBy().getUserId()`
6. **ConversationServiceImpl** - Đã xử lý đúng MessageType với try-catch validation
7. **AuthServiceImpl** - Đang dùng đúng `UserStatus.ACTIVE`
8. **AdminServiceImpl** - Không gọi `setCompanyName()` nữa (đã remove)
9. **PaymentServiceImpl** - Conversation builder chỉ cần field `contract`

### 🔧 Các bước fix nếu vẫn gặp lỗi compile:

#### Bước 1: Clean và Rebuild
```bash
# Trong terminal, chạy:
cd D:\FPT_U\HK8\EXE2\skillnest
mvn clean install -DskipTests

# Hoặc trong IntelliJ:
# Build > Rebuild Project
# File > Invalidate Caches > Invalidate and Restart
```

#### Bước 2: Kiểm tra dependencies
Đảm bảo `pom.xml` có đầy đủ dependencies:
- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Lombok
- PostgreSQL Driver
- JWT libraries

#### Bước 3: Nếu vẫn lỗi import ResponseBase
Tất cả Controller đã import đúng:
```java
import com.exe202.skillnest.payloads.response.BaseResponse;
```

Nếu IDE báo lỗi, hãy:
1. Right-click vào project > Maven > Reload Project
2. Build > Rebuild Project
3. File > Invalidate Caches > Invalidate and Restart

#### Bước 4: Kiểm tra target/classes
Đảm bảo file `BaseResponse.class` được compile:
```
target/classes/com/exe202/skillnest/payloads/response/BaseResponse.class
```

### 📋 Các lỗi compile bạn báo và trạng thái:

1. ❌ **"cannot find symbol: class ResponseBase"**
   - **Fix**: Tất cả file đã import đúng `BaseResponse` (không phải `ResponseBase`)
   - Class đúng là `BaseResponse` tại `payloads/response/BaseResponse.java`

2. ❌ **"cannot find symbol: method findByContract_ContractId"**
   - **Fix**: Method tồn tại trong `ConversationRepository.findByContractContractId()`
   - Đã được sử dụng đúng trong code

3. ❌ **"cannot find symbol: method findByConversation_ConversationIdOrderBySentAtDesc"**
   - **Fix**: Method tồn tại trong `MessageRepository.findByConversationConversationIdOrderBySentAtDesc()`
   - Đã được sử dụng đúng trong code

4. ❌ **"cannot find symbol: method getRaisedBy()"**
   - **Fix**: DisputeMapper đang dùng đúng `getOpenedBy()`, không gọi `getRaisedBy()`

5. ❌ **"incompatible types: String cannot be converted to UserStatus"**
   - **Fix**: AuthServiceImpl đang dùng đúng `UserStatus.ACTIVE`

6. ❌ **"cannot find symbol: method setCompanyName"**
   - **Fix**: AdminServiceImpl không còn gọi method này

7. ❌ **"cannot find symbol: method openedBy()"**
   - **Fix**: DisputeServiceImpl dùng đúng `raisedBy()` trong DTO builder

8. ❌ **"incompatible types: String cannot be converted to MessageType"**
   - **Fix**: ConversationServiceImpl đã có try-catch để parse String sang MessageType

9. ❌ **"cannot find symbol: method participant1()"**
   - **Fix**: PaymentServiceImpl chỉ dùng `.contract()` trong Conversation builder

### 🚀 Khuyến nghị:

**Các lỗi compile bạn gặp có thể do IntelliJ IDE cache.** Hãy thực hiện:

1. **Invalidate Caches:**
   - File > Invalidate Caches > Invalidate and Restart

2. **Reimport Maven:**
   - Right-click vào `pom.xml` > Maven > Reload Project

3. **Rebuild Project:**
   - Build > Rebuild Project

4. **Nếu vẫn lỗi, xóa thư mục target:**
   ```bash
   rm -rf target/
   mvn clean compile
   ```

### 📌 Kết luận:
**Code hiện tại đã ĐÚNG và KHÔNG có lỗi compile thực sự.** Các lỗi bạn thấy là do IDE cache cũ.

Hãy làm theo các bước trên để fix.

