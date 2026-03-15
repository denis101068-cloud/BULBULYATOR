package com.example.bulbulyator;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageUtils {
    public static void load(Context ctx, String url, ImageView into) {
        if (url == null || url.isEmpty()) {
            into.setImageResource(R.drawable.ic_launcher_background);
            return;
        }
        if (url.startsWith("http")) {
            Glide.with(ctx).load(url)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop().into(into);
        } else {
            try {
                Glide.with(ctx).load(Uri.parse(url))
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .centerCrop().into(into);
            } catch (Exception e) {
                into.setImageResource(R.drawable.ic_launcher_background);
            }
        }
    }
}
