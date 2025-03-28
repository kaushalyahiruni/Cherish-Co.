package lk.javainstitute.app28;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CategoriesFragment extends Fragment {

    private RecyclerView categoryRecyclerView, bestRecyclerView;
    private List<Category> categoryList;
    private CategoryAdapter categoryAdapter;
    private List<Item> itemList;
    private ItemAdapter itemAdapter;
    private FirebaseFirestore firestore;
    private int[] categoryImages = {
            R.drawable.candle,
            R.drawable.frame,
            R.drawable.cmug,
            R.drawable.flowers,
            R.drawable.cbox,
            R.drawable.soap,
            R.drawable.key
    };

    private int[] productImages = {
            R.drawable.p4,
            R.drawable.p13,
            R.drawable.p12,
            R.drawable.p14,
            R.drawable.p24,
            R.drawable.p26,
            R.drawable.p15,
            R.drawable.p6,
            R.drawable.p16,
            R.drawable.p17,
            R.drawable.p18,
            R.drawable.p7,
            R.drawable.p19,
            R.drawable.p20,
            R.drawable.p21,
            R.drawable.p23,
            R.drawable.p22,
            R.drawable.p25,
            R.drawable.p9,
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories, container, false);

        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        bestRecyclerView = view.findViewById(R.id.bestRecyclerView);

        categoryList = new ArrayList<>();
        categoryAdapter = new CategoryAdapter(getContext(), categoryList, category -> {
            Toast.makeText(getContext(), "Clicked: " + category.getName(), Toast.LENGTH_SHORT).show();
        });
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryRecyclerView.setAdapter(categoryAdapter);

        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(getContext(), itemList);
        bestRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        bestRecyclerView.setAdapter(itemAdapter);

        firestore = FirebaseFirestore.getInstance();

        loadCategoriesFromFirebase();
        loadProductsFromFirebase();

        return view;
    }

    private void loadCategoriesFromFirebase() {
        firestore.collection("category").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                categoryList.clear();
                int index = 0;
                for (DocumentSnapshot document : task.getResult()) {
                    String name = document.getString("name");
                    if (name != null) {
                        int imageResourceId = categoryImages[index % categoryImages.length];
                        Category category = new Category(name, imageResourceId);
                        category.setId(document.getId());
                        categoryList.add(category);
                        index++;
                    }
                }
                categoryAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductsFromFirebase() {
        firestore.collection("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                itemList.clear();
                int index = 0;
                for (DocumentSnapshot document : task.getResult()) {
                    String title = document.getString("title");
                    String price = document.getString("price");
                    String description = document.getString("description");
                    // Assign different product images
                    int imageResourceId = productImages[index % productImages.length];
                    itemList.add(new Item(imageResourceId, title, price, description));
                    index++;
                }
                itemAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}