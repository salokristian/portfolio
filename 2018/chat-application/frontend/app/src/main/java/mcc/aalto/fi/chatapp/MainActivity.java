package mcc.aalto.fi.chatapp;

import android.app.NotificationManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import mcc.aalto.fi.chatapp.AccountActivity.LoginActivity;
import mcc.aalto.fi.chatapp.AccountSettingsFragment.AccountSettingsFragment;
import mcc.aalto.fi.chatapp.ChatListFragment.ChatListFragment;
import mcc.aalto.fi.chatapp.ViewModels.ChatViewModel;
import mcc.aalto.fi.chatapp.ViewModels.ChatViewModelFactory;
import mcc.aalto.fi.chatapp.ViewModels.UserViewModel;
import mcc.aalto.fi.chatapp.ViewModels.UserViewModelFactory;

public class MainActivity extends AppCompatActivity {

    public final static int PERMISSION_REQUEST_WRITE_DATA = 0;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    FirebaseAuth.AuthStateListener authStateListener;
    ChatViewModel chatViewModel;
    UserViewModel userViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize fresco image lib
        Fresco.initialize(this);

        firebaseAuth = FirebaseAuth.getInstance();
        chatViewModel = ViewModelProviders.
                of(this, new ChatViewModelFactory(getUserId())).get(ChatViewModel.class);
        userViewModel = ViewModelProviders.
                of(this, new UserViewModelFactory(getUserId())).get(UserViewModel.class);

		// If recovering from a screen rotation, let the fragments start themselves, otherwise open chat listing
        if (savedInstanceState == null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.fragmentContainer, new ChatListFragment());
            fragmentTransaction.commit();
        }

        // Listener called when there is a change in the authentication state.
        authStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if (user == null) {
                startActivity(new Intent(this, LoginActivity.class));
            }
        };

        firebaseDatabase = FirebaseDatabase.getInstance();
    }

    public ChatViewModel getChatViewModel() {
        return chatViewModel;
    }

    public String getUserImageSizing() {
        return userViewModel.getUserStatic().imageSizing;
    }

    public String getUserId() {
        return firebaseAuth.getUid();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_profile:
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainer, new AccountSettingsFragment()).addToBackStack(null).commit();
                return true;
            case R.id.action_logout:
                AccountSettingsFragment.logout(firebaseAuth, firebaseDatabase, (b) -> startActivity(new Intent(getApplicationContext(), LoginActivity.class)),this);
                return true;
            case android.R.id.home:
                getSupportFragmentManager().popBackStackImmediate();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(authStateListener);
        clearNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        clearNotifications();
    }

    private void clearNotifications() {
        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancelAll();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}
