package com.spirovski.selfjourn;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.spirovski.selfjourn.util.JournalApi;

public class LoginActivity extends AppCompatActivity {
    private Button loginButton;
    private Button createAccButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //Firestore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");

    private AutoCompleteTextView emailAdress;
    private EditText password;
    private ProgressBar progressBar;
    private EditText userNameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        progressBar = findViewById(R.id.login_progress);

        emailAdress = findViewById(R.id.email);
        password = findViewById(R.id.password);

        loginButton = findViewById(R.id.email_sign_in_button);
        createAccButton = findViewById(R.id.create_acct_button_login);

        loginButton.setOnClickListener(view ->
                loginEmailPasswordUser(emailAdress.getText().toString(), password.getText().toString()));

        createAccButton.setOnClickListener(view ->
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class)));

    }

    private void loginEmailPasswordUser(String email, String pwd) {
        progressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd)) {

            firebaseAuth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    assert user != null;
                    String currentUserId = user.getUid();

                    collectionReference.whereEqualTo("userId", currentUserId).addSnapshotListener((value, error) -> {
                        assert value != null;

                        if (!value.isEmpty()) {
                            progressBar.setVisibility(View.INVISIBLE);

                            for (QueryDocumentSnapshot snapshot : value) {
                                JournalApi journalApi = JournalApi.getInstance();
                                journalApi.setUsername(snapshot.getString("username"));
                                journalApi.setUserId(snapshot.getString("userId"));

                                startActivity(new Intent(this, PostJournalActivity.class));
                            }
                        } else {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(LoginActivity.this, "No user data available.", Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                progressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Email or Password is incorrect", Toast.LENGTH_SHORT).show();
            });

        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_LONG).show();
        }
    }
}