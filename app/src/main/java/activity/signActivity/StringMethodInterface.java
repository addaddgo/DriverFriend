package activity.signActivity;

/*
 * 处理字符串
 */

public interface StringMethodInterface {

    /*
     * @Parameter string[String]
     * @Return  [boolean]
     * @Exception
     * Description 含有非数字字符就返回false
     */
    boolean checkStringIsContainNon_numericChar(String string);

    /*
     * @Return [boolean] 如果字符串不符合名字规范就返回false
     */

    boolean accordWithRuleName(String string);

    /*
     * @Return  [boolean] 如果字符不符合车牌规范则返回false
     */

    boolean accordWithRuleLicense(String string);
}
