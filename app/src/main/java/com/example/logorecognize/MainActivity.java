package com.example.logorecognize;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int RECORD_REQUEST_CODE = 101;
    private static final int CAMERA_REQUEST_CODE = 102;
    private static final int FILE_REQUEST_CODE = 103;
    private static final String CLOUD_VISION_API_KEY = "AIzaSyCnOXf96WlDw0arn1cu3QYQFJabr3w1UDs";
    String str = "Asus#Duy Tan University#Starbucks#Apple Inc.#Nike#Coca-Cola#Amazon#The North Face#Cotton Incorporated#Crocs#Tesla, Inc.#American Broadcasting Company#Adidas#Burger King#Cartoon Network#Budweiser#Boeing#Gigabyte Technology#Sunsilk#Miami Heat#BBC#Burberry#Canon Inc.#Intel#MTV#Dove#Nokia#20th Century Fox#Yahoo!#FedEx#Puma#H&M#YouTube#Google#Supreme#McDonald's#Levi Strauss & Co.#NBA#Heineken Experience#Monster Energy#Unilever#Motorola Solutions#Los Angeles Lakers#Audi#Nestlé#Red Bull#Reebok#ERKE#Walt Disney World#Vans#Converse#Pepsi#Toyota Financial Services#Ford Motor Company#Mazda#Jeep#Mitsubishi Motors#Mercedes-Benz#Bentley#Land Rover#Kia Motors#Hyundai Motor Company#Nissan#Lexus#Chevrolet#BMW#Cadillac#Toyota#Bugatti#Suzuki#Lamborghini#Honda#Porsche#G Suite#Chanel#Walt Disney Pictures#Vietcombank#Vinaphone#Viettel#FPT Software#Vingroup#Vinamilk#Vietnam Airlines#VNPT#Versace#Toshiba#Daikin#LG Electronics#Samsung Group#Panasonic#Sharp Corporation#Gong Cha#KFC#Pizza Hut#Highlands Coffee#Royal Dutch Shell#Voice of Vietnam#THACO#Asia Commercial Bank#Huawei P8#Huawei#Mastercard#Nvidia#Xbox#NBC";
    public List<String> list = new ArrayList<String>(Arrays.asList(str.split("#")));
    private FloatingActionButton fab;
    private FloatingActionButton fab_cam;
    private FloatingActionButton fab_info;
    private Feature feature;
    private Bitmap bitmap;
    private ImageView imgView;
    private Uri uriPath;
    private boolean fab_check = true;
    private int check;
    private List<String> LogoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imgView = (ImageView)findViewById(R.id.imageView);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        feature = new Feature();
        feature.setType("LOGO_DETECTION");
        feature.setMaxResults(10);

        fab = findViewById(R.id.fab);
        fab_cam = findViewById(R.id.fab_cam);
        fab_info = findViewById(R.id.fab_info);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fab_check){
                    fab_cam.show();
                    fab_info.show();
                    fab_check = false;
                }
                else{
                    fab_cam.hide();
                    fab_info.hide();
                    fab_check = true;
                }
            }
        });
        fab_cam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        fab_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,InfoActivity.class);
                if(LogoName != null){
                    String[] ar = new String[LogoName.size()];
                    LogoName.toArray(ar);
                    intent.putExtra("Info",ar);
                }
                startActivity(intent);
            }
        });
    }

    private void selectImage()
    {
        final CharSequence[] items={"Camera","Gallery", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Image");

        builder.setItems(items, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (items[i].equals("Camera")) {

                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.TITLE, "New Picture");
                    values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
                    uriPath = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, uriPath);

                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);
                    }

                } else if (items[i].equals("Gallery")) {

                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");

                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivityForResult(intent, FILE_REQUEST_CODE);
                    }

                } else if (items[i].equals("Cancel")) {
                    dialogInterface.dismiss();
                }
            }
        });
        builder.show();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            makeRequest(Manifest.permission.CAMERA);
        }
        if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            makeRequest(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            makeRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }

    private int checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission);
    }

    private void makeRequest(String permission) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, RECORD_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogoName = new ArrayList<String>();
        ProgressDialog pd = new ProgressDialog(MainActivity.this);
        if(resultCode == RESULT_OK){
            check = 0;
            LogoName = new ArrayList<String>();
            if(requestCode == CAMERA_REQUEST_CODE){

                try {
                    bitmap =
                            scaleBitmapDown(
                                    MediaStore.Images.Media.getBitmap(getContentResolver(), uriPath),
                                    2400);
                } catch (IOException e) { }

                Matrix mat = new Matrix();
                mat.postRotate(90);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0,bitmap.getWidth(),bitmap.getHeight(), mat, true);

                int x = bitmap.getWidth();
                int y = bitmap.getHeight()/2;

                Bitmap resizedbitmap1 = Bitmap.createBitmap(bitmap, 0,0,x, y);
                Bitmap resizedbitmap2 = Bitmap.createBitmap(bitmap, 0,y,x, y);

                callCloudVision(bitmap, feature);
                callCloudVision(resizedbitmap1, feature);
                callCloudVision(resizedbitmap2, feature);

                pd.setMessage("loading");
                pd.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        imgView.setImageBitmap(bitmap);
                    }
                }, 12000);

                imgView.setImageBitmap(bitmap);
            } else if(requestCode == FILE_REQUEST_CODE){

                Uri selectedImageUri = data.getData();
                try {
                    bitmap =  MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                } catch (IOException e) { }

                if(bitmap.getWidth() > bitmap.getHeight()){
                    Matrix mat = new Matrix();
                    mat.postRotate(90);
                    bitmap = Bitmap.createBitmap(bitmap, 0, 0,bitmap.getWidth(),bitmap.getHeight(), mat, true);
                }

                int x = bitmap.getWidth();
                int y = bitmap.getHeight()/2;

                Bitmap resizedbitmap1 = Bitmap.createBitmap(bitmap, 0,0,x, y);
                Bitmap resizedbitmap2 = Bitmap.createBitmap(bitmap, 0,y,x, y);

                callCloudVision(bitmap, feature);
                callCloudVision(resizedbitmap1, feature);
                callCloudVision(resizedbitmap2, feature);

                pd.setMessage("loading");
                pd.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pd.dismiss();
                        imgView.setImageBitmap(bitmap);
                    }
                }, 12000);

                imgView.setImageBitmap(bitmap);
            }
        }
    }

    private void callCloudVision(final Bitmap bitmap, final Feature feature) {
        final List<Feature> featureList = new ArrayList<>();
        featureList.add(feature);

        final List<AnnotateImageRequest> annotateImageRequests = new ArrayList<>();

        AnnotateImageRequest annotateImageReq = new AnnotateImageRequest();
        annotateImageReq.setFeatures(featureList);
        annotateImageReq.setImage(getImageEncodeImage(bitmap));
        annotateImageRequests.add(annotateImageReq);

        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object... params) {
                try {

                    HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
                    JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

                    VisionRequestInitializer requestInitializer = new VisionRequestInitializer(CLOUD_VISION_API_KEY);

                    Vision.Builder builder = new Vision.Builder(httpTransport, jsonFactory, null);
                    builder.setVisionRequestInitializer(requestInitializer);

                    Vision vision = builder.build();

                    BatchAnnotateImagesRequest batchAnnotateImagesRequest = new BatchAnnotateImagesRequest();
                    batchAnnotateImagesRequest.setRequests(annotateImageRequests);

                    Vision.Images.Annotate annotateRequest = vision.images().annotate(batchAnnotateImagesRequest);
                    annotateRequest.setDisableGZipContent(true);
                    BatchAnnotateImagesResponse response = annotateRequest.execute();
                    return formatAnnotation(response);
                } catch (GoogleJsonResponseException e) {
                    Log.d("MainActivity", "failed to make API request because " + e.getContent());
                } catch (IOException e) {
                    Log.d("MainActivity", "failed to make API request because of other IOException " + e.getMessage());
                }
                return "Cloud Vision API request failed. Check logs for details.";
            }

            protected void onPostExecute(String result) {
            }
        }.execute();
    }

    private Bitmap scaleBitmapDown(Bitmap bitmap, int maxDimension) {

        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        int resizedWidth = maxDimension;
        int resizedHeight = maxDimension;

        if (originalHeight > originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = (int) (resizedHeight * (float) originalWidth / (float) originalHeight);
        } else if (originalWidth > originalHeight) {
            resizedWidth = maxDimension;
            resizedHeight = (int) (resizedWidth * (float) originalHeight / (float) originalWidth);
        } else if (originalHeight == originalWidth) {
            resizedHeight = maxDimension;
            resizedWidth = maxDimension;
        }
        return Bitmap.createScaledBitmap(bitmap, resizedWidth, resizedHeight, false);
    }

    @NonNull
    private Image getImageEncodeImage(Bitmap bitmap) {
        Image base64EncodedImage = new Image();

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
        byte[] imageBytes = byteArrayOutputStream.toByteArray();

        base64EncodedImage.encodeContent(imageBytes);
        return base64EncodedImage;
    }

    private String formatAnnotation(BatchAnnotateImagesResponse response) {
        AnnotateImageResponse imageResponses = response.getResponses().get(0);

        List<EntityAnnotation> entityAnnotation;
        entityAnnotation = imageResponses.getLogoAnnotations();
        Canvas canvas = new Canvas(bitmap);
        String name_Logo = "";

        Paint paint = new Paint();
        paint.setTextSize(30.0f);
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(2.0f);
        paint.setStyle(Paint.Style.STROKE);

        Paint paint1 = new Paint();
        paint1.setTextSize(30.0f);
        paint1.setColor(Color.GREEN);
        paint1.setStrokeWidth(2.0f);

        int x = bitmap.getHeight()/2;

        if(check < 2){
            if (entityAnnotation != null) {
                for (EntityAnnotation entity : entityAnnotation) {
                    name_Logo = entity.getDescription();
                    if(LogoName.contains(name_Logo) || !(list.contains(name_Logo)))
                        continue;
                    LogoName.add(name_Logo);
                    String tmp = entity.getBoundingPoly().getVertices().toString().replaceAll("\\D"," ");

                    tmp = tmp.replaceAll("    "," ");
                    tmp = tmp.replaceAll("  "," ");
                    tmp = tmp.trim();

                    String[] position = tmp.split(" ");
                    canvas.drawText(name_Logo, Integer.parseInt(position[0]), Integer.parseInt(position[1])+25, paint1);
                    canvas.drawRect(Float.parseFloat(position[0]), Float.parseFloat(position[1]), Float.parseFloat(position[2]), Float.parseFloat(position[5]), paint);

                }
            }
        }
        else {
            if (entityAnnotation != null) {
                for (EntityAnnotation entity : entityAnnotation) {
                    name_Logo = entity.getDescription();
                    if(LogoName.contains(name_Logo) || !(list.contains(name_Logo)))
                        continue;
                    LogoName.add(name_Logo);
                    String tmp = entity.getBoundingPoly().getVertices().toString().replaceAll("\\D"," ");

                    tmp = tmp.replaceAll("    "," ");
                    tmp = tmp.replaceAll("  "," ");
                    tmp = tmp.trim();

                    String[] position = tmp.split(" ");
                    canvas.drawText(name_Logo, Integer.parseInt(position[0]), Integer.parseInt(position[1])+25+bitmap.getHeight()/2, paint1);
                    canvas.drawRect(Float.parseFloat(position[0]), Float.parseFloat(position[1])+x, Float.parseFloat(position[2]), Float.parseFloat(position[5])+x, paint);

                }
            }
        }
        check++;
        return "Done";
    }
}
