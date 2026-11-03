# Ứng Dụng Chăm Sóc Sức Khỏe & Thể Hình (Android Java + Supabase)

Ứng dụng di động trên nền tảng **Android Studio**, lập trình 100% bằng **Java**, giao diện hiện đại tông màu **Trắng - Xanh Lá (White & Green)**, tích hợp đồng bộ dữ liệu đám mây với hệ thống CSDL **Supabase** (`https://rtqmvnhadjetpiategzi.supabase.co`).

---

## 🌟 Các chức năng chính

1. **Theo dõi dinh dưỡng (Nutrition Tracking)**:
   - Ghi lại các món ăn theo từng bữa: Bữa sáng, Bữa trưa, Bữa tối, Ăn vặt.
   - Thống kê tự động lượng Calo (kcal) nạp vào so với mục tiêu hàng ngày (vd: 2000 kcal).
   - Phân tích chi tiết tỉ lệ đa lượng Macro: Chất đạm (Protein), Tinh bột (Carbohydrate), Chất béo (Fat).
   - Thanh tiến độ (ProgressBar) trực quan theo dõi lượng calo còn lại.

2. **Quản lý cân nặng (Weight Management)**:
   - Ghi nhận cân nặng (kg) và chiều cao (cm) theo ngày kèm ghi chú mục tiêu.
   - Tính toán chỉ số khối cơ thể (BMI) tự động và phân loại thể trạng: *Thiếu cân*, *Bình thường (Lý tưởng)*, *Tiền béo phì*, *Béo phì*.
   - Theo dõi lịch sử biến động cân nặng qua danh sách trực quan.

3. **Theo dõi tập luyện (Workout Tracking)**:
   - Ghi nhật ký bài tập chi tiết: Tên bài tập (Bench Press, Squat, Hít đất...), Nhóm cơ (Ngực, Chân, Lưng, Tay, Bụng...), Số hiệp (Sets), Số lần lặp (Reps), Mức tạ (Weight kg), Thời gian tập (phút).
   - Tính toán tổng calo tiêu hao trong ngày và tổng thời gian rèn luyện.

4. **Đếm thời gian nghỉ (Rest Timer)**:
   - Đồng hồ bấm giờ đếm ngược thời gian nghỉ chuyên dụng giữa các hiệp tập thể hình / cardio.
   - Nút chọn nhanh thời gian nghỉ: **30 giây**, **60 giây**, **90 giây**, **120 giây**.
   - Các nút điều khiển: **Bắt đầu (Start)**, **Tạm dừng (Pause)**, **Tiếp tục (Resume)**, **Đặt lại (Reset)**, **Cộng thêm 15 giây (+15s)**.
   - Phát **âm thanh Bíp (ToneGenerator)** và **rung máy (Vibration)** báo hiệu ngay khi hết giờ nghỉ để tiếp tục hiệp tập tiếp theo.

5. **Nhắc uống nước (Water Reminder)**:
   - Theo dõi lượng nước uống trong ngày so với mục tiêu (2000 ml).
   - Các nút bấm thêm nhanh tiện lợi: **+150ml (Cốc nhỏ)**, **+250ml (Ly nước)**, **+500ml (Chai nước)**.
   - Công tắc Bật/Tắt nhắc nhở uống nước tự động bằng hệ thống **Android AlarmManager & Notification**.

6. **Lập lịch tập (Workout Schedule)**:
   - Kế hoạch tập luyện cố định theo các ngày trong tuần (Thứ 2 đến Chủ Nhật).
   - Ghi nhận nhóm cơ mục tiêu, giờ tập dự kiến (vd: 17:30) và các bài tập cần thực hiện.
   - Tích chọn hoàn thành (Checkmark) khi đã tập xong.

7. **Lưu trữ CSDL Supabase (Offline-First Architecture)**:
   - Tích hợp endpoint REST API: `https://rtqmvnhadjetpiategzi.supabase.co/rest/v1/`
   - Kiến trúc **Offline-First**: Dữ liệu được lưu trữ và phản hồi tức thì tại SQLite local database trên máy. Khi có kết nối mạng và khóa Anon Key, app đồng bộ tự động lên Supabase Cloud.
   - Menu cấu hình Supabase trên thanh công cụ (Toolbar) cho phép nhập Anon Key và kiểm tra kết nối mọi lúc.

---

## 🎨 Tông màu thiết kế (White & Green Theme)

- **Primary Green**: `#2E7D32` (Xanh lá đậm thanh lịch, tạo cảm giác tin cậy, y tế và sức khỏe).
- **Primary Variant**: `#1B5E20` (Xanh lá đậm sâu cho Status bar).
- **Secondary / Mint Green**: `#4CAF50` & `#81C784` (Xanh tươi mát cho thanh tiến độ và điểm nhấn).
- **Background White**: `#F8FAF8` & `#FFFFFF` (Nền trắng tinh khôi, sạch sẽ, thoáng mắt).
- **Surface & Cards**: Thẻ Card bo tròn góc 16dp với đổ bóng nhẹ sang trọng.

---

## 🚀 Hướng dẫn cài đặt & Khởi chạy trên Android Studio

### Bước 1: Khởi tạo bảng dữ liệu trên Supabase

1. Truy cập vào dự án Supabase của bạn tại: [https://supabase.com/dashboard/project/rtqmvnhadjetpiategzi](https://supabase.com/dashboard/project/rtqmvnhadjetpiategzi)
2. Chọn mục **SQL Editor** ở thanh menu bên trái.
3. Mở file `supabase_schema.sql` trong thư mục gốc dự án này, copy toàn bộ nội dung và dán vào SQL Editor, sau đó nhấn **Run**.
4. Toàn bộ 5 bảng (`nutrition_logs`, `weight_logs`, `workout_logs`, `water_logs`, `workout_schedules`) kèm chính sách Row Level Security (RLS) và dữ liệu mẫu sẽ được tạo tự động.

### Bước 2: Lấy Supabase Anon Public Key

1. Trong trang quản lý Supabase, vào mục **Project Settings** (biểu tượng bánh răng) -> **API**.
2. Tìm dòng **Project API keys** -> copy mã **anon / public key**.

### Bước 3: Mở ứng dụng trong Android Studio

1. Mở **Android Studio**.
2. Chọn **File** -> **Open...** và chọn thư mục `c:\chamsocsuckhoe`.
3. Đợi Android Studio hoàn tất quá trình Gradle Sync (Sync Project with Gradle Files).
4. Chọn máy ảo Android (Emulator) hoặc cắm điện thoại Android thật (bật USB Debugging).
5. Nhấn nút **Run** (biểu tượng tam giác màu xanh) hoặc tổ hợp phím `Shift + F10` để cài đặt và khởi chạy ứng dụng!

### Bước 4: Nhập Anon Key trong Ứng dụng

1. Trên góc phải thanh công cụ của ứng dụng, nhấn vào biểu tượng **Cài đặt** (hoặc nhấn vào dòng *"Supabase Cloud: Chưa cấu hình Anon Key"* ở trang chủ).
2. Dán Anon Key đã copy ở Bước 2 vào và bấm **Lưu lại**.
3. Bấm biểu tượng **Đồng bộ đám mây** trên thanh công cụ để đồng bộ dữ liệu tức thì!

---

## 📁 Cấu trúc mã nguồn

```
c:\chamsocsuckhoe\
├── app/
│   ├── build.gradle                          # Cấu hình module, dependencies (Retrofit, Gson, Material...)
│   └── src/main/
│       ├── AndroidManifest.xml               # Khai báo Permissions, Activity, Receiver
│       ├── java/com/suckhoe/app/
│       │   ├── HealthApp.java                # Khởi tạo Kênh thông báo & Alarm
│       │   ├── MainActivity.java             # Điều hướng BottomNavigation & Toolbar
│       │   ├── data/
│       │   │   ├── api/
│       │   │   │   ├── SupabaseApi.java      # Retrofit endpoints cho Supabase REST
│       │   │   │   └── SupabaseClient.java   # HTTP Client với auth headers & base URL
│       │   │   ├── local/
│       │   │   │   └── AppDatabaseHelper.java# SQLite local database lưu trữ offline
│       │   │   ├── model/
│       │   │   │   ├── NutritionItem.java    # Model dinh dưỡng & calo
│       │   │   │   ├── WeightRecord.java     # Model cân nặng & tính BMI
│       │   │   │   ├── WorkoutExercise.java  # Model bài tập luyện
│       │   │   │   ├── WorkoutSchedule.java  # Model lịch tập tuần
│       │   │   │   └── WaterLog.java         # Model uống nước
│       │   │   └── repository/               # Repository đồng bộ SQLite <-> Supabase
│       │   ├── receiver/
│       │   │   ├── WaterReminderReceiver.java# Báo thức nhắc uống nước
│       │   │   └── WorkoutReminderReceiver.java# Báo thức nhắc giờ tập
│       │   ├── ui/
│       │   │   ├── adapter/                  # RecyclerView Adapters
│       │   │   ├── dialog/                   # RestTimerDialog, AddNutrition, AddWorkout...
│       │   │   └── fragment/                 # Home, Nutrition, Workout, Schedule, WeightWater
│       │   └── util/
│       │       ├── DateUtils.java
│       │       ├── NotificationHelper.java
│       │       └── PreferenceManager.java
│       └── res/                              # Layouts, Drawables, Colors, Themes (Trắng - Xanh lá)
├── supabase_schema.sql                       # Toàn bộ script tạo bảng Supabase
├── build.gradle                              # Gradle cấu hình dự án
├── settings.gradle                           # Khai báo modules
└── README.md
```
