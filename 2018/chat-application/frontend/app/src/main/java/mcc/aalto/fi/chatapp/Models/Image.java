package mcc.aalto.fi.chatapp.Models;

import android.graphics.Bitmap;

public class Image {
	public final static String IMAGE_LOW = "low";
	public final static String IMAGE_HIGH = "high";
	public final static String IMAGE_ORIGINAL = "original";

	public String low;
	public String high;
	public String original;
	public String senderId;
	public String imageSizing;
	public String imageFeature;

	public Image() {}

	public String getImageName(String downloadResolution) {
		if (downloadResolution == null) {
			return low;
		}

		switch (downloadResolution) {
			case IMAGE_ORIGINAL:
				if (original != null) {
					return original;
				}
			case IMAGE_HIGH:
				if (high != null) {
					return high;
				}
			default:
				return low;
		}
	}

	public String getOriginalIfExists() {
		return imageSizing.equals(IMAGE_ORIGINAL) ? original : "";
	}

	public static Bitmap scaleImageForUpload(Bitmap image, String uploadResolution) {
		if (uploadResolution == null) {
			return image;
		}

		switch (uploadResolution) {
			case IMAGE_LOW:
				return resizeImage(image, 640, 480);
			case IMAGE_HIGH:
				return resizeImage(image, 1280, 960);
			default:
				return image;
		}
	}

	// In absence of a good library, stackoverflow helps :)
	// https://stackoverflow.com/questions/15440647/scaled-bitmap-maintaining-aspect-ratio
	private static Bitmap resizeImage(Bitmap image, int maxWidth, int maxHeight) {
		if (maxHeight > 0 && maxWidth > 0) {
			int width = image.getWidth();
			int height = image.getHeight();
			float ratioBitmap = (float) width / (float) height;
			float ratioMax = (float) maxWidth / (float) maxHeight;

			int finalWidth = maxWidth;
			int finalHeight = maxHeight;
			if (ratioMax > ratioBitmap) {
				finalWidth = (int) ((float)maxHeight * ratioBitmap);
			} else {
				finalHeight = (int) ((float)maxWidth / ratioBitmap);
			}
			image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
			return image;
		} else {
			return image;
		}
	}
}
