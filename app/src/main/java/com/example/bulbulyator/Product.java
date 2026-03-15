package com.example.bulbulyator;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Product {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "price")
    public double price;

    @ColumnInfo(name = "image_url")
    public String imageUrl;

    @ColumnInfo(name = "seller_id")
    public int sellerId;

    @ColumnInfo(name = "seller_name")
    public String sellerName;

    @ColumnInfo(name = "category")
    public String category;
}
