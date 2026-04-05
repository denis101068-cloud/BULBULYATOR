package com.example.bulbulyator;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Locale;

public class CheckoutActivity extends BaseActivity {

    private TextInputEditText cityInput, streetInput, houseInput, apartmentInput;
    private TextInputEditText cardNumberInput, cardExpiryInput, cardCvvInput;
    private TextView totalPriceText, productNameView, productPriceView;
    private ImageView productImage;

    private SupabaseDb db;
    private SharedPreferences prefs;
    private int userId, productId;
    private String productNameStr, productImageUrl;
    private double productPriceVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db     = SupabaseDb.getInstance();
        prefs  = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        productId       = getIntent().getIntExtra("productId", -1);
        productNameStr  = getIntent().getStringExtra("productName");
        productPriceVal = getIntent().getDoubleExtra("productPrice", 0);
        productImageUrl = getIntent().getStringExtra("productImageUrl");

        cityInput       = findViewById(R.id.cityInput);
        streetInput     = findViewById(R.id.streetInput);
        houseInput      = findViewById(R.id.houseInput);
        apartmentInput  = findViewById(R.id.apartmentInput);
        cardNumberInput = findViewById(R.id.cardNumberInput);
        cardExpiryInput = findViewById(R.id.cardExpiryInput);
        cardCvvInput    = findViewById(R.id.cardCvvInput);
        totalPriceText  = findViewById(R.id.totalPriceText);
        productNameView = findViewById(R.id.checkoutProductName);
        productPriceView= findViewById(R.id.checkoutProductPrice);
        productImage    = findViewById(R.id.checkoutProductImage);

        productNameView.setText(productNameStr);
        String priceStr = NumberFormat.getInstance(new Locale("ru", "RU")).format(productPriceVal) + " ₽";
        productPriceView.setText(priceStr);
        totalPriceText.setText(priceStr);

        if (productImageUrl != null) {
            if (productImageUrl.startsWith("http")) Glide.with(this).load(productImageUrl).centerCrop().into(productImage);
            else { try { Glide.with(this).load(Uri.parse(productImageUrl)).centerCrop().into(productImage); } catch (Exception e) { productImage.setImageResource(R.drawable.ic_launcher_background); } }
        }

        setupCardFormatting();
        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        findViewById(R.id.confirmOrderButton).setOnClickListener(v -> confirmOrder());
    }

    @Override
    protected void onResume() { super.onResume(); applyTheme(); }

    private void setupCardFormatting() {
        cardNumberInput.addTextChangedListener(new TextWatcher() {
            boolean fmt;
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {
                if (fmt) return; fmt = true;
                String d = s.toString().replaceAll(" ", "");
                if (d.length() > 16) d = d.substring(0, 16);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < d.length(); i++) { if (i > 0 && i % 4 == 0) sb.append(" "); sb.append(d.charAt(i)); }
                s.replace(0, s.length(), sb.toString()); fmt = false;
            }
        });
        cardExpiryInput.addTextChangedListener(new TextWatcher() {
            boolean fmt;
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            public void onTextChanged(CharSequence s, int a, int b, int c) {}
            public void afterTextChanged(Editable s) {
                if (fmt) return; fmt = true;
                String d = s.toString().replaceAll("/", "");
                if (d.length() > 4) d = d.substring(0, 4);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < d.length(); i++) { if (i == 2) sb.append("/"); sb.append(d.charAt(i)); }
                s.replace(0, s.length(), sb.toString()); fmt = false;
            }
        });
    }

    private void confirmOrder() {
        String city   = cityInput.getText().toString().trim();
        String street = streetInput.getText().toString().trim();
        String house  = houseInput.getText().toString().trim();
        String apt    = apartmentInput.getText().toString().trim();
        String card   = cardNumberInput.getText().toString().replaceAll(" ", "");
        String expiry = cardExpiryInput.getText().toString().trim();
        String cvv    = cardCvvInput.getText().toString().trim();

        if (city.isEmpty() || street.isEmpty() || house.isEmpty()) { Toast.makeText(this, "Заполните адрес доставки", Toast.LENGTH_SHORT).show(); return; }
        if (card.length() < 16) { Toast.makeText(this, "Введите корректный номер карты", Toast.LENGTH_SHORT).show(); return; }
        if (expiry.length() < 5) { Toast.makeText(this, "Введите срок действия карты", Toast.LENGTH_SHORT).show(); return; }
        if (cvv.length() < 3) { Toast.makeText(this, "Введите CVV/CVC код", Toast.LENGTH_SHORT).show(); return; }

        Order order = new Order();
        order.userId       = userId;
        order.productId    = productId;
        order.productName  = productNameStr;
        order.productPrice = productPriceVal;
        order.status       = "В пути";
        order.orderDate    = System.currentTimeMillis();

        String maskedCard = "**** **** **** " + card.substring(12);
        String address = city + ", " + street + ", д." + house + (apt.isEmpty() ? "" : ", кв." + apt);

        new Thread(() -> {
            db.orderDao().insert(order);
            runOnUiThread(() -> {
                Toast.makeText(this, "Заказ оформлен!\nДоставка: " + address + "\nКарта: " + maskedCard, Toast.LENGTH_LONG).show();
                finish();
            });
        }).start();
    }
}
