package com.example.bulbulyator;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ProductDao {
    @Query("SELECT * FROM product")
    List<Product> getAll();

    @Query("SELECT * FROM product WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<Product> search(String query);

    @Query("SELECT DISTINCT category FROM product WHERE category IS NOT NULL AND category != ''")
    List<String> getDistinctCategories();

    @Query("SELECT * FROM product WHERE category = :category")
    List<Product> getByCategory(String category);

    @Query("SELECT * FROM product WHERE id = :productId")
    Product getById(int productId);

    @Insert
    void insert(Product product);

    @Delete
    void delete(Product product);
}
