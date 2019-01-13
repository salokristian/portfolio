package mcc.aalto.fi.chatapp.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import mcc.aalto.fi.chatapp.Models.Image;
import mcc.aalto.fi.chatapp.Models.Message;


public class ChatImagesViewModel extends ViewModel {
	DatabaseReference database;
	MutableLiveData<List<Message>> imageMessages;
	String chatId;
	Long userJoinedTimestamp;

	public ChatImagesViewModel(String chatId, Long userJoinedTimestamp) {
		this.chatId = chatId;
		this.userJoinedTimestamp = userJoinedTimestamp;

		database = FirebaseDatabase.getInstance().getReference();
		imageMessages = new MutableLiveData<>();
		imageMessages.setValue(new ArrayList<>());

		loadImages();
	}

	public LiveData<List<Message>> getImageMessages() {
		return imageMessages;
	}

	private void loadImages() {
		ChildEventListener messageEventListener = new ChildEventListener() {
			@Override
			public void onChildAdded(@NonNull DataSnapshot messageSnapshot, @Nullable String s) {
				Message message = messageSnapshot.getValue(Message.class);

				if (message.image != null) {
					message.image.senderId = message.sender;

					if (message.createdAt >= userJoinedTimestamp) {
						List<Message> allImageMessages = imageMessages.getValue();
						allImageMessages.add(message);

						imageMessages.setValue(allImageMessages);
					}
				}
			}

			@Override
			public void onCancelled(@NonNull DatabaseError databaseError) {
				Log.w("CHAT_IMAGE_VIEW_MODEL","CANCEL", databaseError.toException());
			}

			@Override
			public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Log.d("CHAT_IMAGE_VIEW_MODEL", "CHANGE " + dataSnapshot.getKey());
			}

			@Override
			public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
				Log.d("CHAT_IMAGE_VIEW_MODEL", "REMOVE " + dataSnapshot.getKey());
			}

			@Override
			public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
				Log.d("CHAT_IMAGE_VIEW_MODEL", "MOVE " + dataSnapshot.getKey());
			}
		};

		database.child("messages").child(chatId).addChildEventListener(messageEventListener);
	}
}
