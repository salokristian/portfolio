package mcc.aalto.fi.chatapp.ChatFragment.ChatImagesFragment;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;

import mcc.aalto.fi.chatapp.ChatFragment.FullScreenImageFragment;
import mcc.aalto.fi.chatapp.ChatFragment.OnImageClickListener;
import mcc.aalto.fi.chatapp.MainActivity;
import mcc.aalto.fi.chatapp.Models.Chat;
import mcc.aalto.fi.chatapp.Models.Image;
import mcc.aalto.fi.chatapp.Models.Message;
import mcc.aalto.fi.chatapp.Models.User;
import mcc.aalto.fi.chatapp.R;
import mcc.aalto.fi.chatapp.ViewModels.ChatImagesViewModel;
import mcc.aalto.fi.chatapp.ViewModels.ChatImagesViewModelFactory;
import mcc.aalto.fi.chatapp.ViewModels.ChatViewModel;

public class ChatImagesFragment extends Fragment {
	private final static int GROUP_BY_DATE = 0;
	private final static int GROUP_BY_SENDER = 1;
	private final static int GROUP_BY_FEATURES = 2;

	StorageReference storageRef;
	ChatImagesViewModel chatImagesViewModel;
	ChatViewModel chatViewModel;
	ListView imageCategoryList;
	ImageCategoryAdapter imageCategoryAdapter;
	List<Message> imageMessages;
	String chatId;
	Long userJoinedTimestamp;
	String userImageSizing;
	Integer groupBy = GROUP_BY_DATE;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		chatId = getArguments().getString("chatId");
		userJoinedTimestamp = getArguments().getLong("userJoinedTimestamp");
		setHasOptionsMenu(true);

		// Maintain this fragment across orientation changes
		setRetainInstance(true);

		return inflater.inflate(R.layout.fragment_chat_images, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		storageRef = FirebaseStorage.getInstance().getReference();
		userImageSizing = ((MainActivity) getActivity()).getUserImageSizing();
		chatImagesViewModel = ViewModelProviders.of(this,
				new ChatImagesViewModelFactory(chatId, userJoinedTimestamp)).get(ChatImagesViewModel.class);
		chatViewModel = ((MainActivity)getActivity()).getChatViewModel();
		imageCategoryList = view.findViewById(R.id.chatImageCategoriesList);

		chatImagesViewModel.getImageMessages().observe(this, this::onNewImageMessages);
	}

	private void onNewImageMessages(List<Message> imageMessages) {
		if (imageMessages != null) {
			this.imageMessages = imageMessages;
		}

		LinkedHashMap<String, List<Image>> imagesByCategory = getImagesByCategory(this.imageMessages);
		List<User> imageSenders = getImageSenders(this.imageMessages);
		OnImageClickListener imageClickListener = (view, image) -> openImageFullScreen(image);


		imageCategoryAdapter = new ImageCategoryAdapter(getActivity(), storageRef, imagesByCategory, userImageSizing, imageSenders, imageClickListener);
		imageCategoryList.setAdapter(imageCategoryAdapter);
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

	private List<User> getImageSenders(List<Message> imageMessages) {
		if (groupBy == GROUP_BY_SENDER) {
			return chatViewModel.getChat(chatId).memberProfiles;
		} else {
			return null;
		}
	}

	private LinkedHashMap<String, List<Image>> getImagesByCategory(List<Message> imageMessages) {
		switch (groupBy) {
			case GROUP_BY_DATE:
				return groupImagesByDate(imageMessages);
			case GROUP_BY_SENDER:
				return groupImagesBySender(imageMessages);
			case GROUP_BY_FEATURES:
				return groupImagesByFeatures(imageMessages);
			default:
				return groupImagesByDate(imageMessages);
		}
	}

	private static LinkedHashMap<String, List<Image>> groupImagesByDate(List<Message> imageMessages) {
		LinkedHashMap<String, List<Image>> groupedImages = new LinkedHashMap<>();
		ListIterator li = imageMessages.listIterator(imageMessages.size());

		// Iterate the images in reverse, as they are ordered in ascending order by Firebase timestamps,
		// but we need descending order
		while (li.hasPrevious()) {
			Message imageMessage = (Message) li.previous();

			Date sentAt = new Date(imageMessage.createdAt);
			DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, Locale.ENGLISH);
			String sentAtFormatted = df.format(sentAt);

			if (groupedImages.containsKey(sentAtFormatted)) {
				groupedImages.get(sentAtFormatted).add(imageMessage.image);
			} else {
				List<Image> imagesOnDate = new ArrayList<>();
				imagesOnDate.add(imageMessage.image);

				groupedImages.put(sentAtFormatted, imagesOnDate);
			}
		}

		return groupedImages;
	}

	private LinkedHashMap<String, List<Image>> groupImagesBySender(List<Message> imageMessages) {
		LinkedHashMap<String, List<Image>> groupedImages = new LinkedHashMap<>();
		Chat chat = chatViewModel.getChat(chatId);

		imageMessages.forEach(imageMessage -> {
			User sender = chat.memberProfiles.stream()
					.filter(chatMember -> chatMember.id.equals(imageMessage.sender))
					.findFirst()
					.orElse(null);

			if (groupedImages.containsKey(sender.displayName)) {
				groupedImages.get(sender.displayName).add(imageMessage.image);
			} else {
				List<Image> imagesBySender = new ArrayList<>();
				imagesBySender.add(imageMessage.image);

				groupedImages.put(sender.displayName, imagesBySender);
			}
		});

		return groupedImages;
	}

	private static LinkedHashMap<String, List<Image>> groupImagesByFeatures(List<Message> imageMessages) {
		LinkedHashMap<String, List<Image>> groupedImages = new LinkedHashMap<>();

		imageMessages.forEach(imageMessage -> {
			String feature = imageMessage.image.imageFeature;

			if (groupedImages.containsKey(feature)) {
				groupedImages.get(feature).add(imageMessage.image);
			} else {
				List<Image> imagesBySender = new ArrayList<>();
				imagesBySender.add(imageMessage.image);

				groupedImages.put(feature, imagesBySender);
			}
		});

		return groupedImages;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.gallery_menu, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.change_gallery_grouping_date:
				groupBy = GROUP_BY_DATE;
				break;
			case R.id.change_gallery_grouping_sender:
				groupBy = GROUP_BY_SENDER;
				break;
			case R.id.change_gallery_grouping_features:
				groupBy = GROUP_BY_FEATURES;
				break;
			default:
				return super.onOptionsItemSelected(item);
		}
		onNewImageMessages(null);

		return true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
