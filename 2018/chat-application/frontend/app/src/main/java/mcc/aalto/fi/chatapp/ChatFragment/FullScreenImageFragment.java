package mcc.aalto.fi.chatapp.ChatFragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import mcc.aalto.fi.chatapp.GlideApp;
import mcc.aalto.fi.chatapp.MainActivity;
import mcc.aalto.fi.chatapp.R;
import mcc.aalto.fi.chatapp.Utils;
import me.relex.photodraweeview.PhotoDraweeView;

public class FullScreenImageFragment extends Fragment {
	StorageReference storageRef;
	String imageName;
	String originalVersionName;

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		imageName = getArguments().getString("imageName");
		originalVersionName = getArguments().getString("originalVersionName");

		// Display arrow in action bar for back navigation to all chats view
		((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Maintain this fragment across orientation changes
		setRetainInstance(true);

		// Add gallery to options menu
		setHasOptionsMenu(true);

		return inflater.inflate(R.layout.fragment_full_screen_image, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		storageRef = FirebaseStorage.getInstance().getReference();

		storageRef.child(imageName).getDownloadUrl().addOnCompleteListener(imageUriResult -> {
			PhotoDraweeView imageView = view.findViewById(R.id.full_screen_image);
			imageView.setPhotoUri(imageUriResult.getResult());
		});
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();

		if (!originalVersionName.isEmpty()) {
			inflater.inflate(R.menu.full_screen_image_menu, menu);
		}

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.full_screen_image_download:
				downloadImage();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	private void downloadImage() {
		if (!hasWritePermission()) {
			getWritePermission();
			return;
		}

		StorageReference imageRef = storageRef.child(originalVersionName);

		GlideApp.with(this)
				.asBitmap()
				.load(imageRef)
				.into(new SimpleTarget<Bitmap>() {
					@Override
					public void onResourceReady(@NonNull Bitmap imageBitmap, @Nullable Transition<? super Bitmap> transition) {
						File imagesDir = getImagesDir();
						String imageName = "download_" + Utils.getIsoTimestamp() + ".png";
						File imageFile = new File(imagesDir, imageName);

						try {
							imageFile.createNewFile();
							FileOutputStream out = new FileOutputStream(imageFile);

							// PNG is a lossless format, the compression factor (100) is ignored
							imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);

							Toast.makeText(getActivity(), "Image downloaded successfully.", Toast.LENGTH_SHORT).show();

						} catch (Exception e) {
							Toast.makeText(getActivity(), "A problem occurred when downloading image.", Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
					}
				});
	}

	private File getImagesDir() {
		// Get the app's public image dir at /Pictures/chat_app
		File file = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "chat_app");
		if (!file.mkdirs()) {
			Log.e("FULL_SCREEN_IMAGE_FRAGMENT", "Directory already created");
		}

		return file;
	}

	private boolean hasWritePermission() {
		return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
				== PackageManager.PERMISSION_GRANTED;
	}

	private void getWritePermission() {
		// Creates an async request to get permission
		ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
				MainActivity.PERMISSION_REQUEST_WRITE_DATA);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		switch (requestCode) {
			case MainActivity.PERMISSION_REQUEST_WRITE_DATA:
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					downloadImage();
				}
			default:
				super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}
}
