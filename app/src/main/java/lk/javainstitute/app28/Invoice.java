package lk.javainstitute.app28;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Invoice extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView companyTitle, companySlogan, orderIdText, timestampText;
    private TextView productTitle1, productQuantity1, productPrice1;
    private TextView productTitle2, productQuantity2, productPrice2;
    private TextView subtotalText, deliveryFeeText, grandTotalText, statusText;
    private ImageView backIcon;
    private Button actionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_invoice);

        db = FirebaseFirestore.getInstance();

        initializeViews();

        String productTitle = getIntent().getStringExtra("PRODUCT_TITLE");
        if (productTitle != null) {
            fetchOrderDetails(productTitle);
        } else {
            Toast.makeText(this, "No product title provided", Toast.LENGTH_SHORT).show();
        }

        backIcon.setOnClickListener(v -> finish());

        actionButton.setOnClickListener(v -> {
            Toast.makeText(this, "Downloading invoice...", Toast.LENGTH_SHORT).show();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        companyTitle = findViewById(R.id.company_title);
        companySlogan = findViewById(R.id.company_slogan);
        orderIdText = findViewById(R.id.order_id_text);
        timestampText = findViewById(R.id.timestamp_text);
        productTitle1 = findViewById(R.id.product_title_1);
        productQuantity1 = findViewById(R.id.product_quantity_1);
        productPrice1 = findViewById(R.id.product_price_1);
        productTitle2 = findViewById(R.id.product_title_2);
        productQuantity2 = findViewById(R.id.product_quantity_2);
        productPrice2 = findViewById(R.id.product_price_2);
        subtotalText = findViewById(R.id.subtotal_text);
        deliveryFeeText = findViewById(R.id.delivery_fee_text);
        grandTotalText = findViewById(R.id.grand_total_text);
        statusText = findViewById(R.id.status_text);
        backIcon = findViewById(R.id.back_icon);
        actionButton = findViewById(R.id.action_button);
    }

    private void fetchOrderDetails(String productTitle) {
        db.collection("orders")
                .whereArrayContains("productTitles", productTitle)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot querySnapshot = task.getResult();
                        if (querySnapshot != null && !querySnapshot.isEmpty()) {
                            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                            updateUIWithOrderData(document);
                        } else {
                            Toast.makeText(this, "No order found with product: " + productTitle, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error fetching order: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateUIWithOrderData(DocumentSnapshot document) {
        String payHereOrderId = document.getString("orderId");
        orderIdText.setText("Order ID: " + (payHereOrderId != null ? payHereOrderId : document.getId()));

        Long timestampMillis = document.getLong("timestamp");
        if (timestampMillis != null) {
            Date timestamp = new Date(timestampMillis);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            timestampText.setText(sdf.format(timestamp));
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) document.get("items");
        if (items != null && !items.isEmpty()) {
            Map<String, Object> item1 = items.get(0);
            productTitle1.setText((String) item1.get("title"));
            productQuantity1.setText("Quantity: " + item1.get("quantity").toString());
            productPrice1.setText("Price: Rs. " + String.format("%.2f",
                    Double.parseDouble(item1.get("price").toString())));

            if (items.size() > 1) {
                Map<String, Object> item2 = items.get(1);
                productTitle2.setText((String) item2.get("title"));
                productQuantity2.setText("Quantity: " + item2.get("quantity").toString());
                productPrice2.setText("Price: Rs. " + String.format("%.2f",
                        Double.parseDouble(item2.get("price").toString())));
            } else {
                productTitle2.setVisibility(View.GONE);
                productQuantity2.setVisibility(View.GONE);
                productPrice2.setVisibility(View.GONE);
            }
        }

        Double subtotal = document.getDouble("subtotal");
        if (subtotal != null) {
            subtotalText.setText("Rs. " + String.format("%.2f", subtotal));
        }

        Double deliveryFee = document.getDouble("deliveryFee");
        if (deliveryFee != null) {
            deliveryFeeText.setText("Rs. " + String.format("%.2f", deliveryFee));
        }

        Double grandTotal = document.getDouble("grandTotal");
        if (grandTotal != null) {
            grandTotalText.setText("Rs. " + String.format("%.2f", grandTotal));
        }

        String status = document.getString("status");
        if (status != null) {
            statusText.setText(status);
        }
    }
}