package com.example.bulbulyator;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface UserDao {
    @Query("SELECT * FROM user")
    List<User> getAll();

    @Query("SELECT * FROM user WHERE email = :email LIMIT 1")
    User findByEmail(String email);

    @Query("SELECT * FROM user WHERE uid = :userId LIMIT 1")
    User getById(int userId);

    @Query("SELECT * FROM user WHERE email = :email AND password = :password LIMIT 1")
    User login(String email, String password);

    @Insert
    void insertAll(User... users);

    @Update
    void update(User user);

    @Delete
    void delete(User user);
}
