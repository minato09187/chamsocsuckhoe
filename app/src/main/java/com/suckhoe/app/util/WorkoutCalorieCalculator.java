package com.suckhoe.app.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Bộ công cụ tính toán Calo tiêu hao tập luyện khoa học (theo MET, Khối lượng tạ Volume & Thời gian).
 * Cung cấp danh mục bài tập tương ứng cho từng nhóm cơ.
 */
public class WorkoutCalorieCalculator {

    private static final Map<String, List<String>> MUSCLE_EXERCISES = new HashMap<>();

    static {
        // 1. Ngực (Chest)
        List<String> chest = new ArrayList<>();
        chest.add("Barbell Bench Press (Đẩy ngực tạ đòn)");
        chest.add("Incline Dumbbell Press (Đẩy ngực trên tạ đơn)");
        chest.add("Dumbbell Flyes (Banh ngực tạ đơn)");
        chest.add("Push-up (Hít đất)");
        chest.add("Decline Bench Press (Đẩy ngực dưới)");
        chest.add("Chest Dips (Chống xà kép ngực)");
        chest.add("Cable Crossover (Kéo cáp ép ngực)");
        chest.add("Machine Chest Press (Đẩy ngực máy)");
        MUSCLE_EXERCISES.put("Ngực", chest);

        // 2. Lưng xô (Back)
        List<String> back = new ArrayList<>();
        back.add("Deadlift (Kéo tạ đòn toàn thân)");
        back.add("Barbell Row (Kéo tạ đòn gập người)");
        back.add("Lat Pulldown (Kéo xô máy)");
        back.add("Pull-up (Hít xà đơn)");
        back.add("Seated Cable Row (Kéo cáp ngồi)");
        back.add("Single-arm Dumbbell Row (Chèo tạ đơn 1 tay)");
        back.add("T-Bar Row (Kéo xô thanh T)");
        back.add("Hyperextension (Gập lưng dưới)");
        MUSCLE_EXERCISES.put("Lưng xô", back);

        // 3. Chân (Legs / Glutes)
        List<String> legs = new ArrayList<>();
        legs.add("Barbell Squat (Gánh đùi tạ đòn)");
        legs.add("Leg Press (Đạp đùi máy nghiêng)");
        legs.add("Romanian Deadlift (RDL đùi sau)");
        legs.add("Dumbbell Lunge (Chùng chân bước tới)");
        legs.add("Leg Extension (Đá đùi trước máy)");
        legs.add("Leg Curl (Cuộn đùi sau máy)");
        legs.add("Calf Raise (Nhón bắp chân)");
        legs.add("Hip Thrust (Nâng hông đẩy mông)");
        MUSCLE_EXERCISES.put("Chân", legs);

        // 4. Vai (Shoulders)
        List<String> shoulders = new ArrayList<>();
        shoulders.add("Overhead Press (Đẩy vai tạ đòn OHP)");
        shoulders.add("Dumbbell Shoulder Press (Đẩy vai tạ đơn)");
        shoulders.add("Dumbbell Lateral Raise (Dang tạ 2 bên vai)");
        shoulders.add("Face Pull (Kéo cáp vào mặt)");
        shoulders.add("Front Dumbbell Raise (Nâng tạ phía trước)");
        shoulders.add("Arnold Press (Đẩy vai xoay tay)");
        shoulders.add("Barbell Shrugs (Nhún cầu vai)");
        MUSCLE_EXERCISES.put("Vai", shoulders);

        // 5. Tay trước (Biceps)
        List<String> biceps = new ArrayList<>();
        biceps.add("Barbell Bicep Curl (Cuốn tạ đòn tay trước)");
        biceps.add("Dumbbell Hammer Curl (Cuốn tạ búa)");
        biceps.add("Incline Dumbbell Curl (Cuốn tạ ghế nghiêng)");
        biceps.add("Preacher Curl (Cuốn tay trước trên bàn)");
        biceps.add("Cable Bicep Curl (Kéo cáp cuốn tay trước)");
        biceps.add("Concentration Curl (Cuốn tạ tập trung)");
        MUSCLE_EXERCISES.put("Tay trước", biceps);

        // 6. Tay sau (Triceps)
        List<String> triceps = new ArrayList<>();
        triceps.add("Tricep Pushdown (Kéo cáp tay sau)");
        triceps.add("Skull Crusher (Nằm đẩy tạ sau đầu)");
        triceps.add("Overhead Tricep Extension (Đẩy tạ sau đầu)");
        triceps.add("Close Grip Bench Press (Đẩy ngực hẹp tay sau)");
        triceps.add("Bench Dips (Nhún tay sau mép ghế)");
        triceps.add("Tricep Kickback (Đá tay sau tạ đơn)");
        MUSCLE_EXERCISES.put("Tay sau", triceps);

        // 7. Bụng (Abs / Core)
        List<String> abs = new ArrayList<>();
        abs.add("Plank (Giữ người thẳng)");
        abs.add("Crunch (Gập bụng sàn)");
        abs.add("Hanging Leg Raise (Treo xà nhấc chân)");
        abs.add("Russian Twist (Xoay eo kiểu Nga)");
        abs.add("Ab Wheel Rollout (Lăn con lăn bụng)");
        abs.add("Mountain Climbers (Leo núi tại chỗ)");
        abs.add("Bicycle Crunch (Gập bụng đạp xe)");
        MUSCLE_EXERCISES.put("Bụng", abs);

        // 8. Cardio
        List<String> cardio = new ArrayList<>();
        cardio.add("Chạy bộ máy (Treadmill Running)");
        cardio.add("Nhảy dây (Jumping Rope)");
        cardio.add("Đạp xe tĩnh (Stationary Cycling)");
        cardio.add("Đi bộ dốc (Incline Walking)");
        cardio.add("Chèo thuyền máy (Rowing Machine)");
        cardio.add("Bơi lội (Swimming)");
        cardio.add("Leo cầu thang máy (Stair Climber)");
        MUSCLE_EXERCISES.put("Cardio", cardio);

        // 9. Toàn thân (Full Body / HIIT)
        List<String> fullbody = new ArrayList<>();
        fullbody.add("Burpee (Hít đất bật nhảy)");
        fullbody.add("Kettlebell Swing (Vung tạ ấm)");
        fullbody.add("HIIT Toàn thân (Tabata)");
        fullbody.add("Clean and Jerk (Cử đẩy tạ đòn)");
        fullbody.add("Battle Ropes (Đập dây thừng)");
        MUSCLE_EXERCISES.put("Toàn thân", fullbody);
    }

    /**
     * Lấy danh sách các bài tập gợi ý cho nhóm cơ tương ứng
     */
    public static List<String> getExercisesForMuscle(String muscleGroup) {
        if (muscleGroup == null) return Collections.emptyList();
        for (Map.Entry<String, List<String>> entry : MUSCLE_EXERCISES.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(muscleGroup.trim())) {
                return entry.getValue();
            }
        }
        // Trường hợp không tìm thấy nhóm cơ cụ thể -> Trả về danh sách bài tập ngực/phổ thông
        return MUSCLE_EXERCISES.get("Ngực");
    }

    /**
     * Lấy toàn bộ danh sách bài tập cho tất cả các nhóm cơ
     */
    public static List<String> getAllExercises() {
        List<String> all = new ArrayList<>();
        for (List<String> list : MUSCLE_EXERCISES.values()) {
            all.addAll(list);
        }
        return all;
    }

    /**
     * Tự động tính toán lượng Calo tiêu hao dựa trên công thức thể thao khoa học:
     * Calories = (MET * 3.5 * CânNặngKg / 200) * ThờiGianPhút + Hệ số khối lượng tạ (Volume)
     *
     * @param exerciseName Tên bài tập
     * @param muscleGroup Nhóm cơ
     * @param sets Số hiệp
     * @param reps Số rep mỗi hiệp
     * @param weightLifted Mức tạ (kg)
     * @param durationMinutes Thời gian tập (phút)
     * @param userWeightKg Cân nặng người dùng (kg)
     * @return Số Calo tiêu hao ước tính (kcal)
     */
    public static int calculateCaloriesBurned(String exerciseName, String muscleGroup, int sets, int reps, double weightLifted, int durationMinutes, double userWeightKg) {
        double weight = (userWeightKg > 35 && userWeightKg < 200) ? userWeightKg : 68.0;
        int duration = Math.max(1, durationMinutes);
        int validSets = Math.max(1, sets);
        int validReps = Math.max(1, reps);

        // 1. Xác định chỉ số MET (Metabolic Equivalent of Task)
        double met = 4.5; // Mặc định tập kháng lực vừa phải

        String muscle = (muscleGroup != null) ? muscleGroup.toLowerCase(Locale.getDefault()) : "";
        String exName = (exerciseName != null) ? exerciseName.toLowerCase(Locale.getDefault()) : "";

        if (muscle.contains("cardio") || exName.contains("chay") || exName.contains("nhay day") || exName.contains("dap xe") || exName.contains("boi")) {
            if (exName.contains("nhay day") || exName.contains("hiit") || exName.contains("chay")) {
                met = 10.0; // Cardio cường độ cao
            } else {
                met = 7.5; // Cardio vừa
            }
        } else if (muscle.contains("chan") || muscle.contains("lung") || muscle.contains("toan than") || exName.contains("squat") || exName.contains("deadlift")) {
            // Nhóm cơ lớn (Compound lifts) tiêu hao năng lượng rất cao
            met = 6.0;
        } else if (muscle.contains("nguc") || exName.contains("bench press")) {
            met = 5.2;
        } else if (muscle.contains("vai")) {
            met = 4.8;
        } else if (muscle.contains("tay") || muscle.contains("bung") || exName.contains("plank") || exName.contains("curl")) {
            met = 4.0;
        }

        // 2. Tính Calo nền theo thời gian tập (Base Metabolic Cost):
        // Công thức chuẩn ACSM: Calo/phút = (MET * 3.5 * CânNặng) / 200
        double baseCalories = (met * 3.5 * weight / 200.0) * duration;

        // 3. Tính Calo cơ học sinh công phụ trội theo khối lượng tạ (Volume load):
        // Volume = số hiệp x số rep x số kg tạ
        double volumeKg = validSets * validReps * Math.max(0.0, weightLifted);
        double volumeBonus = 0.0;
        if (weightLifted > 0) {
            // Nâng tạ tiêu hao thêm năng lượng cơ học và phục hồi EPOC
            volumeBonus = volumeKg * 0.035;
        } else {
            // Bài tập trọng lượng cơ thể (Calisthenics / Bodyweight)
            volumeBonus = validSets * validReps * 0.35;
        }

        int totalCalories = (int) Math.round(baseCalories + volumeBonus);

        // Đảm bảo mức tối thiểu và hợp lý
        if (totalCalories < 15) totalCalories = 15;
        if (totalCalories > 1200) totalCalories = 1200;

        return totalCalories;
    }
}

