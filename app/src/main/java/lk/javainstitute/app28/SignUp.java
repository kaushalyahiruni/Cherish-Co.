package lk.javainstitute.app28;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.constraintLayout1), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView textView = findViewById(R.id.textViewGotoSignIn);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, SignIn.class);
                startActivity(intent);
            }
        });

        Button button2 = findViewById(R.id.buttonSignUp);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextInputLayout textInputLayout1 = findViewById(R.id.textInputLayout1);
                TextInputLayout textInputLayout2 = findViewById(R.id.textInputLayout2);
                TextInputLayout textInputLayout3 = findViewById(R.id.textInputLayout3);
                TextInputLayout textInputLayout4 = findViewById(R.id.textInputLayout4);

                String userName = textInputLayout1.getEditText().getText().toString().trim();
                String mobile = textInputLayout2.getEditText().getText().toString().trim();
                String email = textInputLayout3.getEditText().getText().toString().trim();
                String password = textInputLayout4.getEditText().getText().toString().trim();

                if (userName.isEmpty()) {
                    new AlertDialog.Builder(view.getContext())
                            .setMessage("Username is required")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                } else if (!userName.matches("^[a-zA-Z]+$")) {
                    new AlertDialog.Builder(view.getContext())
                            .setTitle("Invalid username Name")
                            .setMessage("Username should contain only letters")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

                if (mobile.isEmpty()) {
                    new AlertDialog.Builder(view.getContext())
                            .setMessage("Mobile is required")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                } else if (!mobile.matches("^[0-9]{10}$")) {
                    new AlertDialog.Builder(view.getContext())
                            .setMessage("Enter a valid 10-digit mobile number")
                            .setPositiveButton("OK", null)
                            .show();
                    return;
                }

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

                Log.i("Cherish", userName);
                Log.i("Cherish", mobile);
                Log.i("Cherish", email);
                Log.i("Cherish", password);

                saveUser(userName, mobile, email, password);

                Intent intent = new Intent(SignUp.this, SignIn.class);
                intent.putExtra("navigateTo", "ProfileFragment");
                startActivity(intent);
                finish();

                textInputLayout1.getEditText().setText("");
                textInputLayout2.getEditText().setText("");
                textInputLayout3.getEditText().setText("");
                textInputLayout4.getEditText().setText("");
            }
        });
    }

    private void saveUser(String userName, String mobile, String email, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("userName", userName);
        user.put("mobile", mobile);
        user.put("email", email);
        user.put("password", password);
        user.put("status", "Active");

        db.collection("user")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    Log.d("userdb", "User added to Firestore with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.w("userdb", "Error adding user to Firestore", e);
                });

        UserDB userDB = new UserDB(this, "app28.db", null, 1);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    SQLiteDatabase sqLiteDatabase = userDB.getWritableDatabase();
                    sqLiteDatabase.execSQL("INSERT INTO `user` (`username`, `mobile`, `email`, `password`) " +
                            "VALUES (?, ?, ?, ?)", new String[]{userName, mobile, email, password});
                    Log.i("Cherish", "User saved to SQLite");
                } catch (Exception e) {
                    Log.e("Cherish", "Error saving to SQLite: " + e.getMessage());
                }
            }
        }).start();
    }
}

class UserDB extends SQLiteOpenHelper {
    public UserDB(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE user (\n" +
                "    id       INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "    username TEXT    NOT NULL,\n" +
                "    mobile   TEXT    NOT NULL,\n" +
                "    email    TEXT    NOT NULL,\n" +
                "    password TEXT    NOT NULL\n" +
                ");\n");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS user");
        onCreate(sqLiteDatabase);
    }
}