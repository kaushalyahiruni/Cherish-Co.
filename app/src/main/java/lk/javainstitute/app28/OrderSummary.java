package lk.javainstitute.app28;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;

public class OrderSummary extends AppCompatActivity {

    private ViewPager imageSlider;
    private TabLayout sliderDots;
    private TextView titleValue, colorValue, quantityValue, totalPriceValue, customTextValue, specialNoteValue;
    private Button saveButton1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.title2));

        imageSlider = findViewById(R.id.imageSlider);
        sliderDots = findViewById(R.id.sliderDots);
        titleValue = findViewById(R.id.titleValue);
        colorValue = findViewById(R.id.colorValue);
        quantityValue = findViewById(R.id.quantityValue);
        totalPriceValue = findViewById(R.id.totalPriceValue);
        customTextValue = findViewById(R.id.textValue);
        specialNoteValue = findViewById(R.id.noteValue);

        int[] images = {R.drawable.p3, R.drawable.p6, R.drawable.p16};
        ImageSliderAdapter adapter = new ImageSliderAdapter(this, images);
        imageSlider.setAdapter(adapter);
        sliderDots.setupWithViewPager(imageSlider);

        Intent intent = getIntent();
        if (intent != null) {
            String title = intent.getStringExtra("title");
            String price = intent.getStringExtra("price");
            String color = intent.getStringExtra("color");
            int quantity = intent.getIntExtra("quantity", 0);
            String customText = intent.getStringExtra("customText");
            String specialNote = intent.getStringExtra("specialNote");

            titleValue.setText(title != null ? title : "N/A");
            colorValue.setText(color != null ? color : "N/A");
            quantityValue.setText(String.valueOf(quantity));
            customTextValue.setText(customText != null ? customText : "N/A");
            specialNoteValue.setText(specialNote != null ? specialNote : "N/A");
            totalPriceValue.setText("Rs. " + calculateTotalPrice(price, quantity));
        }

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

class ImageSliderAdapter extends PagerAdapter {
    private Context context;
    private int[] images;

    public ImageSliderAdapter(Context context, int[] images) {
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