-- =====================================================================
-- HE THONG CSDL SUPABASE CHO UNG DUNG CHAM SOC SUC KHOE
-- Supabase Project URL: https://rtqmvnhadjetpiategzi.supabase.co
-- Sao chep toan bo file nay va chay trong muc "SQL Editor" tren Supabase
-- =====================================================================

-- 1. BẢNG THEO DÕI DINH DƯỠNG (nutrition_logs)
CREATE TABLE IF NOT EXISTS public.nutrition_logs (
    id BIGSERIAL PRIMARY KEY,
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
    exercise_name TEXT NOT NULL,    -- Tên bài tập (vd: Bench Press, Squat, Chạy bộ...)
    muscle_group TEXT DEFAULT 'Toàn thân', -- Nhóm cơ (Ngực, Chân, Lưng, Tay, Bụng...)
    sets INTEGER DEFAULT 3,         -- Số hiệp
    reps INTEGER DEFAULT 12,        -- Số lần lặp
    weight REAL DEFAULT 0,          -- Mức tạ (kg) nếu có
    duration_minutes INTEGER DEFAULT 30, -- Thời gian tập (phút)
    calories_burned INTEGER DEFAULT 0,   -- Calo tiêu hao (kcal)
    log_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. BẢNG THEO DÕI UỐNG NƯỚC (water_logs)
CREATE TABLE IF NOT EXISTS public.water_logs (
    id BIGSERIAL PRIMARY KEY,
    amount_ml INTEGER NOT NULL DEFAULT 250,   -- Lượng nước vừa uống (ml)
    target_ml INTEGER NOT NULL DEFAULT 2000,  -- Mục tiêu trong ngày (ml)
    log_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. BẢNG LẬP LỊCH TẬP (workout_schedules)
CREATE TABLE IF NOT EXISTS public.workout_schedules (
    id BIGSERIAL PRIMARY KEY,
    day_of_week TEXT NOT NULL,       -- Thứ 2, Thứ 3, Thứ 4, Thứ 5, Thứ 6, Thứ 7, Chủ Nhật
    title TEXT NOT NULL,             -- Tên buổi tập (vd: Tập ngực & tay sau)
    target_muscle TEXT,              -- Nhóm cơ mục tiêu
    time_of_day TEXT DEFAULT '17:30',-- Giờ tập (HH:mm)
    is_completed BOOLEAN DEFAULT FALSE, -- Trạng thái hoàn thành
    notes TEXT,                      -- Ghi chú bài tập
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =====================================================================
-- BẬT ROW LEVEL SECURITY (RLS) VÀ CẤP QUYỀN TRUY CẬP CHO ANON KEY
-- =====================================================================

ALTER TABLE public.nutrition_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.weight_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.workout_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.water_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.workout_schedules ENABLE ROW LEVEL SECURITY;

-- Cho phép API (anon) thực hiện đầy đủ SELECT, INSERT, UPDATE, DELETE
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
-- DỮ LIỆU MẪU BAN ĐẦU (SEED DATA ĐỂ KIỂM THỬ)
-- =====================================================================

INSERT INTO public.nutrition_logs (food_name, calories, protein, carbs, fat, meal_type, log_date)
VALUES 
('Phở bò tái', 450, 25.0, 55.0, 12.0, 'Bữa sáng', CURRENT_DATE),
('Ức gà áp chảo & cơm gạo lứt', 520, 42.0, 60.0, 8.0, 'Bữa trưa', CURRENT_DATE),
('Chuối & Whey Protein', 220, 26.0, 28.0, 2.0, 'Ăn vặt', CURRENT_DATE);

INSERT INTO public.weight_logs (weight, height, bmi, note, logged_date)
VALUES 
(68.5, 172.0, 23.15, 'Mục tiêu giảm mỡ duy trì cơ', CURRENT_DATE);

INSERT INTO public.workout_logs (exercise_name, muscle_group, sets, reps, weight, duration_minutes, calories_burned, log_date)
VALUES 
('Barbell Bench Press', 'Ngực', 4, 10, 60.0, 20, 150, CURRENT_DATE),
('Incline Dumbbell Press', 'Ngực trên', 3, 12, 18.0, 15, 110, CURRENT_DATE),
('Dips (Xà kép)', 'Ngực dưới & Tay sau', 3, 12, 0.0, 15, 95, CURRENT_DATE);

INSERT INTO public.water_logs (amount_ml, target_ml, log_date)
VALUES 
(1250, 2000, CURRENT_DATE);

INSERT INTO public.workout_schedules (day_of_week, title, target_muscle, time_of_day, is_completed, notes)
VALUES 
('Thứ 2', 'Tập Ngực & Tay sau', 'Ngực - Tay sau', '17:30', false, 'Bench Press 4x10, Incline 3x12'),
('Thứ 3', 'Tập Lưng & Tay trước', 'Lưng xô - Tay trước', '17:30', false, 'Deadlift, Lat Pulldown, Bicep Curl'),
('Thứ 4', 'Nghỉ ngơi hoặc Cardio nhẹ', 'Cardio', '18:00', false, 'Đi bộ 30 phút hoặc bơi lội'),
('Thứ 5', 'Tập Chân & Bụng', 'Chân - Bắp chuối - Bụng', '17:30', false, 'Squat, Leg Press, Plank 3 hiệp'),
('Thứ 6', 'Tập Vai & Toàn thân', 'Vai - Cầu vai', '17:30', false, 'Overhead Press, Lateral Raise'),
('Thứ 7', 'Cardio / Chạy bộ ngoài trời', 'Tim mạch', '06:30', false, 'Chạy 5km công viên'),
('Chủ Nhật', 'Nghỉ ngơi phục hồi (Rest Day)', 'Phục hồi', '00:00', false, 'Giãn cơ yoga nhẹ nhàng');

