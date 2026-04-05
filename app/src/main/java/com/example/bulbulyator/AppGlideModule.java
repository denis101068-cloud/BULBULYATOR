//package com.example.bulbulyator;
//
//import android.content.Context;
//
//import androidx.annotation.NonNull;
//
//import com.bumptech.glide.Glide;
//import com.bumptech.glide.GlideBuilder;
//import com.bumptech.glide.Registry;
//import com.bumptech.glide.annotation.GlideModule;
//import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
//import com.bumptech.glide.load.model.GlideUrl;
//import com.bumptech.glide.module.AppGlideModule;
//
//import java.io.InputStream;
//
//import okhttp3.OkHttpClient;
//
//@GlideModule
//public class AppGlideModule extends com.bumptech.glide.module.AppGlideModule {
//
//    @Override
//    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
//        // OkHttp автоматически следует редиректам (picsum.photos и др.)
//        OkHttpClient client = new OkHttpClient.Builder()
//                .followRedirects(true)
//                .followSslRedirects(true)
//                .build();
//        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
//    }
//}
