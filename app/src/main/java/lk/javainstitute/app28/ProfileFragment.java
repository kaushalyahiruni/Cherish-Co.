package lk.javainstitute.app28;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

import lk.javainstitute.app28.UserDB;

public class ProfileFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout existingAddressesLayout;
    private ImageView imageView;
    private String username;
    private String email;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        db = FirebaseFirestore.getInstance();
        existingAddressesLayout = view.findViewById(R.id.existingAddressesLayout);

        Button signUpButton = view.findViewById(R.id.button5);
        Button addAddressButton = view.findViewById(R.id.button);
        Button saveButton = view.findViewById(R.id.saveButton);
        CardView addAddressCardView = view.findViewById(R.id.addAddressCardView);
        TextView usernameTextView = view.findViewById(R.id.textView11);
        TextView emailTextView = view.findViewById(R.id.textView10);
        TextView viewOrdersTextView = view.findViewById(R.id.textView_view_orders);

        fetchUserData(view);

        signUpButton.setOnClickListener(view1 -> {
            Intent intent = new Intent(getActivity(), SignUp.class);
            startActivity(intent);
        });

        addAddressButton.setOnClickListener(view12 -> addAddressCardView.setVisibility(View.VISIBLE));

        saveButton.setOnClickListener(v -> {
            EditText recipientNameEditText = view.findViewById(R.id.recipientName);
            EditText phoneNumberEditText = view.findViewById(R.id.phoneNumber);
            EditText cityOrDistrictEditText = view.findViewById(R.id.cityOrDistrict);
            EditText addressEditText = view.findViewById(R.id.address);

            String recipientName = recipientNameEditText.getText().toString();
            String phoneNumber = phoneNumberEditText.getText().toString();
            String cityOrDistrict = cityOrDistrictEditText.getText().toString();
            String address = addressEditText.getText().toString();

            Map<String, Object> addressData = new HashMap<>();
            addressData.put("recipient", recipientName);
            addressData.put("mobile", phoneNumber);
            addressData.put("district", cityOrDistrict);
            addressData.put("user_address", address);

            db.collection("address")
                    .add(addressData)
                    .addOnSuccessListener(documentReference -> {
                        recipientNameEditText.setText("");
                        phoneNumberEditText.setText("");
                        cityOrDistrictEditText.setText("");
                        addressEditText.setText("");
                        addAddressCardView.setVisibility(View.GONE);
                        retrieveAndDisplayAddresses();
                    })
                    .addOnFailureListener(e -> {});
        });

        imageView = view.findViewById(R.id.imageView7);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), ProfileUpdate.class);
                intent.putExtra("userEmail", email);
                startActivity(intent);
            }
        });

        viewOrdersTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), Orders.class);
                startActivity(intent);
            }
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        retrieveAndDisplayAddresses();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("username", username);
        outState.putString("email", email);
    }

    private void fetchUserData(View view) {
        SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String signedInEmail = prefs.getString("email", null);

        if (signedInEmail != null && isNetworkAvailable()) {
            db.collection("user")
                    .whereEqualTo("email", signedInEmail)
                    .limit(1)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            username = document.getString("userName");
                            email = document.getString("email");
                            updateUI(view);
                        } else {
                            fetchFromSQLite(view);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (e instanceof FirebaseFirestoreException &&
                                ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.UNAVAILABLE) {
                            fetchFromSQLite(view);
                        } else {
                            Toast.makeText(getContext(), "Error fetching Firestore data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            fetchFromSQLite(view);
        }
    }

    private void fetchFromSQLite(View view) {
        UserDB userDB = new UserDB(getContext(), "app28.db", null, 1);
        SQLiteDatabase sqLiteDatabase = userDB.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery("SELECT username, email FROM user ORDER BY id DESC LIMIT 1", null);
        if (cursor.moveToFirst()) {
            username = cursor.getString(cursor.getColumnIndexOrThrow("username"));
            email = cursor.getString(cursor.getColumnIndexOrThrow("email"));
        } else {
            Log.d("userdb", "No user data found in SQLite");
        }
        cursor.close();
        sqLiteDatabase.close();

        updateUI(view);
    }

    private void updateUI(View view) {
        TextView usernameTextView = view.findViewById(R.id.textView11);
        TextView emailTextView = view.findViewById(R.id.textView10);
        Button signUpButton = view.findViewById(R.id.button5);

        if (username != null && email != null) {
            usernameTextView.setText(username);
            emailTextView.setText(email);
            signUpButton.setVisibility(View.GONE);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void retrieveAndDisplayAddresses() {
        if (existingAddressesLayout == null) {
            return;
        }

        existingAddressesLayout.removeAllViews();

        db.collection("address")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String documentId = document.getId();

                        LinearLayout newAddressLayout = new LinearLayout(getContext());
                        newAddressLayout.setOrientation(LinearLayout.HORIZONTAL);
                        LinearLayout.LayoutParams addressParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        addressParams.setMargins(0, 16, 0, 0);
                        newAddressLayout.setLayoutParams(addressParams);

                        LinearLayout textLayout = new LinearLayout(getContext());
                        textLayout.setOrientation(LinearLayout.VERTICAL);
                        textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                                0,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                1
                        ));

                        TextView recipientNameTextView = new TextView(getContext());
                        recipientNameTextView.setText(document.getString("recipient"));
                        textLayout.addView(recipientNameTextView);

                        TextView phoneNumberTextView = new TextView(getContext());
                        phoneNumberTextView.setText(document.getString("mobile"));
                        textLayout.addView(phoneNumberTextView);

                        TextView cityOrDistrictTextView = new TextView(getContext());
                        cityOrDistrictTextView.setText(document.getString("district"));
                        textLayout.addView(cityOrDistrictTextView);

                        TextView addressTextView = new TextView(getContext());
                        addressTextView.setText(document.getString("user_address"));
                        textLayout.addView(addressTextView);

                        newAddressLayout.addView(textLayout);

                        ImageView addressImageView = new ImageView(getContext());
                        addressImageView.setImageResource(R.drawable.bin1);
                        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        addressImageView.setLayoutParams(imageParams);

                        addressImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                db.collection("address")
                                        .document(documentId)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(getContext(), "Address deleted successfully", Toast.LENGTH_SHORT).show();
                                                retrieveAndDisplayAddresses();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(getContext(), "Failed to delete address: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });

                        newAddressLayout.addView(addressImageView);
                        existingAddressesLayout.addView(newAddressLayout);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Failed to load addresses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}