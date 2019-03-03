package activity;
/*
 * 起始活动：申请权限，显示进入界面
 * 权限的申请一次性全部申请
 * 申请的权限：
 * 【访问内存】：SignActivity
 * 【相机】：SignActivity
 * 【网络】：SignActivity
 * 通讯码：1
 */
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import com.example.hp.driverfriend.R;


import java.util.ArrayList;

import activity.signActivity.SignActivity;
import manager.AAL;

public class MainActivity extends Activity {


    /*
     * 目的：记录需要申请的权限
     * 使用处：权限申请处onResume
     * 注意：需要申请的权限，才记录到此。INTERNET这个权限不需要申请。
     */
    private String[] permissions;
    //生成权限列表:permissions
    {
        permissions = new String[]
                {
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA,
                };
    }
    /*
     * Function 与permissions对应的中文名
     * Where 函数translateEnglishPermissions
     */
    private String[] ChinesePermissions;
    {
        ChinesePermissions = new String[]
                {
                        "储存权限",
                        "相机权限",
                };
    }

     // 提示用户申请权限（禁止询问后）
    private AlertDialog alertDialogNoAgain;

     //提示用户申请权限
    private AlertDialog alertDialog;

    //显示：进入界面
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //requestPermissions(this.permissions,1);
        Intent intent  = new Intent(this,MyInformationActivity.class);
        startActivity(intent);
        finish();
    }


    /*权限申请函数
     * 描述：用户必须接受所有的权限申请，才能进入程序。
     * 申请的权限：实例参数permissions
     */
    private void requestMyPermissions(){
        ArrayList<String> permissionsRequestList = new ArrayList<>();
        String[] permissionsRequest;
        //将没有申请的权限加到permissionsRequestList中
        for(int i = 0 ; i < this.permissions.length ; i++){
            if(checkSelfPermission(this.permissions[i]) == PackageManager.PERMISSION_DENIED){
                permissionsRequestList.add(this.permissions[i]);
            }
        }
        //将permissionsRequestList中权限字符放到permissionRequest数组中
        permissionsRequest = new String[permissionsRequestList.size()];
        for (int i = 0; i < permissionsRequest.length; i++) {
            permissionsRequest[i] = permissionsRequestList.get(i);
        }
        //如果有没有被允许的权限，就申请
        if(permissionsRequest.length == 0){//进入程序
            Intent intent = new Intent(this,SignActivity.class);
            startActivity(intent);
            finish();
        }else{//申请
            requestPermissions(this.permissions,1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){

            //判定是否被禁止访问
            boolean toSetting = false;
            for(int i =0; i< permissions.length ;i++){
                if(checkSelfPermission(permissions[i]) == PackageManager.PERMISSION_DENIED) {
                    if (!shouldShowRequestPermissionRationale(permissions[i])) {
                        toSetting = true;
                        break;
                    }
                }
            }

            //将被禁止的权限们，转换成一条中文字符串
           ArrayList<String> stringsDenied = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                    stringsDenied.add(permissions[i]);
                }
            }
            String[] ChinesePermissions = translateEnglishPermission(stringsDenied);
            String ChinesePermission = combineUnablePermissions(ChinesePermissions);
            boolean intoApp = true;
            for(int i = 0; i < permissions.length ; i++){
                if(grantResults[i] == PackageManager.PERMISSION_DENIED){
                    intoApp = false;
                    break;
                }
            }
            //如果权限全部开启
            if(intoApp){
                Intent intent = new Intent(this,SignActivity.class);
                startActivity(intent);
                finish();
            }else {
                if (toSetting) {//如果被禁止再次询问
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setPositiveButton("去允许", new alertDialogButtonNoAgain(this.alertDialogNoAgain))
                            .setMessage("以下的权限被禁止：\n" + ChinesePermission + "请打开以下权限，否则程序无法运行。")
                            .setTitle("权限申请")
                            .setCancelable(false);
                    this.alertDialogNoAgain = builder.create();
                    this.alertDialogNoAgain.setCanceledOnTouchOutside(false);
                    this.alertDialogNoAgain.show();
                } else {//再次询问
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("权限申请")
                            .setMessage("以下的权限被禁止：\n" + ChinesePermission + "没有这些权限，应用程序无法运行。请打开这些权限。")
                            .setPositiveButton("允许", new alertDialogButton(this.alertDialog));
                    this.alertDialog = builder.create();
                    this.alertDialog.setCanceledOnTouchOutside(false);
                    this.alertDialog.show();
                }
            }
        }
    }


     //手动设置应用程序权限(含有禁止再次询问)
    private class alertDialogButtonNoAgain implements DialogInterface.OnClickListener{

        //提醒用户开启权限(禁止询问后)
        private AlertDialog alertDialog;

        //将alertDialog传入
        private alertDialogButtonNoAgain(AlertDialog alertDialog) {
            this.alertDialog = alertDialog;
        }

        //将原先的提示框删除，一系列操作后，跳转到主活动的onActivityResult
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(this.alertDialog != null && alertDialog.isShowing()){
                alertDialog.dismiss();
            }
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package",getPackageName(),null);
            intent.setData(uri);
            startActivityForResult(intent,AAL.SETTING);
        }
    }

     // 弹出：申请权限（允许再次询问）
    private class alertDialogButton implements DialogInterface.OnClickListener{

        //提醒用户开启权限（允许再次询问）
        private AlertDialog alertDialog;

        //将alertDialog传入
        private alertDialogButton(AlertDialog alertDialog){
            this.alertDialog = alertDialog;
        }
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if(this.alertDialog != null && alertDialog.isShowing()){
                alertDialog.dismiss();
            }
            requestMyPermissions();
        }
    }

    //手动设置权限之后,决定是否再次申请
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == AAL.SETTING){
            requestMyPermissions();
        }
    }

    /*
     * @Parameter EnglishStrings{ArrayList<String>} 英文权限名
     * @Return String[] 中文权限名
     * @Exception 如果参数含有非权限字符串(或者是在方法中不出现的权限)，可能会返回长度短于参数的字符串数组，可能为null
     * Description 将英文权限名转换成中文权限名
     */
    private String[] translateEnglishPermission(ArrayList<String> EnglishStrings){
        String[] newStrings = new String[EnglishStrings.size()];
        for (int i = 0 ; i< EnglishStrings.size() ; i++){
            for (int j = 0; j < this.permissions.length; j++) {
                if(EnglishStrings.get(i).equals(this.permissions[j])){
                    newStrings[i] = this.ChinesePermissions[j];
                }
            }
        }
        return newStrings;
    }

    /*
     * @Parameter strings{String[]}
     * @Return String
     * @Exception
     * Description 将字符串组合并成一个字符串
     * 如：
     *  相机权限
     *  储存权限
     */
    private String combineUnablePermissions(String[] strings){
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < strings.length ; i++) {
            stringBuffer.append("   " + strings[i] + "\n");
        }
        return stringBuffer.toString();
    }
}
