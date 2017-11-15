package com.creative.informatics.camera;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.utils.Converters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import android.net.Uri;


/**
 * Created by K on 9/28/2017.
 */

public class DocCrop extends CordovaPlugin {

    private final String TAG = "DocCrop";
    private String mFilePath;
    private Bitmap mImgBitmap;
    private Bitmap mResultBmp;
    private String mResultPath;
    private File pictureFile;

    private ImageView docview;
    private ImageProcess imageProcess;
    private List<Point> cornerPoint;
    private Button newphotoButton;
    private Button cropButton;

    private double a1,b1,a2,b2,a3,b3,a4,b4;

    private double xscalefactor;
    private double yscalefactor;
    private Uri contentURI;

    Point p1,p2,p3,p4;

    public static String TAG = "ImagePicker";
    private CallbackContext callbackContext;
	private JSONObject params;

    private static final int SELECT_PICTURE = 1;

    private static final String IMAGE_DIRECTORY = "/demonuts";
    private int GALLERY = 1, CAMERA = 2;
    private Mat startM;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.d(TAG, "OpenCV loaded succeffully!!");
                    startM = new Mat();
                default:
                    break;
            }
        }
    };

    public String execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        this.callbackContext = callbackContext;
        this.params = args.getJSONObject(0);
		if (action.equals("fromURI")) {
			// Intent intent = new Intent(this);
			// int max = 20;
			// int desiredWidth = 0;
			// int desiredHeight = 0;
			// int quality = 100;
			if (this.params.has("tx1")) {
				a1 = this.params.getDouble("tx1");
			}
            
			if (this.params.has("ty1")) {
				b1 = this.params.getDouble("ty1");
			}
            
			if (this.params.has("tx2")) {
				a2 = this.params.getDouble("tx2");
			}
            
			if (this.params.has("ty2")) {
				b2 = this.params.getDouble("ty2");
			}
            
			if (this.params.has("tx3")) {
				a3 = this.params.getDouble("tx3");
			}
            
			if (this.params.has("ty3")) {
				b3 = this.params.getDouble("ty3");
			}
            
			if (this.params.has("tx4")) {
				a4 = this.params.getDouble("tx4");
			}
            
			if (this.params.has("ty4")) {
				b4 = this.params.getDouble("ty4");
			}
            
			if (this.params.has("image")) {
				contentURI = this.params.getData("image");
			}
            
			if (this.cordova != null) {
				this.cordova.startActivityForResult((CordovaPlugin) this, DocCrop.this, 0);
			}

            mImgBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
            imageProcess = new ImageProcess();

            xscalefactor = 1.0;
            yscalefactor = 1.0;

            p1 = new Point(a1*xscalefactor,b1*yscalefactor);
            p2 = new Point(a2*xscalefactor,b2*yscalefactor);
            p3 = new Point(a3*xscalefactor,b3*yscalefactor);
            p4 = new Point(a4*xscalefactor,b4*yscalefactor);

            cornerPoint = new ArrayList<Point>();

            cornerPoint.add(p1);
            cornerPoint.add(p2);
            cornerPoint.add(p3);
            cornerPoint.add(p4);

            startM = Converters.vector_Point2f_to_Mat(cornerPoint);
            mResultBmp = imageProcess.warpAuto(mImgBitmap, startM);
        
            storeImage(mResultBmp);
            mResultPath = pictureFile.getAbsolutePath();

            return mResultPath;

		}
		return pathURI;
	}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }


    private void storeImage(Bitmap image) {
        pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG,
                    "Error creating media file, check storage permissions: ");
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private  File getOutputMediaFile(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "CameraScan");

        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == this.RESULT_CANCELED) {
            return;
        }

        if (data != null) {
            // Uri contentURI = data.getData();
            try {
                

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return uri.getPath();
    }

}
