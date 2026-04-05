package com.example.bulbulyator;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseClient {

    private static final String TAG  = "SupabaseClient";
    private static final String BASE  = "https://varjvazplnphxbtiinex.supabase.co";
    private static final String URL   = BASE + "/rest/v1/";
    private static final String STORAGE_URL = BASE + "/storage/v1/object/";
    private static final String KEY  = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZhcmp2YXpwbG5waHhidGlpbmV4Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzM1MTc3NzcsImV4cCI6MjA4OTA5Mzc3N30.QgJfx-XsVzA9lAoKgTROIixnet1QICCzQF8tWM274O0";
    private static final MediaType JSON = MediaType.get("application/json");

    private static final OkHttpClient client = new OkHttpClient();

    /** GET /table?query */
    public static JSONArray get(String table, String query) {
        String url = URL + table + (query != null && !query.isEmpty() ? "?" + query : "");
        Request req = new Request.Builder()
                .url(url)
                .addHeader("apikey", KEY)
                .addHeader("Authorization", "Bearer " + KEY)
                .addHeader("Content-Type", "application/json")
                .get()
                .build();
        try (Response resp = client.newCall(req).execute()) {
            String body = resp.body().string();
            if (!resp.isSuccessful()) Log.e(TAG, "GET " + table + " -> " + resp.code() + ": " + body);
            return new JSONArray(body);
        } catch (Exception e) {
            Log.e(TAG, "GET " + table + " error", e);
            return new JSONArray();
        }
    }

    /** POST /table — insert, returns inserted row */
    public static JSONObject post(String table, JSONObject data) {
        Request req = new Request.Builder()
                .url(URL + table)
                .addHeader("apikey", KEY)
                .addHeader("Authorization", "Bearer " + KEY)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .post(RequestBody.create(data.toString(), JSON))
                .build();
        try (Response resp = client.newCall(req).execute()) {
            String body = resp.body().string();
            if (!resp.isSuccessful()) Log.e(TAG, "POST " + table + " -> " + resp.code() + ": " + body);
            JSONArray arr = new JSONArray(body);
            return arr.length() > 0 ? arr.getJSONObject(0) : null;
        } catch (Exception e) {
            Log.e(TAG, "POST " + table + " error", e);
            return null;
        }
    }

    /** PATCH /table?query — update */
    public static boolean patch(String table, String query, JSONObject data) {
        Request req = new Request.Builder()
                .url(URL + table + "?" + query)
                .addHeader("apikey", KEY)
                .addHeader("Authorization", "Bearer " + KEY)
                .addHeader("Content-Type", "application/json")
                .patch(RequestBody.create(data.toString(), JSON))
                .build();
        try (Response resp = client.newCall(req).execute()) {
            return resp.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    /** DELETE /table?query */
    public static boolean delete(String table, String query) {
        Request req = new Request.Builder()
                .url(URL + table + "?" + query)
                .addHeader("apikey", KEY)
                .addHeader("Authorization", "Bearer " + KEY)
                .addHeader("Content-Type", "application/json")
                .delete()
                .build();
        try (Response resp = client.newCall(req).execute()) {
            return resp.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Загружает файл в Supabase Storage.
     * @param bucket  имя бакета (например "avatars")
     * @param path    путь внутри бакета (например "42/avatar.jpg")
     * @param ctx     контекст для чтения URI
     * @param uri     локальный URI файла
     * @param mime    MIME-тип ("image/jpeg", "image/png" и т.д.)
     * @return публичный URL файла или null при ошибке
     */
    public static String uploadFile(String bucket, String path, Context ctx, Uri uri, String mime) {
        try {
            InputStream is = ctx.getContentResolver().openInputStream(uri);
            if (is == null) { Log.e("Storage", "InputStream is null for uri: " + uri); return null; }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int n;
            while ((n = is.read(chunk)) != -1) buffer.write(chunk, 0, n);
            is.close();
            byte[] bytes = buffer.toByteArray();
            Log.d("Storage", "Uploading " + bytes.length + " bytes to " + bucket + "/" + path);

            // upsert=true — перезаписывает если файл уже есть
            Request req = new Request.Builder()
                    .url(STORAGE_URL + bucket + "/" + path)
                    .addHeader("apikey", KEY)
                    .addHeader("Authorization", "Bearer " + KEY)
                    .addHeader("x-upsert", "true")
                    .put(RequestBody.create(bytes, MediaType.get(mime)))
                    .build();

            try (Response resp = client.newCall(req).execute()) {
                String body = resp.body() != null ? resp.body().string() : "";
                Log.d("Storage", "Response " + resp.code() + ": " + body);
                if (!resp.isSuccessful()) return null;
                // Возвращаем публичный URL
                return BASE + "/storage/v1/object/public/" + bucket + "/" + path;
            }
        } catch (Exception e) {
            Log.e("Storage", "uploadFile error", e);
            return null;
        }
    }
}
