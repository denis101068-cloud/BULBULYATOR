package com.example.bulbulyator;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrdersActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefresh;
    private TextView emptyView;
    private AppDatabase db;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        db = AppDatabase.getInstance(this);
        userId = getSharedPreferences("UserPrefs", MODE_PRIVATE).getInt("userId", -1);

        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.ordersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        swipeRefresh = findViewById(R.id.swipeRefresh);
        emptyView = findViewById(R.id.emptyView);

        swipeRefresh.setColorSchemeColors(0xFFCB11AB);
        swipeRefresh.setOnRefreshListener(() -> {
            loadOrders();
            swipeRefresh.setRefreshing(false);
        });

        loadOrders();
    }

    private void loadOrders() {
        List<Order> orders = db.orderDao().getUserOrders(userId);
        emptyView.setVisibility(orders.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(orders.isEmpty() ? View.GONE : View.VISIBLE);
        recyclerView.setAdapter(new OrderAdapter(orders));
    }

    class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {
        private List<Order> orders;

        OrderAdapter(List<Order> orders) { this.orders = orders; }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(orders.get(position), position);
        }

        @Override
        public int getItemCount() { return orders.size(); }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView orderProductName, orderPrice, orderDate, orderStatus;
            Button cancelButton;

            ViewHolder(View itemView) {
                super(itemView);
                orderProductName = itemView.findViewById(R.id.orderProductName);
                orderPrice = itemView.findViewById(R.id.orderPrice);
                orderDate = itemView.findViewById(R.id.orderDate);
                orderStatus = itemView.findViewById(R.id.orderStatus);
                cancelButton = itemView.findViewById(R.id.cancelButton);
            }

            void bind(Order order, int position) {
                orderProductName.setText(order.productName);
                NumberFormat fmt = NumberFormat.getInstance(new Locale("ru", "RU"));
                orderPrice.setText(fmt.format(order.productPrice) + " ₽");
                SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
                orderDate.setText("Заказ от " + sdf.format(new Date(order.orderDate)));
                orderStatus.setText(order.status);

                switch (order.status) {
                    case "Доставлен":
                        orderStatus.setTextColor(Color.parseColor("#4CAF50"));
                        orderStatus.setBackgroundColor(Color.parseColor("#E8F5E9"));
                        break;
                    case "В пути":
                        orderStatus.setTextColor(Color.parseColor("#FF9800"));
                        orderStatus.setBackgroundColor(Color.parseColor("#FFF3E0"));
                        break;
                    default:
                        orderStatus.setTextColor(Color.parseColor("#9E9E9E"));
                        orderStatus.setBackgroundColor(Color.parseColor("#F5F5F5"));
                        break;
                }

                cancelButton.setVisibility("В пути".equals(order.status) ? View.VISIBLE : View.GONE);
                cancelButton.setOnClickListener(v -> new AlertDialog.Builder(OrdersActivity.this)
                        .setTitle("Отмена заказа")
                        .setMessage("Отменить заказ \"" + order.productName + "\"?")
                        .setPositiveButton("Отменить", (d, w) -> {
                            db.orderDao().delete(order.id);
                            orders.remove(position);
                            notifyDataSetChanged();
                            if (orders.isEmpty()) {
                                emptyView.setVisibility(View.VISIBLE);
                                recyclerView.setVisibility(View.GONE);
                            }
                        })
                        .setNegativeButton("Нет", null)
                        .show());
            }
        }
    }
}
