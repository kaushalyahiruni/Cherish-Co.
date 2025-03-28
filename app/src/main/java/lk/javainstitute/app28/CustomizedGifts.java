package lk.javainstitute.app28;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomizedGifts extends Fragment {

    private static final int GALLERY_REQUEST_CODE = 123;
    private static final String TAG = "CustomizedGifts";
    private ViewPager viewPager;
    private ImagePagerAdapter imagePagerAdapter;
    private List<Uri> imageUriList;
    private FirebaseFirestore db;
    private TextInputEditText customTextInput, specialNoteInput;
    private TextView productTitle, productPrice, productColor, quantityText;
    private ImageView productImageView;
    private Button uploadImageButton, submitCustomizationButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customized_gifts, container, false);

        db = FirebaseFirestore.getInstance();

        uploadImageButton = view.findViewById(R.id.uploadImageButton);
        submitCustomizationButton = view.findViewById(R.id.submitCustomizationButton);
        viewPager = view.findViewById(R.id.viewPager);
        imageUriList = new ArrayList<>();

        productTitle = view.findViewById(R.id.productTitle);
        productPrice = view.findViewById(R.id.productPrice);
        productColor = view.findViewById(R.id.productColor);
        quantityText = view.findViewById(R.id.quantityText);
        productImageView = view.findViewById(R.id.productImageView);
        customTextInput = view.findViewById(R.id.customTextInput);
        specialNoteInput = view.findViewById(R.id.specialNoteInput);

        uploadImageButton.setOnClickListener(v -> openGallery());

        imagePagerAdapter = new ImagePagerAdapter(getContext(), imageUriList);
        viewPager.setAdapter(imagePagerAdapter);

        Bundle args = getArguments();
        if (args != null) {
            String title = args.getString("title");
            String price = args.getString("price");
            String color = args.getString("color");
            int quantity = args.getInt("quantity");
            int imageResourceId = args.getInt("imageResourceId", 0);

            productTitle.setText(title);
            productPrice.setText("Rs. " + price);
            productColor.setText(color);
            quantityText.setText("Quantity: " + quantity);

            if (imageResourceId != 0) {
                productImageView.setImageResource(imageResourceId);
            }

            submitCustomizationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    submitCustomization(title, price, color, quantity);
                }
            });
        } else {
            Log.w(TAG, "Arguments bundle is null");
            Toast.makeText(getContext(), "No product data available", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == getActivity().RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        imageUriList.add(imageUri);
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    imageUriList.add(imageUri);
                }

                if (imageUriList.size() > 3) {
                    Toast.makeText(getContext(), "You can only select up to 3 images", Toast.LENGTH_SHORT).show();
                    imageUriList = imageUriList.subList(0, 3);
                }
                imagePagerAdapter.notifyDataSetChanged();
            }
        }
    }

    private void submitCustomization(String title, String price, String color, int quantity) {
        Log.d(TAG, "Submit button clicked");

        String customText = customTextInput.getText() != null ? customTextInput.getText().toString().trim() : "";
        String specialNote = specialNoteInput.getText() != null ? specialNoteInput.getText().toString().trim() : "";

        if (customText.isEmpty() || specialNote.isEmpty()) {
            Toast.makeText(getContext(), "Please fill all fields before submitting", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> customizationData = new HashMap<>();
        customizationData.put("title", title);
        customizationData.put("price", price);
        customizationData.put("color", color);
        customizationData.put("quantity", quantity);
        customizationData.put("text", customText);
        customizationData.put("note", specialNote);
        customizationData.put("timestamp", FieldValue.serverTimestamp());

        Log.d(TAG, "Data to save: " + customizationData);

        db.collection("customization")
                .add(customizationData)
                .addOnSuccessListener(documentReference -> {
                    Log.i(TAG, "Document saved with ID: " + documentReference.getId());
                    Toast.makeText(getContext(), "Customization saved successfully", Toast.LENGTH_SHORT).show();
                    clearFields();

                    Intent intent = new Intent(getContext(), OrderSummary.class);
                    intent.putExtra("title", title);
                    intent.putExtra("price", price);
                    intent.putExtra("color", color);
                    intent.putExtra("quantity", quantity);
                    intent.putExtra("customText", customText);
                    intent.putExtra("specialNote", specialNote);
                    startActivity(intent);

                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving document", e);
                    Toast.makeText(getContext(), "Failed to save: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void clearFields() {
        customTextInput.setText("");
        specialNoteInput.setText("");

        productTitle.setText("Product Title");
        productPrice.setText("Product Price");
        productColor.setText("Product Color");
        quantityText.setText("Quantity");

        imageUriList.clear();
        imagePagerAdapter.notifyDataSetChanged();
    }
}