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
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import android.widget.Toast;

public class MainActivity extends Activity {

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    Bitmap mBitmap, bitmap1;
    private EmotionServiceClient client;
    private EditText mEditText;
    final Context context = this;
    ByteArrayOutputStream binaryOutStream;
    ByteArrayInputStream binaryInStream;
    private File dir_image2, dir_image;
    private FileOutputStream fos;
    private FileInputStream fis;
    ImageView cameraView;
    Uri uriTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (client == null) {
            client = new EmotionServiceRestClient(getString(R.string.subscription_key));
        }

        //mButtonSelectImage = (Button) findViewById(R.id.buttonSelectImage);
        mEditText = (EditText) findViewById(R.id.editTextResult);
        mEditText.setText("");
        mCamera = getCameraInstance();
        mCameraPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mCameraPreview);
        //mCamera.startPreview();
        cameraView = (ImageView) findViewById(R.id.imageView2);

        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                doRecognize();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        //mCamera.startPreview();
                        doRecognize();
//                        mCamera.startPreview();
//                        mCamera.takePicture(null, null, mPicture);
//                        new AsyncTask<Void, Void, Void>() {
//                            @Override
//                            protected void onPreExecute() {
////                                mCamera.startPreview();
////                                mCamera.takePicture(null, null, mPicture);
//                            }
//                            @Override
//                            protected Void doInBackground( Void... voids ) {
//
////                                mCamera.takePicture(null, null, mPicture);
//                                return null;
//                            }
//                            @Override
//                            protected void onPostExecute(Void aVoid) {
////                                mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
////                                        uriTarget, getContentResolver());
////                                if (mBitmap != null) {
////                                    // Show the image on screen.
//////                                    ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
//////                                    imageView.setImageBitmap(mBitmap);
////
////                                    // Add detection log.
//////                                    Toast.makeText(MainActivity.this,
//////                                            "Image: " + uriTarget + " resized to ", Toast.LENGTH_LONG).show();
////
////                                    Log.d("RecognizeActivity", "Image: " + uriTarget + " resized to " + mBitmap.getWidth()
////                                            + "x" + mBitmap.getHeight());
////
////                                    doRecognize();
////                                }
////                                else{
////                                    mEditText.setText("Error: no bitmap" );
////                                }
//
//                            }
//                        }.execute();
                    }
                }, 0, 2000);
            }
        });

        if (getString(R.string.subscription_key).startsWith("Please")) {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.add_subscription_key_tip_title))
                    .setMessage(getString(R.string.add_subscription_key_tip))
                    .setCancelable(false)
                    .show();
        }



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private Camera getCameraInstance() {
        Camera camera = null;
        try {
            camera = Camera.open();
        } catch (Exception e) {
            // cannot get camera or does not exist

        }
        return camera;
    }

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

                Toast.makeText(MainActivity.this,
                        "Image saved: " + uriTarget.toString(), Toast.LENGTH_LONG).show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mEditText.append(uriTarget.toString());
            mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                    uriTarget, getContentResolver());
//            mBitmap=RotateBitmap(mBitmap,90);
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
                doRecognize();
            } else {
                mEditText.setText("Error: no bitmap");
            }
            //mCamera.startPreview();
            //mCamera.startPreview();

            //mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            //iv_image.setImageBitmap(mBitmap);
        }

    };


    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MyCameraApp");
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
                .format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator
                + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }


    public void activityRecognize(View v) {
        Intent intent = new Intent(this, RecognizeActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void doRecognize() {
        //mButtonSelectImage.setEnabled(false);

        // Do emotion detection using auto-detected faces.
        try {
            new MainActivity.doRequest(false).execute();
        } catch (Exception e) {
//            mEditText.append("Error encountered. Exception is: " + e.toString());
            Log.e("error", e.toString());
        }

        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        if (faceSubscriptionKey.equalsIgnoreCase("Please_add_the_face_subscription_key_here")) {
            mEditText.append("\n\nThere is no face subscription key in res/values/strings.xml. Skip the sample for detecting emotions using face rectangles\n");
        } else {
//            // Do emotion detection using face rectangles provided by Face API.
//            try {
//                new MainActivity.doRequest(true).execute();
//            } catch (Exception e) {
//                mEditText.append("Error encountered. Exception is: " + e.toString());
//            }
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

        List<RecognizeResult> result = null;
        //
        // Detect emotion by auto-detecting faces in the image.
        //
        result = this.client.recognizeImage(inputStream);

        String json = gson.toJson(result);
        Log.d("result", json);

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

    private class doRequest extends AsyncTask<String, String, List<RecognizeResult>> {
        // Store error message
        private Exception e = null;
        private boolean useFaceRectangles = false;

        public doRequest(boolean useFaceRectangles) {
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
            mCamera.takePicture(null, null, mPicture);
        }

        @Override
        protected List<RecognizeResult> doInBackground(String... args) {
            if (this.useFaceRectangles == false) {
                try {
                    return processWithAutoFaceDetection();
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            } else {
                try {
                    return processWithFaceRectangles();
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecognizeResult> result) {
            super.onPostExecute(result);
            // Display based on error existence

            if (this.useFaceRectangles == false) {
                mEditText.append("\n\nRecognizing emotions with auto-detected face rectangles...\n");
            } else {
                mEditText.append("\n\nRecognizing emotions with existing face rectangles from Face API...\n");
            }
            if (e != null) {
                //mEditText.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                if (result.size() == 0) {
                    mEditText.append("No emotion detected :(");
                } else {
                    Integer count = 0;
                    // Covert bitmap to a mutable bitmap by copying it
                    Bitmap bitmapCopy = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas faceCanvas = new Canvas(bitmapCopy);
                    faceCanvas.drawBitmap(mBitmap, 0, 0, null);
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(5);
                    paint.setColor(Color.RED);

                    for (RecognizeResult r : result) {
                        mEditText.append(String.format("\nFace #%1$d \n", count));
                        mEditText.append(String.format("\t anger: %1$.5f\n", r.scores.anger));
                        mEditText.append(String.format("\t contempt: %1$.5f\n", r.scores.contempt));
                        mEditText.append(String.format("\t disgust: %1$.5f\n", r.scores.disgust));
                        mEditText.append(String.format("\t fear: %1$.5f\n", r.scores.fear));
                        mEditText.append(String.format("\t happiness: %1$.5f\n", r.scores.happiness));
                        mEditText.append(String.format("\t neutral: %1$.5f\n", r.scores.neutral));
                        mEditText.append(String.format("\t sadness: %1$.5f\n", r.scores.sadness));
                        mEditText.append(String.format("\t surprise: %1$.5f\n", r.scores.surprise));
                        mEditText.append(String.format("\t face rectangle: %d, %d, %d, %d", r.faceRectangle.left, r.faceRectangle.top, r.faceRectangle.width, r.faceRectangle.height));
                        faceCanvas.drawRect(r.faceRectangle.left,
                                r.faceRectangle.top,
                                r.faceRectangle.left + r.faceRectangle.width,
                                r.faceRectangle.top + r.faceRectangle.height,
                                paint);
                        count++;
                    }
//                    ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
//                    imageView.setImageDrawable(new BitmapDrawable(getResources(), mBitmap));
                }
                mEditText.setSelection(0);
                // mCamera.stopPreview();
            }

            //mButtonSelectImage.setEnabled(true);
        }
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

//    public void TakeScreenshot(){
//
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
//        int nu = preferences.getInt("image_num",0);
//        nu++;
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putInt("image_num",nu);
//        editor.commit();
//        mCameraPreview.setDrawingCacheEnabled(true);
//        mCameraPreview.buildDrawingCache(true);
//        mBitmap = Bitmap.createBitmap(mCameraPreview.getDrawingCache());
//        mCameraPreview.setDrawingCacheEnabled(false);
//        binaryOutStream = new ByteArrayOutputStream();
//        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, binaryOutStream);
//        byte[] bitmapdata = binaryOutStream.toByteArray();
//        binaryInStream = new ByteArrayInputStream(bitmapdata);
//
//        String picId=String.valueOf(nu);
//        String myfile="MyImage"+picId+".jpeg";
//
//        dir_image = new  File(Environment.getExternalStorageDirectory()+
//                File.separator+"My Custom Folder");
//        dir_image.mkdirs();
//
//        try {
//            File tmpFile = new File(dir_image,myfile);
//            fos = new FileOutputStream(tmpFile);
//
//            byte[] buf = new byte[1024];
//            int len;
//            while ((len = binaryInStream.read(buf)) > 0) {
//                fos.write(buf, 0, len);
//            }
//            binaryInStream.close();
//            fos.close();
//
//            Toast.makeText(getApplicationContext(),
//                    "The file is saved at :/My Custom Folder/"+"MyImage"+picId+".jpeg",Toast.LENGTH_LONG).show();
//
//            bitmap1 = null;
//            cameraView.setImageBitmap(bitmap1);
//            mCamera.startPreview();
////            button1.setClickable(true);
////            button1.setVisibility(View.VISIBLE);//<----UNHIDE HER
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//    }
//
//    private PictureCallback mPicture = new PictureCallback() {
//
//        @Override
//        public void onPictureTaken(byte[] data, Camera camera) {
//            dir_image2 = new  File(Environment.getExternalStorageDirectory()+
//                    File.separator+"My Custom Folder");
//            dir_image2.mkdirs();
//
//
//            File tmpFile = new File(dir_image2,"TempImage.jpg");
//            try {
//                fos = new FileOutputStream(tmpFile);
//                fos.write(data);
//                fos.close();
//            } catch (FileNotFoundException e) {
//                Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
//            } catch (IOException e) {
//                Toast.makeText(getApplicationContext(),"Error",Toast.LENGTH_LONG).show();
//            }
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//
//            bitmap1 = decodeFile(tmpFile);
//            mBitmap=Bitmap.createScaledBitmap(bitmap1,mCameraPreview.getWidth(),mCameraPreview.getHeight(),true);
//            cameraView.setImageBitmap(mBitmap);
//            tmpFile.delete();
//            TakeScreenshot();
//
//        }
//    };
//
//
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
