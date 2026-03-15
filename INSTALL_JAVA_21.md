# 🔧 Hướng Dẫn Cài Đặt Java 21 trên Windows

## Bước 1: Download Java 21

### Option 1: Oracle JDK 21 (Recommended)
1. Truy cập: https://www.oracle.com/java/technologies/downloads/#java21
2. Chọn **Windows** tab
3. Download: **x64 Installer** (jdk-21_windows-x64_bin.exe)

### Option 2: Eclipse Temurin 21 (OpenJDK)
1. Truy cập: https://adoptium.net/temurin/releases/?version=21
2. Chọn:
   - **Operating System**: Windows
   - **Architecture**: x64
   - **Package Type**: JDK
   - **Version**: 21 - LTS
3. Download file `.msi`

## Bước 2: Cài Đặt Java 21

### Cài đặt Oracle JDK:
1. Double-click file `.exe` vừa download
2. Nhấn **Next**
3. Chọn thư mục cài đặt (mặc định: `C:\Program Files\Java\jdk-21`)
4. Nhấn **Next** → **Install**
5. Đợi cài đặt hoàn tất → **Close**

### Cài đặt Eclipse Temurin:
1. Double-click file `.msi`
2. Chọn các option:
   - ✅ **Set JAVA_HOME variable**
   - ✅ **Add to PATH**
   - ✅ **Associate .jar files**
3. Nhấn **Install**

## Bước 3: Cấu Hình Biến Môi Trường (Environment Variables)

### 3.1. Mở System Properties:
```
Cách 1: Win + R → gõ "sysdm.cpl" → Enter
Cách 2: Right-click "This PC" → Properties → Advanced system settings
```

### 3.2. Cấu hình JAVA_HOME:
1. Nhấn nút **Environment Variables**
2. Trong **System variables**, nhấn **New**
3. Nhập:
   - **Variable name**: `JAVA_HOME`
   - **Variable value**: `C:\Program Files\Java\jdk-21` (hoặc đường dẫn bạn đã cài)
4. Nhấn **OK**

### 3.3. Cập nhật PATH:
1. Trong **System variables**, tìm và chọn **Path**
2. Nhấn **Edit**
3. Nhấn **New** và thêm: `%JAVA_HOME%\bin`
4. Di chuyển dòng này lên **ĐẦU TIÊN** trong danh sách (quan trọng!)
5. Nhấn **OK** → **OK** → **OK**

## Bước 4: Xóa Cache Maven (Quan trọng!)

Mở **Command Prompt** hoặc **PowerShell** và chạy:

```cmd
cd D:\FPT_U\HK8\EXE2\skillnest
.\mvnw.cmd clean
```

## Bước 5: Kiểm Tra Cài Đặt

Mở **Command Prompt MỚI** (đóng cửa sổ cũ) và chạy:

```cmd
java -version
```

**Kết quả mong đợi:**
```
java version "21.0.x" 2024-xx-xx LTS
Java(TM) SE Runtime Environment (build 21.0.x+xx-LTS-xxx)
Java HotSpot(TM) 64-Bit Server VM (build 21.0.x+xx-LTS-xxx, mixed mode, sharing)
```

Kiểm tra JAVA_HOME:
```cmd
echo %JAVA_HOME%
```

**Kết quả mong đợi:**
```
C:\Program Files\Java\jdk-21
```

Kiểm tra Maven sử dụng Java đúng:
```cmd
.\mvnw.cmd -version
```

**Kết quả mong đợi:**
```
Apache Maven 3.x.x
Maven home: ...
Java version: 21.0.x, vendor: Oracle Corporation
Java home: C:\Program Files\Java\jdk-21
```

## Bước 6: Build Project

```cmd
cd D:\FPT_U\HK8\EXE2\skillnest
.\mvnw.cmd clean package -DskipTests
```

**Kết quả mong đợi:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 30-60s
[INFO] Finished at: 2026-03-11T21:xx:xx+07:00
```

## ⚠️ Troubleshooting

### Lỗi 1: Maven vẫn dùng Java cũ
**Giải pháp:**
1. Đóng TẤT CẢ cửa sổ Command Prompt/PowerShell/Terminal
2. Đóng IntelliJ IDEA (nếu đang mở)
3. Mở Command Prompt MỚI
4. Chạy lại `java -version` để kiểm tra

### Lỗi 2: "JAVA_HOME is set to an invalid directory"
**Giải pháp:**
```cmd
# Kiểm tra đường dẫn có đúng không
dir "C:\Program Files\Java\jdk-21"

# Nếu không tồn tại, tìm đường dẫn đúng:
dir "C:\Program Files\Java\"

# Set lại JAVA_HOME với đường dẫn đúng
```

### Lỗi 3: Maven wrapper không hoạt động
**Giải pháp:**
```cmd
# Xóa cache Maven wrapper
rd /s /q .mvn
rd /s /q %USERPROFILE%\.m2\wrapper

# Download lại wrapper
mvn wrapper:wrapper
```

### Lỗi 4: Có nhiều phiên bản Java
**Giải pháp:**
1. Gỡ cài đặt các phiên bản Java cũ:
   - Control Panel → Programs → Uninstall a program
   - Tìm "Java" và gỡ các phiên bản cũ
2. Chỉ giữ lại Java 21

### Lỗi 5: IntelliJ IDEA vẫn dùng Java cũ
**Giải pháp:**
1. File → Project Structure (Ctrl+Alt+Shift+S)
2. Project Settings → Project:
   - SDK: Chọn "21" (nếu không có, nhấn Add SDK → JDK → chọn đường dẫn Java 21)
   - Language level: 21
3. Platform Settings → SDKs:
   - Xóa các SDK cũ (Java 11, 17...)
   - Giữ lại Java 21
4. Settings → Build, Execution, Deployment → Build Tools → Maven:
   - JDK for importer: Chọn "21"
   - Maven home directory: Bundled (Maven 3)

## 🎯 Script Tự Động Kiểm Tra

Tạo file `check-java.cmd`:
```cmd
@echo off
echo ========================================
echo Checking Java Installation
echo ========================================
echo.

echo 1. Java Version:
java -version
echo.

echo 2. JAVA_HOME:
echo %JAVA_HOME%
echo.

echo 3. Maven Version:
call mvnw.cmd -version
echo.

echo 4. Java in PATH:
where java
echo.

echo ========================================
echo Done!
echo ========================================
pause
```

Chạy file này để kiểm tra nhanh cấu hình Java.

## 📞 Support

Nếu vẫn gặp lỗi, gửi cho tôi output của:
```cmd
java -version
echo %JAVA_HOME%
.\mvnw.cmd -version
```

---
**Last Updated**: March 11, 2026

