package tool;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

import com.example.hp.driverfriend.R;
/*
 * 呈现图片，是否选择
 */
public class SelectImageActivity extends Activity {
    //获取数据
    private Intent intent;
    //图片
    private ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_image_activity);
        intent = getIntent();
        imageView = findViewById(R.id.show_imageView);
        byte[] mg = intent.getByteArrayExtra("imageData");
        Bitmap bitmap = BitmapFactory.decodeByteArray(mg,0,mg.length);
        imageView.setImageBitmap(bitmap);
    }
}
