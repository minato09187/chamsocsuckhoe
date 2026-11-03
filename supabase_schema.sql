-- =====================================================================
-- HE THONG CSDL SUPABASE CHO UNG DUNG CSSK (DA NGUOI DUNG)
-- Supabase Project URL: https://rtqmvnhadjetpiategzi.supabase.co
-- File nay an toan de chay lai nhieu lan (Idempotent Script)
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
    meal_type TEXT NOT NULL DEFAULT 'Bữa sáng',
    log_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 2. BẢNG QUẢN LÝ CÂN NẶNG & BMI (weight_logs)
CREATE TABLE IF NOT EXISTS public.weight_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT DEFAULT 1,
    weight REAL NOT NULL,
    height REAL DEFAULT 170,
    bmi REAL,
    note TEXT,
    logged_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 3. BẢNG THEO DÕI TẬP LUYỆN (workout_logs)
CREATE TABLE IF NOT EXISTS public.workout_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT DEFAULT 1,
    exercise_name TEXT NOT NULL,
    muscle_group TEXT DEFAULT 'Toàn thân',
    sets INTEGER DEFAULT 3,
    reps INTEGER DEFAULT 12,
    weight REAL DEFAULT 0,
    duration_minutes INTEGER DEFAULT 30,
    calories_burned INTEGER DEFAULT 0,
    log_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 4. BẢNG THEO DÕI UỐNG NƯỚC (water_logs)
CREATE TABLE IF NOT EXISTS public.water_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT DEFAULT 1,
    amount_ml INTEGER NOT NULL DEFAULT 250,
    target_ml INTEGER NOT NULL DEFAULT 2000,
    log_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- 5. BẢNG LẬP LỊCH TẬP (workout_schedules)
CREATE TABLE IF NOT EXISTS public.workout_schedules (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT DEFAULT 1,
    day_of_week TEXT NOT NULL,
    title TEXT NOT NULL,
    target_muscle TEXT,
    time_of_day TEXT DEFAULT '17:30',
    is_completed BOOLEAN DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMPTZ DEFAULT NOW()
);

-- =====================================================================
-- BỔ SUNG CỘT user_id (NẾU BẢNG ĐÃ ĐƯỢC TẠO TỪ TRƯỚC)
-- =====================================================================

ALTER TABLE public.nutrition_logs ADD COLUMN IF NOT EXISTS user_id BIGINT DEFAULT 1;
ALTER TABLE public.weight_logs ADD COLUMN IF NOT EXISTS user_id BIGINT DEFAULT 1;
ALTER TABLE public.workout_logs ADD COLUMN IF NOT EXISTS user_id BIGINT DEFAULT 1;
ALTER TABLE public.water_logs ADD COLUMN IF NOT EXISTS user_id BIGINT DEFAULT 1;
ALTER TABLE public.workout_schedules ADD COLUMN IF NOT EXISTS user_id BIGINT DEFAULT 1;

-- =====================================================================
-- BẬT ROW LEVEL SECURITY (RLS) VÀ TẠO CHÍNH SÁCH AN TOÀN (DROP TRƯỚC KHI TẠO)
-- =====================================================================

ALTER TABLE public.app_users ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.nutrition_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.weight_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.workout_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.water_logs ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.workout_schedules ENABLE ROW LEVEL SECURITY;

-- 1. app_users
DROP POLICY IF EXISTS "Allow public read-write app_users" ON public.app_users;
CREATE POLICY "Allow public read-write app_users" 
ON public.app_users FOR ALL USING (true) WITH CHECK (true);

-- 2. nutrition_logs
DROP POLICY IF EXISTS "Allow public read-write nutrition_logs" ON public.nutrition_logs;
CREATE POLICY "Allow public read-write nutrition_logs" 
ON public.nutrition_logs FOR ALL USING (true) WITH CHECK (true);

-- 3. weight_logs
DROP POLICY IF EXISTS "Allow public read-write weight_logs" ON public.weight_logs;
CREATE POLICY "Allow public read-write weight_logs" 
ON public.weight_logs FOR ALL USING (true) WITH CHECK (true);

-- 4. workout_logs
DROP POLICY IF EXISTS "Allow public read-write workout_logs" ON public.workout_logs;
CREATE POLICY "Allow public read-write workout_logs" 
ON public.workout_logs FOR ALL USING (true) WITH CHECK (true);

-- 5. water_logs
DROP POLICY IF EXISTS "Allow public read-write water_logs" ON public.water_logs;
CREATE POLICY "Allow public read-write water_logs" 
ON public.water_logs FOR ALL USING (true) WITH CHECK (true);

-- 6. workout_schedules
DROP POLICY IF EXISTS "Allow public read-write workout_schedules" ON public.workout_schedules;
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

-- Cập nhật dữ liệu mẫu cũ sang user_id = 1 nếu đang null
UPDATE public.nutrition_logs SET user_id = 1 WHERE user_id IS NULL;
UPDATE public.weight_logs SET user_id = 1 WHERE user_id IS NULL;
UPDATE public.workout_logs SET user_id = 1 WHERE user_id IS NULL;
UPDATE public.water_logs SET user_id = 1 WHERE user_id IS NULL;
UPDATE public.workout_schedules SET user_id = 1 WHERE user_id IS NULL;
