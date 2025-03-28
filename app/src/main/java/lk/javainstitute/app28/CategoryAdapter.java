package lk.javainstitute.app28;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private Context context;
    private List<Category> categoryList;
    private OnCategoryClickListener clickListener;

    public CategoryAdapter(Context context, List<Category> categoryList, OnCategoryClickListener clickListener) {
        this.context = context;
        this.categoryList = categoryList;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.category_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.name.setText(category.getName());

        // Set the drawable resource directly
        holder.image.setImageResource(category.getImageResourceId());

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onCategoryClick(category);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        ImageView image;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.categoryTitle);
            image = itemView.findViewById(R.id.categoryImage);
        }
    }
}