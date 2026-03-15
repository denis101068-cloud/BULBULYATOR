package com.example.bulbulyator;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Order {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user_id")
    public int userId;

    @ColumnInfo(name = "product_id")
    public int productId;

    @ColumnInfo(name = "product_name")
    public String productName;

    @ColumnInfo(name = "product_price")
    public double productPrice;

    @ColumnInfo(name = "status")
    public String status; // "В пути", "Доставлен", "Обрабатывается"

    @ColumnInfo(name = "order_date")
    public long orderDate;
}
