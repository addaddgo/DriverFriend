package tool;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.camera2.*;
import android.location.SettingInjectorService;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.app.AlertDialog;
import com.example.hp.driverfriend.R;

import org.litepal.crud.DataSupport;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import activity.MainActivity;
import manager.AAL;


/*
 * 获取一张照片：相机 相册
 * getPictureFromAlbum 从相册中获取照片
 * 通许码 3
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

    //使用其的回调
    private ImageReader imageReader;

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

    //申请限权提示框
    private AlertDialog alertDialogAgain;//允许再次询问
    private AlertDialog alertDialogNoAgain;//不允许再次询问

    //图片数据名
    public static String  imageName = "bitmap";
    //图片数据
    private byte[] imageData;
    //成功返回上一个活动照片
    public static int getImage = 1;
    public static int noImage = 2;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity);
        this.surfaceView = findViewById(R.id.camera_surface_view);
        this.takeAfterEnsureButton = findViewById(R.id.take_after_ensure_button);
        this.resetButton = findViewById(R.id.reset_image_button);
        this.albumButton = findViewById(R.id.album_button);
        this.surfaceHolder = this.surfaceView.getHolder();
        //TODO(1): 需要和服务端商讨
        this.imageReader = ImageReader.newInstance(500,500,ImageFormat.JPEG,1);
        this.imageReader.setOnImageAvailableListener(new CameraImageReaderListener(),cameraHandler);
        openMyCamera();
        initButton();
    }

    //初始化相机
    private void initCamera(){
        this.cameraManager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        this.handlerThread = new HandlerThread("Camera2");
        this.handlerThread.start();
        this.cameraHandler = new Handler(this.handlerThread.getLooper());
        this.mainHandler = new Handler(getMainLooper());
        shouldOpenCamera();
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

    //相机
    private void openMyCamera(){

        this.surfaceHolder.addCallback(this);
        //点击拍照
        this.takeAfterEnsureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lock();
            }
        });
    }

    //如果限权允许就打开相机，反之申请
    private void shouldOpenCamera(){
            if(checkCallingOrSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){//限权允许
            }else{//申请限权
                String[] per = new String[]{Manifest.permission.CAMERA};
                if(shouldShowRequestPermissionRationale(per[0])){
                    initAlertDialogAgain();
                }else{
                    initAlertDialogNoAgain();
                }
            }
    }

    //允许再次询问提示框
    private void initAlertDialogAgain() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("限权申请")
                .setMessage("请打开相机权限")
                .setCancelable(false)
                .setPositiveButton("允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(alertDialogAgain != null && alertDialogAgain.isShowing()){
                            alertDialogAgain.dismiss();
                        }
                        String[] strings = {Manifest.permission.CAMERA};
                        requestPermissions(strings,AAL.SETTING);
                    }
                });
        this.alertDialogAgain = builder.create();
        this.alertDialogAgain.setCanceledOnTouchOutside(false);
        this.alertDialogAgain.show();
    }
    //不允许再次询问提示框
    private void initAlertDialogNoAgain(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("限权申请")
                .setMessage("您没有打开相机权限")
                .setCancelable(false)
                .setPositiveButton("去允许", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(alertDialogNoAgain != null && alertDialogNoAgain.isShowing()){
                            alertDialogNoAgain.dismiss();
                        }
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package",getPackageName(),null);
                        intent.setData(uri);
                        startActivityForResult(intent,AAL.SETTING);
                    }
                });
        this.alertDialogNoAgain = builder.create();
        this.alertDialogNoAgain.setCanceledOnTouchOutside(false);
        this.alertDialogNoAgain.show();
    }

    //申请结果
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == AAL.SETTING){
            shouldOpenCamera();
        }
    }
    //设置结果
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AAL.SETTING){
            shouldOpenCamera();
        }
        if(requestCode == AAL.SelectImageActivity){
            if(resultCode == SelectImageActivity.unsuccessful){
                MyToast.toastShowShort(this,"取消");
            }
            if(resultCode == SelectImageActivity.successful){
                Intent intent = new Intent();
                intent.putExtra(imageName,imageData);
                setResult(getImage,intent);
                finish();
            }
        }
        //TODO(1)contentResolver弄明白
        if(requestCode == AAL.ALBUM){
            if(resultCode == RESULT_OK){
                Uri uri = data.getData();
                ContentResolver contentResolver = this.getContentResolver();
                Bitmap bitmap;
                try {
                    bitmap = BitmapFactory.decodeStream(contentResolver.openInputStream(uri));
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    if(bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream)){
                        Intent intent = new Intent();
                        intent.putExtra(imageName,byteArrayOutputStream.toByteArray());
                        setResult(getImage,intent);
                        finish();
                    }else {
                        MyToast.toastShowLong(this,"图片打开失败");
                    }

                }catch (FileNotFoundException e){
                    e.printStackTrace();
                    MyToast.toastShowLong(this,"图片打开失败");
                }

            }
        }
    }

    //拍摄请求
    private void lock(){
        try {
            this.captureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CameraMetadata.CONTROL_AF_TRIGGER_START);
            this.cameraCaptureSession.capture(this.captureRequestBuilder.build(), new takePictureCall(),
                    cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    //拍摄请求回调
    private class takePictureCall extends CameraCaptureSession.CaptureCallback{
        @Override
        public void onCaptureCompleted(CameraCaptureSession session,CaptureRequest request,TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            cameraCaptureSession = session;
            takeAPicture();
        }
    }
    //拍照
    private void takeAPicture(){
        try{
            final CaptureRequest.Builder builder = this.cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            builder.addTarget(this.imageReader.getSurface());
            builder.set(CaptureRequest.CONTROL_AF_MODE,
                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
            builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);
            this.cameraCaptureSession.stopRepeating();
            this.cameraCaptureSession.abortCaptures();
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

    //获取相片后，imageReader的回调
    private class CameraImageReaderListener implements ImageReader.OnImageAvailableListener{
        @Override
        public void onImageAvailable(ImageReader reader) {
            Intent intent = new Intent(getSelf(),SelectImageActivity.class);
            Image image = reader.acquireLatestImage();
            ByteBuffer byteBuffer = image.getPlanes()[0].getBuffer();
            imageData = new byte[byteBuffer.remaining()];
            byteBuffer.get(imageData);
            intent.putExtra(imageName,imageData);
            startActivityForResult(intent,AAL.SelectImageActivity);
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
            cameraDevice.createCaptureSession(Arrays.asList(this.surfaceHolder.getSurface(),this.imageReader.getSurface()),new MyCameraCaptureSessionStateCallBack(),null);
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

    //初始化按钮
    private void initButton(){
        this.albumButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAlbum();
            }
        });
    }
    //打开相册
    public void openAlbum(){
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,AAL.ALBUM);
    }


    //从相册中获取相片
    public Bitmap getPictureFromAlbum(){
        return null;
    }

    //活动的activityForResult方法
    public void inTheEnd(int requestCode, int resultCode, Intent data){

    }

    //得到本身
    private Context getSelf(){
        return this;
    }

    //保存的图片
    private class MyImage extends DataSupport{
        private Image identificationImage;
        private void setIdentificationImage(Image identificationImage) {
            this.identificationImage = identificationImage;
        }
    }

}
