package lk.javainstitute.app28;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImagePagerAdapter extends PagerAdapter {

    private Context context;
    private List<Uri> imageUriList;

    public ImagePagerAdapter(Context context, List<Uri> imageUriList) {
        this.context = context;
        this.imageUriList = imageUriList;
    }

    @Override
    public int getCount() {
        return imageUriList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.product_image_item, container, false);

        ImageView imageView = view.findViewById(R.id.imageView);
        Uri imageUri = imageUriList.get(position);

        Glide.with(context).load(imageUri).into(imageView);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
