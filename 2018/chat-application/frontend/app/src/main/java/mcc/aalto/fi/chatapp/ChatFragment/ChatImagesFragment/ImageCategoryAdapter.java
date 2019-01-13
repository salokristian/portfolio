package mcc.aalto.fi.chatapp.ChatFragment.ChatImagesFragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.storage.StorageReference;

import java.util.LinkedHashMap;
import java.util.List;

import mcc.aalto.fi.chatapp.ChatFragment.OnImageClickListener;
import mcc.aalto.fi.chatapp.GlideApp;
import mcc.aalto.fi.chatapp.Models.Image;
import mcc.aalto.fi.chatapp.Models.User;
import mcc.aalto.fi.chatapp.R;

public class ImageCategoryAdapter extends BaseAdapter {
	private Context context;
	private StorageReference storageRef;
	private LinkedHashMap<String, List<Image>> imageCategories;
	private String downloadResolution;
	private List<User> imageSenders;
	private OnImageClickListener imageClickListener;

	public ImageCategoryAdapter(Context context, StorageReference storageRef, LinkedHashMap<String, List<Image>> imageCategories, String downloadResolution, List<User> imageSenders, OnImageClickListener imageClickListener) {
		this.context = context;
		this.storageRef = storageRef;
		this.imageCategories = imageCategories;
		this.downloadResolution = downloadResolution;
		this.imageSenders = imageSenders;
		this.imageClickListener = imageClickListener;
	}

	@Override
	public int getCount() {
		return imageCategories.size();
	}

	@Override
	public Object getItem(int i) {
		return imageCategories.keySet().toArray()[i];
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	private User getImageSender(List<Image> imagesInCategory) {
		String senderId = imagesInCategory.get(0).senderId;

		return imageSenders.stream()
				.filter(sender -> sender.id.equals(senderId))
				.findFirst()
				.orElse(null);
	}

	@Override
	public View getView(int i, View view, ViewGroup viewGroup) {
		String categoryTitle = (String) getItem(i);
		List<Image> imagesInCategory = imageCategories.get(categoryTitle);

		if (view == null) {
			view = LayoutInflater.from(context)
					.inflate(R.layout.chat_images_category, viewGroup, false);
		}

		TextView categoryTitleView = view.findViewById(R.id.chatImageCategoryTitle);
		ImageView categoryTitleSenderImageView = view.findViewById(R.id.chatImageCategorySenderImage);
		GridView categoryImageList = view.findViewById(R.id.chatImageCategoryGrid);

		categoryTitleView.setText(categoryTitle);

		if (imageSenders != null) {
			User imageSender = getImageSender(imagesInCategory);
			StorageReference profileImageRef = storageRef.child(imageSender.photoUrl);

			GlideApp.with(context)
					.load(profileImageRef)
					.placeholder(R.mipmap.ic_launcher_round)
					.circleCrop()
					.into(categoryTitleSenderImageView);

		} else {
			categoryTitleSenderImageView.setVisibility(View.GONE);
		}

		ImageAdapter imageAdapter = new ImageAdapter(storageRef, context, imagesInCategory, downloadResolution, imageClickListener);
		categoryImageList.setAdapter(imageAdapter);

		return view;
	}
}
