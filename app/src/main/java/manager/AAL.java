package manager;

import java.security.PublicKey;

/*
 * 通讯录：用数字[通讯码]来标注每一个活动
 * 每一个活动都拥有一个整型的实例变量，这个整型变量代表此活动。
 * 整型变量的命名规则：from[活动名]。
 * 这些整型变量的值，在此类当中统一管理。
 */
public class AAL {
    public static int MainActivity = 1;//主活动
    public static int SignActivity = 2;//注册活动
    public static int CameraActivity = 3;//相机活动
    public static int SelectImageActivity = 4;//选择照片的活动
    public static int SETTING = 5;//设置活动
    public static int ALBUM = 6;//相册
}
