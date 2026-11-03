package com.suckhoe.app.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.suckhoe.app.data.model.NutritionItem;
import com.suckhoe.app.data.model.User;
import com.suckhoe.app.data.model.WaterLog;
import com.suckhoe.app.data.model.WeightRecord;
import com.suckhoe.app.data.model.WorkoutExercise;
import com.suckhoe.app.data.model.WorkoutSchedule;
import com.suckhoe.app.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class AppDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "cssk_app.db";
    private static final int DATABASE_VERSION = 2;

    // Tables
    public static final String TABLE_USERS = "app_users";
    public static final String TABLE_NUTRITION = "nutrition_logs";
    public static final String TABLE_WEIGHT = "weight_logs";
    public static final String TABLE_WORKOUT = "workout_logs";
    public static final String TABLE_WATER = "water_logs";
    public static final String TABLE_SCHEDULE = "workout_schedules";

    private static AppDatabaseHelper instance;

    public static synchronized AppDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AppDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private AppDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 0. Bảng Người dùng
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, " +
                "password TEXT, " +
                "full_name TEXT, " +
                "email TEXT, " +
                "created_at TEXT)");

        // 1. Dinh dưỡng
        db.execSQL("CREATE TABLE " + TABLE_NUTRITION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "food_name TEXT, " +
                "calories INTEGER, " +
                "protein REAL, " +
                "carbs REAL, " +
                "fat REAL, " +
                "meal_type TEXT, " +
                "log_date TEXT, " +
                "created_at TEXT)");

        // 2. Cân nặng
        db.execSQL("CREATE TABLE " + TABLE_WEIGHT + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "weight REAL, " +
                "height REAL, " +
                "bmi REAL, " +
                "note TEXT, " +
                "logged_date TEXT, " +
                "created_at TEXT)");

        // 3. Tập luyện
        db.execSQL("CREATE TABLE " + TABLE_WORKOUT + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "exercise_name TEXT, " +
                "muscle_group TEXT, " +
                "sets INTEGER, " +
                "reps INTEGER, " +
                "weight REAL, " +
                "duration_minutes INTEGER, " +
                "calories_burned INTEGER, " +
                "log_date TEXT, " +
                "created_at TEXT)");

        // 4. Uống nước
        db.execSQL("CREATE TABLE " + TABLE_WATER + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "amount_ml INTEGER, " +
                "target_ml INTEGER, " +
                "log_date TEXT, " +
                "created_at TEXT)");

        // 5. Lịch tập
        db.execSQL("CREATE TABLE " + TABLE_SCHEDULE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_id INTEGER, " +
                "day_of_week TEXT, " +
                "title TEXT, " +
                "target_muscle TEXT, " +
                "time_of_day TEXT, " +
                "is_completed INTEGER, " +
                "notes TEXT, " +
                "created_at TEXT)");

        seedInitialData(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        String today = DateUtils.getTodayIsoDate();

        // 1. Tạo 2 tài khoản mẫu:
        db.execSQL("INSERT INTO " + TABLE_USERS + " (username, password, full_name, email, created_at) VALUES " +
                "('nguyenvana', '123456', 'Nguyễn Văn A', 'vana@gmail.com', '" + today + "')," +
                "('tranthib', '123456', 'Trần Thị B', 'thib@gmail.com', '" + today + "')");

        // Dữ liệu mẫu cho Người dùng 1 (Nguyễn Văn A - id = 1)
        db.execSQL("INSERT INTO " + TABLE_NUTRITION + " (user_id, food_name, calories, protein, carbs, fat, meal_type, log_date) VALUES " +
                "(1, 'Phở bò tái', 450, 25.0, 55.0, 12.0, 'Bữa sáng', '" + today + "')," +
                "(1, 'Ức gà áp chảo & cơm gạo lứt', 520, 42.0, 60.0, 8.0, 'Bữa trưa', '" + today + "')," +
                "(1, 'Chuối & Sữa chua không đường', 180, 10.0, 30.0, 2.5, 'Ăn vặt', '" + today + "')");

        db.execSQL("INSERT INTO " + TABLE_WEIGHT + " (user_id, weight, height, bmi, note, logged_date) VALUES " +
                "(1, 68.5, 172.0, 23.1, 'Mục tiêu tăng cơ giảm mỡ', '" + today + "')");

        db.execSQL("INSERT INTO " + TABLE_WORKOUT + " (user_id, exercise_name, muscle_group, sets, reps, weight, duration_minutes, calories_burned, log_date) VALUES " +
                "(1, 'Barbell Bench Press', 'Ngực', 4, 10, 60.0, 20, 150, '" + today + "')," +
                "(1, 'Incline Dumbbell Press', 'Ngực trên', 3, 12, 18.0, 15, 110, '" + today + "')");

        db.execSQL("INSERT INTO " + TABLE_WATER + " (user_id, amount_ml, target_ml, log_date) VALUES (1, 1250, 2000, '" + today + "')");

        db.execSQL("INSERT INTO " + TABLE_SCHEDULE + " (user_id, day_of_week, title, target_muscle, time_of_day, is_completed, notes) VALUES " +
                "(1, 'Thứ 2', 'Tập Ngực & Tay sau', 'Ngực - Tay sau', '17:30', 1, 'Bench Press 4x10')," +
                "(1, 'Thứ 3', 'Tập Lưng & Tay trước', 'Lưng xô', '17:30', 0, 'Deadlift, Lat Pulldown')," +
                "(1, 'Thứ 5', 'Tập Chân & Bụng', 'Chân - Bụng', '17:30', 0, 'Squat, Leg Press')");

        // Dữ liệu mẫu cho Người dùng 2 (Trần Thị B - id = 2)
        db.execSQL("INSERT INTO " + TABLE_NUTRITION + " (user_id, food_name, calories, protein, carbs, fat, meal_type, log_date) VALUES " +
                "(2, 'Salad cá hồi & trứng luộc', 380, 28.0, 15.0, 14.0, 'Bữa trưa', '" + today + "')," +
                "(2, 'Sinh tố bơ yến mạch', 250, 8.0, 32.0, 7.0, 'Ăn vặt', '" + today + "')");

        db.execSQL("INSERT INTO " + TABLE_WEIGHT + " (user_id, weight, height, bmi, note, logged_date) VALUES " +
                "(2, 52.0, 160.0, 20.3, 'Duy trì vóc dáng thon gọn', '" + today + "')");

        db.execSQL("INSERT INTO " + TABLE_WORKOUT + " (user_id, exercise_name, muscle_group, sets, reps, weight, duration_minutes, calories_burned, log_date) VALUES " +
                "(2, 'Chạy bộ máy Cardio', 'Cardio', 1, 1, 0.0, 30, 220, '" + today + "')," +
                "(2, 'Yoga giãn cơ & Plank', 'Bụng', 3, 1, 0.0, 20, 90, '" + today + "')");

        db.execSQL("INSERT INTO " + TABLE_WATER + " (user_id, amount_ml, target_ml, log_date) VALUES (2, 1750, 2000, '" + today + "')");

        db.execSQL("INSERT INTO " + TABLE_SCHEDULE + " (user_id, day_of_week, title, target_muscle, time_of_day, is_completed, notes) VALUES " +
                "(2, 'Thứ 2', 'Chạy bộ & Nhảy dây', 'Cardio', '06:00', 1, 'Chạy 3km')," +
                "(2, 'Thứ 4', 'Pilates & Yoga', 'Toàn thân', '18:00', 0, 'Bài tập dẻo dai')," +
                "(2, 'Thứ 7', 'Bơi lội thư giãn', 'Toàn thân', '07:00', 0, 'Bơi 45 phút')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NUTRITION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
        onCreate(db);
    }

    // ==========================================
    // 0. USER AUTHENTICATION & PROFILES
    // ==========================================
    public long registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", user.getUsername().trim().toLowerCase());
        cv.put("password", user.getPassword());
        cv.put("full_name", user.getFullName().trim());
        cv.put("email", user.getEmail() != null ? user.getEmail().trim() : "");
        cv.put("created_at", DateUtils.getCurrentTime());
        try {
            return db.insert(TABLE_USERS, null, cv);
        } catch (Exception e) {
            return -1; // trùng username hoặc lỗi
        }
    }

    public User loginUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE LOWER(username) = LOWER(?) AND password = ? LIMIT 1",
                new String[]{username.trim(), password});
        User user = null;
        if (c.moveToFirst()) {
            user = new User();
            user.setId(c.getLong(c.getColumnIndexOrThrow("id")));
            user.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
            user.setPassword(c.getString(c.getColumnIndexOrThrow("password")));
            user.setFullName(c.getString(c.getColumnIndexOrThrow("full_name")));
            user.setEmail(c.getString(c.getColumnIndexOrThrow("email")));
        }
        c.close();
        return user;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_USERS + " ORDER BY id ASC", null);
        if (c.moveToFirst()) {
            do {
                User u = new User();
                u.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                u.setUsername(c.getString(c.getColumnIndexOrThrow("username")));
                u.setFullName(c.getString(c.getColumnIndexOrThrow("full_name")));
                u.setEmail(c.getString(c.getColumnIndexOrThrow("email")));
                list.add(u);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    // ==========================================
    // 1. DINH DƯỠNG CRUD (THEO USER ID)
    // ==========================================
    public long insertNutrition(NutritionItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", item.getUserId());
        cv.put("food_name", item.getFoodName());
        cv.put("calories", item.getCalories());
        cv.put("protein", item.getProtein());
        cv.put("carbs", item.getCarbs());
        cv.put("fat", item.getFat());
        cv.put("meal_type", item.getMealType());
        cv.put("log_date", item.getLogDate());
        cv.put("created_at", DateUtils.getCurrentTime());
        return db.insert(TABLE_NUTRITION, null, cv);
    }

    public List<NutritionItem> getNutritionByDate(long userId, String date) {
        List<NutritionItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NUTRITION + " WHERE user_id = ? AND log_date = ? ORDER BY id DESC",
                new String[]{String.valueOf(userId), date});
        if (c.moveToFirst()) {
            do {
                NutritionItem item = new NutritionItem();
                item.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                item.setUserId(c.getLong(c.getColumnIndexOrThrow("user_id")));
                item.setFoodName(c.getString(c.getColumnIndexOrThrow("food_name")));
                item.setCalories(c.getInt(c.getColumnIndexOrThrow("calories")));
                item.setProtein(c.getDouble(c.getColumnIndexOrThrow("protein")));
                item.setCarbs(c.getDouble(c.getColumnIndexOrThrow("carbs")));
                item.setFat(c.getDouble(c.getColumnIndexOrThrow("fat")));
                item.setMealType(c.getString(c.getColumnIndexOrThrow("meal_type")));
                item.setLogDate(c.getString(c.getColumnIndexOrThrow("log_date")));
                list.add(item);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public void deleteNutrition(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NUTRITION, "id = ?", new String[]{String.valueOf(id)});
    }

    // ==========================================
    // 2. CÂN NẶNG CRUD (THEO USER ID)
    // ==========================================
    public long insertWeight(WeightRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", record.getUserId());
        cv.put("weight", record.getWeight());
        cv.put("height", record.getHeight());
        cv.put("bmi", record.getBmi());
        cv.put("note", record.getNote());
        cv.put("logged_date", record.getLoggedDate());
        cv.put("created_at", DateUtils.getCurrentTime());
        return db.insert(TABLE_WEIGHT, null, cv);
    }

    public List<WeightRecord> getAllWeights(long userId) {
        List<WeightRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_WEIGHT + " WHERE user_id = ? ORDER BY id DESC",
                new String[]{String.valueOf(userId)});
        if (c.moveToFirst()) {
            do {
                WeightRecord record = new WeightRecord();
                record.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                record.setUserId(c.getLong(c.getColumnIndexOrThrow("user_id")));
                record.setWeight(c.getDouble(c.getColumnIndexOrThrow("weight")));
                record.setHeight(c.getDouble(c.getColumnIndexOrThrow("height")));
                record.setBmi(c.getDouble(c.getColumnIndexOrThrow("bmi")));
                record.setNote(c.getString(c.getColumnIndexOrThrow("note")));
                record.setLoggedDate(c.getString(c.getColumnIndexOrThrow("logged_date")));
                list.add(record);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public WeightRecord getLatestWeight(long userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_WEIGHT + " WHERE user_id = ? ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(userId)});
        WeightRecord record = null;
        if (c.moveToFirst()) {
            record = new WeightRecord();
            record.setId(c.getLong(c.getColumnIndexOrThrow("id")));
            record.setUserId(c.getLong(c.getColumnIndexOrThrow("user_id")));
            record.setWeight(c.getDouble(c.getColumnIndexOrThrow("weight")));
            record.setHeight(c.getDouble(c.getColumnIndexOrThrow("height")));
            record.setBmi(c.getDouble(c.getColumnIndexOrThrow("bmi")));
            record.setNote(c.getString(c.getColumnIndexOrThrow("note")));
            record.setLoggedDate(c.getString(c.getColumnIndexOrThrow("logged_date")));
        }
        c.close();
        return record;
    }

    public void deleteWeight(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WEIGHT, "id = ?", new String[]{String.valueOf(id)});
    }

    // ==========================================
    // 3. TẬP LUYỆN CRUD (THEO USER ID)
    // ==========================================
    public long insertWorkout(WorkoutExercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", exercise.getUserId());
        cv.put("exercise_name", exercise.getExerciseName());
        cv.put("muscle_group", exercise.getMuscleGroup());
        cv.put("sets", exercise.getSets());
        cv.put("reps", exercise.getReps());
        cv.put("weight", exercise.getWeight());
        cv.put("duration_minutes", exercise.getDurationMinutes());
        cv.put("calories_burned", exercise.getCaloriesBurned());
        cv.put("log_date", exercise.getLogDate());
        cv.put("created_at", DateUtils.getCurrentTime());
        return db.insert(TABLE_WORKOUT, null, cv);
    }

    public List<WorkoutExercise> getWorkoutsByDate(long userId, String date) {
        List<WorkoutExercise> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_WORKOUT + " WHERE user_id = ? AND log_date = ? ORDER BY id DESC",
                new String[]{String.valueOf(userId), date});
        if (c.moveToFirst()) {
            do {
                WorkoutExercise item = new WorkoutExercise();
                item.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                item.setUserId(c.getLong(c.getColumnIndexOrThrow("user_id")));
                item.setExerciseName(c.getString(c.getColumnIndexOrThrow("exercise_name")));
                item.setMuscleGroup(c.getString(c.getColumnIndexOrThrow("muscle_group")));
                item.setSets(c.getInt(c.getColumnIndexOrThrow("sets")));
                item.setReps(c.getInt(c.getColumnIndexOrThrow("reps")));
                item.setWeight(c.getDouble(c.getColumnIndexOrThrow("weight")));
                item.setDurationMinutes(c.getInt(c.getColumnIndexOrThrow("duration_minutes")));
                item.setCaloriesBurned(c.getInt(c.getColumnIndexOrThrow("calories_burned")));
                item.setLogDate(c.getString(c.getColumnIndexOrThrow("log_date")));
                list.add(item);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public void deleteWorkout(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WORKOUT, "id = ?", new String[]{String.valueOf(id)});
    }

    // ==========================================
    // 4. UỐNG NƯỚC CRUD (THEO USER ID)
    // ==========================================
    public WaterLog getWaterLog(long userId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_WATER + " WHERE user_id = ? AND log_date = ? ORDER BY id DESC LIMIT 1",
                new String[]{String.valueOf(userId), date});
        WaterLog log = null;
        if (c.moveToFirst()) {
            log = new WaterLog();
            log.setId(c.getLong(c.getColumnIndexOrThrow("id")));
            log.setUserId(c.getLong(c.getColumnIndexOrThrow("user_id")));
            log.setAmountMl(c.getInt(c.getColumnIndexOrThrow("amount_ml")));
            log.setTargetMl(c.getInt(c.getColumnIndexOrThrow("target_ml")));
            log.setLogDate(c.getString(c.getColumnIndexOrThrow("log_date")));
        }
        c.close();
        return log;
    }

    public void updateWaterLog(long userId, String date, int addedMl, int targetMl) {
        WaterLog current = getWaterLog(userId, date);
        SQLiteDatabase db = this.getWritableDatabase();
        if (current == null) {
            ContentValues cv = new ContentValues();
            cv.put("user_id", userId);
            cv.put("amount_ml", Math.max(0, addedMl));
            cv.put("target_ml", targetMl);
            cv.put("log_date", date);
            cv.put("created_at", DateUtils.getCurrentTime());
            db.insert(TABLE_WATER, null, cv);
        } else {
            ContentValues cv = new ContentValues();
            int newAmount = Math.max(0, current.getAmountMl() + addedMl);
            cv.put("amount_ml", newAmount);
            cv.put("target_ml", targetMl);
            db.update(TABLE_WATER, cv, "id = ?", new String[]{String.valueOf(current.getId())});
        }
    }

    public void setWaterLogAmount(long userId, String date, int exactAmountMl, int targetMl) {
        WaterLog current = getWaterLog(userId, date);
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("amount_ml", Math.max(0, exactAmountMl));
        cv.put("target_ml", targetMl);
        if (current == null) {
            cv.put("user_id", userId);
            cv.put("log_date", date);
            cv.put("created_at", DateUtils.getCurrentTime());
            db.insert(TABLE_WATER, null, cv);
        } else {
            db.update(TABLE_WATER, cv, "id = ?", new String[]{String.valueOf(current.getId())});
        }
    }

    // ==========================================
    // 5. LỊCH TẬP CRUD (THEO USER ID)
    // ==========================================
    public long insertSchedule(WorkoutSchedule schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", schedule.getUserId());
        cv.put("day_of_week", schedule.getDayOfWeek());
        cv.put("title", schedule.getTitle());
        cv.put("target_muscle", schedule.getTargetMuscle());
        cv.put("time_of_day", schedule.getTimeOfDay());
        cv.put("is_completed", schedule.isCompleted() ? 1 : 0);
        cv.put("notes", schedule.getNotes());
        cv.put("created_at", DateUtils.getCurrentTime());
        return db.insert(TABLE_SCHEDULE, null, cv);
    }

    public List<WorkoutSchedule> getAllSchedules(long userId) {
        List<WorkoutSchedule> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SCHEDULE + " WHERE user_id = ? ORDER BY id ASC",
                new String[]{String.valueOf(userId)});
        if (c.moveToFirst()) {
            do {
                WorkoutSchedule item = new WorkoutSchedule();
                item.setId(c.getLong(c.getColumnIndexOrThrow("id")));
                item.setUserId(c.getLong(c.getColumnIndexOrThrow("user_id")));
                item.setDayOfWeek(c.getString(c.getColumnIndexOrThrow("day_of_week")));
                item.setTitle(c.getString(c.getColumnIndexOrThrow("title")));
                item.setTargetMuscle(c.getString(c.getColumnIndexOrThrow("target_muscle")));
                item.setTimeOfDay(c.getString(c.getColumnIndexOrThrow("time_of_day")));
                item.setCompleted(c.getInt(c.getColumnIndexOrThrow("is_completed")) == 1);
                item.setNotes(c.getString(c.getColumnIndexOrThrow("notes")));
                list.add(item);
            } while (c.moveToNext());
        }
        c.close();
        return list;
    }

    public void updateScheduleCompleted(long id, boolean isCompleted) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_completed", isCompleted ? 1 : 0);
        db.update(TABLE_SCHEDULE, cv, "id = ?", new String[]{String.valueOf(id)});
    }

    public void deleteSchedule(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SCHEDULE, "id = ?", new String[]{String.valueOf(id)});
    }
}
