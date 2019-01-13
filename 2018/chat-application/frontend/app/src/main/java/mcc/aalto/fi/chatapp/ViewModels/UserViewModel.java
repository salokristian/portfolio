package mcc.aalto.fi.chatapp.ViewModels;

import android.accounts.Account;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import mcc.aalto.fi.chatapp.AccountActivity.LoginActivity;
import mcc.aalto.fi.chatapp.AccountSettingsFragment.AccountSettingsFragment;
import mcc.aalto.fi.chatapp.MainActivity;
import mcc.aalto.fi.chatapp.Models.User;

public class UserViewModel extends ViewModel {
	private String userId;
	private MutableLiveData<User> user;
	private DatabaseReference database;

	public UserViewModel(String userId) {
		database = FirebaseDatabase.getInstance().getReference();

		this.userId = userId;
		this.user = new MutableLiveData<>();

		this.user.setValue(new User(userId, "", new ArrayList<String>(), ""));

		loadUser();
	}

	public LiveData<User> getUser() {
		return user;
	}

	public User getUserStatic() {
		return user.getValue();
	}

	private void loadUser() {
		ValueEventListener publicUserListener = new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				User existingUser = user.getValue();

				User publicUser = dataSnapshot.getValue(User.class);

				try {
					if (publicUser.displayName != null) {
						existingUser.displayName = publicUser.displayName;
					}

					if (publicUser.photoUrl != null) {
						existingUser.photoUrl = publicUser.photoUrl;
					}
				} catch (NullPointerException ex) {
					Log.d("err", "logout");
					AccountSettingsFragment.clearAuth(FirebaseAuth.getInstance(), (b) -> {});
				}

				user.setValue(existingUser);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Log.w("USER_VIEW_MODEL", "loadUser public cancelled", databaseError.toException());
			}
		};

		ValueEventListener privateUserListener = new ValueEventListener() {
			@Override
			public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
				User existingUser = user.getValue();

				User privateUser = dataSnapshot.getValue(User.class);

				try {
					if (privateUser.imageSizing != null) {
						existingUser.imageSizing = privateUser.imageSizing;
					}
				} catch (NullPointerException ex) {
					AccountSettingsFragment.clearAuth(FirebaseAuth.getInstance(), (b) -> {});
				}

				user.setValue(existingUser);
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Log.w("USER_VIEW_MODEL", "loadUser private cancelled", databaseError.toException());
			}
		};

		database.child("users").child("public").child(userId).addValueEventListener(publicUserListener);
		database.child("users").child("private").child(userId).addValueEventListener(privateUserListener);
	}
}
