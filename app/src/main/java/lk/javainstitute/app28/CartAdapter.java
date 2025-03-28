package lk.javainstitute.app28;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItemList;
    private CartFragment cartFragment;

    public CartAdapter(List<CartItem> cartItemList, CartFragment cartFragment) {
        this.cartItemList = cartItemList;
        this.cartFragment = cartFragment;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItemList.get(position);

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Picasso.get().load(item.getImageUrl()).into(holder.imageViewProduct);
        } else {
            holder.imageViewProduct.setImageResource(item.getImageResource());
        }

        holder.textViewProductName.setText(item.getProductName());
        holder.textViewProductPrice.setText("Rs. " + String.format("%.2f", item.getProductPrice()));
        holder.textViewProductQuantity.setText(String.valueOf(item.getQuantity()));

        holder.itemView.setOnClickListener(v -> {
            cartFragment.onCartItemClicked(position);
        });

        holder.imageButtonRemove.setOnClickListener(v -> {
            cartFragment.removeCartItem(position);
        });
    }

    @Override
    public int getItemCount() {
        return cartItemList.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName, textViewProductPrice, textViewProductQuantity;
        ImageButton imageButtonRemove;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            textViewProductQuantity = itemView.findViewById(R.id.textViewProductQuantity);
            imageButtonRemove = itemView.findViewById(R.id.imageButtonRemove);
        }
    }
}
