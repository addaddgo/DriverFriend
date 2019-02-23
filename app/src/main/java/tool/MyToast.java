package tool;

import android.content.Context;
import android.widget.Toast;



public class MyToast {
    public static void toastShowShort(Context context,String string){
        Toast.makeText(context,string,Toast.LENGTH_SHORT).show();
    }
    public static void toastShowLong(Context context,String string){
        Toast.makeText(context,string,Toast.LENGTH_LONG).show();
    }
}
