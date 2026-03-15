package com.example.bulbulyator;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteDao {
    @Query("SELECT * FROM favorite WHERE user_id = :userId")
    List<Favorite> getUserFavorites(int userId);

    @Query("SELECT * FROM product WHERE id IN (SELECT product_id FROM favorite WHERE user_id = :userId)")
    List<Product> getUserFavoriteProducts(int userId);

    @Query("SELECT * FROM favorite WHERE user_id = :userId AND product_id = :productId LIMIT 1")
    Favorite findFavorite(int userId, int productId);

    @Insert
    void insert(Favorite favorite);

    @Query("DELETE FROM favorite WHERE user_id = :userId AND product_id = :productId")
    void delete(int userId, int productId);
}
