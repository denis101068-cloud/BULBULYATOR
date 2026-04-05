package com.example.bulbulyator;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class SupabaseUserDao {

    private static final String TAG = "UserDao";

    private User fromJson(JSONObject o) {
        try {
            User u = new User();
            u.uid       = o.getInt("id");
            u.firstName = o.optString("first_name", "");
            u.email     = o.optString("email", "");
            u.password  = o.optString("password", "");
            u.avatarUri = o.optString("avatar_uri", "");
            u.bannerUri = o.optString("banner_uri", "");
            u.bio       = o.optString("bio", "");
            return u;
        } catch (Exception e) {
            Log.e(TAG, "fromJson error", e);
            return null;
        }
    }

    private String enc(String s) {
        try { return URLEncoder.encode(s, StandardCharsets.UTF_8.name()); }
        catch (Exception e) { return s; }
    }

    public User findByEmail(String email) {
        try {
            JSONArray arr = SupabaseClient.get("users1", "email=eq." + enc(email) + "&limit=1");
            Log.d(TAG, "findByEmail(" + email + ") -> " + arr.length() + " rows");
            if (arr.length() > 0) return fromJson(arr.getJSONObject(0));
        } catch (Exception e) { Log.e(TAG, "findByEmail error", e); }
        return null;
    }

    public User getById(int userId) {
        try {
            JSONArray arr = SupabaseClient.get("users1", "id=eq." + userId + "&limit=1");
            if (arr.length() > 0) return fromJson(arr.getJSONObject(0));
        } catch (Exception e) { Log.e(TAG, "getById error", e); }
        return null;
    }

    public User login(String email, String password) {
        try {
            JSONArray arr = SupabaseClient.get("users1",
                    "email=eq." + enc(email) + "&password=eq." + password + "&limit=1");
            Log.d(TAG, "login(" + email + ") -> " + arr.length() + " rows");
            if (arr.length() > 0) return fromJson(arr.getJSONObject(0));
        } catch (Exception e) { Log.e(TAG, "login error", e); }
        return null;
    }

    public User insertAndReturn(User user) {
        try {
            JSONObject data = new JSONObject();
            data.put("first_name", user.firstName);
            data.put("email", user.email);
            data.put("password", user.password);
            if (user.bio != null) data.put("bio", user.bio);
            Log.d(TAG, "insertAndReturn: " + data);
            JSONObject result = SupabaseClient.post("users1", data);
            Log.d(TAG, "insertAndReturn result: " + result);
            if (result != null) return fromJson(result);
        } catch (Exception e) { Log.e(TAG, "insertAndReturn error", e); }
        return null;
    }

    public void update(User user) {
        try {
            JSONObject data = new JSONObject();
            data.put("first_name", user.firstName);
            data.put("bio", user.bio != null ? user.bio : "");
            data.put("avatar_uri", user.avatarUri != null ? user.avatarUri : "");
            data.put("banner_uri", user.bannerUri != null ? user.bannerUri : "");
            SupabaseClient.patch("users1", "id=eq." + user.uid, data);
        } catch (Exception e) { Log.e(TAG, "update error", e); }
    }
}
