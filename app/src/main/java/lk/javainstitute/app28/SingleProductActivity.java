package lk.javainstitute.app28;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import model.Colors;

public class SingleProductActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private TextView productTitle, productPrice, quantityText, productDescription;
    private Button decreaseQuantity, increaseQuantity, addToCartButton, buttonToCustomize;
    private int quantity = 1;
    private ProductImageAdapter productImageAdapter;
    private FirebaseFirestore db;
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_product);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.title2));

        viewPager = findViewById(R.id.viewPager);
        productTitle = findViewById(R.id.productTitle);
        productPrice = findViewById(R.id.productPrice);
        quantityText = findViewById(R.id.quantityText);
        decreaseQuantity = findViewById(R.id.decreaseQuantity);
        increaseQuantity = findViewById(R.id.increaseQuantity);
        addToCartButton = findViewById(R.id.addToCartButton);
        productDescription = findViewById(R.id.productDescription);
        buttonToCustomize = findViewById(R.id.buttonToCustomize);
        spinner = findViewById(R.id.spinner);

        db = FirebaseFirestore.getInstance();

        ArrayList<Colors> colorList = new ArrayList<>();
        colorList.add(new Colors(R.drawable.wheel, "Default"));
        colorList.add(new Colors(R.drawable.black, "Black"));
        colorList.add(new Colors(R.drawable.red, "Red"));
        colorList.add(new Colors(R.drawable.blue, "Blue"));
        colorList.add(new Colors(R.drawable.green, "Green"));
        colorList.add(new Colors(R.drawable.yellow, "Yellow"));
        colorList.add(new Colors(R.drawable.purple, "Purple"));

        ColorAdaptor colorAdapter = new ColorAdaptor(this, R.layout.color_spinner_item, colorList);
        spinner.setAdapter(colorAdapter);

        int imageResourceId = getIntent().getIntExtra("imageResourceId", 0);
        String title = getIntent().getStringExtra("title");
        String priceStr = getIntent().getStringExtra("price");
        String description = getIntent().getStringExtra("description");

        productTitle.setText(title);
        productPrice.setText("Rs. " + priceStr);
        productDescription.setText(description);

        List<Integer> images = new ArrayList<>();
        images.add(imageResourceId);
        images.add(R.drawable.p1);
        images.add(R.drawable.p2);
        productImageAdapter = new ProductImageAdapter(this, images);
        viewPager.setAdapter(productImageAdapter);

        decreaseQuantity.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityText.setText(String.valueOf(quantity));
            }
        });

        increaseQuantity.setOnClickListener(v -> {
            quantity++;
            quantityText.setText(String.valueOf(quantity));
        });

        addToCartButton.setOnClickListener(v -> {
            double price = Double.parseDouble(priceStr);
            String productTitleText = productTitle.getText().toString();
            int imageResource = getIntent().getIntExtra("imageResourceId", 0);

            CartItem cartItem = new CartItem(imageResource, productTitleText, price, quantity, null);
            CollectionReference cartCollection = db.collection("cart");
            cartCollection.add(cartItem)
                    .addOnSuccessListener(documentReference -> {
                        cartItem.setDocumentId(documentReference.getId());
                        Toast.makeText(SingleProductActivity.this, "Added to Cart", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(SingleProductActivity.this, MainActivity3.class);
                        intent.putExtra("navigateTo", "CartFragment");
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> Toast.makeText(SingleProductActivity.this, "Failed to add to Cart", Toast.LENGTH_SHORT).show());
        });

        buttonToCustomize.setOnClickListener(v -> {
            Intent intent = new Intent(SingleProductActivity.this, MainActivity3.class);
            intent.putExtra("navigateTo", "CustomizedGiftsFragment");
            intent.putExtra("title", productTitle.getText().toString());
            intent.putExtra("price", productPrice.getText().toString().replace("Rs. ", ""));
            Colors selectedColor = (Colors) spinner.getSelectedItem();
            intent.putExtra("color", selectedColor.getColor());
            intent.putExtra("quantity", quantity);
            // Optionally remove imageResourceId if not needed
            intent.putExtra("imageResourceId", getIntent().getIntExtra("imageResourceId", 0));
            startActivity(intent);
        });
    }
}

class ColorAdaptor extends ArrayAdapter<Colors> {
    private final List<Colors> colorDataList;
    private final int layout;

    public ColorAdaptor(@NonNull Context context, int resource, @NonNull List<Colors> objects) {
        super(context, resource, objects);
        colorDataList = objects;
        layout = resource;
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(layout, parent, false);
        ImageView imageView = view.findViewById(R.id.imageView);
        TextView textView = view.findViewById(R.id.textView);

        Colors colorData = colorDataList.get(position);
        imageView.setImageResource(colorData.getResourceId());
        textView.setText(colorData.getColor());
        return view;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return getDropDownView(position, convertView, parent);
    }
}