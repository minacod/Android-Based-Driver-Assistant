package com.example.android.captureframes;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {
    SurfaceView mVideoCaptureView;
    Camera mCamera;
    CameraPreview mCp;
    EditText mEditText;
    TextView mTextAlertView;
    int counter;
    Long time;
    ArrayList<Long> cap;
    ArrayList<Long> res;
    RequestQueue requestQueue;
    Boolean responseFlag;
    int frameWidth;
    int frameHeight;
    OverlayView overlayView;
    Double frameArea;
    ImageView alertView;
    MediaPlayer mp ;
    private BorderedText borderedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    1);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    2);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    2);
        }

        final float textSizePx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                10, getResources().getDisplayMetrics());
        borderedText = new BorderedText(textSizePx);
        borderedText.setTypeface(Typeface.MONOSPACE);

        mVideoCaptureView = (SurfaceView)findViewById(R.id.sv);
        mEditText=(EditText)findViewById(R.id.et_ip);
        mTextAlertView=(TextView)findViewById(R.id.tv_alert_objects);
        alertView=(ImageView)findViewById(R.id.iv_alert);
        overlayView=(OverlayView)findViewById(R.id.ov_rectangles);
        mp =new MediaPlayer();
        Uri uri = Uri.parse("android.resource://com.example.android.captureframes/"+ R.raw.alert);
        try {
            mp.setDataSource(MainActivity.this, uri );
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        cap = new ArrayList<>();
        res = new ArrayList<>();

        requestQueue = Volley.newRequestQueue(this);

        SurfaceHolder sh = mVideoCaptureView.getHolder();
        mCamera = Camera.open(findBackFacingCamera());
        Camera.Parameters parameters = mCamera.getParameters();
        //parameters.setPreviewFpsRange(10000,10000);
        mCamera.setParameters(parameters);

        mCp = new CameraPreview(this,mCamera,sh);
        mCp.refreshCamera(mCamera);
        responseFlag =true;
        counter=0;

    }

    public void cap(View view) {
        time=System.currentTimeMillis();
        mCp.refreshCamera(mCamera);
        mEditText.setVisibility(View.GONE);
        mCamera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {
                if(responseFlag){
                    Long tmp = System.currentTimeMillis();
                    cap.add(tmp);
                    counter++;
                    Camera.Parameters parameters = camera.getParameters();
                    int width = parameters.getPreviewSize().width;
                    frameWidth = width;
                    int height = parameters.getPreviewSize().height;
                    frameHeight= height ;
                    frameArea = (double) (height * width);
                    int imageFormat = parameters.getPreviewFormat();
                    if (imageFormat == ImageFormat.NV21)
                    {
                        //responseFlag=false;
                        float [] rgb = yuvToRgb( bytes, width, height);
                        //serverOption(bytes,width,height);

                        Long tmp2 = System.currentTimeMillis();
                        res.add(tmp2);
                }
                }
            }
        });
    }

    private void serverOption(byte[] bytes, int width, int height) {
        Rect rect = new Rect(0, 0, width, height);
        YuvImage img = new YuvImage(bytes, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Log.i("before compressToJpeg","Time "+String.valueOf(counter));
        img.compressToJpeg(rect, 15, out );
        Log.i("after compressToJpeg","Time "+String.valueOf(counter));
        String url;

        if(mEditText.getText().toString().equals(""))
            url = "http://192.168.1.4:8000/api/";
        else
            url = mEditText.getText().toString();
        Log.i("before base64","Time "+String.valueOf(counter));
        String image = getStringImage(out);
        Log.i("after base64","Time "+String.valueOf(counter));

        uploadImage(url,image);

    }

    public void stop(View view) {
        if(mCamera!=null){
            mEditText.setVisibility(View.VISIBLE);
            mCamera.stopPreview();
        }
        time=System.currentTimeMillis()-time;
        Log.i("Fps",String.valueOf(res.size()*1000/time));
        long ava=0;
        int i;
        for(i=0;i<res.size();i++){
            Long dif=res.get(i)-cap.get(i);
            ava+=dif;
            Log.i("time taken",String.valueOf(dif));
        }
        Log.i("average",String.valueOf(ava/i));
    }

    private int findBackFacingCamera() {
        int cameraId = -1;
        //Search for the back facing camera
        //get the number of cameras
        int numberOfCameras = Camera.getNumberOfCameras();
        //for every camera check
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    public String getStringImage(ByteArrayOutputStream ba) {
        byte[] imageByte = ba.toByteArray();
        try {
            ba.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Base64.encodeToString(imageByte, Base64.DEFAULT);
    }

    private void uploadImage(String url, final String image){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                boolean alert=false;
                responseFlag=true;
                Log.i("YOLO Response",response);
                StringBuilder objects= new StringBuilder();
                StringBuilder alertObjects= new StringBuilder();
                List<ClassifiedObject> result = new ArrayList<>();

                try {
                    JSONObject json = new JSONObject(response);
                    int i=0;
                    while(true){
                       JSONObject tmp = json.getJSONObject(String.valueOf(i));
                       if(tmp == null)
                           break;
                       String object =tmp.getString("name");
                       float acc =(float) tmp.getDouble("accuracy");
                       objects.append(object).append(" ");
                       Double x =tmp.getDouble("x1");
                       Double y =tmp.getDouble("y1");
                       Double h =tmp.getDouble("y2");
                       Double w =tmp.getDouble("x2");
                       BoxPosition boxPosition = new BoxPosition(x-(w/2),y-(h/2),w,h);
                       ClassifiedObject co = new ClassifiedObject(i,object,acc,boxPosition);
                       result.add(co);
                       Double objArea = h*w ;
                       Double ratio = objArea/frameArea;
                       Double fw=(double)frameWidth;
                       Log.i("ratio",String.valueOf(ratio));
                       if(object.equals("car")&&ratio>=0.08&&(x>(fw/5)&&x<(fw-fw/5))){
                           alert=true;
                           alertObjects.append(object).append(" ");
                       }
                       else if (object.equals("person")&&ratio>=0.015){
                           alert=true;
                           alertObjects.append(object).append(" ");
                       }
                       else if (object.equals("bus")&& ratio>=0.11&&(x>(fw/5)&&x<(fw-fw/5))){
                           alert=true;
                           alertObjects.append(object);
                       }
                       else if (object.equals("truck")&& ratio>=0.11&&(x>(fw/5)&&x<(fw-fw/5))){
                           alert=true;
                           alertObjects.append(object);
                       }
                       else if ( ratio>=0.17&&(x>(fw/5)&&x<(fw-fw/5))){
                           alert=true;
                           alertObjects.append(object);
                       }
                       i++;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Log.i("objects",objects.toString());
                overlayView.setFrameHeight(frameHeight);
                overlayView.setFrameWidth(frameWidth);
                overlayView.setResults(result);
                overlayView.addCallback(MainActivity.this::renderAdditionalInformation);
                if (alert)
                {
                    alertView.setVisibility(View.VISIBLE);
                    mTextAlertView.setVisibility(View.VISIBLE);
                    mTextAlertView.setText(alertObjects.toString());
                    mp.start();
                }else {
                    if(mp.isPlaying())
                        mp.stop();
                    alertView.setVisibility(View.GONE);
                    mTextAlertView.setVisibility(View.GONE);
                    mTextAlertView.setText(alertObjects.toString());

                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley Error",error+"");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String ,String> params = new HashMap<String,String>();
                params.put("image",image);
                return params;
            }
        };
        requestQueue.add(stringRequest);
    }

    private void renderAdditionalInformation(final Canvas canvas) {
        final Vector<String> lines = new Vector();
        borderedText.drawLines(canvas, 10, 10, lines);
    }

    int [] decodeYUV420SP( byte[] yuv420sp, int width, int height) {

        int[] rgb = new int[width*height];
        final int frameSize = width * height;

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
        return rgb;
    }

    float[] yuvToRgb(byte[] pixels, int width, int height) {
        Log.i("bytearray",String.valueOf(pixels.length));
        int size = width * height / 2;
        Log.i("bytearray",String.valueOf(size));
        float[] rgb = new float[width * height / 2 * 3];
        for (int i = 0; i < size; i++) {
            int r = pixels[i * 3] & 0xff; // red
            int g = pixels[i * 3 + 1] & 0xff; // green
            int b = pixels[i * 3 + 2] & 0xff; // blue
            float tmp = (float) (r/256.0);
            rgb[i*3] =tmp;
            tmp = (float) (g/256.0);
            rgb[i*3+1]=tmp;
            tmp=(float) (b/256.0);
            rgb[i*3+2]=tmp;
        }
        printMax(rgb);
        return rgb;
    }

    void printMax(float[] m){
        float max=0;
        for (int i=0;i<m.length;i++)  {
            if(m[i]>max)
                max=m[i];
        }
        Log.i("max",String.valueOf(max));
    }

    @Override
    protected void onDestroy() {
        if(mCamera!=null){
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
        if(mp!=null)
            mp.release();
        super.onDestroy();
    }
}
