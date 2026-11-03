package com.suckhoe.app.data.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.suckhoe.app.data.model.NutritionItem;
import com.suckhoe.app.data.model.WaterLog;
import com.suckhoe.app.data.model.WeightRecord;
import com.suckhoe.app.data.model.WorkoutExercise;
import com.suckhoe.app.data.model.WorkoutSchedule;
import com.suckhoe.app.util.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class AppDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "suckhoe_app.db";
    private static final int DATABASE_VERSION = 1;

    // Tables
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
        // 1. Dinh dưỡng
        db.execSQL("CREATE TABLE " + TABLE_NUTRITION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
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
                "weight REAL, " +
                "height REAL, " +
                "bmi REAL, " +
                "note TEXT, " +
                "logged_date TEXT, " +
                "created_at TEXT)");

        // 3. Tập luyện
        db.execSQL("CREATE TABLE " + TABLE_WORKOUT + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
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
                "amount_ml INTEGER, " +
                "target_ml INTEGER, " +
                "log_date TEXT, " +
                "created_at TEXT)");

        // 5. Lịch tập
        db.execSQL("CREATE TABLE " + TABLE_SCHEDULE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "day_of_week TEXT, " +
                "title TEXT, " +
                "target_muscle TEXT, " +
                "time_of_day TEXT, " +
                "is_completed INTEGER, " +
                "notes TEXT, " +
                "created_at TEXT)");

        // Thêm dữ liệu mẫu ban đầu để giao diện hiển thị đẹp mắt
        seedInitialData(db);
    }

    private void seedInitialData(SQLiteDatabase db) {
        String today = DateUtils.getTodayIsoDate();

        // Mẫu dinh dưỡng
        db.execSQL("INSERT INTO " + TABLE_NUTRITION + " (food_name, calories, protein, carbs, fat, meal_type, log_date) VALUES " +
                "('Phở bò tái', 450, 25.0, 55.0, 12.0, 'Bữa sáng', '" + today + "')," +
                "('Ức gà áp chảo & cơm gạo lứt', 520, 42.0, 60.0, 8.0, 'Bữa trưa', '" + today + "')," +
                "('Chuối & Sữa chua không đường', 180, 10.0, 30.0, 2.5, 'Ăn vặt', '" + today + "')");

        // Mẫu cân nặng
        db.execSQL("INSERT INTO " + TABLE_WEIGHT + " (weight, height, bmi, note, logged_date) VALUES " +
                "(68.5, 172.0, 23.1, 'Mục tiêu giảm mỡ giữ cơ bắp', '" + today + "')");

        // Mẫu bài tập
        db.execSQL("INSERT INTO " + TABLE_WORKOUT + " (exercise_name, muscle_group, sets, reps, weight, duration_minutes, calories_burned, log_date) VALUES " +
                "('Barbell Bench Press', 'Ngực', 4, 10, 60.0, 20, 150, '" + today + "')," +
                "('Incline Dumbbell Press', 'Ngực trên', 3, 12, 18.0, 15, 110, '" + today + "')," +
                "('Dips (Xà kép)', 'Ngực dưới & Tay sau', 3, 12, 0.0, 15, 95, '" + today + "')");

        // Mẫu nước
        db.execSQL("INSERT INTO " + TABLE_WATER + " (amount_ml, target_ml, log_date) VALUES (1250, 2000, '" + today + "')");

        // Mẫu lịch tập 7 ngày
        db.execSQL("INSERT INTO " + TABLE_SCHEDULE + " (day_of_week, title, target_muscle, time_of_day, is_completed, notes) VALUES " +
                "('Thứ 2', 'Tập Ngực & Tay sau', 'Ngực - Tay sau', '17:30', 1, 'Bench Press 4x10, Incline 3x12')," +
                "('Thứ 3', 'Tập Lưng & Tay trước', 'Lưng xô - Tay trước', '17:30', 0, 'Deadlift, Lat Pulldown, Bicep Curl')," +
                "('Thứ 4', 'Nghỉ ngơi hoặc Cardio nhẹ', 'Cardio', '18:00', 0, 'Đi bộ 30 phút hoặc bơi lội')," +
                "('Thứ 5', 'Tập Chân & Bụng', 'Chân - Bụng', '17:30', 0, 'Squat, Leg Press, Plank 3 hiệp')," +
                "('Thứ 6', 'Tập Vai & Cầu vai', 'Vai', '17:30', 0, 'Overhead Press, Lateral Raise')," +
                "('Thứ 7', 'Cardio / Chạy bộ', 'Tim mạch', '06:30', 0, 'Chạy 5km công viên')," +
                "('Chủ Nhật', 'Nghỉ ngơi phục hồi (Rest Day)', 'Toàn thân', '00:00', 0, 'Giãn cơ yoga nhẹ nhàng')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NUTRITION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WEIGHT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WATER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
        onCreate(db);
    }

    // ==========================================
    // 1. DINH DƯỠNG CRUD
    // ==========================================
    public long insertNutrition(NutritionItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
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

    public List<NutritionItem> getNutritionByDate(String date) {
        List<NutritionItem> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_NUTRITION + " WHERE log_date = ? ORDER BY id DESC", new String[]{date});
        if (c.moveToFirst()) {
            do {
                NutritionItem item = new NutritionItem();
                item.setId(c.getLong(c.getColumnIndexOrThrow("id")));
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
    // 2. CÂN NẶNG CRUD
    // ==========================================
    public long insertWeight(WeightRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("weight", record.getWeight());
        cv.put("height", record.getHeight());
        cv.put("bmi", record.getBmi());
        cv.put("note", record.getNote());
        cv.put("logged_date", record.getLoggedDate());
        cv.put("created_at", DateUtils.getCurrentTime());
        return db.insert(TABLE_WEIGHT, null, cv);
    }

    public List<WeightRecord> getAllWeights() {
        List<WeightRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_WEIGHT + " ORDER BY id DESC", null);
        if (c.moveToFirst()) {
            do {
                WeightRecord record = new WeightRecord();
                record.setId(c.getLong(c.getColumnIndexOrThrow("id")));
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

    public WeightRecord getLatestWeight() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_WEIGHT + " ORDER BY id DESC LIMIT 1", null);
        WeightRecord record = null;
        if (c.moveToFirst()) {
            record = new WeightRecord();
            record.setId(c.getLong(c.getColumnIndexOrThrow("id")));
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
    // 3. TẬP LUYỆN CRUD
    // ==========================================
    public long insertWorkout(WorkoutExercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
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

    public List<WorkoutExercise> getWorkoutsByDate(String date) {
        List<WorkoutExercise> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_WORKOUT + " WHERE log_date = ? ORDER BY id DESC", new String[]{date});
        if (c.moveToFirst()) {
            do {
                WorkoutExercise item = new WorkoutExercise();
                item.setId(c.getLong(c.getColumnIndexOrThrow("id")));
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
    // 4. UỐNG NƯỚC CRUD
    // ==========================================
    public WaterLog getWaterLog(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_WATER + " WHERE log_date = ? ORDER BY id DESC LIMIT 1", new String[]{date});
        WaterLog log = null;
        if (c.moveToFirst()) {
            log = new WaterLog();
            log.setId(c.getLong(c.getColumnIndexOrThrow("id")));
            log.setAmountMl(c.getInt(c.getColumnIndexOrThrow("amount_ml")));
            log.setTargetMl(c.getInt(c.getColumnIndexOrThrow("target_ml")));
            log.setLogDate(c.getString(c.getColumnIndexOrThrow("log_date")));
        }
        c.close();
        return log;
    }

    public void updateWaterLog(String date, int addedMl, int targetMl) {
        WaterLog current = getWaterLog(date);
        SQLiteDatabase db = this.getWritableDatabase();
        if (current == null) {
            ContentValues cv = new ContentValues();
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

    // ==========================================
    // 5. LỊCH TẬP CRUD
    // ==========================================
    public long insertSchedule(WorkoutSchedule schedule) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("day_of_week", schedule.getDayOfWeek());
        cv.put("title", schedule.getTitle());
        cv.put("target_muscle", schedule.getTargetMuscle());
        cv.put("time_of_day", schedule.getTimeOfDay());
        cv.put("is_completed", schedule.isCompleted() ? 1 : 0);
        cv.put("notes", schedule.getNotes());
        cv.put("created_at", DateUtils.getCurrentTime());
        return db.insert(TABLE_SCHEDULE, null, cv);
    }

    public List<WorkoutSchedule> getAllSchedules() {
        List<WorkoutSchedule> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_SCHEDULE + " ORDER BY id ASC", null);
        if (c.moveToFirst()) {
            do {
                WorkoutSchedule item = new WorkoutSchedule();
                item.setId(c.getLong(c.getColumnIndexOrThrow("id")));
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
