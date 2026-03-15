package com.example.bulbulyator;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface OrderDao {
    @Query("SELECT * FROM `order` WHERE user_id = :userId ORDER BY order_date DESC")
    List<Order> getUserOrders(int userId);

    @Query("SELECT * FROM `order` WHERE user_id = :userId AND status = :status ORDER BY order_date DESC")
    List<Order> getUserOrdersByStatus(int userId, String status);

    @Insert
    void insert(Order order);

    @Query("DELETE FROM `order` WHERE id = :orderId")
    void delete(int orderId);

    @Query("UPDATE `order` SET status = :status WHERE id = :orderId")
    void updateStatus(int orderId, String status);
}
