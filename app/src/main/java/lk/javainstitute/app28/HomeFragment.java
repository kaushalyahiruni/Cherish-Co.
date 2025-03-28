package lk.javainstitute.app28;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private List<Item> itemList;
    private FirebaseFirestore firestore;

    private int[] imageResources = {
            R.drawable.p4,
            R.drawable.p13,
            R.drawable.p6,
            R.drawable.p12,
            R.drawable.p14,
            R.drawable.p3,
            R.drawable.p26,
            R.drawable.p15,
            R.drawable.p16,
            R.drawable.p6,
            R.drawable.p17,
            R.drawable.p18,
            R.drawable.p7,
            R.drawable.p19,
            R.drawable.p20,
            R.drawable.p21,
            R.drawable.p23,
            R.drawable.p22,
            R.drawable.p9,

    };

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final int SHAKE_THRESHOLD = 800;
    private long lastUpdate = 0;
    private float lastX, lastY, lastZ;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        itemList = new ArrayList<>();
        itemAdapter = new ItemAdapter(getContext(), itemList);
        recyclerView.setAdapter(itemAdapter);

        firestore = FirebaseFirestore.getInstance();

        loadInitialData();

        SearchView searchView = view.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchProducts(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchProducts(newText);
                return true;
            }
        });

        if (getActivity() != null) {
            sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null && accelerometer != null) {
            sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    private final SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long currentTime = System.currentTimeMillis();
                if ((currentTime - lastUpdate) > 100) {
                    long timeDiff = currentTime - lastUpdate;
                    lastUpdate = currentTime;

                    float x = event.values[0];
                    float y = event.values[1];
                    float z = event.values[2];

                    float speed = Math.abs(x + y + z - lastX - lastY - lastZ) / timeDiff * 10000;
                    if (speed > SHAKE_THRESHOLD) {
                        openSearchView();
                    }

                    lastX = x;
                    lastY = y;
                    lastZ = z;
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };

    private void openSearchView() {
        if (getView() != null) {
            SearchView searchView = getView().findViewById(R.id.searchView);
            if (searchView != null) {
                searchView.setIconified(false);
                searchView.requestFocus();
                if (getActivity() != null) {
                    android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(searchView, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }
        }
    }

    private void loadInitialData() {
        firestore.collection("products").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int index = 0;
                for (DocumentSnapshot document : task.getResult()) {
                    String title = document.getString("title");
                    String price = document.getString("price");
                    String description = document.getString("description");

                    int imageResourceId = imageResources[index % imageResources.length];
                    itemList.add(new Item(imageResourceId, title, price, description));

                    index++;
                }
                itemAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void searchProducts(String query) {
        itemList.clear();
        itemAdapter.notifyDataSetChanged();

        Query firestoreQuery = firestore.collection("products")
                .orderBy("title")
                .startAt(query)
                .endAt(query + "\uf8ff");

        firestoreQuery.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                int index = 0;
                for (DocumentSnapshot document : task.getResult()) {
                    String title = document.getString("title");
                    String price = document.getString("price");
                    String description = document.getString("description");

                    int imageResourceId = imageResources[index % imageResources.length];
                    itemList.add(new Item(imageResourceId, title, price, description));

                    index++;
                }
                itemAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getContext(), "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void retrieveAndDisplayAddresses() {
        Toast.makeText(getContext(), "Addresses retrieval not implemented here.", Toast.LENGTH_SHORT).show();
    }
}