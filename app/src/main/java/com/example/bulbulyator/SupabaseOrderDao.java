package com.example.bulbulyator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SupabaseOrderDao {

    private Order fromJson(JSONObject o) {
        try {
            Order order = new Order();
            order.id           = o.getInt("id");
            order.userId       = o.optInt("user_id", 0);
            order.productId    = o.optInt("product_id", 0);
            order.productName  = o.optString("product_name", "");
            order.productPrice = o.optDouble("product_price", 0);
            order.status       = o.optString("status", "В пути");
            order.orderDate    = o.optLong("order_date", 0);
            return order;
        } catch (Exception e) { return null; }
    }

    public List<Order> getUserOrders(int userId) {
        JSONArray arr = SupabaseClient.get("orders1",
                "user_id=eq." + userId + "&order=order_date.desc");
        List<Order> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            try { Order o = fromJson(arr.getJSONObject(i)); if (o != null) list.add(o); }
            catch (Exception ignored) {}
        }
        return list;
    }

    public void insert(Order order) {
        try {
            JSONObject data = new JSONObject();
            data.put("user_id", order.userId);
            data.put("product_id", order.productId);
            data.put("product_name", order.productName);
            data.put("product_price", order.productPrice);
            data.put("status", order.status);
            data.put("order_date", order.orderDate);
            SupabaseClient.post("orders1", data);
        } catch (Exception ignored) {}
    }

    public void delete(int orderId) {
        SupabaseClient.delete("orders1", "id=eq." + orderId);
    }
}
