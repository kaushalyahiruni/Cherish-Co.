package lk.javainstitute.app28;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Orders extends AppCompatActivity {

    private static final String TAG = "Orders";

    private RecyclerView ordersRecyclerView;
    private OrderAdapter orderAdapter;
    private List<Order> orderList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.title2));
        setContentView(R.layout.activity_orders);

        db = FirebaseFirestore.getInstance();

        ordersRecyclerView = findViewById(R.id.orders_recycler_view);
        if (ordersRecyclerView == null) {
            Log.e(TAG, "orders_recycler_view not found in layout");
            Toast.makeText(this, "Error: RecyclerView not found", Toast.LENGTH_SHORT).show();
            return;
        }
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        orderList = new ArrayList<>();
        orderAdapter = new OrderAdapter(orderList, db);
        ordersRecyclerView.setAdapter(orderAdapter);

        ImageView backIcon = findViewById(R.id.back_icon);
        if (backIcon != null) {
            backIcon.setOnClickListener(v -> finish());
        } else {
            Log.e(TAG, "back_icon not found in layout");
        }

        loadOrdersFromFirestore();
    }

    private void loadOrdersFromFirestore() {
        db.collection("orders")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    orderList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            List<Map<String, Object>> items = (List<Map<String, Object>>) document.get("items");
                            Double subtotal = document.getDouble("subtotal") != null ? document.getDouble("subtotal") : 0.0;
                            Double deliveryFee = document.getDouble("deliveryFee") != null ? document.getDouble("deliveryFee") : 0.0;
                            Double grandTotal = document.getDouble("grandTotal") != null ? document.getDouble("grandTotal") : 0.0;
                            Long orderId = document.getLong("orderId") != null ? document.getLong("orderId") : 0L;
                            Long timestamp = document.getLong("timestamp") != null ? document.getLong("timestamp") : 0L;
                            String status = document.getString("status");
                            String documentId = document.getId();

                            List<OrderItem> orderItems = new ArrayList<>();
                            if (items != null) {
                                for (Map<String, Object> item : items) {
                                    String title = item.get("title") != null ? (String) item.get("title") : "Unknown Item";
                                    Long quantity = item.get("quantity") != null ? ((Number) item.get("quantity")).longValue() : 0L;
                                    Double price = item.get("price") != null ? ((Number) item.get("price")).doubleValue() : 0.0;
                                    orderItems.add(new OrderItem(title, quantity, price));
                                }
                            } else {
                                Log.w(TAG, "No items found in order document: " + documentId);
                            }

                            Order order = new Order(orderItems, subtotal, deliveryFee, grandTotal, orderId, timestamp, status, documentId);
                            orderList.add(order);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing document " + document.getId() + ": " + e.getMessage(), e);
                        }
                    }
                    orderAdapter.notifyDataSetChanged();
                    if (orderList.isEmpty()) {
                        Toast.makeText(Orders.this, "No orders found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading orders: " + e.getMessage(), e);
                    Toast.makeText(Orders.this, "Failed to load orders: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}

class Order {
    private List<OrderItem> items;
    private double subtotal;
    private double deliveryFee;
    private double grandTotal;
    private long orderId;
    private long timestamp;
    private String status;
    private String documentId;

    public Order(List<OrderItem> items, double subtotal, double deliveryFee, double grandTotal, long orderId, long timestamp, String status, String documentId) {
        this.items = items;
        this.subtotal = subtotal;
        this.deliveryFee = deliveryFee;
        this.grandTotal = grandTotal;
        this.orderId = orderId;
        this.timestamp = timestamp;
        this.status = status != null ? status : "Pending";
        this.documentId = documentId;
    }

    public List<OrderItem> getItems() { return items; }
    public double getSubtotal() { return subtotal; }
    public double getDeliveryFee() { return deliveryFee; }
    public double getGrandTotal() { return grandTotal; }
    public long getOrderId() { return orderId; }
    public long getTimestamp() { return timestamp; }
    public String getStatus() { return status; }
    public String getDocumentId() { return documentId; }
    public void setStatus(String status) { this.status = status; }
}

class OrderItem {
    private String title;
    private long quantity;
    private double price;

    public OrderItem(String title, long quantity, double price) {
        this.title = title;
        this.quantity = quantity;
        this.price = price;
    }

    public String getTitle() { return title; }
    public long getQuantity() { return quantity; }
    public double getPrice() { return price; }
}

class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orderList;
    private FirebaseFirestore db;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public OrderAdapter(List<Order> orderList, FirebaseFirestore db) {
        this.orderList = orderList;
        this.db = db;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.orderIdText.setText("Order ID: " + order.getOrderId());
        holder.timestampText.setText(dateFormat.format(new Date(order.getTimestamp())));

        List<OrderItem> items = order.getItems();
        if (items != null && !items.isEmpty()) {
            holder.productTitle1.setText(items.get(0).getTitle());
            holder.productQuantity1.setText("Quantity: " + items.get(0).getQuantity());
            holder.productPrice1.setText(String.format("Price: Rs. %.2f", items.get(0).getPrice()));

            if (items.size() > 1) {
                holder.productTitle2.setText(items.get(1).getTitle());
                holder.productQuantity2.setText("Quantity: " + items.get(1).getQuantity());
                holder.productPrice2.setText(String.format("Price: Rs. %.2f", items.get(1).getPrice()));
            } else {
                holder.productTitle2.setVisibility(View.GONE);
                holder.productQuantity2.setVisibility(View.GONE);
                holder.productPrice2.setVisibility(View.GONE);
            }
        } else {
            holder.productTitle1.setVisibility(View.GONE);
            holder.productQuantity1.setVisibility(View.GONE);
            holder.productPrice1.setVisibility(View.GONE);
            holder.productTitle2.setVisibility(View.GONE);
            holder.productQuantity2.setVisibility(View.GONE);
            holder.productPrice2.setVisibility(View.GONE);
        }

        holder.subtotalText.setText(String.format("Rs. %.2f", order.getSubtotal()));
        holder.deliveryFeeText.setText(String.format("Rs. %.2f", order.getDeliveryFee()));
        holder.grandTotalText.setText(String.format("Rs. %.2f", order.getGrandTotal()));
        holder.statusText.setText(order.getStatus());
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView orderIdText, timestampText;
        TextView productTitle1, productQuantity1, productPrice1;
        TextView productTitle2, productQuantity2, productPrice2;
        TextView subtotalText, deliveryFeeText, grandTotalText;
        TextView statusText;
        ImageView productImage;

        OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                orderIdText = itemView.findViewById(R.id.order_id_text);
                timestampText = itemView.findViewById(R.id.timestamp_text);
                productTitle1 = itemView.findViewById(R.id.product_title_1);
                productQuantity1 = itemView.findViewById(R.id.product_quantity_1);
                productPrice1 = itemView.findViewById(R.id.product_price_1);
                productTitle2 = itemView.findViewById(R.id.product_title_2);
                productQuantity2 = itemView.findViewById(R.id.product_quantity_2);
                productPrice2 = itemView.findViewById(R.id.product_price_2);
                subtotalText = itemView.findViewById(R.id.subtotal_text);
                deliveryFeeText = itemView.findViewById(R.id.delivery_fee_text);
                grandTotalText = itemView.findViewById(R.id.grand_total_text);
                statusText = itemView.findViewById(R.id.status_text);
                productImage = itemView.findViewById(R.id.product_image);
            } catch (Exception e) {
                Log.e(TAG, "Error initializing OrderViewHolder: " + e.getMessage(), e);
            }
        }
    }
}