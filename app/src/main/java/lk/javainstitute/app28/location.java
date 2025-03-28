package lk.javainstitute.app28;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class location extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_location);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.map), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SupportMapFragment supportMapFragment = new SupportMapFragment();

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.frameLayoutMap, supportMapFragment);
        fragmentTransaction.commit();

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap) {

                LatLng latLng = new LatLng(6.874753299870477, 79.93688665219575);

                googleMap.animateCamera(
                        CameraUpdateFactory.newCameraPosition(
                                new CameraPosition.Builder()
                                        .target(latLng).zoom(18).build()
                        )
                );

                googleMap.addMarker(
                        new MarkerOptions()
                                .position(latLng)
                                .title("Cherish & Co.")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop))
                );

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        marker.setTitle("Hello! This is Cherish & Co.");
                        return false;
                    }
                });

                LatLng latLng1 = new LatLng(6.876314926433034, 79.93532935454675);
                LatLng latLng2 = new LatLng(6.877966225415671, 79.93510458890589);
                LatLng latLng3 = new LatLng(6.879529620258682, 79.93470731682594);

                googleMap.addMarker(
                        new MarkerOptions()
                                .position(latLng1)
                                .title("Cherish & Co.")
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.shop))
                );


            }
        });
    }
}