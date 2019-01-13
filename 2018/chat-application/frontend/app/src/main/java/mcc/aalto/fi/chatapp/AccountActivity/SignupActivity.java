package mcc.aalto.fi.chatapp.AccountActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import mcc.aalto.fi.chatapp.MainActivity;
import mcc.aalto.fi.chatapp.Models.Image;
import mcc.aalto.fi.chatapp.Models.User;
import mcc.aalto.fi.chatapp.R;

public class SignupActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_FROM_GALLERY = 1;

    private static final String DB_REFERENCE_USERS = "users";
    private final String DB_REFERENCE_PUBLIC = "public";
    private final String DB_REFERENCE_PRIVATE = "private";
    private static final String DB_REFERENCE_PROFILE_IMAGE = "profileImages";
    private final String DB_REFERENCE_INDEX = "name_index";
    private static final String TAG = "SIGNUPACTIVITY";

    private final int MIN_PASSWORD_LENGTH = 8;
    private final int MIN_DISPLAY_NAME_LENGTH = 4;

    TextInputLayout displayNameTextInputLayout, emailTextInputLayout, passwordTextInputLayout;
    EditText emailEditTxt, passwordEditTxt, displayNameEditTxt;
    TextView signupProfileTextView;
    ImageView signupProfileImageView;
    Button signupButton;
    FirebaseAuth firebaseAuth;
    ActionBar actionBar;
    ProgressDialog progressDialog;
    FirebaseDatabase firebaseDatabase;
    Bitmap profileImageBitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // TextInputLayouts
        displayNameTextInputLayout = findViewById(R.id.signupDisplayNameTextInputLayout);
        emailTextInputLayout = findViewById(R.id.signupEmailTextInputLayout);
        passwordTextInputLayout = findViewById(R.id.signupPasswordTextInputLayout);
        // EditTexts
        displayNameEditTxt = findViewById(R.id.signupDisplayNameEditText);
        emailEditTxt = findViewById(R.id.signupEmailEditText);
        passwordEditTxt = findViewById(R.id.signupPasswordEditText);
        // Buttons
        signupButton = findViewById(R.id.signupButton);
        // TextViews
        signupProfileTextView = findViewById(R.id.signupProfileTextView);
        // ImageViews
        signupProfileImageView = findViewById(R.id.signupProfileImageView);
        // Others
        actionBar = getSupportActionBar();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        // Set button listeners
        signupButton.setOnClickListener(v -> {
            String displayName = displayNameEditTxt.getText().toString().trim();
            String email = emailEditTxt.getText().toString().trim();
            String password = passwordEditTxt.getText().toString().trim();

            areInputsValid(email, password, displayName);

        });

        // Set text changed listeners
        displayNameEditTxt.addTextChangedListener(signupTextWatcher);
        emailEditTxt.addTextChangedListener(signupTextWatcher);
        passwordEditTxt.addTextChangedListener(signupTextWatcher);

        // Set back button
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Hide keyboard when activity started.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        handleActivityResult(this, requestCode, resultCode, imageReturnedIntent, (bitmap -> {
            profileImageBitmap = bitmap;
            loadImageInto(this, profileImageBitmap, signupProfileImageView);
        }));
    }

    public static void handleActivityResult(Activity activity, int requestCode, int resultCode, Intent imageReturnedIntent, Consumer<Bitmap> callback) {
        if (resultCode == RESULT_OK) {

            if (requestCode == PICK_IMAGE_FROM_GALLERY) {
                Uri selectedImage = imageReturnedIntent.getData();

                try {
                    callback.accept(MediaStore.Images.Media.getBitmap(activity.getContentResolver(), selectedImage));
                } catch (IOException e) {
                    Log.w(TAG, "onActivityResult error: " + e.getMessage());
                }

            } else {
                Log.w(TAG, Integer.toString(resultCode));
            }
        }
    }


    /**
     * Signs up a user and sets profile information
     *
     * @param email
     * @param password
     * @param displayName
     */
    private void signup(String email, String password, String displayName) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    finalizeSigningup(displayName);
                } else {
                    showFailedSignupDialog();
                }
            });
    }

    /**
     * Helper method for finalizing signing up.
     *
     * @param displayName
     */
    private void finalizeSigningup(String displayName) {
        String uId = firebaseAuth.getCurrentUser().getUid();

        if (profileImageBitmap != null) {
            uploadImage(getResources(), profileImageBitmap, uId, (path) -> addUserAndUpdateProfile(displayName, path));
        } else {
            addUserAndUpdateProfile(displayName, "notavailable");
        }
    }

    public static void uploadImage(Resources res, Bitmap image, String uId, Consumer<String> callback) {
        UploadTask profileImageUploadTask = saveProfileImage(res, image, uId);
        new Thread(() -> {
            try {
                com.google.android.gms.tasks.Tasks.await(profileImageUploadTask);
                StorageReference imageRef = FirebaseStorage.getInstance().getReference()
                        .child(DB_REFERENCE_USERS)
                        .child(uId)
                        .child(DB_REFERENCE_PROFILE_IMAGE)
                        .child(uId);

                String imagePath = imageRef.getPath();
                callback.accept(imagePath);
                    /*
                    imageRef.child(uId).getDownloadUrl().addOnSuccessListener(uri -> {
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "finalizeSigningup error: " + e.getMessage());
                    }); */

            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }


    /**
     * Add user data into database and saves profile settings
     *
     * @param displayName
     * @param uri
     */
    private void addUserAndUpdateProfile(String displayName, String uri) {
        insertUserIntoDb(displayName, uri);
        Task<Void> profileUpdate = setUserProfile(displayName, uri);

        profileUpdate.addOnCompleteListener(t -> {
            progressDialog.dismiss();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        });
    }


    /**
     * Inserts information of logged user and notification token for the current device into FireBase users database
     *
     * @param displayName
     * @param photoUrl
     */
    private void insertUserIntoDb(String displayName, String photoUrl) {
        DatabaseReference userDatabase = firebaseDatabase.getReference(DB_REFERENCE_USERS);
        String uId = firebaseAuth.getCurrentUser().getUid();
        String token = FirebaseInstanceId.getInstance().getToken();

        while (token == null) {
            token = FirebaseInstanceId.getInstance().getToken();
        }

        User user = new User(displayName, photoUrl, Arrays.asList(token));

        // Set default image sizing preferences for the user
        Map<String, String> imagePreferences = new HashMap<String, String>() {
            {
                put("imageSizing", "low");
            }
        };

        userDatabase.child(DB_REFERENCE_PUBLIC).child(uId).setValue(user);
        userDatabase.child(DB_REFERENCE_PRIVATE).child(uId).setValue(imagePreferences);
    }

    /**
     * Checks if the picked username is already in use
     *
     * @param email
     * @param password
     * @param displayName
     *
     */
    private void displayNameCheckup(String email, String password, String displayName){
        DatabaseReference userDatabase = firebaseDatabase.getReference(DB_REFERENCE_USERS);
        userDatabase.child(DB_REFERENCE_INDEX).child(displayName).setValue(true, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    displayNameTextInputLayout.setError(getString(R.string.signup_failed_display_name_not_unique));
                    SigningupDialog(false);
                } else {
                    signup(email, password, displayName);
                }
            }
        });
    }

    /**
     * Checks if provided user information is valid for signing up
     *
     * @param email
     * @param password
     * @param displayName
     * @return boolean true for valid false for invalid
     */
    private void areInputsValid(String email, String password, String displayName) {
        SigningupDialog(true);


        if (displayName.isEmpty() || displayName.length() < MIN_DISPLAY_NAME_LENGTH) {
            displayNameTextInputLayout.setError(getString(R.string.signup_failed_display_name_short));
            SigningupDialog(false);

        } else if (email.isEmpty() || !isEmailValid(email)) {
            emailTextInputLayout.setError(getString(R.string.signup_failed_email));
            SigningupDialog(false);

        } else if (password.isEmpty() || password.length() < MIN_PASSWORD_LENGTH) {
            passwordTextInputLayout.setError(getString(R.string.signup_failed_password));
            SigningupDialog(false);

        } else {
            displayNameCheckup( email, password, displayName);
        }
    }

    /**
     * Creates TextWatcher for editTexts
     *
     * @return created TextWatcher
     */
    private TextWatcher signupTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            String displayName = displayNameEditTxt.getText().toString().trim();
            String email = emailEditTxt.getText().toString().trim();
            String password = passwordEditTxt.getText().toString().trim();

            if (!displayName.isEmpty()) {
                displayNameTextInputLayout.setErrorEnabled(false);
            }
            if (!email.isEmpty()) {
                emailTextInputLayout.setErrorEnabled(false);
            }

            if (!password.isEmpty()) {
                passwordTextInputLayout.setErrorEnabled(false);
            }
        }
    };


    /**
     * Shows a ProgressDialog during signing up process
     * @param run true to turn on, false to turn off
     */
    private void SigningupDialog(boolean run){
        if (run == true) {
            progressDialog = ProgressDialog.show(SignupActivity.this, getString(R.string.signup_in_progress), getString(R.string.signup_waiting), true, true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        } else {
            progressDialog.cancel();
        }

    }

    /**
     * Shows a AlertDialog when signing up is failed.
     */
    private void showFailedSignupDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(SignupActivity.this).create();
        alertDialog.setTitle(getString(R.string.signup_failed_title));
        alertDialog.setMessage(getString(R.string.signup_failed_explanation));

        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.signup_failed_confirm),
                (dialog, which) -> {
                    dialog.dismiss();
                });

        alertDialog.show();
    }


    /**
     * Checks if provided email's format is correct
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Set user profile name and photo
     *
     * @param displayName
     * @return An asynchronous operation which sets profile settings.
     */
    private Task<Void> setUserProfile(String displayName, String url) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(Uri.parse(url))
                    .build();

            return user.updateProfile(profileUpdates);
        } else {
            Toast.makeText(getApplicationContext(), getString(R.string.signup_profile_update_failed), Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    /**
     * Opens gallery to select a profile photo
     *
     * @param view
     */
    public void onClickOpenGallery(View view) {
        Intent pickImage = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickImage, PICK_IMAGE_FROM_GALLERY);
    }


    /**
     * Saves profile image to storage
     *
     * @param image
     * @param userId
     * @return UploadTask
     */
    private static UploadTask saveProfileImage(Resources res, Bitmap image, String userId) {
        StorageReference userStorageRef = FirebaseStorage.getInstance().getReference()
                .child(DB_REFERENCE_USERS)
                .child(userId)
                .child(DB_REFERENCE_PROFILE_IMAGE);

        Bitmap scaledImage = Image.scaleImageForUpload(image, res.getString(R.string.signup_default_image_size));

        StorageMetadata imageMetadata = new StorageMetadata.Builder()
                .setCustomMetadata("imageSizing", res.getString(R.string.signup_default_image_size))
                .build();

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        scaledImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] imageByteArray = stream.toByteArray();

        StorageReference imageRef = userStorageRef.child(userId);

        return imageRef.putBytes(imageByteArray, imageMetadata);
    }

    /**
     * Loads selected profile image
     *
     * @param bitmap profile photo in bitmap format
     */
    public static void loadImageInto(Activity activity, Bitmap bitmap, ImageView view) {
        RequestOptions options = new RequestOptions();
        options.fitCenter();

        Glide.with(activity)
                .asBitmap()
                .load(bitmap)
                .apply(options)
                .into(view);
    }


}
