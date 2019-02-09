package activity.signActivity;
/*
 * 注册账户活动窗口
 * 用户注册：名字，电话，车牌号，身份证号,身份证照片
 * 注意：
 *  1：电话号码，身份证号，车牌号都需要检测格式
 *  2: 上传身份证照片需要照片，需要有【访问内存】,【打开相机】,【访问网络】，三个限权
 *  通讯码：2
 */
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.hp.driverfriend.R;

public class SignActivity extends Activity {

    public int forSignActivity = 2;
    /*
     * Target 处理信息用户填写的信息
     * Function 使用接口中的函数来处理相应的信息
     */
    private StringMethodInterface methodSignInterface;

     // Target 获得用户名字
    private EditText nameEditText;

    //获得车牌号
    private EditText licenseNumberEditText;

    //获得身份证号码
    private EditText identificationNumberEditText;

    //显示身份证
    private ImageView identificationImage;

    //提交所有的信息
    private Button submitButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_sign);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /*
     * Description 初始化组件
     */
    private void init(){
        this.identificationNumberEditText = findViewById(R.id.sign_identification_number);
        this.licenseNumberEditText = findViewById(R.id.sign_license_plate_number);
        this.nameEditText = findViewById(R.id.sign_name);
        this.identificationImage = findViewById(R.id.identification_imge_view);
        this.submitButton = findViewById(R.id.submit_button);
        this.methodSignInterface = new SignMethod();
    }

    /*
     * Description 给组件设置监听者
     */
    private void addAllListeners(){

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}
