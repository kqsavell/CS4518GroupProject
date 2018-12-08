package wpi.jhyuen.deeplearningproject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TextInputEditText;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    // Symbolic Constants
    private final static int REQUEST_IMAGE_CAPTURE = 1;
    private final static int REQUEST_FROM_PHOTOS = 2;

    // Class Variables
    private FirebaseVisionTextRecognizer onDeviceModel = null;
    private FirebaseVisionTextRecognizer offDeviceModel = null;
    public boolean useOnDevice = true;

    private String curPhotoPath = "";
    public BottomSheetBehavior bsBehavior = null;

    // Widgets
    private TextView mTextView_latency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Prepare models
        onDeviceModel = FirebaseVision.getInstance().getOnDeviceTextRecognizer();
        offDeviceModel = FirebaseVision.getInstance().getCloudTextRecognizer();

        //TODO: Hook up event listeners for the camera/photo app button

        // Hook up widget variables
        mTextView_latency = findViewById(R.id.textView_latency);
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
    public View.OnClickListener fromCameraListener = new View.OnClickListener() {
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
    public View.OnClickListener fromPhotosListener = new View.OnClickListener() {
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

    /**Event listener for when the "email" button is pressed.
     * Sends an email to the user */
    public View.OnClickListener emailListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            sendEmail();
        }
    };


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Async Inference tasks
    ////////////////////////////////////////////////////////////////////////////////////////////////

    /** AsyncTask to run inference on-device */
    private class OnDeviceInf extends AsyncTask<Bitmap, Float, String> {

        float OnDefStart;
        float OnDefEnd;

        @Override
        protected void onPreExecute()
        {
            OnDefStart = SystemClock.uptimeMillis(); // Start timer
        }

        /**Run inference using on device Firebase model*/
        protected String doInBackground(Bitmap... imgFiles) {
            // Prepare image for inference
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imgFiles[0]);

            // Run the model
            Task<FirebaseVisionText> result = onDeviceModel.processImage(image)
                .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                    @Override
                    public void onSuccess(FirebaseVisionText firebaseVisionText) {
                        Log.d("VISION", "On Device inference completed successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("VISION", "Off device inference failed");
                    }
                });

            // Get the results
            String infText = "";
            try {
                Tasks.await(result);
                infText = result.getResult().getText();
                Log.d("VISION", "Inferred text: " + infText);
            } catch(Exception npe) {
                npe.printStackTrace();
            }
            return infText;
        }

        /**Send results to UI */
        protected void onPostExecute(String result){
            // Calculate time taken to do inference
            OnDefEnd = SystemClock.uptimeMillis();
            String latency = "" + (OnDefEnd - OnDefStart);
            mTextView_latency.setText("Inference Latency: " + latency + "ms");

            bsBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            EditText notes = (EditText)findViewById(R.id.editNotes);
            notes.setText(result);
        }
    }

    /** AsyncTask to run inference off-device */
    private class OffDeviceInf extends AsyncTask<Bitmap, Float, String> {

        float OffDefStart;
        float OffDefEnd;

        @Override
        protected void onPreExecute()
        {
            OffDefStart = SystemClock.uptimeMillis(); // Start timer
        }

        /**Run inference using cloud-based Firebase model*/
        protected String doInBackground(Bitmap... imgFiles) {
            OffDefStart = SystemClock.uptimeMillis(); // Start timer

            // Prepare image for inference
            FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(imgFiles[0]);

            // Run the model
            Task<FirebaseVisionText> result = offDeviceModel.processImage(image)
                    .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                        @Override
                        public void onSuccess(FirebaseVisionText firebaseVisionText) {
                            Log.d("VISION", "Off device inference completed successfully");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("VISION", "Off device inference failed");
                        }
                    });

            // Get the results
            String infText = "";
            try {
                Tasks.await(result);
                infText = result.getResult().getText();
                Log.d("VISION", "Inferred text: " + infText);
            } catch(Exception npe) {
                npe.printStackTrace();
            }
            return infText;
        }

        /**Send results to UI */
        protected void onPostExecute(String result){
            // Calculate time taken to do inference
            OffDefEnd = SystemClock.uptimeMillis();
            String latency = "" + (OffDefEnd - OffDefStart);
            mTextView_latency.setText("Inference Latency: " + latency + "ms");

            bsBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            EditText notes = (EditText)findViewById(R.id.editNotes);
            notes.setText(result);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Email
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void sendEmail() {
        Log.i("Send email", "");

        Pattern emailPattern = Patterns.EMAIL_ADDRESS; // API level 8+
        Account[] accounts = AccountManager.get(MainActivity.this).getAccounts();
        String possibleEmail = "";

        EditText titleText  = findViewById(R.id.editTitle);
        EditText bodyText  = findViewById(R.id.editNotes);
        String title = titleText.getText().toString();
        String body = bodyText.getText().toString();

        for (Account account : accounts) {
            if (emailPattern.matcher(account.name).matches()) {
                possibleEmail = account.name;
            }
        }

        if (possibleEmail == "") {
            System.out.print("No email found");
        }

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("message/rfc822");
        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{possibleEmail});
        i.putExtra(Intent.EXTRA_SUBJECT, title);
        i.putExtra(Intent.EXTRA_TEXT   , body);
        try {
            startActivity(Intent.createChooser(i, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
        
    }
}
