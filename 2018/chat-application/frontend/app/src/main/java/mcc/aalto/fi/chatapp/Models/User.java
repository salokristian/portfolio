package mcc.aalto.fi.chatapp.Models;

import java.util.List;

// A class for storing Users that are fetched and sent to the firebase data
import android.graphics.Bitmap;
import com.google.firebase.auth.FirebaseAuth;

public class User {
	public String displayName;
	public String photoUrl;
	public String id;
	public String imageSizing;
	public List<String> notificationTokens;

	// An empty constructor is neede by the firebase DB methods
	public User() {
	}

	public User(String username) {
		this.displayName = username;
	}

	public User(String displayName, String photoUrl, List<String> notificationTokens) {
		this.displayName = displayName;
		this.photoUrl = photoUrl;
		this.notificationTokens = notificationTokens;
	}

	public User(String displayName, String photoUrl, List<String> notificationTokens, String id) {
		this.displayName = displayName;
		this.photoUrl = photoUrl;
		this.notificationTokens = notificationTokens;
		this.id = id;
	}

	public void merge(User privateUser) {
		this.imageSizing = privateUser.imageSizing;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		User user = (User) o;

		return id != null ? id.equals(user.id) : user.id == null;
	}

	@Override
	public int hashCode() {
		return id != null ? id.hashCode() : 0;
	}

	public boolean isSelf() {
		return FirebaseAuth.getInstance().getUid().equals(this.id);
	}

	@Override
	public String toString() {
		return "User{" +
				"displayName='" + displayName + '\'' +
				", photoUrl='" + photoUrl + '\'' +
				", id='" + id + '\'' +
				", notificationTokens=" + notificationTokens +
				'}';
	}

}
