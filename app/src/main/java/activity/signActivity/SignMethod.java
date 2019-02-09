package activity.signActivity;

/*
 * 处理字符串信息
 */
public class SignMethod implements StringMethodInterface {

    //车牌前两位
    private String[] licenseContainString;

    protected SignMethod(){
        initArgument();
    }

    //Description: 初始化实例变量
    private void initArgument(){
        this.licenseContainString  = new String[]{
                ""
        };
    }

    @Override
    public boolean checkStringIsContainNon_numericChar(String string) {
        boolean flag = false;
        char a;
        for (int i = 0; i < string.length(); i++) {
            a = string.charAt(i);
            switch (a){
                case '1':break;
                case '2':break;
                case '3':break;
                case '4':break;
                case '5':break;
                case '6':break;
                case '7':break;
                case '8':break;
                case '9':break;
                case '0':break;
                default: flag = true;
            }
            if(flag){
                break;
            }
        }
        return flag;
    }

    /*
    *  姓名的格式规范
    *  1: 不能含有
    */
    @Override
    public boolean accordWithRuleName(String string) {
        return true;
    }

    //Description: 不符合车牌号的规则，返回false
    @Override
    public boolean accordWithRuleLicense(String string) {
        return false;
    }
}
