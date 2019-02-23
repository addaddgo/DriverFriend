package tool;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.example.hp.driverfriend.R;
/*
 * 呈现图片，是否选择
 */
public class SelectImageActivity extends Activity {

    //是否成功标识码
    public static int successful = 1;
    public static int unsuccessful =2;
    //获取数据
    private Intent intent;
    //图片控件
    private ImageView imageView;
    //图片
    private Bitmap bitmap;
    //按钮
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_image_activity);
        imageView = findViewById(R.id.show_imageView);
        button = findViewById(R.id.ensure_button);
        showImage();
        initButton();
    }
    //显示图片
    private void showImage(){
        this.intent = getIntent();
        byte[] mg = this.intent.getByteArrayExtra(CameraActivity.imageName);
        this.bitmap = BitmapFactory.decodeByteArray(mg,0,mg.length);
        this.imageView.setImageBitmap(this.bitmap);
    }
    //返回 successful;
    private void  initButton(){
        this.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(successful);
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(this.unsuccessful);
    }
}
