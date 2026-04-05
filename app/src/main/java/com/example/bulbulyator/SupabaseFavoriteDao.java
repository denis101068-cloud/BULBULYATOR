package com.example.bulbulyator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SupabaseFavoriteDao {

    public List<Product> getUserFavoriteProducts(int userId) {
        // Получаем product_id из favorites1
        JSONArray favArr = SupabaseClient.get("favorites1",
                "user_id=eq." + userId + "&select=product_id");
        if (favArr.length() == 0) return new ArrayList<>();

        // Собираем список id через in=(1,2,3)
        StringBuilder ids = new StringBuilder();
        for (int i = 0; i < favArr.length(); i++) {
            try {
                if (i > 0) ids.append(",");
                ids.append(favArr.getJSONObject(i).getInt("product_id"));
            } catch (Exception ignored) {}
        }

        JSONArray prodArr = SupabaseClient.get("products1", "id=in.(" + ids + ")");
        List<Product> list = new ArrayList<>();
        for (int i = 0; i < prodArr.length(); i++) {
            try {
                Product p = new Product();
                JSONObject o = prodArr.getJSONObject(i);
                p.id          = o.getInt("id");
                p.name        = o.optString("name", "");
                p.description = o.optString("description", "");
                p.price       = o.optDouble("price", 0);
                p.imageUrl    = o.optString("image_url", "");
                p.category    = o.optString("category", "");
                p.sellerId    = o.optInt("seller_id", 0);
                p.sellerName  = o.optString("seller_name", "");
                list.add(p);
            } catch (Exception ignored) {}
        }
        return list;
    }

    public Favorite findFavorite(int userId, int productId) {
        JSONArray arr = SupabaseClient.get("favorites1",
                "user_id=eq." + userId + "&product_id=eq." + productId + "&limit=1");
        if (arr.length() > 0) {
            try {
                Favorite f = new Favorite();
                f.id        = arr.getJSONObject(0).getInt("id");
                f.userId    = userId;
                f.productId = productId;
                return f;
            } catch (Exception ignored) {}
        }
        return null;
    }

    public void insert(Favorite fav) {
        try {
            JSONObject data = new JSONObject();
            data.put("user_id", fav.userId);
            data.put("product_id", fav.productId);
            SupabaseClient.post("favorites1", data);
        } catch (Exception ignored) {}
    }

    public void delete(int userId, int productId) {
        SupabaseClient.delete("favorites1",
                "user_id=eq." + userId + "&product_id=eq." + productId);
    }
}
