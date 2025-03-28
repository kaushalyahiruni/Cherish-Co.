package lk.javainstitute.app28;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CartFragment extends Fragment {

    private RecyclerView recyclerView;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItemList;
    private FirebaseFirestore db;
    private TextView textViewTotalPrice;
    private static final String TAG = "CartFragment";

    private Button buttonCheckout;

    public CartFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        buttonCheckout = view.findViewById(R.id.buttonCheckout);
        recyclerView = view.findViewById(R.id.recyclerViewCart);
        textViewTotalPrice = view.findViewById(R.id.textViewTotal);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        cartItemList = new ArrayList<>();
        cartAdapter = new CartAdapter(cartItemList, this);
        recyclerView.setAdapter(cartAdapter);

        db = FirebaseFirestore.getInstance();

        loadCartItems();

        buttonCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double totalPrice = calculateTotalPrice();

                if (totalPrice > 0) {
                    Intent intent = new Intent(getActivity(), Checkout.class);
                    intent.putExtra("cartItems", (ArrayList<CartItem>) cartItemList);
                    intent.putExtra("totalAmount", totalPrice);
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), "Your cart is empty!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void loadCartItems() {
        CollectionReference cartCollection = db.collection("cart");

        cartCollection.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Failed to load cart items: " + error.getMessage());
                Toast.makeText(getContext(), "Failed to load cart items.", Toast.LENGTH_SHORT).show();
                return;
            }

            cartItemList.clear();
            if (value != null) {
                HashMap<String, CartItem> mergedItems = new HashMap<>();

                for (DocumentSnapshot document : value.getDocuments()) {
                    try {
                        CartItem cartItem = document.toObject(CartItem.class);
                        if (cartItem != null) {
                            cartItem.setDocumentId(document.getId());

                            if (mergedItems.containsKey(cartItem.getProductName())) {
                                CartItem existingItem = mergedItems.get(cartItem.getProductName());
                                existingItem.setQuantity(existingItem.getQuantity() + cartItem.getQuantity());
                            } else {
                                mergedItems.put(cartItem.getProductName(), cartItem);
                            }

                            Log.d(TAG, "Cart item loaded: " + cartItem.getProductName());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing document: " + document.getId(), e);
                    }
                }

                cartItemList.addAll(mergedItems.values());
            }

            cartAdapter.notifyDataSetChanged();
            updateTotalPrice();
        });
    }

    public void removeCartItem(int position) {
        if (position < 0 || position >= cartItemList.size()) {
            Log.e(TAG, "Invalid position: " + position);
            return;
        }

        CartItem itemToRemove = cartItemList.get(position);
        String documentId = itemToRemove.getDocumentId();

        Log.d(TAG, "Attempting to remove item with documentId: " + documentId + " at position: " + position);

        if (documentId != null) {
            db.collection("cart").document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Item successfully removed from Firestore with documentId: " + documentId);
                        cartItemList.remove(position);
                        cartAdapter.notifyItemRemoved(position);
                        updateTotalPrice();
                        Toast.makeText(getContext(), "Item removed", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to remove item from Firestore with documentId: " + documentId, e);
                        Toast.makeText(getContext(), "Failed to remove item", Toast.LENGTH_SHORT).show();
                    });
        } else {
            Log.e(TAG, "documentId is null for item at position: " + position);
            Toast.makeText(getContext(), "Failed to remove item: documentId is null", Toast.LENGTH_SHORT).show();
        }
    }

    public void onCartItemClicked(int position) {
        if (position < 0 || position >= cartItemList.size()) {
            Log.e(TAG, "Invalid position for click: " + position);
            return;
        }

        CartItem clickedItem = cartItemList.get(position);
        String productName = clickedItem.getProductName();

        // Navigate to OrderSummaryUpdate with only productName
        Intent intent = new Intent(getActivity(), OrderSummaryUpdate.class);
        intent.putExtra("productName", productName);
        startActivity(intent);
    }

    private double calculateTotalPrice() {
        double totalPrice = 0;
        for (CartItem item : cartItemList) {
            totalPrice += item.getProductPrice() * item.getQuantity();
        }
        return totalPrice;
    }

    private void updateTotalPrice() {
        double totalPrice = calculateTotalPrice();
        textViewTotalPrice.setText("Total: Rs. " + String.format("%.2f", totalPrice));
    }

    public void addToCart(String productName, double productPrice, int quantity, String imageUrl) {
        CollectionReference cartCollection = db.collection("cart");

        cartCollection.whereEqualTo("productName", productName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot existingItem = queryDocumentSnapshots.getDocuments().get(0);
                        String documentId = existingItem.getId();
                        int currentQuantity = existingItem.getLong("quantity").intValue();
                        int newQuantity = currentQuantity + quantity;

                        cartCollection.document(documentId)
                                .update("quantity", newQuantity)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Cart updated successfully for " + productName + " with new quantity: " + newQuantity);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to update cart for " + productName, e);
                                    Toast.makeText(getContext(), "Failed to update cart", Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        CartItem cartItem = new CartItem(productName, productPrice, quantity, imageUrl);
                        cartCollection.add(cartItem)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d(TAG, "New item added to cart: " + productName);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to add new item " + productName + " to cart", e);
                                    Toast.makeText(getContext(), "Failed to add item to cart", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking cart for " + productName, e);
                    Toast.makeText(getContext(), "Error checking cart", Toast.LENGTH_SHORT).show();
                    CartItem cartItem = new CartItem(productName, productPrice, quantity, imageUrl);
                    cartCollection.add(cartItem)
                            .addOnSuccessListener(documentReference -> Log.d(TAG, "Fallback: New item added to cart: " + productName))
                            .addOnFailureListener(e2 -> Log.e(TAG, "Fallback failed: " + e2.getMessage()));
                });
    }
}