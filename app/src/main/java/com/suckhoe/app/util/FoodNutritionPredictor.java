package com.suckhoe.app.util;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Bộ công cụ AI / Tri thức dinh dưỡng món ăn Việt Nam & Quốc tế.
 * Tự động phân tích tên món ăn, gợi ý danh mục và ước tính Calo, Protein, Carbs, Fat.
 */
public class FoodNutritionPredictor {

    public static class Prediction {
        public final int calories;
        public final double protein;
        public final double carbs;
        public final double fat;
        public final String mealType; // Bữa sáng, Bữa trưa, Bữa tối, Ăn vặt
        public final String description;

        public Prediction(int calories, double protein, double carbs, double fat, String mealType, String description) {
            this.calories = Math.max(0, calories);
            this.protein = Math.max(0, Math.round(protein * 10.0) / 10.0);
            this.carbs = Math.max(0, Math.round(carbs * 10.0) / 10.0);
            this.fat = Math.max(0, Math.round(fat * 10.0) / 10.0);
            this.mealType = mealType;
            this.description = description;
        }
    }

    private static class DishData {
        String displayName;
        int calories;
        double protein;
        double carbs;
        double fat;
        String mealType;

        DishData(String displayName, int calories, double protein, double carbs, double fat, String mealType) {
            this.displayName = displayName;
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fat = fat;
            this.mealType = mealType;
        }
    }

    // Danh bạ dinh dưỡng chuẩn xác cho hàng loạt món ăn phổ biến
    private static final Map<String, DishData> DISH_DATABASE = new HashMap<>();
    private static final List<String> SUGGESTIONS = new ArrayList<>();

    private static void registerDish(String key, String displayName, int cal, double pro, double carb, double fat, String meal) {
        DishData dish = new DishData(displayName, cal, pro, carb, fat, meal);
        DISH_DATABASE.put(normalize(key), dish);
        DISH_DATABASE.put(normalize(displayName), dish);
        if (!SUGGESTIONS.contains(displayName)) {
            SUGGESTIONS.add(displayName);
        }
    }

    static {
        // === 1. BỮA SÁNG (MÓN NƯỚC, BÁNH MÌ, XÔI, BÁNH CUỐN) ===
        registerDish("pho bo", "Phở bò tái", 450, 26.0, 56.0, 12.0, "Bữa sáng");
        registerDish("pho bo chin", "Phở bò chín", 480, 28.0, 56.0, 14.0, "Bữa sáng");
        registerDish("pho bo sot vang", "Phở bò sốt vang", 530, 28.0, 58.0, 18.0, "Bữa sáng");
        registerDish("pho ga", "Phở gà", 400, 25.0, 52.0, 10.0, "Bữa sáng");
        registerDish("bun bo hue", "Bún bò Huế", 520, 28.0, 60.0, 16.0, "Bữa sáng");
        registerDish("bun rieu", "Bún riêu cua", 430, 20.0, 55.0, 14.0, "Bữa sáng");
        registerDish("bun oc", "Bún ốc", 380, 18.0, 52.0, 9.0, "Bữa sáng");
        registerDish("bun thang", "Bún thang", 410, 24.0, 53.0, 11.0, "Bữa sáng");
        registerDish("bun ca", "Bún cá", 420, 22.0, 54.0, 12.0, "Bữa sáng");
        registerDish("bun moc", "Bún mọc", 440, 22.0, 55.0, 13.0, "Bữa sáng");
        registerDish("mien ga", "Miến gà", 380, 24.0, 48.0, 9.0, "Bữa sáng");
        registerDish("hu tieu nam vang", "Hủ tiếu Nam Vang", 480, 24.0, 58.0, 14.0, "Bữa sáng");
        registerDish("mi quang", "Mì Quảng", 490, 25.0, 58.0, 15.0, "Bữa sáng");
        registerDish("banh da cua", "Bánh đa cua", 460, 22.0, 58.0, 13.0, "Bữa sáng");

        registerDish("banh mi thit", "Bánh mì thịt nướng", 430, 18.0, 48.0, 17.0, "Bữa sáng");
        registerDish("banh mi pate", "Bánh mì pate trứng", 410, 16.0, 46.0, 16.0, "Bữa sáng");
        registerDish("banh mi trung", "Bánh mì trứng ốp la", 360, 14.0, 44.0, 13.0, "Bữa sáng");
        registerDish("banh mi chao", "Bánh mì chảo", 540, 26.0, 48.0, 24.0, "Bữa sáng");
        registerDish("banh mi cha", "Bánh mì chả lụa", 380, 16.0, 46.0, 14.0, "Bữa sáng");

        registerDish("xoi xeo", "Xôi xéo ruốc hành", 450, 12.0, 72.0, 13.0, "Bữa sáng");
        registerDish("xoi ga", "Xôi gà xé", 480, 24.0, 68.0, 12.0, "Bữa sáng");
        registerDish("xoi thit", "Xôi thịt kho trứng", 580, 24.0, 70.0, 22.0, "Bữa sáng");
        registerDish("xoi lac", "Xôi đậu phộng / lạc", 420, 11.0, 65.0, 12.0, "Bữa sáng");

        registerDish("banh cuon", "Bánh cuốn thịt chả", 390, 16.0, 52.0, 12.0, "Bữa sáng");
        registerDish("banh bao", "Bánh bao nhân thịt trứng cút", 340, 12.0, 46.0, 11.0, "Bữa sáng");
        registerDish("banh gio", "Bánh giò nóng", 320, 11.0, 40.0, 12.0, "Bữa sáng");

        // === 2. BỮA TRƯA & CƠM VĂN PHÒNG ===
        registerDish("com tam suon", "Cơm tấm sườn nướng", 620, 28.0, 76.0, 21.0, "Bữa trưa");
        registerDish("com tam suon bi cha", "Cơm tấm sườn bì chả", 780, 36.0, 84.0, 30.0, "Bữa trưa");
        registerDish("com ga xoi mo", "Cơm gà xối mỡ", 680, 32.0, 78.0, 26.0, "Bữa trưa");
        registerDish("com rang dua bo", "Cơm rang dưa bò", 650, 28.0, 75.0, 24.0, "Bữa trưa");
        registerDish("com rang thap cam", "Cơm rang thập cẩm", 580, 20.0, 72.0, 21.0, "Bữa trưa");
        registerDish("com thit kho tau", "Cơm thịt kho tàu trứng", 690, 26.0, 72.0, 28.0, "Bữa trưa");
        registerDish("com suon xao chua ngot", "Cơm sườn xào chua ngọt", 640, 26.0, 74.0, 23.0, "Bữa trưa");
        registerDish("com ca kho to", "Cơm cá kho tộ", 520, 28.0, 70.0, 12.0, "Bữa trưa");
        registerDish("com bo xao", "Cơm bò xào rau cần", 560, 30.0, 70.0, 14.0, "Bữa trưa");
        registerDish("com trang", "Cơm trắng (1 bát)", 200, 4.5, 45.0, 0.5, "Bữa trưa");
        registerDish("com gao lut", "Cơm gạo lứt (1 bát)", 180, 5.0, 38.0, 1.2, "Bữa trưa");

        registerDish("bun cha", "Bún chả Hà Nội", 580, 28.0, 68.0, 20.0, "Bữa trưa");
        registerDish("bun dau mam tom", "Bún đậu mắm tôm chả cốm", 650, 28.0, 70.0, 26.0, "Bữa trưa");
        registerDish("bun thit nuong", "Bún thịt nướng chả giò", 590, 26.0, 72.0, 19.0, "Bữa trưa");
        registerDish("mi xao bo", "Mì xào bò rau cải", 580, 26.0, 65.0, 22.0, "Bữa trưa");
        registerDish("mi xao hai san", "Mì xào hải sản", 540, 28.0, 64.0, 18.0, "Bữa trưa");

        // === 3. BỮA TỐI & HEALTHY / GYM / GIẢM CÂN ===
        registerDish("uc ga ap chao", "Ức gà áp chảo (150g)", 240, 42.0, 0.0, 6.0, "Bữa tối");
        registerDish("uc ga luoc", "Ức gà luộc (150g)", 210, 43.0, 0.0, 3.5, "Bữa tối");
        registerDish("salad ca hoi", "Salad cá hồi sốt mè rang", 340, 28.0, 12.0, 18.0, "Bữa tối");
        registerDish("salad uc ga", "Salad ức gà xà lách", 280, 32.0, 14.0, 8.0, "Bữa tối");
        registerDish("salad rau tron", "Salad rau củ trộn dầu giấm", 120, 3.0, 12.0, 6.0, "Bữa tối");
        registerDish("bo bit tet", "Bò bít tết (Beefsteak 150g)", 420, 36.0, 5.0, 27.0, "Bữa tối");
        registerDish("bo xao", "Thịt bò xào bông cải xanh", 310, 28.0, 10.0, 15.0, "Bữa tối");
        registerDish("ca hoi nuong", "Cá hồi nướng măng tây", 360, 32.0, 4.0, 22.0, "Bữa tối");
        registerDish("tom luoc", "Tôm hấp / luộc (150g)", 140, 30.0, 1.0, 1.5, "Bữa tối");
        registerDish("trung luoc", "Trứng gà luộc (2 quả)", 150, 13.0, 1.0, 10.5, "Bữa sáng");
        registerDish("trung op la", "Trứng ốp la (2 quả)", 190, 13.0, 1.5, 14.0, "Bữa sáng");
        registerDish("dau phu luoc", "Đậu phụ luộc (1 bìa)", 120, 12.0, 3.0, 6.0, "Bữa tối");
        registerDish("dau phu sot ca chua", "Đậu phụ sốt cà chua", 210, 14.0, 12.0, 11.0, "Bữa trưa");

        registerDish("canh chua ca", "Canh chua cá lóc", 180, 18.0, 12.0, 6.0, "Bữa tối");
        registerDish("canh cua rau day", "Canh cua mồng tơi rau đay", 120, 10.0, 8.0, 4.0, "Bữa trưa");
        registerDish("canh rau ngot thit bam", "Canh rau ngót thịt băm", 140, 12.0, 6.0, 6.0, "Bữa trưa");
        registerDish("rau muong xao toi", "Rau muống xào tỏi", 130, 3.0, 8.0, 9.0, "Bữa trưa");
        registerDish("rau cai luoc", "Rau cải thìa / bắp cải luộc", 50, 2.5, 9.0, 0.4, "Bữa tối");
        registerDish("khoai lang", "Khoai lang luộc (1 củ vừa)", 160, 2.5, 37.0, 0.2, "Bữa sáng");

        // === 4. TRÁI CÂY, ĂN VẶT & ĐỒ UỐNG ===
        registerDish("chuoi", "Chuối tiêu (1 quả vừa)", 105, 1.3, 27.0, 0.3, "Ăn vặt");
        registerDish("tao", "Táo tây (1 quả vừa)", 95, 0.5, 25.0, 0.3, "Ăn vặt");
        registerDish("cam", "Cam tươi (1 quả)", 65, 1.2, 16.0, 0.2, "Ăn vặt");
        registerDish("dua hau", "Dưa hấu (1 miếng lớn)", 80, 1.5, 20.0, 0.2, "Ăn vặt");
        registerDish("bo", "Quả bơ (1/2 quả)", 160, 2.0, 9.0, 15.0, "Ăn vặt");
        registerDish("xoai", "Xoài chín (1/2 quả)", 100, 1.0, 25.0, 0.5, "Ăn vặt");
        registerDish("oi", "Ổi chín (1 quả vừa)", 70, 2.5, 15.0, 0.8, "Ăn vặt");

        registerDish("sua chua khong duong", "Sữa chua không đường (1 hộp)", 65, 4.5, 6.0, 3.0, "Ăn vặt");
        registerDish("sua chua co duong", "Sữa chua có đường (1 hộp)", 105, 3.5, 15.0, 3.0, "Ăn vặt");
        registerDish("sua tuoi khong duong", "Sữa tươi tiệt trùng không đường (200ml)", 125, 6.5, 9.5, 6.5, "Bữa sáng");
        registerDish("whey protein", "Whey Protein Isolate (1 muỗng)", 120, 25.0, 2.0, 1.0, "Ăn vặt");
        registerDish("yen mach", "Yến mạch ngâm sữa (Oatmeal)", 240, 10.0, 42.0, 4.5, "Bữa sáng");
        registerDish("sinh to bo", "Sinh tố bơ sữa đặc", 320, 5.0, 42.0, 16.0, "Ăn vặt");
        registerDish("nuoc cam", "Nước cam tươi vắt nguyên chất", 90, 1.5, 21.0, 0.2, "Ăn vặt");

        registerDish("tra sua", "Trà sữa trân châu (ly vừa)", 380, 2.0, 68.0, 12.0, "Ăn vặt");
        registerDish("ca phe sua", "Cà phê sữa đá (nâu đá)", 160, 2.5, 26.0, 4.5, "Bữa sáng");
        registerDish("ca phe den", "Cà phê đen đá (ít đường)", 25, 0.3, 5.0, 0.1, "Bữa sáng");
        registerDish("che buoi", "Chè bưởi nước cốt dừa", 290, 3.0, 52.0, 8.0, "Ăn vặt");
        registerDish("banh ngot", "Bánh ngọt / bánh bông lan", 260, 5.0, 38.0, 10.0, "Ăn vặt");

        Collections.sort(SUGGESTIONS);
    }

    /**
     * Lấy toàn bộ danh sách tên món ăn mẫu để cấp cho AutoCompleteTextView
     */
    public static List<String> getAllFoodSuggestions() {
        return Collections.unmodifiableList(SUGGESTIONS);
    }

    /**
     * Tự động dự đoán dinh dưỡng theo tên món ăn do người dùng nhập.
     * Sử dụng thuật toán:
     * 1. Khớp từ khóa trực tiếp trong thư viện tri thức món ăn Việt.
     * 2. Khớp gần đúng (Fuzzy/Substring).
     * 3. Phân rã thành phần món ăn (Base tinh bột + Nguồn đạm + Phương pháp chế biến).
     */
    public static Prediction predict(String rawInput) {
        if (rawInput == null || rawInput.trim().isEmpty()) {
            return new Prediction(300, 15.0, 40.0, 8.0, "Bữa trưa", "Giá trị mặc định bữa ăn");
        }

        String inputNorm = normalize(rawInput.trim());

        // 1. Tìm khớp trực tiếp trong Database
        if (DISH_DATABASE.containsKey(inputNorm)) {
            DishData d = DISH_DATABASE.get(inputNorm);
            if (d != null) {
                return new Prediction(d.calories, d.protein, d.carbs, d.fat, d.mealType, "Nhận diện chính xác món: " + d.displayName);
            }
        }

        // 2. Tìm kiếm chứa trong (Sub-match) đối với các món trong danh bạ
        DishData bestMatch = null;
        int maxKeyLen = 0;
        for (Map.Entry<String, DishData> entry : DISH_DATABASE.entrySet()) {
            String key = entry.getKey();
            if (inputNorm.contains(key) || key.contains(inputNorm)) {
                if (key.length() > maxKeyLen) {
                    maxKeyLen = key.length();
                    bestMatch = entry.getValue();
                }
            }
        }
        if (bestMatch != null) {
            return new Prediction(bestMatch.calories, bestMatch.protein, bestMatch.carbs, bestMatch.fat, bestMatch.mealType,
                    "Ước tính theo món tương tự: " + bestMatch.displayName);
        }

        // 3. Phân tích ngữ nghĩa theo thành phần (Component Breakdown Algorithm)
        int estimatedCalories = 0;
        double estimatedProtein = 0;
        double estimatedCarbs = 0;
        double estimatedFat = 0;
        String mealType = "Bữa trưa";

        // A. Nền tảng tinh bột / Cơ sở món ăn
        if (inputNorm.contains("com") || inputNorm.contains("xoi")) {
            estimatedCalories += 250;
            estimatedCarbs += 50.0;
            estimatedProtein += 5.0;
            mealType = inputNorm.contains("xoi") ? "Bữa sáng" : "Bữa trưa";
        } else if (inputNorm.contains("pho") || inputNorm.contains("bun") || inputNorm.contains("hu tieu") || inputNorm.contains("mien") || inputNorm.contains("mi")) {
            estimatedCalories += 220;
            estimatedCarbs += 45.0;
            estimatedProtein += 6.0;
            mealType = (inputNorm.contains("pho") || inputNorm.contains("hu tieu")) ? "Bữa sáng" : "Bữa trưa";
        } else if (inputNorm.contains("banh mi") || inputNorm.contains("banh bao") || inputNorm.contains("banh cuon")) {
            estimatedCalories += 200;
            estimatedCarbs += 40.0;
            estimatedProtein += 5.0;
            mealType = "Bữa sáng";
        } else if (inputNorm.contains("salad") || inputNorm.contains("rau") || inputNorm.contains("canh")) {
            estimatedCalories += 60;
            estimatedCarbs += 8.0;
            estimatedProtein += 2.0;
            mealType = "Bữa tối";
        } else if (inputNorm.contains("tra sua") || inputNorm.contains("che") || inputNorm.contains("banh") || inputNorm.contains("kem")) {
            estimatedCalories += 280;
            estimatedCarbs += 55.0;
            estimatedFat += 6.0;
            mealType = "Ăn vặt";
        } else if (inputNorm.contains("sinh to") || inputNorm.contains("nuoc ep") || inputNorm.contains("sua")) {
            estimatedCalories += 150;
            estimatedCarbs += 28.0;
            estimatedProtein += 4.0;
            mealType = "Ăn vặt";
        } else if (inputNorm.contains("trai cay") || inputNorm.contains("chuoi") || inputNorm.contains("tao") || inputNorm.contains("cam") || inputNorm.contains("oi")) {
            estimatedCalories += 80;
            estimatedCarbs += 20.0;
            estimatedProtein += 1.0;
            mealType = "Ăn vặt";
        } else {
            // Nền tảng trung bình
            estimatedCalories += 150;
            estimatedCarbs += 25.0;
            estimatedProtein += 5.0;
        }

        // B. Nguồn Đạm (Protein Source)
        if (inputNorm.contains("bo")) {
            estimatedCalories += 180;
            estimatedProtein += 24.0;
            estimatedFat += 8.0;
        } else if (inputNorm.contains("uc ga") || inputNorm.contains("ga")) {
            estimatedCalories += 140;
            estimatedProtein += 26.0;
            estimatedFat += 3.5;
        } else if (inputNorm.contains("heo") || inputNorm.contains("lon") || inputNorm.contains("suon") || inputNorm.contains("ba chi")) {
            estimatedCalories += 220;
            estimatedProtein += 18.0;
            estimatedFat += 16.0;
        } else if (inputNorm.contains("ca") || inputNorm.contains("tom") || inputNorm.contains("muc") || inputNorm.contains("hai san") || inputNorm.contains("cua")) {
            estimatedCalories += 120;
            estimatedProtein += 22.0;
            estimatedFat += 2.5;
        } else if (inputNorm.contains("trung")) {
            estimatedCalories += 90;
            estimatedProtein += 7.0;
            estimatedFat += 6.0;
        } else if (inputNorm.contains("dau phu") || inputNorm.contains("dau hu") || inputNorm.contains("chay")) {
            estimatedCalories += 80;
            estimatedProtein += 8.0;
            estimatedFat += 4.0;
        }

        // C. Phương pháp nấu nướng (Cooking Method & Fat Content)
        if (inputNorm.contains("chien") || inputNorm.contains("ran") || inputNorm.contains("xao") || inputNorm.contains("sot vang") || inputNorm.contains("kho tau")) {
            estimatedCalories += 100;
            estimatedFat += 10.0;
        } else if (inputNorm.contains("kho") || inputNorm.contains("rim") || inputNorm.contains("nuong")) {
            estimatedCalories += 50;
            estimatedFat += 4.0;
        } else if (inputNorm.contains("luoc") || inputNorm.contains("hap")) {
            // Giữ nguyên calo thanh đạm
            estimatedFat += 0.5;
        } else {
            estimatedCalories += 40;
            estimatedFat += 4.0;
        }

        // Đảm bảo mức calo cân đối tối thiểu
        if (estimatedCalories < 80) estimatedCalories = 120;
        if (estimatedProtein < 2.0) estimatedProtein = 3.0;

        return new Prediction(estimatedCalories, estimatedProtein, estimatedCarbs, estimatedFat, mealType,
                "Dự đoán thông minh theo thành phần món ăn");
    }

    /**
     * Chuẩn hóa chuỗi tiếng Việt: loại bỏ dấu, chuyển về chữ thường, xóa ký tự đặc biệt
     */
    public static String normalize(String str) {
        if (str == null) return "";
        String nfd = Normalizer.normalize(str.toLowerCase(Locale.getDefault()), Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        String result = pattern.matcher(nfd).replaceAll("");
        result = result.replace('đ', 'd').replace('Đ', 'd');
        result = result.replaceAll("[^a-z0-9 ]", " ").replaceAll("\\s+", " ").trim();
        return result;
    }
}
