package com.example.bulbulyator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SupabaseProductDao {

    private Product fromJson(JSONObject o) {
        try {
            Product p = new Product();
            p.id          = o.getInt("id");
            p.name        = o.optString("name", "");
            p.description = o.optString("description", "");
            p.price       = o.optDouble("price", 0);
            p.imageUrl    = o.optString("image_url", "");
            p.category    = o.optString("category", "");
            p.sellerId    = o.optInt("seller_id", 0);
            p.sellerName  = o.optString("seller_name", "");
            return p;
        } catch (Exception e) { return null; }
    }

    private List<Product> toList(JSONArray arr) {
        List<Product> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            try { Product p = fromJson(arr.getJSONObject(i)); if (p != null) list.add(p); }
            catch (Exception ignored) {}
        }
        return list;
    }

    public List<Product> getAll() {
        return toList(SupabaseClient.get("products1", "order=id.asc"));
    }

    public Product getById(int id) {
        try {
            JSONArray arr = SupabaseClient.get("products1", "id=eq." + id + "&limit=1");
            if (arr.length() > 0) return fromJson(arr.getJSONObject(0));
        } catch (Exception ignored) {}
        return null;
    }

    public List<Product> search(String query) {
        // Supabase ilike для поиска
        String q = "or=(name.ilike.*" + query + "*,description.ilike.*" + query + "*)";
        return toList(SupabaseClient.get("products1", q));
    }

    public List<Product> getByCategory(String category) {
        return toList(SupabaseClient.get("products1", "category=eq." + category));
    }

    public List<String> getDistinctCategories() {
        JSONArray arr = SupabaseClient.get("products1", "select=category&order=category.asc");
        List<String> cats = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            try {
                String cat = arr.getJSONObject(i).optString("category", "");
                if (!cat.isEmpty() && !cats.contains(cat)) cats.add(cat);
            } catch (Exception ignored) {}
        }
        return cats;
    }

    public void updateImageUrl(int id, String imageUrl) {
        try {
            JSONObject data = new JSONObject();
            data.put("image_url", imageUrl);
            SupabaseClient.patch("products1", "id=eq." + id, data);
        } catch (Exception ignored) {}
    }

    public void deleteBySeller(int sellerId) {
        SupabaseClient.delete("products1", "seller_id=eq." + sellerId);
    }

    public void insert(Product p) {
        try {
            JSONObject data = new JSONObject();
            data.put("name", p.name);
            data.put("description", p.description);
            data.put("price", p.price);
            data.put("image_url", p.imageUrl != null ? p.imageUrl : "");
            data.put("category", p.category != null ? p.category : "");
            data.put("seller_id", p.sellerId);
            data.put("seller_name", p.sellerName != null ? p.sellerName : "");
            SupabaseClient.post("products1", data);
        } catch (Exception ignored) {}
    }
}
