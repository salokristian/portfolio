package mcc.aalto.fi.chatapp.AccountSettingsFragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import mcc.aalto.fi.chatapp.AccountActivity.LoginActivity;
import mcc.aalto.fi.chatapp.AccountActivity.SignupActivity;
import mcc.aalto.fi.chatapp.GlideApp;
import mcc.aalto.fi.chatapp.MainActivity;
import mcc.aalto.fi.chatapp.Modals;
import mcc.aalto.fi.chatapp.Models.User;
import mcc.aalto.fi.chatapp.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class AccountSettingsFragment extends Fragment {
    private final int PICK_IMAGE_FROM_GALLERY = 1;

    static String DB_REFERENCE_USERS = "users";
    static String DB_REFERENCE_PUBLIC = "public";
    static String DB_REFERENCE_NOTIFICATION_TOKENS = "notificationTokens";

    String userId, originalUsername;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;

    EditText displayName;
    TextView profileImageChange;
    TextView email;
    ImageView profileImage;
    Spinner imageSizes;
    Button saveButton;
    static ProgressDialog progressDialog;
    Bitmap profileImageBitmap;


    public AccountSettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(false);

        this.firebaseAuth = FirebaseAuth.getInstance();
        this.firebaseDatabase = FirebaseDatabase.getInstance();
        this.userId = firebaseAuth.getUid();

        profileImage = view.findViewById(R.id.profileImage);
        profileImageChange = view.findViewById(R.id.profileImageChange);
        displayName = view.findViewById(R.id.username);
        email = view.findViewById(R.id.email);
        imageSizes = view.findViewById(R.id.imageQualitySpinner);
        saveButton = view.findViewById(R.id.saveButton);


        profileImage.setOnClickListener(view1 -> {
            Intent pickImage = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickImage, PICK_IMAGE_FROM_GALLERY);
        });

        profileImageChange.setOnClickListener(view1 -> {
            Intent pickImage = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickImage, PICK_IMAGE_FROM_GALLERY);
        });

        getSelf(user -> {
            displayName.setText(user.displayName);
            email.setText(firebaseAuth.getCurrentUser().getEmail());

            if (user.photoUrl != null && !user.photoUrl.isEmpty()) {
                profileImage.setImageURI(Uri.parse(user.photoUrl));

                StorageReference senderImageRef = FirebaseStorage.getInstance().getReference().child(user.photoUrl);
                GlideApp.with(getContext())
                        .load(senderImageRef)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .placeholder(R.drawable.chat_app_profile_photo)
                        .circleCrop()
                        .into(profileImage);
            }

            originalUsername = user.displayName;

            List<String> sizes = Arrays.asList(getResources().getStringArray(R.array.setting_image_sizes));

            if (!sizes.contains(user.imageSizing)) {
                user.imageSizing = sizes.get(1);
            }
            imageSizes.setSelection(sizes.indexOf(user.imageSizing));
        });

        saveButton.setOnClickListener(v -> {
            String newName = displayName.getText().toString();
            setNewNameIfAvailable(newName, (isAvailable) -> {
                if (!isAvailable) {
                    Modals.showMessage(getContext(), "The username '" + newName + "' is not available.");
                    return;
                }

                if (!originalUsername.equals(newName)) {
                    firebaseDatabase.getReference().child("users").child("name_index").child(originalUsername).removeValue();
                }
                firebaseDatabase.getReference().child("users").child("private").child(userId).child("imageSizing").setValue(imageSizes.getSelectedItem().toString());
                firebaseDatabase.getReference().child("users").child("public").child(userId).child("displayName").setValue(displayName.getText().toString());

                originalUsername = newName;
            });
            showConfirmation();
            // Close fragment after saving settings
            getActivity().getSupportFragmentManager().popBackStackImmediate();
        });
    }

    private void showConfirmation() {
        Snackbar snackbar = Snackbar
                .make(getView(), getString(R.string.profile_updated), Snackbar.LENGTH_LONG);

        snackbar.show();
    }

    public void setNewNameIfAvailable(String name, Consumer<Boolean> callback) {
        firebaseDatabase.getReference().child("users").child("name_index").child(name).setValue(true,
                (databaseError, databaseReference) -> callback.accept(databaseError == null)
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Display arrow in action bar for back navigation to all chats view
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getActivity().setTitle("Profile Settings");

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account_settings, container, false);
    }

    /**
     * Removes the notification token of the current device and logs out
     */
    public static void logout(FirebaseAuth auth, FirebaseDatabase db, Consumer<Boolean> callback, Context context) {
        showProgressDialog(context, true);
        String token = FirebaseInstanceId.getInstance().getToken();
        String uId = auth.getUid();
        DatabaseReference userDatabase = db.getReference(DB_REFERENCE_USERS);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    FirebaseInstanceId.getInstance().deleteInstanceId();

                    DatabaseReference userRef = userDatabase.child(DB_REFERENCE_PUBLIC).child(uId).child(DB_REFERENCE_NOTIFICATION_TOKENS);

                    userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // GenericTypeIndicator helps to resolve types for generic collections at runtime
                            GenericTypeIndicator<List<String>> genericTypeIndicator = new GenericTypeIndicator<List<String>>() {
                            };
                            List<String> notificationTokens = dataSnapshot.getValue(genericTypeIndicator);
                            // Remove current token from the list
                            notificationTokens.remove(token);
                            // Update users database
                            userDatabase.child(DB_REFERENCE_PUBLIC).child(uId).child(DB_REFERENCE_NOTIFICATION_TOKENS).removeValue();
                            userDatabase.child(DB_REFERENCE_PUBLIC).child(uId).child(DB_REFERENCE_NOTIFICATION_TOKENS).setValue(notificationTokens);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // On addListenerForSingleValueEvent cancelled
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                clearAuth(auth, callback);
                showProgressDialog(context, false);
            }
        }.execute();
    }

    private static void showProgressDialog(Context context, boolean show){
        if (show) {
            progressDialog = ProgressDialog.show(context, "Logging out", "Please wait...", true, true);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        } else {
            progressDialog.dismiss();
        }

    }

    /**
     * Logs out and shows LoginActivity
     */
    public static void clearAuth(FirebaseAuth auth, Consumer<Boolean> callback) {
        auth.signOut();
        callback.accept(auth.getUid() == null);
    }

    private void getSelf(Consumer<User> callback) {
        UserPair pair = new UserPair(callback);
        firebaseDatabase.getReference().child("users").child("public").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pair.setPublic(dataSnapshot.getValue(User.class));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        firebaseDatabase.getReference().child("users").child("private").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pair.setPrivate(dataSnapshot.getValue(User.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Remove the action bar back arrow when navigating back to all chats fragment
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getActivity().setTitle("ChatApp");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        SignupActivity.handleActivityResult(getActivity(), requestCode, resultCode, imageReturnedIntent, (bitmap -> {
            profileImageBitmap = bitmap;
            SignupActivity.loadImageInto(getActivity(), bitmap, profileImage);
            SignupActivity.uploadImage(getResources(), bitmap, userId, (path) ->
                    firebaseDatabase.getReference().child("users").child("public").child(userId).child("photoUrl").setValue(path));

        }));
    }
}

class UserPair {
    private User privateUser;
    private User publicUser;
    Consumer<User> callback;

    public UserPair(Consumer<User> callback) {
        this.callback = callback;
    }

    void setPrivate(User u) {
        this.privateUser = u;
        callIfComplete();
    }

    void setPublic(User u) {
        this.publicUser = u;
        callIfComplete();
    }

    void callIfComplete() {
        if (privateUser != null && publicUser != null) {
            publicUser.merge(privateUser);
            callback.accept(publicUser);
        }
    }
}
