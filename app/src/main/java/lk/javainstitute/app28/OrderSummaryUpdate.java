package lk.javainstitute.app28;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OrderSummaryUpdate extends AppCompatActivity {

    private ViewPager imageSlider;
    private TextView titleValue, totalPriceValue;
    private TextInputEditText colorInput, quantityInput, textInput, noteInput;
    private Button proceedButton;
    private FirebaseFirestore db;
    private static final String TAG = "OrderSummaryUpdate";
    private String customizationDocId;
    private String cartDocId;
    private String productPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary_update);

        imageSlider = findViewById(R.id.imageSlider);
        titleValue = findViewById(R.id.titleValue);
        totalPriceValue = findViewById(R.id.totalPriceValue);
        colorInput = findViewById(R.id.colorInput);
        quantityInput = findViewById(R.id.quantityInput);
        textInput = findViewById(R.id.textInput);
        noteInput = findViewById(R.id.noteInput);
        proceedButton = findViewById(R.id.saveButton1);

        db = FirebaseFirestore.getInstance();

        int[] images = {R.drawable.p3, R.drawable.p6, R.drawable.p16};
        ImageSliderAdapter1 adapter = new ImageSliderAdapter1(this, images);
        imageSlider.setAdapter(adapter);

        Intent intent = getIntent();
        if (intent != null) {
            String productName = intent.getStringExtra("productName");
            if (productName != null) {
                titleValue.setText(productName);
                fetchCustomizationData(productName);
            } else {
                Toast.makeText(this, "No product name provided", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        proceedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateFirestoreData();
            }
        });
    }

    private void fetchCustomizationData(String productName) {
        db.collection("customization")
                .whereEqualTo("title", productName)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        customizationDocId = document.getId();
                        productPrice = document.getString("price");
                        String color = document.getString("color");
                        int quantity = document.getLong("quantity").intValue();
                        String customText = document.getString("text");
                        String specialNote = document.getString("note");

                        colorInput.setText(color != null ? color : "N/A");
                        quantityInput.setText(String.valueOf(quantity));
                        textInput.setText(customText != null ? customText : "N/A");
                        noteInput.setText(specialNote != null ? specialNote : "N/A");
                        totalPriceValue.setText("Rs. " + calculateTotalPrice(productPrice, quantity));

                        quantityInput.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {}

                            @Override
                            public void afterTextChanged(Editable s) {
                                String qtyStr = s.toString();
                                int newQuantity = qtyStr.isEmpty() ? 0 : Integer.parseInt(qtyStr);
                                totalPriceValue.setText("Rs. " + calculateTotalPrice(productPrice, newQuantity));
                            }
                        });
                    } else {
                        colorInput.setText("N/A");
                        quantityInput.setText("0");
                        textInput.setText("N/A");
                        noteInput.setText("N/A");
                        totalPriceValue.setText("Rs. 0.00");

                        Toast.makeText(this, "No customization data found for " + productName, Toast.LENGTH_SHORT).show();
                    }

                    fetchCartDocumentId(productName);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch customization data: " + e.getMessage());
                    Toast.makeText(this, "Error loading customization data", Toast.LENGTH_SHORT).show();
                    colorInput.setText("N/A");
                    quantityInput.setText("0");
                    textInput.setText("N/A");
                    noteInput.setText("N/A");
                    totalPriceValue.setText("Rs. 0.00");

                    fetchCartDocumentId(productName);
                });
    }

    private void fetchCartDocumentId(String productName) {
        db.collection("cart")
                .whereEqualTo("productName", productName)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                        cartDocId = document.getId();
                        if (productPrice == null) {
                            productPrice = String.valueOf(document.getDouble("productPrice"));
                            int quantity = document.getLong("quantity").intValue();
                            totalPriceValue.setText("Rs. " + calculateTotalPrice(productPrice, quantity));
                        }
                    } else {
                        Log.w(TAG, "No cart item found for " + productName);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch cart document ID: " + e.getMessage());
                });
    }

    private void updateFirestoreData() {
        String title = titleValue.getText().toString();
        String color = colorInput.getText().toString();
        String qtyStr = quantityInput.getText().toString();
        String customText = textInput.getText().toString();
        String specialNote = noteInput.getText().toString();

        if (title.isEmpty() || color.isEmpty() || qtyStr.isEmpty() || customText.isEmpty() || specialNote.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(qtyStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> customizationData = new HashMap<>();
        customizationData.put("title", title);
        customizationData.put("price", productPrice);
        customizationData.put("color", color);
        customizationData.put("quantity", quantity);
        customizationData.put("text", customText);
        customizationData.put("note", specialNote);

        Map<String, Object> cartData = new HashMap<>();
        cartData.put("productName", title);
        cartData.put("productPrice", Double.parseDouble(productPrice));
        cartData.put("quantity", quantity);

        if (customizationDocId != null) {
            db.collection("customization").document(customizationDocId)
                    .update(customizationData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Customization updated successfully");
                        updateCartCollection(title, cartData);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update customization: " + e.getMessage());
                        Toast.makeText(this, "Failed to update customization", Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("customization")
                    .add(customizationData)
                    .addOnSuccessListener(documentReference -> {
                        customizationDocId = documentReference.getId();
                        Log.d(TAG, "Customization created successfully");
                        updateCartCollection(title, cartData);
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to create customization: " + e.getMessage());
                        Toast.makeText(this, "Failed to create customization", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void updateCartCollection(String productName, Map<String, Object> cartData) {
        if (cartDocId != null) {
            db.collection("cart").document(cartDocId)
                    .update(cartData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Cart updated successfully");
                        Toast.makeText(this, "Order updated successfully", Toast.LENGTH_SHORT).show();
                        proceedToCart();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to update cart: " + e.getMessage());
                        Toast.makeText(this, "Failed to update cart", Toast.LENGTH_SHORT).show();
                    });
        } else {
            db.collection("cart")
                    .whereEqualTo("productName", productName)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            cartDocId = document.getId();
                            db.collection("cart").document(cartDocId)
                                    .update(cartData)
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Cart updated successfully");
                                        Toast.makeText(this, "Order updated successfully", Toast.LENGTH_SHORT).show();
                                        proceedToCart();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to update cart: " + e.getMessage());
                                        Toast.makeText(this, "Failed to update cart", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            db.collection("cart")
                                    .add(cartData)
                                    .addOnSuccessListener(documentReference -> {
                                        cartDocId = documentReference.getId();
                                        Log.d(TAG, "Cart created successfully");
                                        Toast.makeText(this, "Order updated successfully", Toast.LENGTH_SHORT).show();
                                        proceedToCart();
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Failed to create cart: " + e.getMessage());
                                        Toast.makeText(this, "Failed to create cart", Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to fetch cart document for update: " + e.getMessage());
                        Toast.makeText(this, "Error updating cart", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void proceedToCart() {
        Intent intent = new Intent(OrderSummaryUpdate.this, MainActivity3.class);
        intent.putExtra("navigateTo", "CartFragment"); // Signal MainActivity3 to show CartFragment
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Return to existing instance
        startActivity(intent);
        finish();
    }

    private String calculateTotalPrice(String price, int quantity) {
        if (price == null || price.isEmpty()) return "0.00";
        try {
            double priceValue = Double.parseDouble(price);
            double total = priceValue * quantity;
            return String.format("%.2f", total);
        } catch (NumberFormatException e) {
            return "0.00";
        }
    }
}

class ImageSliderAdapter1 extends PagerAdapter {
    private Context context;
    private int[] images;

    public ImageSliderAdapter1(Context context, int[] images) {
        this.context = context;
        this.images = images;
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(images[position]);
        container.addView(imageView);
        return imageView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}