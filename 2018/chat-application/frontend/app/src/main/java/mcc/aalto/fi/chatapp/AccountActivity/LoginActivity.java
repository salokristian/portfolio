package mcc.aalto.fi.chatapp.AccountActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;

import mcc.aalto.fi.chatapp.MainActivity;
import mcc.aalto.fi.chatapp.Models.User;
import mcc.aalto.fi.chatapp.R;

public class LoginActivity extends AppCompatActivity {

    TextInputLayout emailTextInputLayout, passwordTextInputLayout;
    EditText emailEditTxt, passwordEditTxt;
    Button loginButton;
    TextView signupTxtView;
    FirebaseAuth firebaseAuth;
    ProgressDialog progressDialog;
    FirebaseDatabase firebaseDatabase;

    String DB_REFERENCE_USERS = "users";
    String DB_REFERENCE_PUBLIC = "public";
    String DB_REFERENCE_NOTIFICATION_TOKENS = "notificationTokens";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // TextLayoutInputs
        emailTextInputLayout = findViewById(R.id.loginEmailTextInputLayout);
        passwordTextInputLayout = findViewById(R.id.loginPasswordTextInputLayout);
        // EditTexts
        emailEditTxt = findViewById(R.id.loginEmailEditText);
        passwordEditTxt = findViewById(R.id.loginPasswordEditText);
        // Others
        signupTxtView = findViewById(R.id.loginSignupTextView);
        loginButton = findViewById(R.id.loginButton);

        // Set button listeners
        loginButton.setOnClickListener(v -> {
            String email = emailEditTxt.getText().toString().trim();
            String password = passwordEditTxt.getText().toString().trim();

            login(email, password);
        });

        // Set text changed listeners
        emailEditTxt.addTextChangedListener(loginTextWatcher);
        passwordEditTxt.addTextChangedListener(loginTextWatcher);

        initializeFirebase();

        // Hide keyboard when activity started.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    /**
     * Starts SignupActivity
     *
     * @param v view
     */
    public void onClickSignup(View v) {
        startActivity(new Intent(getApplicationContext(), SignupActivity.class));
    }


    /**
     * Creates TextWatcher for editTexts
     *
     * @return created TextWatcher
     */
    private TextWatcher loginTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String email = emailEditTxt.getText().toString().trim();
            String password = passwordEditTxt.getText().toString().trim();

            if (!email.isEmpty()) {
                emailTextInputLayout.setErrorEnabled(false);
            }

            if (!password.isEmpty()) {
                passwordTextInputLayout.setErrorEnabled(false);
            }
        }
    };


    /**
     * Initialize FireBase for the current activity
     */
    private void initializeFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }
    }


    /**
     * Shows a ProgressDialog during loggining in process
     */
    private void showLogginingDialog() {
        progressDialog = ProgressDialog.show(LoginActivity.this, getString(R.string.login_in_progress), getString(R.string.login_waiting), true, true);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    }


    /**
     * Shows a AlertDialog when loggining in is failed.
     */
    private void showFailedLoginDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
        alertDialog.setTitle(getString(R.string.login_failed_title));
        alertDialog.setMessage(getString(R.string.login_failed_explanation));

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.login_failed_confirm),
                (dialog, which) -> {
                    dialog.dismiss();
                });

        alertDialog.show();
    }


    /**
     * Authenticate user and logs user in. Shows the main activity as a result of successful login.
     *
     * @param email
     * @param password
     */
    private void login(String email, String password) {
        if (!areInputsValid(email, password)) {
            return;
        } else {
            showLogginingDialog();
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(LoginActivity.this, task -> {
                        progressDialog.dismiss();

                        if (!task.isSuccessful()) {
                            showFailedLoginDialog();

                        } else {
                            insertUserIntoDb();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
        }

    }


    /**
     * Checks if provided user information is valid for loggining in
     *
     * @param email
     * @param password
     * @return boolean true for valid false for invalid
     */
    private boolean areInputsValid(String email, String password) {
        boolean inputAreValid = true;

        if (TextUtils.isEmpty(email)) {
            emailTextInputLayout.setError(getString(R.string.login_empty_email));
            inputAreValid = false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordTextInputLayout.setError(getString(R.string.login_empty_password));
            inputAreValid = false;
        }

        return inputAreValid;
    }


    /**
     * Inserts information of logged user and notification token for the current device into FireBase users database
     */
    private void insertUserIntoDb() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        String token = FirebaseInstanceId.getInstance().getToken();
        List<String> notificationTokens = new ArrayList<>();

        DatabaseReference userDatabase = firebaseDatabase.getReference(DB_REFERENCE_USERS);
        DatabaseReference userRef = userDatabase.child(DB_REFERENCE_PUBLIC).child(firebaseAuth.getCurrentUser().getUid()).child(DB_REFERENCE_NOTIFICATION_TOKENS);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {};

                if (dataSnapshot.getValue(genericTypeIndicator) != null) {
                    notificationTokens.addAll(dataSnapshot.getValue(genericTypeIndicator));
                }
                if (!notificationTokens.contains(token)) {
                    notificationTokens.add(token);
                }

                User user = new User(currentUser.getDisplayName(), currentUser.getPhotoUrl().toString(), notificationTokens);
                userDatabase.child(DB_REFERENCE_PUBLIC).child(currentUser.getUid()).setValue(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // On addListenerForSingleValueEvent cancelled
            }
        });
    }

}
