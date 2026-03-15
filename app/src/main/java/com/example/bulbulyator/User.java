package com.example.bulbulyator;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class User {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "first_name")
    public String firstName;

    @ColumnInfo(name = "last_name")
    public String lastName;

    @ColumnInfo(name = "email")
    public String email;

    @ColumnInfo(name = "password")
    public String password;

    @ColumnInfo(name = "avatar_uri")
    public String avatarUri;

    @ColumnInfo(name = "banner_uri")
    public String bannerUri;

    @ColumnInfo(name = "bio")
    public String bio;
}

