package com.polaricemobile;

import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class SupabaseConfig {
    private static final String SUPABASE_URL = "https://enihpqdvczvkaoafmpib.supabase.co";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVuaWhwcWR2Y3p2a2FvYWZtcGliIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc5NjA5NDUsImV4cCI6MjA3MzUzNjk0NX0.lOq5-Xe9I2ThA49NLJpAHl-BtZ35bjZ183NNcCfCjQ8";
    private static final String AUTH_URL = SUPABASE_URL + "/auth/v1";
    private static final String REST_URL = SUPABASE_URL + "/rest/v1";

    private static OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(30, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS).build();
    private static Gson gson = new Gson();

    public static class AuthResponse { public User user; public String access_token; public String refresh_token; public String error; public String error_description; }
    public static class User { public String id; public String email; }
    public static class Delivery { public int id; public String cliente_nome; public String endereco_entrega; public String status; public String motorista_id; public int cliente_id; public int transacao_id; }

    public static AuthResponse signIn(String email, String password) throws IOException {
        JsonObject loginData = new JsonObject(); loginData.addProperty("email", email); loginData.addProperty("password", password);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), gson.toJson(loginData));
        Request request = new Request.Builder().url(AUTH_URL + "/token?grant_type=password").post(body).addHeader("apikey", SUPABASE_ANON_KEY).addHeader("Content-Type", "application/json").build();
        try (Response response = httpClient.newCall(request).execute()) { String responseBody = response.body().string(); return gson.fromJson(responseBody, AuthResponse.class); }
    }

    public static Delivery[] getPendingDeliveries(String motoristaId, String accessToken) throws IOException {
        String url = REST_URL + "/entregas?status=eq.pendente&motorista_id=eq." + motoristaId + "&select=*";
        Request request = new Request.Builder().url(url).get().addHeader("apikey", SUPABASE_ANON_KEY).addHeader("Authorization", "Bearer " + accessToken).addHeader("Content-Type", "application/json").build();
        try (Response response = httpClient.newCall(request).execute()) { String responseBody = response.body().string(); JsonArray jsonArray = gson.fromJson(responseBody, JsonArray.class); return gson.fromJson(jsonArray, Delivery[].class); }
    }

    public static boolean completeDelivery(int deliveryId, String accessToken) throws IOException {
        JsonObject updateData = new JsonObject(); updateData.addProperty("status", "concluído");
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), gson.toJson(updateData));
        String url = REST_URL + "/entregas?id=eq." + deliveryId;
        Request request = new Request.Builder().url(url).patch(body).addHeader("apikey", SUPABASE_ANON_KEY).addHeader("Authorization", "Bearer " + accessToken).addHeader("Content-Type", "application/json").addHeader("Prefer", "return=minimal").build();
        try (Response response = httpClient.newCall(request).execute()) { return response.isSuccessful(); }
    }

    public static boolean signOut(String accessToken) throws IOException {
        Request request = new Request.Builder().url(AUTH_URL + "/logout").post(RequestBody.create(MediaType.parse("application/json"), "{}")).addHeader("apikey", SUPABASE_ANON_KEY).addHeader("Authorization", "Bearer " + accessToken).addHeader("Content-Type", "application/json").build();
        try (Response response = httpClient.newCall(request).execute()) { return response.isSuccessful(); }
    }
}