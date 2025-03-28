package lk.javainstitute.app28;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import com.google.android.material.textfield.TextInputEditText;

import java.util.HashMap;
import java.util.Map;

public class ProfileUpdate extends AppCompatActivity {

    private TextView textViewTitle;
    private TextInputEditText editTextUsername;
    private TextInputEditText editTextMobile;
    private TextInputEditText editTextEmail;
    private Button buttonUpdate;
    private Button buttonLogout;
    private Button buttonaboutUs;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String documentId;
    private String currentUserEmail; // To store the current user's email
    private UserDB userDB; // SQLite database helper

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile_update);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profileUpdate), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        textViewTitle = findViewById(R.id.textViewTitle);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextMobile = findViewById(R.id.editTextMobile);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonUpdate = findViewById(R.id.buttonUpdate);
        buttonLogout = findViewById(R.id.buttonLogout);
        buttonaboutUs = findViewById(R.id.button2);

        // Initialize SQLite database
        userDB = new UserDB(this, "app28.db", null, 1);

        Intent intent = getIntent();
        currentUserEmail = intent.getStringExtra("userEmail");
        if (currentUserEmail == null) {
            Toast.makeText(this, "No user email provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchUserDataByEmail(currentUserEmail);

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String updatedUsername = editTextUsername.getText().toString().trim();
                String updatedMobile = editTextMobile.getText().toString().trim();
                String updatedEmail = editTextEmail.getText().toString().trim();

                if (updatedUsername.isEmpty() || updatedMobile.isEmpty() || updatedEmail.isEmpty()) {
                    Toast.makeText(ProfileUpdate.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (documentId != null) {
                    Map<String, Object> updatedUser = new HashMap<>();
                    updatedUser.put("userName", updatedUsername);
                    updatedUser.put("mobile", updatedMobile);
                    updatedUser.put("email", updatedEmail);

                    db.collection("user")
                            .document(documentId)
                            .update(updatedUser)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(ProfileUpdate.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                    Log.d("Firestore", "Document successfully updated");
                                    currentUserEmail = updatedEmail; // Update the current email if it changed

                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                SQLiteDatabase sqLiteDatabase = userDB.getWritableDatabase();
                                                sqLiteDatabase.execSQL(
                                                        "UPDATE user SET username = ?, mobile = ?, email = ? WHERE email = ?",
                                                        new String[]{updatedUsername, updatedMobile, updatedEmail, currentUserEmail}
                                                );
                                                Log.i("SQLite", "User data updated in SQLite");
                                            } catch (Exception e) {
                                                Log.e("SQLite", "Error updating SQLite: " + e.getMessage());
                                            }
                                        }
                                    }).start();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ProfileUpdate.this, "Error updating profile in Firestore", Toast.LENGTH_SHORT).show();
                                Log.w("Firestore", "Error updating document", e);
                            });
                } else {
                    Toast.makeText(ProfileUpdate.this, "No user document selected", Toast.LENGTH_SHORT).show();
                }
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileUpdate.this, SignIn.class);
                startActivity(intent);
                finish();
                Toast.makeText(ProfileUpdate.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            }
        });

        buttonaboutUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileUpdate.this, location.class);
                startActivity(intent);
                finish();
            }
        });

        TextView textViewContactUs = findViewById(R.id.textViewContactUs);
        textViewContactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:+94784451213"));
                startActivity(intent);
            }
        });

    }

    private void fetchUserDataByEmail(String email) {
        db.collection("user")
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                            documentId = document.getId();

                            String username = document.getString("userName");
                            String mobile = document.getString("mobile");
                            String emailFromDb = document.getString("email");

                            editTextUsername.setText(username != null ? username : "");
                            editTextMobile.setText(mobile != null ? mobile : "");
                            editTextEmail.setText(emailFromDb != null ? emailFromDb : "");

                        } else {
                            Toast.makeText(ProfileUpdate.this, "No user data found for this email in Firestore.", Toast.LENGTH_SHORT).show();
                            Log.d("Firestore", "No documents found for email: " + email);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ProfileUpdate.this, "Error fetching user data.", Toast.LENGTH_SHORT).show();
                    Log.w("Firestore", "Error getting user data", e);
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (userDB != null) {
            userDB.close(); // Close the database when the activity is destroyed
        }
    }
}