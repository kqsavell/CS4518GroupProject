package wpi.jhyuen.deeplearningproject;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Symbolic Constants
    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private final static int REQUEST_FROM_PHOTOS = 2;

    // Class Variables
    private FirebaseVisionTextRecognizer onDeviceModel = null;
    private FirebaseVisionTextRecognizer offDeviceModel = null;
    private boolean useOnDevice = true;

    private String curPhotoPath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Prepare models
        onDeviceModel = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        offDeviceModel = FirebaseVision.getInstance().getCloudTextRecognizer();

        //TODO: Hook up event listeners for the camera/photo app button
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            if (resultCode == RESULT_OK) {
                // Load the bitmap file returned from the camera
                Bitmap foundImg = null;
                try {
                    foundImg = BitmapFactory.decodeFile(curPhotoPath);
                }
                catch (NullPointerException npe) {
                    npe.printStackTrace();
                }

                // Start the inference task to identify text in the image
                if (useOnDevice) {
                    AsyncTask onInfTask = new OnDeviceInf().execute(foundImg);
                }
                else {
                    AsyncTask onInfTask = new OffDeviceInf().execute(foundImg);
                }
            }
        }
        else if (requestCode == REQUEST_FROM_PHOTOS) {
            if (resultCode == RESULT_OK) {
                // Load the bitmap file returned from the photos app
                Bitmap foundImg = null;
                try {
                    Uri selectedImg = data.getData();
                    foundImg = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImg);
                }
                catch (Exception npe) {
                    npe.printStackTrace();
                }

                // Start the inference task to identify text in the image
                if (useOnDevice) {
                    AsyncTask onInfTask = new OnDeviceInf().execute(foundImg);
                }
                else {
                    AsyncTask onInfTask = new OffDeviceInf().execute(foundImg);
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Event Listeners
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**Event listener for when the "from camera" button is pressed.
     * Starts the camera app to get a picture from it*/
    private View.OnClickListener fromCameraListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Prepare the file URI
            Uri photoURI = preparePhotoFile();

            // Create the intent and add photo URI
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (photoURI != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            }

            // Send the intent
            List<ResolveInfo> activities = getPackageManager().queryIntentActivities(takePictureIntent, 0);
            if (activities.size() >= 1) {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    };

    /**Event listener for when the "from photos" button is pressed.
     * Starts the photos app to get a picture from it*/
    private View.OnClickListener fromPhotosListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Create the intent
            Intent getPictureIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            // Send the intent
            List<ResolveInfo> activities = getPackageManager().queryIntentActivities(getPictureIntent, 0);
            if (activities.size() >= 1) {
                startActivityForResult(getPictureIntent, REQUEST_FROM_PHOTOS);
            }
        }
    };

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Async Inference tasks
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /** AsyncTask to run inference on-device */
    private class OnDeviceInf extends AsyncTask<Bitmap, Float, String> {

        /**Run inference using on device Firebase model*/
        protected String doInBackground(Bitmap... imgFiles) {

            // Prepare image for inference
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imgFiles[0]);

            // Run the model
            Task<FirebaseVisionText> result = onDeviceModel.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        Log.d("VISION", "Inference completed successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("VISION", "Inference failed");
                    }
                });

            // Get the results
            String infText = "";
            try {
                infText = result.getResult().getText();
            } catch(NullPointerException npe) {
                npe.printStackTrace();
            }
            return infText;
        }

        /**Send results to UI */
        protected void onPostExecute(String result){
            //TODO: Send result text to TextView
        }
    }

    /** AsyncTask to run inference off-device */
    private class OffDeviceInf extends AsyncTask<Bitmap, Float, String> {

        /**Run inference using cloud-based Firebase model*/
        protected String doInBackground(Bitmap... imgFiles) {
            // Prepare image for inference
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imgFiles[0]);

            // Run the model
            Task<FirebaseVisionText> result = offDeviceModel.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                            Log.d("VISION", "Inference completed successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("VISION", "Inference failed");
                        }
                    });

            // Get the results
            String infText = "";
            try {
                infText = result.getResult().getText();
            } catch(NullPointerException npe) {
                npe.printStackTrace();
            }
            return infText;
        }

        /**Send results to UI */
        protected void onPostExecute(String result){
            //TODO: Send result text to TextView
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Helper Functions
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /**Sets up the file Uri for storing a photo to be used for inference */
    private Uri preparePhotoFile() {
        Uri photoUri = null;
        try {
            File photoStorage = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File photo = File.createTempFile("temp", ".bmp", photoStorage);
            photoUri = FileProvider.getUriForFile(this, "wpi.jhyuen.deeplearningproject.fileprovider", photo);

            // Get the photo path
            curPhotoPath = photo.getAbsolutePath();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        return photoUri;
    }
}
