package com.example.bulbulyator;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private View emptyView;
    private SupabaseDb db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        db     = SupabaseDb.getInstance();
        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("userId", -1);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());
        recyclerView = findViewById(R.id.ordersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefresh = findViewById(R.id.swipeRefresh);
        emptyView    = findViewById(R.id.emptyView);

        swipeRefresh.setColorSchemeColors(0xFFFFD700);
        swipeRefresh.setOnRefreshListener(() -> { loadOrders(); swipeRefresh.setRefreshing(false); });
        loadOrders();
    }

    @Override
    protected void onResume() { super.onResume(); applyTheme(); }

    private void loadOrders() {
        new Thread(() -> {
            List<Order> orders = db.orderDao().getUserOrders(userId);
            runOnUiThread(() -> {
                emptyView.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
                recyclerView.setVisibility(orders.isEmpty() ? View.GONE : View.VISIBLE);
                recyclerView.setAdapter(new OrderAdapter(orders));
            });
        }).start();
    }

    class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
        private final List<Order> orders;
        OrderAdapter(List<Order> o) { this.orders = o; }

        @NonNull public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false));
        }
        public void onBindViewHolder(@NonNull ViewHolder h, int pos) { h.bind(orders.get(pos), pos); }
        public int getItemCount() { return orders.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView orderProductName, orderPrice, orderDate, orderStatus;
            Button cancelButton;

            ViewHolder(View v) {
                super(v);
                orderProductName = v.findViewById(R.id.orderProductName);
                orderPrice       = v.findViewById(R.id.orderPrice);
                orderDate        = v.findViewById(R.id.orderDate);
                orderStatus      = v.findViewById(R.id.orderStatus);
                cancelButton     = v.findViewById(R.id.cancelButton);
            }

            void bind(Order order, int position) {
                ((androidx.cardview.widget.CardView) itemView).setCardBackgroundColor(ThemeManager.getCardBg(OrdersActivity.this));
                orderProductName.setTextColor(ThemeManager.getTextPrimary(OrdersActivity.this));
                orderPrice.setTextColor(ThemeManager.getGold());
                orderDate.setTextColor(ThemeManager.getTextSecondary(OrdersActivity.this));

                orderProductName.setText(order.productName);
                orderPrice.setText(NumberFormat.getInstance(new Locale("ru", "RU")).format(order.productPrice) + " ₽");
                orderDate.setText("Заказ от " + new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(new Date(order.orderDate)));
                orderStatus.setText(order.status);

                switch (order.status) {
                    case "Доставлен": orderStatus.setTextColor(Color.parseColor("#4CAF50")); orderStatus.setBackgroundColor(Color.parseColor("#E8F5E9")); break;
                    case "В пути":    orderStatus.setTextColor(Color.parseColor("#FF9800")); orderStatus.setBackgroundColor(Color.parseColor("#FFF3E0")); break;
                    default:          orderStatus.setTextColor(Color.parseColor("#9E9E9E")); orderStatus.setBackgroundColor(Color.parseColor("#F5F5F5")); break;
                }

                cancelButton.setVisibility("В пути".equals(order.status) ? View.VISIBLE : View.GONE);
                cancelButton.setOnClickListener(v -> new AlertDialog.Builder(OrdersActivity.this)
                        .setTitle("Отмена заказа")
                        .setMessage("Отменить заказ \"" + order.productName + "\"?")
                        .setPositiveButton("Отменить", (d, w) -> new Thread(() -> {
                            db.orderDao().delete(order.id);
                            runOnUiThread(() -> { orders.remove(position); notifyDataSetChanged();
                                if (orders.isEmpty()) { emptyView.setVisibility(View.VISIBLE); recyclerView.setVisibility(View.GONE); }
                            });
                        }).start())
                        .setNegativeButton("Нет", null).show());
            }
        }
    }
}
