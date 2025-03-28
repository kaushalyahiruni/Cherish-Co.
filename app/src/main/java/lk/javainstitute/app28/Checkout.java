package lk.javainstitute.app28;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Checkout extends AppCompatActivity {

    private double totalPrice;
    private TextView textView;
    private RecyclerView orderItemsRecyclerView;
    private OrderSummaryAdapter orderSummaryAdapter;
    private List<CartItem> cartItemList;
    private FirebaseFirestore db;

    private final ActivityResultLauncher<Intent> payHereLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Intent data = result.getData();
                    Serializable serializable = data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
                    String msg;
                    if (serializable instanceof PHResponse) {
                        PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) serializable;
                        msg = response.isSuccess() ? String.format("Payment Success: %s", response.getData()) : String.format("Payment Failed: %s", response);
                        Log.i("App28", msg);
                        textView.setText(msg);

                        if (response.isSuccess()) {
                            saveOrderToFirestore();
                            clearCartItems();
                            sendPaymentSuccessNotification();
                        }
                    } else {
                        Log.i("App28", "Invalid serializable data received.");
                        textView.setText("Payment data is invalid or corrupted.");
                    }
                } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Log.i("App28", "Payment cancelled by user.");
                    textView.setText("User canceled the request");
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.title2));

        db = FirebaseFirestore.getInstance();

        orderItemsRecyclerView = findViewById(R.id.order_items_recycler);
        orderItemsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        cartItemList = (ArrayList<CartItem>) intent.getSerializableExtra("cartItems");
        totalPrice = intent.getDoubleExtra("totalAmount", 0);

        orderSummaryAdapter = new OrderSummaryAdapter(cartItemList);
        orderItemsRecyclerView.setAdapter(orderSummaryAdapter);

        TextView subtotalAmount = findViewById(R.id.subtotal_amount);
        TextView grandTotalAmount = findViewById(R.id.grand_total_amount);
        TextView deliveryFee = findViewById(R.id.deliveryFee);

        subtotalAmount.setText("Rs. " + String.format("%.2f", totalPrice));
        deliveryFee.setText("Rs. " + String.format("%.2f", 250.00));
        grandTotalAmount.setText("Rs. " + String.format("%.2f", totalPrice + 250.00));

        textView = findViewById(R.id.message);

        Button payButton = findViewById(R.id.checkout_button);
        payButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initiatePayment();
            }
        });

        ImageView backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback(this, orderSummaryAdapter, cartItemList, orderItemsRecyclerView));
        itemTouchHelper.attachToRecyclerView(orderItemsRecyclerView);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showNoInternetNotification() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "NO_INTERNET_CHANNEL",
                    "Internet Status",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(channel);
        }

        Notification notification = new NotificationCompat.Builder(this, "NO_INTERNET_CHANNEL")
                .setContentTitle("No Internet Connection")
                .setContentText("Please connect to the internet to complete your payment")
                .setSmallIcon(R.drawable.email)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(2, notification);
    }

    private void initiatePayment() {
        if (!isNetworkAvailable()) {
            showNoInternetNotification();
            textView.setText("Payment failed: No internet connection");
            return;
        }

        Random random = new Random();
        String randomNumber = String.valueOf(random.nextInt(9000) + 1000);
        InitRequest req = new InitRequest();
        req.setMerchantId("1229037");
        req.setCurrency("LKR");
        req.setAmount(totalPrice + 250.00);
        req.setOrderId(randomNumber);
        req.setItemsDescription("Order from Cherish & Co.");
        req.setCustom1("This is the custom message 1");
        req.setCustom2("This is the custom message 2");
        req.getCustomer().setFirstName("Saman");
        req.getCustomer().setLastName("Perera");
        req.getCustomer().setEmail("samanp@gmail.com");
        req.getCustomer().setPhone("+94771234567");
        req.getCustomer().getAddress().setAddress("No.1, Galle Road");
        req.getCustomer().getAddress().setCity("Colombo");
        req.getCustomer().getAddress().setCountry("Sri Lanka");
        req.getCustomer().getDeliveryAddress().setAddress("No.2, Kandy Road");
        req.getCustomer().getDeliveryAddress().setCity("Kadawatha");
        req.getCustomer().getDeliveryAddress().setCountry("Sri Lanka");

        Intent intent = new Intent(this, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        payHereLauncher.launch(intent);
    }

    private void saveOrderToFirestore() {
        Map<String, Object> order = new HashMap<>();
        List<Map<String, Object>> items = new ArrayList<>();

        for (CartItem item : cartItemList) {
            Map<String, Object> itemData = new HashMap<>();
            itemData.put("title", item.getProductName());
            itemData.put("quantity", item.getQuantity());
            itemData.put("price", item.getProductPrice());
            items.add(itemData);
        }

        order.put("items", items);
        order.put("subtotal", totalPrice);
        order.put("deliveryFee", 250.00);
        order.put("grandTotal", totalPrice + 250.00);
        order.put("orderId", new Random().nextInt(9000) + 1000);
        order.put("timestamp", System.currentTimeMillis());
        order.put("status", "Processing");

        db.collection("orders")
                .add(order)
                .addOnSuccessListener(documentReference -> {
                    Log.d(TAG, "Order added successfully with ID: " + documentReference.getId());
                    Toast.makeText(this, "Order saved successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error adding order: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to save order: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void clearCartItems() {
        for (CartItem item : cartItemList) {
            String documentId = item.getDocumentId();
            if (documentId != null) {
                db.collection("cart").document(documentId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Checkout", "Item removed from Firestore: " + documentId);
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Checkout", "Failed to remove item from Firestore: " + documentId, e);
                            Toast.makeText(this, "Failed to clear some cart items", Toast.LENGTH_SHORT).show();
                        });
            }
        }

        cartItemList.clear();
        orderSummaryAdapter.notifyDataSetChanged();
        updateTotalPrice();
    }

    private void sendPaymentSuccessNotification() {
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    "C2",
                    "Channel2",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Intent intent = new Intent(this, Orders.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, "C2")
                .setContentTitle("Cherish & Co.")
                .setContentText("Your payment was processed successfully!")
                .setSmallIcon(R.drawable.email)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .addAction(R.drawable.email, "Check your receipt now!", pendingIntent)
                .build();

        notificationManager.notify(1, notification);
    }

    private void updateTotalPrice() {
        double newTotalPrice = 0;
        for (CartItem item : cartItemList) {
            newTotalPrice += item.getProductPrice() * item.getQuantity();
        }

        TextView subtotalAmount = findViewById(R.id.subtotal_amount);
        TextView grandTotalAmount = findViewById(R.id.grand_total_amount);
        TextView deliveryFee = findViewById(R.id.deliveryFee);

        subtotalAmount.setText("Rs. " + String.format("%.2f", newTotalPrice));
        deliveryFee.setText("Rs. " + String.format("%.2f", 250.00));
        grandTotalAmount.setText("Rs. " + String.format("%.2f", newTotalPrice + 250.00));
    }

    private class OrderSummaryAdapter extends RecyclerView.Adapter<OrderSummaryAdapter.OrderSummaryViewHolder> {

        private List<CartItem> cartItems;

        public OrderSummaryAdapter(List<CartItem> cartItems) {
            this.cartItems = cartItems;
        }

        @NonNull
        @Override
        public OrderSummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_summary, parent, false);
            return new OrderSummaryViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull OrderSummaryViewHolder holder, int position) {
            CartItem item = cartItems.get(position);
            holder.productNameTextView.setText(item.getProductName());
            holder.productPriceTextView.setText("Rs. " + String.format("%.2f", item.getProductPrice()));
            holder.productQuantityTextView.setText("Qty: " + item.getQuantity());
        }

        @Override
        public int getItemCount() {
            return cartItems.size();
        }

        class OrderSummaryViewHolder extends RecyclerView.ViewHolder {
            ImageView productImageView;
            TextView productNameTextView;
            TextView productPriceTextView;
            TextView productQuantityTextView;

            public OrderSummaryViewHolder(@NonNull View itemView) {
                super(itemView);
                productImageView = itemView.findViewById(R.id.product_image);
                productNameTextView = itemView.findViewById(R.id.product_name);
                productPriceTextView = itemView.findViewById(R.id.product_price);
                productQuantityTextView = itemView.findViewById(R.id.product_quantity);
            }
        }

        public void removeItem(int position, List<CartItem> cartItems, RecyclerView recyclerView) {
            if (position < 0 || position >= cartItems.size()) return;

            CartItem itemToRemove = cartItems.get(position);
            String documentId = itemToRemove.getDocumentId();

            if (documentId != null) {
                db.collection("cart").document(documentId)
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            cartItems.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, cartItems.size());
                            updateTotalPrice();
                            Toast.makeText(Checkout.this, "Item removed", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> Log.e("Checkout", "Failed to remove item", e));
            }
        }
    }

    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        private OrderSummaryAdapter adapter;
        private Checkout checkout;
        private List<CartItem> cartItems;
        private RecyclerView recyclerView;

        public SwipeToDeleteCallback(Checkout checkout, OrderSummaryAdapter adapter, List<CartItem> cartItems, RecyclerView recyclerView) {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            this.checkout = checkout;
            this.adapter = adapter;
            this.cartItems = cartItems;
            this.recyclerView = recyclerView;
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            adapter.removeItem(position, cartItems, recyclerView);
        }
    }
}