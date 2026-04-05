package com.example.bulbulyator;

/**
 * Замена AppDatabase — все запросы идут в Supabase.
 * Интерфейс совместим: db.userDao(), db.productDao(), db.orderDao(), db.favoriteDao()
 */
public class SupabaseDb {

    private static SupabaseDb INSTANCE;

    private final SupabaseUserDao    userDao    = new SupabaseUserDao();
    private final SupabaseProductDao productDao = new SupabaseProductDao();
    private final SupabaseOrderDao   orderDao   = new SupabaseOrderDao();
    private final SupabaseFavoriteDao favoriteDao = new SupabaseFavoriteDao();

    public static SupabaseDb getInstance() {
        if (INSTANCE == null) INSTANCE = new SupabaseDb();
        return INSTANCE;
    }

    public SupabaseUserDao    userDao()    { return userDao; }
    public SupabaseProductDao productDao() { return productDao; }
    public SupabaseOrderDao   orderDao()   { return orderDao; }
    public SupabaseFavoriteDao favoriteDao() { return favoriteDao; }
}
