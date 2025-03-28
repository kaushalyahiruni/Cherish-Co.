package lk.javainstitute.app28;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity3 extends AppCompatActivity {

    private ProfileFragment profileFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(R.color.title2));

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        String username = getIntent().getStringExtra("username");
        String email = getIntent().getStringExtra("email");
        String navigateTo = getIntent().getStringExtra("navigateTo");

        Fragment fragment = new HomeFragment();
        if (navigateTo != null) {
            switch (navigateTo) {
                case "ProfileFragment":
                    profileFragment = new ProfileFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("username", username);
                    bundle.putString("email", email);
                    profileFragment.setArguments(bundle);
                    fragment = profileFragment;
                    bottomNavigationView.setSelectedItemId(R.id.profile);
                    break;
                case "CartFragment":
                    fragment = new CartFragment();
                    bottomNavigationView.setSelectedItemId(R.id.cart);
                    break;
                case "CustomizedGiftsFragment":
                    fragment = new CustomizedGifts();
                    Bundle args = new Bundle();
                    args.putString("title", getIntent().getStringExtra("title"));
                    args.putString("price", getIntent().getStringExtra("price"));
                    args.putString("color", getIntent().getStringExtra("color"));
                    args.putInt("quantity", getIntent().getIntExtra("quantity", 1));
                    args.putInt("imageResourceId", getIntent().getIntExtra("imageResourceId", 0));
                    fragment.setArguments(args);
                    bottomNavigationView.setSelectedItemId(R.id.customizedGifts);
                    break;
            }
        }
        replaceFragment(fragment);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.home) {
                    replaceFragment(new HomeFragment());
                    return true;
                } else if (item.getItemId() == R.id.categories) {
                    replaceFragment(new CategoriesFragment());
                    return true;
                } else if (item.getItemId() == R.id.profile) {
                    if (profileFragment == null) {
                        profileFragment = new ProfileFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("username", getIntent().getStringExtra("username"));
                        bundle.putString("email", getIntent().getStringExtra("email"));
                        profileFragment.setArguments(bundle);
                    }
                    replaceFragment(profileFragment);
                    return true;
                } else if (item.getItemId() == R.id.cart) {
                    replaceFragment(new CartFragment());
                    return true;
                } else if (item.getItemId() == R.id.customizedGifts) {
                    replaceFragment(new CustomizedGifts());
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout2, fragment);
        fragmentTransaction.commit();
    }
}