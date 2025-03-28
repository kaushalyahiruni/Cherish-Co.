package lk.javainstitute.app28;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class SignIn extends AppCompatActivity {

    private static final String TAG = "SignIn";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.constraintLayoutSignIn), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button button2 = findViewById(R.id.buttonSignIn);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextInputLayout textInputLayout1 = findViewById(R.id.textInputLayoutEmail);
                TextInputLayout textInputLayout2 = findViewById(R.id.textInputLayoutPassword);

                String email = textInputLayout1.getEditText().getText().toString().trim();
                String password = textInputLayout2.getEditText().getText().toString().trim();

                if (email.isEmpty()) {
                    new AlertDialog.Builder(view.getContext())
                            .setMessage("Email is required")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                } else if (!email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
                    new AlertDialog.Builder(view.getContext())
                            .setMessage("Enter a valid email")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                if (password.isEmpty()) {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Empty Password")
                            .setMessage("Password is required")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                } else if (!password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$")) {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Invalid Password")
                            .setMessage("Password must be at least 8 characters long:\n" +
                                    "- 1 uppercase letter (A-Z)\n" +
                                    "- 1 lowercase letter (a-z)\n" +
                                    "- 1 number (0-9)\n" +
                                    "- 1 special character (@#$%^&+=)")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                searchUser(email, password, view.getContext());
            }
        });
    }

    private void searchUser(String email, String password, Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("user")
                .whereEqualTo("email", email)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        boolean isUserValid = false;
                        String username = "";

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String storedPassword = document.getString("password");
                            if (storedPassword != null && storedPassword.equals(password)) {
                                isUserValid = true;
                                username = document.getString("username");
                                break;
                            }
                        }

                        if (isUserValid) {
                            Log.d(TAG, "SignIn successful - Username: " + username + ", Email: " + email);
                            Intent intent = new Intent(SignIn.this, MainActivity3.class);
                            intent.putExtra("username", username);
                            intent.putExtra("email", email);
                            intent.putExtra("navigateTo", "ProfileFragment");
                            startActivity(intent);
                            finish();

                            saveUserData(username, email);

                            SharedPreferences sharedPreferences2 = getSharedPreferences("lk.javainstitute.app28.userdata", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor2 = sharedPreferences2.edit();
                            editor2.putBoolean("isSignedIn", true);
                            editor2.apply();
                        } else {
                            showErrorDialog("Invalid details, please try again.");
                        }
                    } else {
                        showErrorDialog("Invalid details, please try again.");
                    }
                })
                .addOnFailureListener(e -> {
                    showErrorDialog("Error: " + e.getMessage());
                });
    }

    private void saveUserData(String username, String email) {
        getSharedPreferences("UserPrefs", MODE_PRIVATE)
                .edit()
                .putString("username", username)
                .putString("email", email)
                .apply();
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(SignIn.this)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }
}