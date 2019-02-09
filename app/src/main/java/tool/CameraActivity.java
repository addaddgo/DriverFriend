package tool;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import com.example.hp.driverfriend.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.PropertyResourceBundle;

/*
 * 获取一张照片：相机 相册
 * getPictureFromAlbum 从相册中获取照片
 */
public class CameraActivity extends Activity implements SurfaceHolder.Callback{

    //相机组件
    private CaptureRequest captureRequest;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private CameraManager cameraManager;
    private CameraCaptureSession cameraCaptureSession;

    //预览
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;

    //储存照片
    private ImageReader imageReader;

    //存储位置
    private File file;

    //为相机打开一个新的线程
    private HandlerThread handlerThread;
    private Handler cameraHandler;
    private Handler mainHandler;

    private Button takeAfterEnsureButton;//确定拍摄
    private Button resetButton;//重新拍摄
    private Button albumButton;//打开相册

    //resultCoed
    private int CHOSE_PHOTO = 1;
    private int TAKE_PHOTO = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);
        this.surfaceView = findViewById(R.id.camera_surface_view);
        this.takeAfterEnsureButton = findViewById(R.id.take_after_ensure_button);
        this.resetButton = findViewById(R.id.reset_image_button);
        this.albumButton = findViewById(R.id.album_button);
        this.surfaceHolder = this.surfaceView.getHolder();
        this.file = new File("/storage/driverFriend/picture");
        //TODO(1): 需要和服务端商讨
        this.imageReader = ImageReader.newInstance(500,500,ImageFormat.JPEG,1);
        this.imageReader.setOnImageAvailableListener(new CameraImageReaderListener(),cameraHandler);
        openMyCamera();
    }
    //开启自定义相机
    private void openMyCamera(){
        this.surfaceHolder.addCallback(this);
        //点击拍照
        this.takeAfterEnsureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takeAPicture();
            }
        });
    }

    //拍照
    private void takeAPicture(){
        try{
            final CaptureRequest.Builder builder = this.cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(this.imageReader.getSurface());
            builder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            builder.set(CaptureRequest.CONTROL_AE_MODE,
                    CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            this.cameraCaptureSession.stopRepeating();
            this.cameraCaptureSession.abortCaptures();
            //Problem 这个管道没有更换，demo里面在拍照之前是换了管道的。要先发起一次请求，然后在回调里更换管道
            this.cameraCaptureSession.capture(builder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted( CameraCaptureSession session,CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                }
            }, this.cameraHandler);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }
    }
    //
    //活动相片后，imageReader的回调
    private class CameraImageReaderListener implements ImageReader.OnImageAvailableListener{
        @Override
        public void onImageAvailable(ImageReader reader) {
            cameraHandler.post(new saver(imageReader.acquireLatestImage(),file));
        }
    }
    //开辟另外的线程保存图片
    private class saver implements Runnable{

        final private Image image;
        private File file;

        private saver(Image image,File file){
            this.image = image;
            this.file = file;
        }
        @Override
        public void run() {
            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            FileOutputStream output = null;
            try {
                output = new FileOutputStream(file);
                output.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                image.close();
                if (null != output) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    //surfaceView的回调
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        initCamera();
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    //TODO(1): 每一次打开相机的时候，查看限权。相机运行时,限权中途关闭的处理措施
    //初始化相机
    private void initCamera(){
        this.cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        this.handlerThread = new HandlerThread("Camera2");
        this.handlerThread.start();
        this.cameraHandler = new Handler(this.handlerThread.getLooper());
        this.mainHandler = new Handler(getMainLooper());
        if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            return;
        }else {
            try {
                cameraManager.openCamera(CameraCharacteristics.LENS_FACING_FRONT + "", new MyCameraStateCallback(), cameraHandler);
            }catch (CameraAccessException e){
                e.printStackTrace();
            }
        }
    }

    //打开相机的回调
    private class MyCameraStateCallback extends CameraDevice.StateCallback{
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
           takeView();
        }

        @Override
        public void onClosed( CameraDevice camera) {
            super.onClosed(camera);
        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }

        @Override
        public void onDisconnected( CameraDevice camera) {

        }
    }

    //预览
    private void takeView(){
        try{
            this.captureRequestBuilder = this.cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(this.surfaceHolder.getSurface());
            cameraDevice.createCaptureSession(Arrays.asList(this.surfaceHolder.getSurface()),new MyCameraCaptureSessionStateCallBack(),null);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }

    }

    //开启管道的回调
    private class MyCameraCaptureSessionStateCallBack extends CameraCaptureSession.StateCallback{
        @Override
        public void onConfigured(CameraCaptureSession session) {
            if(false){
                return;
            }else {
                cameraCaptureSession = session;
                try {
                    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                    captureRequest = captureRequestBuilder.build();
                    cameraCaptureSession.setRepeatingRequest(captureRequest,null,cameraHandler);
                }catch (CameraAccessException e){
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onConfigureFailed( CameraCaptureSession session) {

        }
    }


    //从相册中获取相片
     public Bitmap getPictureFromAlbum(){
         return null;
    }

    //打开相册
    public void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,this.CHOSE_PHOTO);
    }



    //活动的activityForResult方法
    public void inTheEnd(int requestCode, int resultCode, Intent data){

    }

}
