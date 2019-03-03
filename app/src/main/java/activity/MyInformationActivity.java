package activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.hp.driverfriend.R;

import data.MyInformation;
import manager.AAL;
import tool.CameraActivity;
import tool.CircleImage;
import tool.GenerallyAlgorithmTool;
import tool.ScrollSelectView;

/*
 * 呈现我的信息和修改信息的界面
 */
public class MyInformationActivity extends Activity {

    //头像初始照片
    private Bitmap bitmap;

    //头像圆形照片
    private CircleImage circleImage;

    //关闭按钮
    private Button closeButton;

    //编辑按钮
    private Button correctButton;

    //用户姓名
    private EditText nameEdit;

    //车牌号
    private EditText licensePlateNumberEdit;

    //紧急通知设置
    private TextView textView;

    //编辑模式下返回的提示框
    private AlertDialog alertDialog;

    //我的信息
    private MyInformation myInformation;

    //编辑模式是否开启
    private boolean openEditMode;

    //滚动选择
    private ScrollSelectView scrollSelectViewYear;
    private ScrollSelectView scrollSelectViewMonth;
    private ScrollSelectView scrollSelectViewDay;
    private ScrollSelectView scrollSelectViewSex;
    //滚动界面的数据
    private int[] years;
    private int[] months;
    private int[] days;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_information_acvitvity);
        this.openEditMode = false;
        setTestData();
        initWidget();
    }


    //TEST:-------一次过---------------一次过--------->:没有使用网络来定义
    //设置滚动数据
    private void setTestData(){
        this.years = new int[]{2000,2001,2002,2003,2004,2005,2006,2007,2008,2009,2010,2011,2012,2013,2014,2015,2016,2017,2018,2019};
        this.months = new int[]{1,2,3,4,5,6,7,8,9,10,11,12};
        this.days = new int[]{1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24};
    }

    //初始化空间
    private void initWidget(){
        this.closeButton = findViewById(R.id.my_information_close_button);
        this.circleImage = findViewById(R.id.my_information_head_portrait);
        this.correctButton = findViewById(R.id.my_information_correct_button);
        this.nameEdit = findViewById(R.id.my_information_name_edit);
        this.licensePlateNumberEdit = findViewById(R.id.my_information_license_plate_number_edit);
        this.scrollSelectViewYear = findViewById(R.id.scrollSelect_year);
        this.scrollSelectViewMonth = findViewById(R.id.scrollSelect_month);
        this.scrollSelectViewDay = findViewById(R.id.scrollSelect_day);
        this.scrollSelectViewSex = findViewById(R.id.scrollSelect_sex);
        //提取信息
        getMyInformationFromData();

        //Edit都不可以编辑
        setTextEditUnableEdited();




        //关闭活动
        this.closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(openEditMode){ //编辑模式下
                    //如果提示框已经存在
                    if(alertDialog !=null &&alertDialog.isShowing()){
                        alertDialog.dismiss();
                    }
                    //弹出提示框
                    AlertDialog.Builder builder = new AlertDialog.Builder(getSelfInContext());
                    builder.setPositiveButton("保存并退出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveMyInformation();
                            finish();
                        }
                    }).setNeutralButton("不保存", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).setMessage("是否需要保存")
                            .setCancelable(false);
                    alertDialog = builder.create();
                    alertDialog.show();
                }else{//非编辑模式下
                    finish();
                }
            }
        });

        //打开编辑模式
        this.correctButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((Button)v).setText("保存");
                openEditMode = true;
                setTextEditAbleEdited();
            }
        });
    }


    //根据initWidget出现的顺序排列

    //设置edit不可编辑
    private void setTextEditUnableEdited(){
        this.nameEdit.setFocusable(false);
        this.nameEdit.setFocusableInTouchMode(false);
        this.licensePlateNumberEdit.setFocusable(false);
        this.licensePlateNumberEdit.setFocusableInTouchMode(false);
        setMyInformation();
    }

    //TODO(1): 先自行初始化数据，头像先不设定
    //从数据库提取信息
    private void getMyInformationFromData(){
        this.myInformation = new MyInformation();
        myInformation.licencePlateNumber = "桂-88888888";
        myInformation.name = "何兴邦";
        myInformation.sex = true;
        myInformation.year = 2000;
        myInformation.month = 4;
        myInformation.day = 22;
    }

    //设置信息,控件未”编辑“之前的数据的初始化
    private void setMyInformation(){
        this.nameEdit.setText(myInformation.name);
        this.scrollSelectViewYear.setShowOnlyOneString(String.valueOf(myInformation.year));
        this.scrollSelectViewMonth.setShowOnlyOneString(String.valueOf(myInformation.month));
        this.scrollSelectViewDay.setShowOnlyOneString(String.valueOf(myInformation.day));
        if(myInformation.sex){
            this.scrollSelectViewSex.setShowOnlyOneString(String.valueOf("男"));
        }else {
            this.scrollSelectViewSex.setShowOnlyOneString("女");
        }
       this.licensePlateNumberEdit.setText(myInformation.licencePlateNumber);
    }


    //设置edit可以编辑
    private  void setTextEditAbleEdited(){
        this.nameEdit.setFocusable(true);
        this.nameEdit.setFocusableInTouchMode(true);
        this.scrollSelectViewYear.setAbleTouched();
        this.scrollSelectViewMonth.setAbleTouched();
        this.scrollSelectViewDay.setAbleTouched();
        this.scrollSelectViewSex.setAbleTouched();
        setScrollData();
        try{
            this.scrollSelectViewYear.starPick(GenerallyAlgorithmTool.findPositionBinarySearch(this.years,this.myInformation.year));
            int a = GenerallyAlgorithmTool.findPositionBinarySearch(this.months,this.myInformation.month);
            this.scrollSelectViewMonth.starPick(GenerallyAlgorithmTool.findPositionBinarySearch(this.months,this.myInformation.month));
            this.scrollSelectViewDay.starPick(GenerallyAlgorithmTool.findPositionBinarySearch(this.days,this.myInformation.day));
            this.scrollSelectViewSex.starPick(myInformation.sex ? 0 : 1);
        }catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }catch (ScrollSelectView.NullDataException e){
            e.showDetail();
        }
        this.licensePlateNumberEdit.setFocusable(true);
        this.licensePlateNumberEdit.setFocusableInTouchMode(true);

    }

    //TODO(2): 需要根据时间设定出生年月日选择上限(现在没有这个要求，而且不需要更新数据)
    //使用TimeManger给出的信息来设定滚动选择界面的界面,每一次组件的刷新都需要重新设定信息
    private void setScrollData(){
        String stringYear[] = new String[this.years.length];
        for(int i =0;i<this.years.length;i++){
            stringYear[i] = String.valueOf(this.years[i]);
        }
        String stringMonth[] = new String[this.months.length];
        for(int i =0;i<this.months.length;i++){
            stringMonth[i] = String.valueOf(this.months[i]);
        }
        String stringDay[] = new String[this.days.length];
        for(int i =0;i<this.days.length;i++){
            stringDay[i] = String.valueOf(this.days[i]);
        }
        this.scrollSelectViewYear.setData((stringYear),4);
        this.scrollSelectViewDay.setData(stringDay,2);
        this.scrollSelectViewMonth.setData(stringMonth,2);
        this.scrollSelectViewSex.setData(new String[]{"男","女"},2);
    }

    //保存[myInformation]
    private void saveMyInformation(){

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == AAL.CameraActivity){
            if(resultCode == CameraActivity.getImage){
                byte[] mg = data.getByteArrayExtra(CameraActivity.imageName);
                this.bitmap = BitmapFactory.decodeByteArray(mg,0,mg.length);
                circleImage.setHeadPortraitByRawBitmap(this.bitmap);
            }else{
                Log.d("CameraActivity","noImage");
            }
        }
    }

    private Context getSelfInContext(){
        return this;
    }
}
