package com.suckhoe.app.data.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suckhoe.app.util.PreferenceManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SupabaseClient {
    private static Retrofit retrofit = null;
    private static SupabaseApi supabaseApi = null;
    private static String cachedUrl = "";
    private static String cachedKey = "";

    public static synchronized SupabaseApi getApi(Context context) {
        PreferenceManager pref = new PreferenceManager(context);
        String baseUrl = pref.getSupabaseUrl();
        String apiKey = pref.getSupabaseKey();

        // Đảm bảo baseUrl kết thúc bằng /rest/v1/
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        if (!baseUrl.endsWith("rest/v1/")) {
            baseUrl += "rest/v1/";
        }

        // Tái sử dụng hoặc tạo mới nếu thay đổi URL/Key
        if (supabaseApi == null || !baseUrl.equals(cachedUrl) || !apiKey.equals(cachedKey)) {
            cachedUrl = baseUrl;
            cachedKey = apiKey;

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            Request.Builder requestBuilder = original.newBuilder()
                                    .header("Content-Type", "application/json")
                                    .header("Prefer", "return=representation");

                            if (cachedKey != null && !cachedKey.trim().isEmpty()) {
                                requestBuilder.header("apikey", cachedKey.trim());
                                requestBuilder.header("Authorization", "Bearer " + cachedKey.trim());
                            }

                            return chain.proceed(requestBuilder.build());
                        }
                    })
                    .addInterceptor(logging)
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            supabaseApi = retrofit.create(SupabaseApi.class);
        }

        return supabaseApi;
    }

    public static boolean hasValidApiKey(Context context) {
        PreferenceManager pref = new PreferenceManager(context);
        String key = pref.getSupabaseKey();
        return key != null && !key.trim().isEmpty();
    }
}

