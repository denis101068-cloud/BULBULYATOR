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

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {

    private TextInputEditText cityInput, streetInput, houseInput, apartmentInput;
    private TextInputEditText cardNumberInput, cardExpiryInput, cardCvvInput;
    private Button confirmButton;
    private TextView totalPriceText, productNameView, productPriceView;
    private ImageView productImage;

    private AppDatabase db;
    private SharedPreferences prefs;
    private int userId;

    private int productId;
    private String productNameStr;
    private double productPriceVal;
    private String productImageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = AppDatabase.getInstance(this);
        prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        productId       = getIntent().getIntExtra("productId", -1);
        productNameStr  = getIntent().getStringExtra("productName");
        productPriceVal = getIntent().getDoubleExtra("productPrice", 0);
        productImageUrl = getIntent().getStringExtra("productImageUrl");

        // Views
        cityInput       = findViewById(R.id.cityInput);
        streetInput     = findViewById(R.id.streetInput);
        houseInput      = findViewById(R.id.houseInput);
        apartmentInput  = findViewById(R.id.apartmentInput);
        cardNumberInput = findViewById(R.id.cardNumberInput);
        cardExpiryInput = findViewById(R.id.cardExpiryInput);
        cardCvvInput    = findViewById(R.id.cardCvvInput);
        confirmButton   = findViewById(R.id.confirmOrderButton);
        totalPriceText  = findViewById(R.id.totalPriceText);
        productNameView = findViewById(R.id.checkoutProductName);
        productPriceView= findViewById(R.id.checkoutProductPrice);
        productImage    = findViewById(R.id.checkoutProductImage);

        // Заполняем данные товара
        productNameView.setText(productNameStr);
        NumberFormat fmt = NumberFormat.getInstance(new Locale("ru", "RU"));
        String priceStr = fmt.format(productPriceVal) + " ₽";
        productPriceView.setText(priceStr);
        totalPriceText.setText(priceStr);

        // Загружаем изображение
        if (productImageUrl != null) {
            if (productImageUrl.startsWith("http")) {
                Glide.with(this).load(productImageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .centerCrop().into(productImage);
            } else {
                try {
                    Glide.with(this).load(Uri.parse(productImageUrl))
                            .placeholder(R.drawable.ic_launcher_background)
                            .centerCrop().into(productImage);
                } catch (Exception e) {
                    productImage.setImageResource(R.drawable.ic_launcher_background);
                }
            }
        }

        setupCardFormatting();

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        confirmButton.setOnClickListener(v -> confirmOrder());
    }

    // Форматирование номера карты: 1234 5678 9012 3456
    private void setupCardFormatting() {
        cardNumberInput.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                // Убираем все пробелы
                String digits = s.toString().replaceAll(" ", "");
                if (digits.length() > 16) digits = digits.substring(0, 16);

                // Добавляем пробелы каждые 4 цифры
                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i > 0 && i % 4 == 0) formatted.append(" ");
                    formatted.append(digits.charAt(i));
                }

                s.replace(0, s.length(), formatted.toString());
                isFormatting = false;
            }
        });

        // Форматирование срока: MM/YY
        cardExpiryInput.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (isFormatting) return;
                isFormatting = true;

                String digits = s.toString().replaceAll("/", "");
                if (digits.length() > 4) digits = digits.substring(0, 4);

                StringBuilder formatted = new StringBuilder();
                for (int i = 0; i < digits.length(); i++) {
                    if (i == 2) formatted.append("/");
                    formatted.append(digits.charAt(i));
                }

                s.replace(0, s.length(), formatted.toString());
                isFormatting = false;
            }
        });
    }

    private void confirmOrder() {
        String city      = cityInput.getText().toString().trim();
        String street    = streetInput.getText().toString().trim();
        String house     = houseInput.getText().toString().trim();
        String apartment = apartmentInput.getText().toString().trim();
        String cardNum   = cardNumberInput.getText().toString().replaceAll(" ", "");
        String expiry    = cardExpiryInput.getText().toString().trim();
        String cvv       = cardCvvInput.getText().toString().trim();

        // Валидация адреса
        if (city.isEmpty() || street.isEmpty() || house.isEmpty()) {
            Toast.makeText(this, "Заполните адрес доставки", Toast.LENGTH_SHORT).show();
            return;
        }

        // Валидация карты
        if (cardNum.length() < 16) {
            Toast.makeText(this, "Введите корректный номер карты (16 цифр)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (expiry.length() < 5) {
            Toast.makeText(this, "Введите срок действия карты (ММ/ГГ)", Toast.LENGTH_SHORT).show();
            return;
        }
        if (cvv.length() < 3) {
            Toast.makeText(this, "Введите CVV/CVC код (3 цифры)", Toast.LENGTH_SHORT).show();
            return;
        }

        // Создаём заказ
        Order order = new Order();
        order.userId       = userId;
        order.productId    = productId;
        order.productName  = productNameStr;
        order.productPrice = productPriceVal;
        order.status       = "В пути";
        order.orderDate    = System.currentTimeMillis();
        db.orderDao().insert(order);

        // Маскируем номер карты для отображения
        String maskedCard = "**** **** **** " + cardNum.substring(12);

        Toast.makeText(this,
                "Заказ оформлен!\n" +
                "Доставка: " + city + ", " + street + ", д." + house +
                (apartment.isEmpty() ? "" : ", кв." + apartment) + "\n" +
                "Карта: " + maskedCard,
                Toast.LENGTH_LONG).show();

        finish();
    }
}
