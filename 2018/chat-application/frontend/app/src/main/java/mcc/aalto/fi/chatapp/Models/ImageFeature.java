package mcc.aalto.fi.chatapp.Models;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.cloud.FirebaseVisionCloudDetectorOptions;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabel;
import com.google.firebase.ml.vision.cloud.label.FirebaseVisionCloudLabelDetector;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * ImageFeature is a helper utility for the MCC chat application.
 *
 * ImageFeature abstracts image labeling with the Google Firebase ML Kit
 * into a single clean API that can be called from the image uploading agent before image uploads
 * to add image features into the upload metadata and display images cleanly in the client gallery.
 */
public class ImageFeature {
    private static final String LOG_TAG = "MLKIT";
    private static final float CONFIDENCE_THRESHOLD_LOWER = 0.6f;

    private static final String TECHNOLOGY = "Technology";
    private static final String FOOD = "Food";
    private static final String SCREENSHOT = "Screenshot";
    private static final String OTHER = "Other";

    /**
     * Asynchronous threaded wrapper class for running the Firebase ML Kit recognition tasks.
     */
    private static class ImageFeatureTask extends AsyncTask<Bitmap, Integer, String> {

        /**
         * Filter confidences and select the best text representation for the given FP values.
         */
        private String filterConfidences(
                float technologyConfidence,
                float foodConfidence,
                float screenshotConfidence
        ) {
            String selectedFeatureLabel = OTHER;

            if (technologyConfidence >= screenshotConfidence
                    && technologyConfidence >= foodConfidence
                    && technologyConfidence >= CONFIDENCE_THRESHOLD_LOWER) {
                selectedFeatureLabel = TECHNOLOGY;
            }

            else if (foodConfidence >= technologyConfidence
                    && foodConfidence >= screenshotConfidence
                    && foodConfidence >= CONFIDENCE_THRESHOLD_LOWER) {
                selectedFeatureLabel = FOOD;
            }

            else if (screenshotConfidence >= technologyConfidence
                    && screenshotConfidence >= foodConfidence
                    && screenshotConfidence >= CONFIDENCE_THRESHOLD_LOWER) {
                selectedFeatureLabel = SCREENSHOT;
            }

            Log.d(LOG_TAG, String.format(
                    Locale.ENGLISH,
                    "Selecting highest confidence feature label %s",
                    selectedFeatureLabel
            ));

            return selectedFeatureLabel;
        }

        /**
         * Filter labels and select best text representation for the list of label obejcts.
         */
        private String filterLabels(List<FirebaseVisionCloudLabel> labels) {
            float technologyConfidence = 0.0f;
            float foodConfidence = 0.0f;
            float screenshotConfidence = 0.0f;

            for (FirebaseVisionCloudLabel label: labels) {
                String textLabel = label.getLabel();
                String entityId = label.getEntityId();  // Google Knowledge Graph Search API ID
                float confidence = label.getConfidence();

                Log.d(LOG_TAG, String.format(
                        Locale.ENGLISH,
                        "Found image label %s with entity id %s from data with confidence %.2f",
                        textLabel, entityId, confidence
                ));

                if (textLabel.contains(SCREENSHOT.toLowerCase())) {
                    screenshotConfidence = confidence;
                } if (textLabel.contains(TECHNOLOGY.toLowerCase())) {
                    technologyConfidence = confidence;
                } if (textLabel.contains(FOOD.toLowerCase())) {
                    foodConfidence = confidence;
                }
            }

            Log.d(LOG_TAG, String.format(
                    Locale.ENGLISH,
                    "Determined confidences for subjects: [%s, %.2f], [%s, %.2f], [%s, %.2f]",
                    TECHNOLOGY, technologyConfidence,
                    FOOD, foodConfidence,
                    SCREENSHOT, screenshotConfidence
            ));

            return filterConfidences(
                    technologyConfidence,
                    foodConfidence,
                    screenshotConfidence
            );
        }

        /**
         * Perform labeling for the given image with Firebase ML Kit in the cloud based detector.
         *
         * This method can only be called from a background thread.
         */
        private String getImageFeature(Bitmap bitmap) {
            try {
                FirebaseVisionCloudDetectorOptions options =
                        new FirebaseVisionCloudDetectorOptions.Builder()
                                .setModelType(FirebaseVisionCloudDetectorOptions.LATEST_MODEL)
                                .setMaxResults(10)
                                .build();

                FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);

                FirebaseVisionCloudLabelDetector detector =
                        FirebaseVision.getInstance().getVisionCloudLabelDetector(options);

                Task<List<FirebaseVisionCloudLabel>> result = detector.detectInImage(image);

                List <FirebaseVisionCloudLabel> labels = Tasks.await(result, 3, TimeUnit.SECONDS);

                return filterLabels(labels);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                Log.e(LOG_TAG, "Exception in Firebase ML Kit API", e);
                return OTHER;
            }
        }

        /**
         * Wrap image detection tasks into a background thread so they can be called from anywhere.
         */
        protected String doInBackground(Bitmap... bitmaps) {
            return getImageFeature(bitmaps[0]);
        }
    }

    /**
     * Perform labeling for the given image with Firebase ML Kit in the cloud based detector.
     *
     * This method can be called frmo anywhere and performs necessary error checking.
     */
    public String getImageFeature(Bitmap image) {
        try {
            String imageFeature = new ImageFeatureTask().execute(image).get(3, TimeUnit.SECONDS);

            Log.d(LOG_TAG, String.format(
                    Locale.ENGLISH,
                    "Returning %s as the selected image feature label",
                    imageFeature
            ));

            return imageFeature;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            Log.e(LOG_TAG, "Exception in image feature detection implementation", e);
            return OTHER;
        }
    }
}
