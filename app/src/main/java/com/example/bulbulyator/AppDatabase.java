package com.example.bulbulyator;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {User.class, Product.class, Order.class, Favorite.class}, version = 6, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();
    public abstract ProductDao productDao();
    public abstract OrderDao orderDao();
    public abstract FavoriteDao favoriteDao();

    private static AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context, AppDatabase.class, "database-name")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
            initializeData(INSTANCE);
        }
        return INSTANCE;
    }

    private static void initializeData(AppDatabase db) {
        if (db.productDao().getAll().isEmpty()) {
            Product p1 = new Product();
            p1.name = "Смартфон Samsung Galaxy S23";
            p1.description = "Современный смартфон с отличной камерой и мощным процессором Snapdragon 8 Gen 2";
            p1.price = 45990;
            p1.imageUrl = "https://images.unsplash.com/photo-1610945415295-d9bbf067e59c?w=400";
            p1.sellerId = 1;
            p1.sellerName = "TechStore";
            p1.category = "Смартфоны";

            Product p2 = new Product();
            p2.name = "Ноутбук ASUS ROG";
            p2.description = "Мощный игровой ноутбук с RTX 4070, 16GB RAM, 144Hz дисплей";
            p2.price = 89990;
            p2.imageUrl = "https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=400";
            p2.sellerId = 1;
            p2.sellerName = "TechStore";
            p2.category = "Ноутбуки";

            Product p3 = new Product();
            p3.name = "Наушники Sony WH-1000XM5";
            p3.description = "Беспроводные наушники с активным шумоподавлением, 30 часов работы";
            p3.price = 12990;
            p3.imageUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400";
            p3.sellerId = 2;
            p3.sellerName = "AudioShop";
            p3.category = "Аудио";

            db.productDao().insert(p1);
            db.productDao().insert(p2);
            db.productDao().insert(p3);
        }
    }
}
