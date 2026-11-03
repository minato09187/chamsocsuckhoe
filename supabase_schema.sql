-- =====================================================================
-- HE THONG CSDL SUPABASE CHO UNG DUNG CSSK (DA NGUOI DUNG)
-- Supabase Project URL: https://rtqmvnhadjetpiategzi.supabase.co
-- Sao chep toan bo file nay va chay trong muc "SQL Editor" tren Supabase
-- =====================================================================

-- 0. BẢNG TÀI KHOẢN NGƯỜI DÙNG (app_users)
CREATE TABLE IF NOT EXISTS public.app_users (
    id BIGSERIAL PRIMARY KEY,
    username TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    full_name TEXT NOT NULL,
    email TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 1. BẢNG THEO DÕI DINH DƯỠNG (nutrition_logs)
CREATE TABLE IF NOT EXISTS public.nutrition_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT DEFAULT 1,
    food_name TEXT NOT NULL,
    calories INTEGER NOT NULL DEFAULT 0,
    protein REAL DEFAULT 0,
    carbs REAL DEFAULT 0,
    fat REAL DEFAULT 0,
    meal_type TEXT NOT NULL DEFAULT 'Bữa sáng', -- 'Bữa sáng', 'Bữa trưa', 'Bữa tối', 'Ăn vặt'
    log_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. BẢNG QUẢN LÝ CÂN NẶNG & BMI (weight_logs)
CREATE TABLE IF NOT EXISTS public.weight_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT DEFAULT 1,
    weight REAL NOT NULL,           -- Cân nặng (kg)
    height REAL DEFAULT 170,        -- Chiều cao (cm)
    bmi REAL,                       -- Chỉ số BMI tính toán
    note TEXT,                      -- Ghi chú tiến độ
    logged_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. BẢNG THEO DÕI TẬP LUYỆN (workout_logs)
CREATE TABLE IF NOT EXISTS public.workout_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT DEFAULT 1,
    exercise_name TEXT NOT NULL,    -- Tên bài tập
    muscle_group TEXT DEFAULT 'Toàn thân', -- Nhóm cơ
    sets INTEGER DEFAULT 3,         -- Số hiệp
    reps INTEGER DEFAULT 12,        -- Số lần lặp
    weight REAL DEFAULT 0,          -- Mức tạ (kg)
    duration_minutes INTEGER DEFAULT 30, -- Thời gian tập (phút)
    calories_burned INTEGER DEFAULT 0,   -- Calo tiêu hao (kcal)
    log_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. BẢNG THEO DÕI UỐNG NƯỚC (water_logs)
CREATE TABLE IF NOT EXISTS public.water_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT DEFAULT 1,
    amount_ml INTEGER NOT NULL DEFAULT 250,   -- Lượng nước (ml)
    target_ml INTEGER NOT NULL DEFAULT 2000,  -- Mục tiêu (ml)
    log_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. BẢNG LẬP LỊCH TẬP (workout_schedules)
CREATE TABLE IF NOT EXISTS public.workout_schedules (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT DEFAULT 1,
    day_of_week TEXT NOT NULL,       -- Thứ 2 -> Chủ Nhật
    title TEXT NOT NULL,             -- Tên buổi tập
    target_muscle TEXT,              -- Nhóm cơ mục tiêu
    time_of_day TEXT DEFAULT '17:30',-- Giờ tập (HH:mm)
    is_completed BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =====================================================================
-- BẬT ROW LEVEL SECURITY (RLS) VÀ CẤP QUYỀN TRUY CẬP CHO ANON KEY
-- =====================================================================

ALTER TABLE public.app_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.nutrition_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.weight_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.workout_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.water_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.workout_schedules ENABLE ROW LEVEL SECURITY;

CREATE POLICY "Allow public read-write app_users" 
ON public.app_users FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Allow public read-write nutrition_logs" 
ON public.nutrition_logs FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Allow public read-write weight_logs" 
ON public.weight_logs FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Allow public read-write workout_logs" 
ON public.workout_logs FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Allow public read-write water_logs" 
ON public.water_logs FOR ALL USING (true) WITH CHECK (true);

CREATE POLICY "Allow public read-write workout_schedules" 
ON public.workout_schedules FOR ALL USING (true) WITH CHECK (true);

-- =====================================================================
-- DỮ LIỆU MẪU BAN ĐẦU CHO 2 TÀI KHOẢN (NGUYỄN VĂN A & TRẦN THỊ B)
-- =====================================================================

INSERT INTO public.app_users (username, password, full_name, email)
VALUES 
('nguyenvana', '123456', 'Nguyễn Văn A', 'vana@gmail.com'),
('tranthib', '123456', 'Trần Thị B', 'thib@gmail.com')
ON CONFLICT (username) DO NOTHING;

-- Dữ liệu mẫu cho Nguyễn Văn A (user_id = 1)
INSERT INTO public.nutrition_logs (user_id, food_name, calories, protein, carbs, fat, meal_type, log_date)
VALUES 
(1, 'Phở bò tái', 450, 25.0, 55.0, 12.0, 'Bữa sáng', CURRENT_DATE),
(1, 'Ức gà áp chảo & cơm gạo lứt', 520, 42.0, 60.0, 8.0, 'Bữa trưa', CURRENT_DATE);

INSERT INTO public.weight_logs (user_id, weight, height, bmi, note, logged_date)
VALUES 
(1, 68.5, 172.0, 23.15, 'Mục tiêu tăng cơ giảm mỡ', CURRENT_DATE);

INSERT INTO public.workout_logs (user_id, exercise_name, muscle_group, sets, reps, weight, duration_minutes, calories_burned, log_date)
VALUES 
(1, 'Barbell Bench Press', 'Ngực', 4, 10, 60.0, 20, 150, CURRENT_DATE);

INSERT INTO public.water_logs (user_id, amount_ml, target_ml, log_date)
VALUES 
(1, 1250, 2000, CURRENT_DATE);

INSERT INTO public.workout_schedules (user_id, day_of_week, title, target_muscle, time_of_day, is_completed, notes)
VALUES 
(1, 'Thứ 2', 'Tập Ngực & Tay sau', 'Ngực - Tay sau', '17:30', false, 'Bench Press 4x10');

-- Dữ liệu mẫu cho Trần Thị B (user_id = 2)
INSERT INTO public.nutrition_logs (user_id, food_name, calories, protein, carbs, fat, meal_type, log_date)
VALUES 
(2, 'Salad cá hồi & trứng luộc', 380, 28.0, 15.0, 14.0, 'Bữa trưa', CURRENT_DATE);

INSERT INTO public.weight_logs (user_id, weight, height, bmi, note, logged_date)
VALUES 
(2, 52.0, 160.0, 20.3, 'Duy trì vóc dáng thon gọn', CURRENT_DATE);

INSERT INTO public.workout_logs (user_id, exercise_name, muscle_group, sets, reps, weight, duration_minutes, calories_burned, log_date)
VALUES 
(2, 'Chạy bộ máy Cardio', 'Cardio', 1, 1, 0.0, 30, 220, CURRENT_DATE);

INSERT INTO public.water_logs (user_id, amount_ml, target_ml, log_date)
VALUES 
(2, 1750, 2000, CURRENT_DATE);
