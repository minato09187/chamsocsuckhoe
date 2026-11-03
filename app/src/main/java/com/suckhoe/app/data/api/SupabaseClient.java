package com.suckhoe.app.data.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
    // Cấu hình kết nối trực tiếp cố định vào hệ thống CSDL Supabase
    public static final String SUPABASE_BASE_URL = "https://rtqmvnhadjetpiategzi.supabase.co/rest/v1/";
    public static final String SUPABASE_API_KEY = "sb_publishable_INOKRi1sm3-Q_Fh05oYVzg_mRGYOI_Q";

    private static Retrofit retrofit = null;
    private static SupabaseApi supabaseApi = null;

    public static synchronized SupabaseApi getApi(Context context) {
        if (supabaseApi == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(new Interceptor() {
                        @Override
                        public Response intercept(Chain chain) throws IOException {
                            Request original = chain.request();
                            Request request = original.newBuilder()
                                    .header("Content-Type", "application/json")
                                    .header("Prefer", "return=representation")
                                    .header("apikey", SUPABASE_API_KEY)
                                    .header("Authorization", "Bearer " + SUPABASE_API_KEY)
                                    .build();
                            return chain.proceed(request);
                        }
                    })
                    .addInterceptor(logging)
                    .build();

            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(SUPABASE_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();

            supabaseApi = retrofit.create(SupabaseApi.class);
        }

        return supabaseApi;
    }

    public static boolean hasValidApiKey(Context context) {
        return true;
    }
}
