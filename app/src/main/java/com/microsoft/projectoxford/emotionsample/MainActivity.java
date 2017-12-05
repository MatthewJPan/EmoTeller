//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.
//
// Microsoft Cognitive Services (formerly Project Oxford): https://www.microsoft.com/cognitive-services
//
// Microsoft Cognitive Services (formerly Project Oxford) GitHub:
// https://github.com/Microsoft/Cognitive-Emotion-Android
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
package com.microsoft.projectoxford.emotionsample;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.FaceRectangle;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.microsoft.projectoxford.emotionsample.helper.ImageHelper;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;

import android.content.SharedPreferences;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.R.attr.content;

public class MainActivity extends Activity implements NetBroadcastReceiver.netEventHandler {

    public final int NUMBER_OF_EMOTIONS = 8;

    private EmotionServiceClient client;

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private ImageView cameraView;

    private TextView mTextView;
    private Button captureButton;
    private Button stopButton;

    private Bitmap mBitmap;
    private Uri uriTarget;
    private Timer timer;

    public final double FACE_SIZE_THRESHOLD = 10000.0;
    public final double SIZE_DIFFERENCE_THRESHOLD = 14000.0;
    public final double CENTER_LEFT_VALUE = 330;

    private String hapticEmotion;
    private String previousEmotion;

    // if the stop button is clicked
    private boolean mute;

    private boolean upFlag1;
    private boolean upFlag2;
    private boolean downFlag1;
    private boolean downFlag2;
    AudioManager audioManager;

    MediaPlayer mediaPlayer;
    //    private boolean wasEmpty;
    PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("!!!", "activity created");
        super.onCreate(savedInstanceState);
        // Keep screen on when the app is running
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        audioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//        wasEmpty = true;

//        PowerManager powerManager = (PowerManager)this.getSystemService(Context.POWER_SERVICE);
//        wakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "My Lock");
//        wakeLock.acquire();
        NetBroadcastReceiver.mListeners.add(this);
        if (client == null) {
            client = new EmotionServiceRestClient(getString(R.string.subscription_key));
        }

        hapticEmotion = "happiness";
        previousEmotion = "some emotion to avoid conflict with null";

        mute = false;

        mTextView = (TextView) findViewById(R.id.textResult);
        mTextView.setMovementMethod(new ScrollingMovementMethod());

//        mCamera = getCameraInstance();
//        mCameraPreview = new CameraPreview(this, getCameraInstance());
        mCameraPreview = new CameraPreview(this);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);

        cameraView = (ImageView) findViewById(R.id.imageView);

        captureButton = (Button) findViewById(R.id.button_capture);
//        captureButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // disable the button once it's clicked
//                captureButton.setEnabled(false);
//                mute = false;
//                timer = new Timer();
//                // takes a photo every 2 second
//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        mCamera = mCameraPreview.getCamera();
//                        mCamera.takePicture(null, null, mPicture);
//                    }
//                }, 0, 2000);
//            }
//
//        });

        stopButton = (Button) findViewById(R.id.button_stop);
//        stopButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (timer != null) {
//                    captureButton.setEnabled(true);
//                    mute = true;
//                    timer.cancel();
//                }
//            }
//
//        });

        if (getString(R.string.subscription_key).startsWith("Please")) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.add_subscription_key_tip_title))
                    .setMessage(getString(R.string.add_subscription_key_tip))
                    .setCancelable(false)
                    .show();
        }


    }

//    public boolean isConnected() throws InterruptedException, IOException
//    {
//        String command = "ping -c 1 google.com";
//        return (Runtime.getRuntime().exec (command).waitFor() == 0);
//    }

    public void onNetChange() {
        if (NetUtil.getNetworkState(this) == NetUtil.NETWORN_NONE) {
            //Toast.makeText(this,"there is problem in Internect connection, please check the settings", Toast.LENGTH_SHORT);
            Log.e("net error", "******error******");
            //TODO: add "internet down" audio

        } else {
            //Toast.makeText(this,"the Internet connection is resumed", Toast.LENGTH_SHORT);
            //TODO: add "internet resumed" audio
        }
    }
    @Override
    protected void onStop() {
        if (timer != null) {
            captureButton.setEnabled(true);
            mute = true;
            timer.cancel();
        }
        super.onStop();
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

    /**
     * Take a photo and do emotion recognition
     */
    private void doRecognize() {
        //mButtonSelectImage.setEnabled(false);

        // Do emotion detection using auto-detected faces.
        try {
            new MainActivity.doRequest(false).execute();
        } catch (Exception e) {
            Log.e("error", e.toString());
        }

//        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
//        if (faceSubscriptionKey.equalsIgnoreCase("Please_add_the_face_subscription_key_here")) {
//            mEditText.append("\n\nThere is no face subscription key in res/values/strings.xml." +
//                    " Skip the sample for detecting emotions using face rectangles\n");
//        } else {
//            // Do emotion detection using face rectangles provided by Face API.
//            try {
//                new MainActivity.doRequest(true).execute();
//            } catch (Exception e) {
//                mEditText.append("Error encountered. Exception is: " + e.toString());
//            }
//        }
    }

    /**
     * Open camera
     * @return Camera instance
     */
    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist
            Log.e("camera error", e.toString());
        }
        return camera;
    }


//    private static File getOutputMediaFile() {
//        File mediaStorageDir = new File(
//                Environment
//                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
//                "MyCameraApp");
//        if (!mediaStorageDir.exists()) {
//            if (!mediaStorageDir.mkdirs()) {
//                Log.d("MyCameraApp", "failed to create directory");
//                return null;
//            }
//        }
//        // Create a media file name
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
//                .format(new Date());
//        File mediaFile;
//        mediaFile = new File(mediaStorageDir.getPath() + File.separator
//                + "IMG_" + timeStamp + ".jpg");
//
//        return mediaFile;
//    }


//    public void activityRecognize(View v) {
//        Intent intent = new Intent(this, RecognizeActivity.class);
//        startActivity(intent);
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private class doRequest extends AsyncTask<String, String, List<RecognizeResult>> {

        // Store error message
        private Exception e = null;
        private boolean useFaceRectangles = false;

        private doRequest(boolean useFaceRectangles) {
            this.useFaceRectangles = useFaceRectangles;
        }

        protected void onPreExecute() {
            // mCamera.stopPreview();
//            try
//            {
//                Thread.sleep(20);
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//            mCamera.startPreview();
//            mCamera.takePicture(null, null, mPicture);
        }

        @Override
        protected List<RecognizeResult> doInBackground(String... args) {
            if (!this.useFaceRectangles) {
                try {
                    return processWithAutoFaceDetection();
                } catch (Exception e) {
                    this.e = e;
                }
            } else {
                try {
                    return processWithFaceRectangles();
                } catch (Exception e) {
                    this.e = e;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecognizeResult> result) {
            super.onPostExecute(result);
            // Display based on error existence

            if (!this.useFaceRectangles) {
                Log.d("detection", "Recognizing emotions with auto-detected face rectangles...");
            } else {
                Log.d("detection", "Recognizing emotions with existing face rectangles from Face API...");
            }

            if (e != null) {
                Log.e("error", e.getMessage());
                this.e = null;

            } else {
                // stop output thread when stop is clicked
                if (!mute) {
                    if (result.size() == 0) {
                        // visual output
//                        mTextView.append("No emotion detected :(\n");
                        displayEmotion(null, null);

                        // haptic & audio output
                        outputAudioFeedback(null);
                        outputHapticFeedback(null);

                        previousEmotion = null;


                    } else {
                        RecognizeResult mainFace = getFace(result);
                        Emotion mainEmotion = getEmotion(mainFace);

                        // visual output
                        displayEmotion(mainFace, mainEmotion);

                        // haptic & audio output
                        outputAudioFeedback(mainEmotion);
                        outputHapticFeedback(mainEmotion);

                        previousEmotion = mainEmotion.getEmotion();
                    }
                }
            }
        }
    }

    private List<RecognizeResult> processWithAutoFaceDetection() throws EmotionServiceException, IOException {
        Log.d("emotion", "Start emotion detection with auto-face detection");

        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long startTime = System.currentTimeMillis();
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE STARTS HERE
        // -----------------------------------------------------------------------

        List<RecognizeResult> result;
        //
        // Detect emotion by auto-detecting faces in the image.
        //
        result = this.client.recognizeImage(inputStream);

        String json = gson.toJson(result);
        Log.d("result", json);
        Log.d("result size", Integer.toString(result.size()));

        Log.d("emotion", String.format("Detection done. Elapsed time: %d ms", (System.currentTimeMillis() - startTime)));
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE ENDS HERE
        // -----------------------------------------------------------------------
        return result;
    }

    private List<RecognizeResult> processWithFaceRectangles() throws EmotionServiceException, com.microsoft.projectoxford.face.rest.ClientException, IOException {
        Log.d("emotion", "Do emotion detection with known face rectangles");
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long timeMark = System.currentTimeMillis();
        Log.d("emotion", "Start face detection using Face API");
        FaceRectangle[] faceRectangles = null;
        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        FaceServiceRestClient faceClient = new FaceServiceRestClient(faceSubscriptionKey, "https://westcentralus.api.cognitive.microsoft.com/face/v1.0");
        Face faces[] = faceClient.detect(inputStream, false, false, null);
        Log.d("emotion", String.format("Face detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));

        if (faces != null) {
            faceRectangles = new FaceRectangle[faces.length];

            for (int i = 0; i < faceRectangles.length; i++) {
                // Face API and Emotion API have different FaceRectangle definition. Do the conversion.
                com.microsoft.projectoxford.face.contract.FaceRectangle rect = faces[i].faceRectangle;
                faceRectangles[i] = new com.microsoft.projectoxford.emotion.contract.FaceRectangle(rect.left, rect.top, rect.width, rect.height);
            }
        }

        List<RecognizeResult> result = null;
        if (faceRectangles != null) {
            inputStream.reset();

            timeMark = System.currentTimeMillis();
            Log.d("emotion", "Start emotion detection using Emotion API");
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE STARTS HERE
            // -----------------------------------------------------------------------
            result = this.client.recognizeImage(inputStream, faceRectangles);

            String json = gson.toJson(result);
            Log.d("result", json);
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE ENDS HERE
            // -----------------------------------------------------------------------
            Log.d("emotion", String.format("Emotion detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));
        }
        return result;
    }

    /**
     * Convert the picture into bitmap and do recognition
     */
    PictureCallback mPicture = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
//
////            dir_image2 = new  File(Environment.getExternalStorageDirectory()+
////                    File.separator+"My Custom Folder");
////            dir_image2.mkdirs();
////            File tmpFile = new File(dir_image2,"TempImage.jpg");
//            File pictureFile = getOutputMediaFile();
////            BitmapFactory.Options options = new BitmapFactory.Options();
////            options.inJustDecodeBounds = false;
//            mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//
//            if (pictureFile == null) {
//                return;
//            }
//            try {
//                FileOutputStream fos = new FileOutputStream(pictureFile);
//                fos.write(data);
//                fos.close();
//                //mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
//                //mBitmap.recycle();
//
////                bmp1 = decodeFile(tmpFile);
////                bmp=Bitmap.createScaledBitmap(bmp1,CamView.getWidth(), CamView.getHeight(),true);
//                mCamera.startPreview();
//
//            } catch (FileNotFoundException e) {
//
//            } catch (IOException e) {
//            }
            mCamera.startPreview();
            uriTarget = getContentResolver().insert//(Media.EXTERNAL_CONTENT_URI, image);
                    (MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());

            OutputStream imageFileOS;
            try {
                imageFileOS = getContentResolver().openOutputStream(uriTarget);
                imageFileOS.write(data);
                imageFileOS.flush();
                imageFileOS.close();

//                Toast.makeText(MainActivity.this,
//                        "Image saved: " + uriTarget.toString(), Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(uriTarget, getContentResolver());
            mBitmap=RotateBitmap(mBitmap,90);

            if (mBitmap != null) {
                // Show the image on screen.
//                                    ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
//                                    imageView.setImageBitmap(mBitmap);

                // Add detection log.
//                                    Toast.makeText(MainActivity.this,
//                                            "Image: " + uriTarget + " resized to ", Toast.LENGTH_LONG).show();

                Log.d("RecognizeActivity", "Image: " + uriTarget + " resized to " + mBitmap.getWidth()
                        + "x" + mBitmap.getHeight());

                cameraView.setImageBitmap(mBitmap);

                // do recognition after convering the picture into bitmap
                doRecognize();

            } else {
                Log.d("error", "no bitmap");
            }
            //mCamera.startPreview();
            //mCamera.startPreview();

            //mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            //iv_image.setImageBitmap(mBitmap);
        }

    };

    /**
     * Get the main face in the recognized face list
     * @param faces the list of recognized faces
     * @return the main face
     */
    private RecognizeResult getFaceOriginal(List<RecognizeResult> faces) {
        double[] faceSizes = new double[faces.size()];
        RecognizeResult face;
        double largest = 0.0;
        int faceNumber = 0;

        for (int i = 0; i < faces.size(); i++) {
            face = faces.get(i);
            faceSizes[i] = face.faceRectangle.height * face.faceRectangle.width;
            if (largest < faceSizes[i]) {
                largest = faceSizes[i];
                faceNumber = i;
            }
        }
        return faces.get(faceNumber);
    }

    /**
     * Get the main face in the recognized face list
     * @param faces the list of recognized faces
     * @return the main face
     */
    private RecognizeResult getFace(List<RecognizeResult> faces) {
        double faceSize;
        List<RecognizeResult> selectedFaces = new ArrayList<RecognizeResult>();
        RecognizeResult face;

        // select faces with size no less than the threshold
        for (int i = 0; i < faces.size(); i++) {
            face = faces.get(i);
            faceSize = getFaceSize(face);
            Log.d("*face size*", Double.toString(faceSize));
            if (faceSize >= FACE_SIZE_THRESHOLD) {
                selectedFaces.add(face);
            }
        }

        int selected = selectedFaces.size();
        if (selected == 0) {
            return null;
        } else if (selected == 1) {
            return selectedFaces.get(0);
        } else {
            // remove faces with large size differences
            List<RecognizeResult> facesToRmv = new ArrayList<>();
            for (int i = 0; i < selected; i++) {
                for (int j = 1; j < selected; j++) {
                    double size_i = getFaceSize(selectedFaces.get(i));
                    double size_j = getFaceSize(selectedFaces.get(j));
                    double sizeDif = Math.abs(size_i - size_j);
                    Log.d("*face size Dif*", Double.toString(sizeDif));

                    if (sizeDif > SIZE_DIFFERENCE_THRESHOLD) {
                        // remove smaller face if difference is greater than threshold
                        if (size_i > size_j) {
                            facesToRmv.add(selectedFaces.get(j));
                        } else {
                            facesToRmv.add(selectedFaces.get(i));
                        }
                    }
                }
            }
            selectedFaces.removeAll(facesToRmv);

            selected = selectedFaces.size();
            if (selected == 0) {
                Log.d("error", "no face selected");
                return null;
            } else if (selected == 1) {
                return selectedFaces.get(0);
            } else {
                // select the centered face
                double disToCenter;
                double smallestDis = Math.abs(CENTER_LEFT_VALUE - selectedFaces.get(0).faceRectangle.left);
                int faceNumber = 0;
                for (int i = 0; i < selected; i++) {
                    disToCenter = Math.abs(CENTER_LEFT_VALUE - selectedFaces.get(i).faceRectangle.left);
                    if (disToCenter < smallestDis) {
                        smallestDis = disToCenter;
                        faceNumber = i;
                    }
                }
                return selectedFaces.get(faceNumber);
            }
        }
    }

    private double getFaceSize(RecognizeResult face) {
        return face.faceRectangle.height * face.faceRectangle.width;
    }

    /**
     * Get the main emotion on the face
     * @param face the face to extract emotion from
     * @return the emotion and the score
     */
    private Emotion getEmotion(RecognizeResult face) {
        if (face != null) {
            double[] emotionScores = new double[NUMBER_OF_EMOTIONS];
            String[] emotions = new String[] {"anger", "contempt", "disgust", "fear", "happiness", "neutral", "sadness", "surprise"};
            emotionScores[0] = face.scores.anger;
            emotionScores[1] = face.scores.contempt;
            emotionScores[2] = face.scores.disgust;
            emotionScores[3] = face.scores.fear;
            emotionScores[4] = face.scores.happiness;
            emotionScores[5] = face.scores.neutral;
            emotionScores[6] = face.scores.sadness;
            emotionScores[7] = face.scores.surprise;

            double largest = 0.0;
            String emotion = "anger";
            for (int i = 0; i < emotionScores.length; i++) {
                if (largest < emotionScores[i]) {
                    largest = emotionScores[i];
                    emotion = emotions[i];
                }
            }
            return new Emotion(emotion, largest);
        }
        return null;
    }

    public class Emotion {

        private String emotion;
        private double score;

        public Emotion(String emotion, double score) {
            this.emotion = emotion;
            this.score = score;
        }

        public String getEmotion() {
            return emotion;
        }

        public double getScore() {
            return score;
        }
    }

    /**
     * Display the main emotion of the main face on the screen
     * @param mainFace the main face in the photo
     * @param mainEmotion the emotion to display
     */
    private void displayEmotion(RecognizeResult mainFace, Emotion mainEmotion) {
        if (mainFace != null) {
            // Covert bitmap to a mutable bitmap by copying it
            Bitmap bitmapCopy = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas faceCanvas = new Canvas(bitmapCopy);
            faceCanvas.drawBitmap(mBitmap, 0, 0, null);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(5);
            paint.setColor(Color.RED);

            mTextView.append(mainEmotion.getEmotion());
            mTextView.append(String.format("\t %1$.5f\n", mainEmotion.getScore()));
            Log.d("========", mainEmotion.getEmotion());
//        mEditText.append(String.format("\t contempt: %1$.5f\n", mainFace.scores.contempt));
//        mEditText.append(String.format("\t disgust: %1$.5f\n", mainFace.scores.disgust));
//        mEditText.append(String.format("\t fear: %1$.5f\n", mainFace.scores.fear));
//        mEditText.append(String.format("\t happiness: %1$.5f\n", mainFace.scores.happiness));
//        mEditText.append(String.format("\t neutral: %1$.5f\n", mainFace.scores.neutral));
//        mEditText.append(String.format("\t sadness: %1$.5f\n", mainFace.scores.sadness));
//        mEditText.append(String.format("\t surprise: %1$.5f\n", mainFace.scores.surprise));
//        mEditText.append(String.format("\t face rectangle: %d, %d, %d, %d", mainFace.faceRectangle.left, mainFace.faceRectangle.top, mainFace.faceRectangle.width, mainFace.faceRectangle.height));

            faceCanvas.drawRect(mainFace.faceRectangle.left,
                    mainFace.faceRectangle.top,
                    mainFace.faceRectangle.left + mainFace.faceRectangle.width,
                    mainFace.faceRectangle.top + mainFace.faceRectangle.height,
                    paint);
        } else {
            mTextView.append("No emotion detected :(\n");
        }

    }

    /**
     * Vibrate when the user-specified emotion is detected
     * @param emotion the emotion to output
     */
    private void outputHapticFeedback(Emotion emotion) {
        Vibrator vb = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        if (emotion == null) {
            // No face/emotion detected
            if (previousEmotion != null) {
                long[] pattern = new long[]{0, 50, 50, 100};
                vb.vibrate(pattern, -1);
            }

        } else {
            String currentEmotion = emotion.getEmotion();
            if (currentEmotion.equals(hapticEmotion)) {
                if (!currentEmotion.equals(previousEmotion)) {
                    vb.vibrate(200);
                }
            }
        }
    }

    /**
     * Play audio cues based on the output emotion
     * @param emotion the emotion to output
     */
    private void outputAudioFeedback(Emotion emotion) {
//        MediaPlayer mediaPlayer;
        if (emotion == null) {
            // No face/emotion detected
            if (previousEmotion != null && previousEmotion != "some emotion to avoid conflict with null") {
                mediaPlayer = MediaPlayer.create(this, R.raw.gone);
                mediaPlayer.start();
                Log.i("!!!!!!", "gone is played");
            }
            //wasEmpty=true;
        } else {
            String currentEmotion = emotion.getEmotion();
            if (!currentEmotion.equals(previousEmotion)) {

                switch (emotion.getEmotion()) {
                    case "anger":
                        mediaPlayer = MediaPlayer.create(this, R.raw.angry);
                        mediaPlayer.start();
                        break;
                    case "contempt":
                        mediaPlayer = MediaPlayer.create(this, R.raw.anger);
                        mediaPlayer.start();
                        break;
                    case "disgust":
                        mediaPlayer = MediaPlayer.create(this, R.raw.disgust);
                        mediaPlayer.start();
                        break;
                    case "fear":
                        mediaPlayer = MediaPlayer.create(this, R.raw.fear_short);
                        mediaPlayer.start();
                        break;
                    case "happiness":
                        mediaPlayer = MediaPlayer.create(this, R.raw.happiness_short);
                        mediaPlayer.start();
                        break;
                    case "sadness":
                        mediaPlayer = MediaPlayer.create(this, R.raw.sadness);
                        mediaPlayer.start();
                        break;
                    case "surprise":
                        mediaPlayer = MediaPlayer.create(this, R.raw.surprise_short);
                        mediaPlayer.start();
                        break;
                    case "neutral":
                        if (previousEmotion == null) {
                            mediaPlayer = MediaPlayer.create(this, R.raw.alert1);
                            mediaPlayer.start();
                        }
                        break;
                    default:
                        break;
                }
            }
            //wasEmpty=false;
        }
    }

    private static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Log.d("*****", "onKeyLongPress down");

            downFlag2 = true;
            downFlag1 = false;
            onStopClicked(stopButton);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d("*****", "onKeyLongPress up");
            upFlag2 = true;
            upFlag1 = false;
            onStartClicked(captureButton);
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
            Log.d("*****", "volume down");
            event.startTracking();
            if (downFlag2 == true) {
                downFlag1 = false;
            } else {
                downFlag1 = true;
                downFlag2 = false;
            }

            return true;
            //return super.onKeyDown(keyCode, event);
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            Log.d("*****", "volume up");
            event.startTracking();
            if (upFlag2 == true) {
                upFlag1 = false;
            } else {
                upFlag1 = true;
                upFlag2 = false;
            }

            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_HOME) {
            if (timer != null) {
                captureButton.setEnabled(true);
                mute = true;
                timer.cancel();
            }
        }
        super.onKeyDown(keyCode, event);
        return true;
    }

    @Override
    public void onPause() {
        super.onPause();
//        wakeLock.release();
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            event.startTracking();
            if (downFlag1) {
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
                Log.i("volume-----", "volume down");
            }
            downFlag1 = true;
            downFlag2 = false;
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {

            event.startTracking();
            if (upFlag1) {
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                Log.i("volume+++++", "volume up");
            }
            upFlag1 = true;
            upFlag2 = false;
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
//            Log.d("*****", "volume down");
//            event.startTracking();
//            downButtonStartTime=System.currentTimeMillis();
//
//            return true;
//            //return super.onKeyDown(keyCode, event);
//        }
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            Log.d("*****", "volume up");
//            event.startTracking();
//            upButtonStartTime= System.currentTimeMillis();
//            return true;
//        }
//        if (keyCode == KeyEvent.KEYCODE_HOME) {
//            if (timer != null) {
//                captureButton.setEnabled(true);
//                mute = true;
//                timer.cancel();
//            }
//        }
//        super.onKeyDown(keyCode, event);
//        return true;
//    }
//    @Override
//    public boolean onKeyUp(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            downButtonStopTime=System.currentTimeMillis();
//            event.startTracking();
//            if (downButtonStartTime-downButtonStopTime<300) {
//                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
//                Log.i("volume-----","volume down");
//            }
//            else{
//                onStopClicked(stopButton);
//            }
//
//            return true;
//        }
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            upButtonStopTime=System.currentTimeMillis();
//            event.startTracking();
//            if (upButtonStartTime-upButtonStopTime<300) {
//                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
//                Log.i("volume+++++","volume up");
//            }
//            else{
//                onStartClicked(captureButton);
//            }
//            return true;
//        }
//        return super.onKeyUp(keyCode, event);
//    }


//    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            Log.d("*****", "onKeyLongPress down");
//            onStopClicked(stopButton);
//            return true;
//        }
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            Log.d("*****", "onKeyLongPress up");
//            onStartClicked(captureButton);
//            return true;
//        }
//        return super.onKeyLongPress(keyCode, event);
//    }

    public void onStartClicked(View view) {
        MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.start);
        mediaPlayer.start();
        // disable the button once it's clicked
        captureButton.setEnabled(false);
        mute = false;
        timer = new Timer();
        // takes a photo every 2 second
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
//                if (isConnected()){
//
//                }
                mCamera = mCameraPreview.getCamera();
                mCamera.takePicture(null, null, mPicture);
            }
        }, 0, 1600);
    }

    public void onStopClicked(View view) {
        if (timer != null) {
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.stop);
            mediaPlayer.start();
            captureButton.setEnabled(true);
            mute = true;
            timer.cancel();
        }
    }

//    public Bitmap decodeFile(File f) {
//        Bitmap b = null;
//        try {
//            // Decode image size
//            BitmapFactory.Options option = new BitmapFactory.Options();
//            option.inJustDecodeBounds = true;
//
//            fis = new FileInputStream(f);
//            BitmapFactory.decodeStream(fis, null, option);
//            fis.close();
//            int IMAGE_MAX_SIZE = 1000;
//            int scale = 1;
//            if (option.outHeight > IMAGE_MAX_SIZE || option.outWidth > IMAGE_MAX_SIZE) {
//                scale = (int) Math.pow(
//                        2,
//                        (int) Math.round(Math.log(IMAGE_MAX_SIZE
//                                / (double) Math.max(option.outHeight, option.outWidth))
//                                / Math.log(0.5)));
//            }
//
//            // Decode with inSampleSize
//            BitmapFactory.Options o2 = new BitmapFactory.Options();
//            o2.inSampleSize = scale;
//            fis = new FileInputStream(f);
//            b = BitmapFactory.decodeStream(fis, null, o2);
//            fis.close();
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        return b;
//    }
}
