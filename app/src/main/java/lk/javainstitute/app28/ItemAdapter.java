package lk.javainstitute.app28;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private List<Item> itemList;
    private Context context;

    public ItemAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.items, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        Item item = itemList.get(position);
        holder.itemImageView.setImageResource(item.getImageResourceId());
        holder.itemTitleTextView.setText(item.getTitle());
        holder.itemPriceTextView.setText("Rs. " + item.getPrice());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SingleProductActivity.class);
            intent.putExtra("imageResourceId", item.getImageResourceId());
            intent.putExtra("title", item.getTitle());
            intent.putExtra("price", item.getPrice());
            intent.putExtra("description", item.getDescription());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImageView;
        TextView itemTitleTextView;
        TextView itemPriceTextView;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.imageView2);
            itemTitleTextView = itemView.findViewById(R.id.textView4);
            itemPriceTextView = itemView.findViewById(R.id.textView7);
        }
    }
}
