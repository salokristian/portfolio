package mcc.aalto.fi.chatapp.ChatFragment.ChatImagesFragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.google.firebase.storage.StorageReference;

import java.util.List;

import mcc.aalto.fi.chatapp.ChatFragment.OnImageClickListener;
import mcc.aalto.fi.chatapp.GlideApp;
import mcc.aalto.fi.chatapp.Models.Image;
import mcc.aalto.fi.chatapp.R;

public class ImageAdapter extends BaseAdapter {
	private StorageReference storageRef;
	private Context context;
	private List<Image> images;
	private String downloadResolution;
	private OnImageClickListener imageClickListener;

	public ImageAdapter(StorageReference storageRef, Context context, List<Image> images, String downloadResolution, OnImageClickListener imageClickListener) {
		this.storageRef = storageRef;
		this.context = context;
		this.images = images;
		this.downloadResolution = downloadResolution;
		this.imageClickListener = imageClickListener;
	}

	@Override
	public int getCount() {
		return images.size();
	}

	@Override
	public Object getItem(int i) {
		return images.get(i);
	}

	@Override
	public long getItemId(int i) {
		return i;
	}

	@Override
	public View getView(int index, View view, ViewGroup viewGroup) {
		Image currentImage = (Image) getItem(index);

		if (view == null) {
			view = LayoutInflater.from(context).
					inflate(R.layout.chat_images_category_image, viewGroup, false);
		}

		ImageView imageView = view.findViewById(R.id.chatImageCategoryImage);
		String imageName = currentImage.getImageName(downloadResolution);
		StorageReference imageRef = storageRef.child(imageName);

		GlideApp.with(context)
				.load(imageRef)
				.placeholder(R.drawable.chat_app_logo)
				.into(imageView);

		imageView.setOnClickListener(clickedImageView -> {
			imageClickListener.onClick(clickedImageView, currentImage);
		});

		return imageView;
	}
}
