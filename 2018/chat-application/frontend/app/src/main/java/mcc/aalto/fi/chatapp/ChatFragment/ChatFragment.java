package mcc.aalto.fi.chatapp.ChatFragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.*;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.leocardz.link.preview.library.TextCrawler;

import mcc.aalto.fi.chatapp.ChatFragment.ChatImagesFragment.ChatImagesFragment;
import mcc.aalto.fi.chatapp.MainActivity;
import mcc.aalto.fi.chatapp.Modals;
import mcc.aalto.fi.chatapp.Models.Chat;
import mcc.aalto.fi.chatapp.Models.Image;
import mcc.aalto.fi.chatapp.Models.ImageFeature;
import mcc.aalto.fi.chatapp.Models.Message;
import mcc.aalto.fi.chatapp.R;
import mcc.aalto.fi.chatapp.UserListFragment.UserListFragment;
import mcc.aalto.fi.chatapp.Utils;
import mcc.aalto.fi.chatapp.ViewModels.ChatViewModel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class ChatFragment extends Fragment {
	private static final int PICK_IMAGE_FROM_CAMERA = 0;
	private static final int PICK_IMAGE_FROM_GALLERY = 1;

	DatabaseReference database;
	StorageReference userStorageRef;
	ChatViewModel chatViewModel;
	String userImageSizing;
	ListView messageList;
	MessageAdapter messageAdapter;
	String currentUserId;
	String chatId;
	String chatName;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		chatId = getArguments().getString("chatId");
		chatName = getArguments().getString("chatName");

		// Display arrow in action bar for back navigation to all chats view
		((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Maintain this fragment across orientation changes
		setRetainInstance(true);

		// Add gallery to options menu
		setHasOptionsMenu(true);

		return inflater.inflate(R.layout.fragment_chat, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		currentUserId = ((MainActivity)getActivity()).getUserId();
		database = FirebaseDatabase.getInstance().getReference();
		userStorageRef = FirebaseStorage.getInstance().getReference().child("users").child(currentUserId).child("messages").child(chatId);

		ImageButton sendButton = view.findViewById(R.id.chatSendMessage);
		sendButton.setOnClickListener(this::onSendButtonClick);

		ImageButton sendImageButton = view.findViewById(R.id.chatSendImage);
		sendImageButton.setOnClickListener(this::onSendImageButtonClick);

		// Listen to changes in the message list and update the view each time new messages are added
		messageList = view.findViewById(R.id.chatMessageList);
		chatViewModel = ((MainActivity)getActivity()).getChatViewModel();
		userImageSizing = ((MainActivity)getActivity()).getUserImageSizing();

		chatViewModel.getMessages(chatId).observe(this, this::onNewMessages);

		messageList.setOnScrollChangeListener(this::handleScroll);

		// Fetch the chat title when opening the chat, changes are not updated before exiting the
		// single chat view.
		if (chatName != null) {
			setTitle(chatName);
		} else {
			setTitle(getTitle(chatViewModel.getChat(chatId), currentUserId));
		}
	}

	// Add gallery to overflow options menu
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		// Only add the options to the menu if they are not already there
		if (menu.findItem(R.id.action_goto_image_gallery) == null) {
			inflater.inflate(R.menu.chat_menu, menu);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.action_goto_image_gallery:
				openGalleryFragment();
				break;
			case R.id.action_add_new_users:
				addNewUsers();
				break;
			case R.id.action_leave_chat:
				Modals.showQuestion(getContext(), getString(R.string.chat_leave_prompt), (leave) -> {
					if (leave)  { leaveChat(); }
				});
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Leaves the current chat. If it's a two user chat, the chat will now become a notepad for the remaining user. If
	 * it's a single user chat, the chat will remain in the database indefinitely but no user will have access.
	 */
	private void leaveChat() {
		sendMessage(chatId, currentUserId, getString(R.string.chat_user_is_leaving));
		database.child("chats").child(chatId).child("members").child(currentUserId).removeValue();
		database.child("users").child("private").child(currentUserId).child("chats").child(chatId).removeValue();

		showConfirmation();
		getActivity().getSupportFragmentManager().popBackStack();
	}

	private void showConfirmation() {
		Snackbar snackbar = Snackbar
				.make(getView(), getString(R.string.chat_is_left), Snackbar.LENGTH_LONG);

		snackbar.show();
	}

	private void openGalleryFragment() {
		Bundle bundle = new Bundle();
		bundle.putString("chatId", chatId);
		bundle.putLong("userJoinedTimestamp", chatViewModel.getUserJoinedTimestamp(chatId));

		Fragment chatImagesFragment = new ChatImagesFragment();
		chatImagesFragment.setArguments(bundle);

		android.support.v4.app.FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
		fragmentTransaction.add(R.id.fragmentContainer, chatImagesFragment).addToBackStack(null).commit();
	}

	private void addNewUsers() {
		Bundle bundle = new Bundle();
		bundle.putString("chatId", chatId);

		Fragment userListFragment = new UserListFragment();
		userListFragment.setArguments(bundle);

		// Remove current chatfragment
		getActivity().getSupportFragmentManager().popBackStack();

		android.support.v4.app.FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
		fragmentTransaction.add(R.id.fragmentContainer, userListFragment).addToBackStack(null).commit();
	}

	private void onNewMessages(List<Message> newMessages) {
		if (messageList.getAdapter() == null) {
			StorageReference storageRef = FirebaseStorage.getInstance().getReference();
			OnImageClickListener imageClickListener = (view, image) -> openImageFullScreen(image);

			messageAdapter = new MessageAdapter(
					getActivity(), newMessages, currentUserId, userImageSizing, storageRef, imageClickListener);
			messageList.setAdapter(messageAdapter);

		} else {
			// Maintain scroll position if the user has scrolled up to old messages, otherwise scroll to new messages
			// The magic 2 is needed because the newMessages list has one new message,
			// and denotes size, whereas getLastVisible position denotes index
			if (messageList.getLastVisiblePosition() == newMessages.size() - 2) {
				messageAdapter.notifyDataSetChanged();

			} else {
				Integer oldTopItemIndex = messageList.getFirstVisiblePosition();
				View topMessageView = messageList.getChildAt(0);
				Integer spaceAboveTopMessage = (topMessageView == null) ? 0
						: (topMessageView.getTop() - messageList.getPaddingTop());

				messageAdapter.notifyDataSetChanged();

				messageList.post(() -> {
					Integer newTopItemIndex = oldTopItemIndex + chatViewModel.getNewMessageCount(chatId);
					messageList.setSelectionFromTop(newTopItemIndex, spaceAboveTopMessage);
				});
			}

		}
	}


	private void openImageFullScreen(Image image) {
		Bundle bundle = new Bundle();

		bundle.putString("imageName", image.getImageName(userImageSizing));
		bundle.putString("originalVersionName", image.getOriginalIfExists());

		Fragment fullScreenImageFragment = new FullScreenImageFragment();
		fullScreenImageFragment.setArguments(bundle);

		android.support.v4.app.FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
		fragmentTransaction.add(R.id.fragmentContainer, fullScreenImageFragment).addToBackStack(null).commit();
	}

	private void handleScroll(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
		if (messageList.getFirstVisiblePosition() < ChatViewModel.MESSAGE_PAGE_SIZE / 3) {
			chatViewModel.fetchMoreMessages(chatId);
		}
	}

	public static String getTitle(Chat chat, String userId) {
		String title = "";

		if (chat.members != null && chat.members.size() > 2) {
			title = chat.title;

		} else if (chat.memberProfiles.size() == 2) {

			if (chat.memberProfiles.get(0).id.equals(userId)) {
				title = chat.memberProfiles.get(1).displayName;
			} else {
				title = chat.memberProfiles.get(0).displayName;
			}
		}
		return title;
	}

	private void setTitle(String title) {
		getActivity().setTitle(title);
	}

	private void onSendButtonClick(View button) {
		TextView inputTextView = getView().findViewById(R.id.chatEnterMessage);

		String userId = currentUserId;
		String text = inputTextView.getText().toString();

		if (text.isEmpty()) {
			Toast.makeText(getActivity(), "Cannot send an empty message.", Toast.LENGTH_LONG).show();

		} else {
			sendMessage(chatId, userId, text);
			inputTextView.setText("");
		}

	}

	public static void sendMessage(String chatId, String userId, String text) {
		DatabaseReference database = FirebaseDatabase.getInstance().getReference();
		Long createdAt = new Date().getTime();
		Message message = new Message(userId, text, createdAt);

		String messageId = database.child("messages").push().getKey();
		database.child("messages").child(chatId).child(messageId).setValue(message);
	}

	private void onSendImageButtonClick(View button) {
		AlertDialog chooseImageUploadDialog = new AlertDialog.Builder(getActivity())
				.setTitle(R.string.chat_image_upload_choose)
				.setItems(R.array.chat_image_upload_options, (dialogInterface, i) -> {
					if (i == PICK_IMAGE_FROM_CAMERA) {
						getImageFromCamera();
					} else if (i == PICK_IMAGE_FROM_GALLERY) {
						getImageFromGallery();
					}
				})
				.setNegativeButton(R.string.chat_image_upload_cancel, (dialogInterface, i) -> {})
				.create();

		chooseImageUploadDialog.show();
	}

	private void getImageFromCamera() {
		Intent takeImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		startActivityForResult(takeImage, PICK_IMAGE_FROM_CAMERA);
	}

	private void getImageFromGallery() {
		Intent pickImage = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(pickImage , PICK_IMAGE_FROM_GALLERY);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		if (resultCode == RESULT_OK) {

			if (requestCode == PICK_IMAGE_FROM_CAMERA) {
				Bitmap selectedImage = (Bitmap) imageReturnedIntent.getExtras().get("data");
				sendImage(selectedImage);

			} else if (requestCode == PICK_IMAGE_FROM_GALLERY) {
				Uri selectedImage = imageReturnedIntent.getData();

				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImage);
					sendImage(bitmap);
				} catch (IOException e) {
					Log.w("CHAT_FRAGMENT", "onActivityResult error: " + e.getMessage());
				}

			} else {
				Log.w("IMAGE_ERROR", Integer.toString(resultCode));
			}
		}
	}

	private void sendImage(Bitmap image) {
		Bitmap scaledImage = Image.scaleImageForUpload(image, userImageSizing);
		String imageFeature = new ImageFeature().getImageFeature(scaledImage);

		StorageMetadata imageMetadata = new StorageMetadata.Builder()
				.setCustomMetadata("imageSizing", userImageSizing)
				.setCustomMetadata("imageFeature", imageFeature)
				.build();

		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		scaledImage.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] imageByteArray = stream.toByteArray();

		StorageReference imageRef = userStorageRef.child(Utils.getIsoTimestamp());

		// TODO: Implement progress bar and remove success toast
		imageRef.putBytes(imageByteArray, imageMetadata).addOnSuccessListener(taskSnapshot -> {
			Toast.makeText(getActivity(),"Image uploaded successfully.", Toast.LENGTH_SHORT).show();
			Log.d("UPLOAD", "SUCCESS");
		}).addOnFailureListener(e -> {
			Toast.makeText(getActivity(),"Image uploading failed. Please try again.", Toast.LENGTH_SHORT).show();
			Log.d("UPLOAD", "FAILURE");
		}).addOnProgressListener(taskSnapshot -> {
			double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
					.getTotalByteCount());
			Log.d("UPLOAD", "Uploaded "+(int)progress+"%");
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		// Remove the action bar back arrow when navigating back to all chats fragment
		((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
		getActivity().setTitle("ChatApp");
	}

	/**
	 * Switches the current fragment to this one, showing the chat with the given chatId.
	 */
	public static void openChat(FragmentActivity activity, String chatId, String name) {
		Bundle bundle = new Bundle();

		// Pass the chatId to the chat messages fragment
		bundle.putString("chatId", chatId);
		bundle.putString("chatName", name);

		Fragment chatFragment = new ChatFragment();
		chatFragment.setArguments(bundle);

		android.support.v4.app.FragmentTransaction fragmentTransaction = activity.getSupportFragmentManager().beginTransaction();

		fragmentTransaction.replace(R.id.fragmentContainer, chatFragment).addToBackStack(null).commit();
	}
}
