package cn.nodesoft.utils;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

/**
 * 由于Java的简单类型不能够精确的对浮点数进行运算，这个工具类提供精
 * 确的浮点数运算，包括加减乘除和四舍五入。
 */
public class DoubleUtil {

    //默认除法运算精度
    private static final int DEF_DIV_SCALE = 10;
    //这个类不能实例化
    private DoubleUtil(){
    }
 
    /**
     * 提供精确的加法运算。
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static double add(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2).doubleValue();
    }
    /**
     * 提供精确的加法运算。
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static BigDecimal addb(BigDecimal v1,BigDecimal v2){
        return v1.add(v2);
    }
    /**
     * 提供精确的加法运算。(传入String类型)
     * @param v1 被加数
     * @param v2 加数
     * @return 两个参数的和
     */
    public static String addString(String v1,String v2){
        if(StringUtils.isBlank(v1)||"null".equals(v1)){
            v1="0";
        }
        if(StringUtils.isBlank(v2)||"null".equals(v2)){
            v2="0";
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.add(b2).stripTrailingZeros().toString();
    }
    /**
     * 提供精确的减法运算。 传入String类型)
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    public static String subString(String v1,String v2){
        if(StringUtils.isBlank(v1)||"null".equals(v1)){
            v1="0";
        }
        if(StringUtils.isBlank(v2)||"null".equals(v2)){
            v2="0";
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.subtract(b2).toString();
    }

    /**
     * 提供精确的减法运算。
     * @param v1 被减数
     * @param v2 减数
     * @return 两个参数的差
     */
    public static double sub(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2).doubleValue();
    }
    /**
     * 提供精确的乘法运算。
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static double mul(double v1,double v2){
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2).doubleValue();
    }
    /**
     * 提供精确的乘法运算。(传入String类型)
     * @param v1 被乘数
     * @param v2 乘数
     * @return 两个参数的积
     */
    public static BigDecimal mulString(String v1,String v2){
        if(StringUtils.isBlank(v1)||"null".equals(v1)){
            v1="0";
        }
        if(StringUtils.isBlank(v2)||"null".equals(v2)){
            v2="0";
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        return b1.multiply(b2);
    }
 
    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
     * 小数点以后10位，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static double div(double v1,double v2){
        return div(v1,v2,DEF_DIV_SCALE);
    }
    /**
     * 提供（相对）精确的除法运算，当发生除不尽的情况时，精确到
     * 小数点以后10位，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @return 两个参数的商
     */
    public static String divString(String v1,String v2){
        if(StringUtils.isBlank(v1) ||StringUtils.isBlank(v2) ||"null".equals(v1) ||"null".equals(v2)){
            return "0";
        }
        BigDecimal b1 = new BigDecimal(v1);
        BigDecimal b2 = new BigDecimal(v2);
        if(b1.compareTo(BigDecimal.ZERO)==0 ||b2.compareTo(BigDecimal.ZERO)==0){
            return "0";
        }

        return b1.divide(b2,DEF_DIV_SCALE,BigDecimal.ROUND_HALF_UP).stripTrailingZeros().toString();
    }
 
    /**
     * 提供（相对）精确的除法运算。当发生除不尽的情况时，由scale参数指
     * 定精度，以后的数字四舍五入。
     * @param v1 被除数
     * @param v2 除数
     * @param scale 表示表示需要精确到小数点以后几位。
     * @return 两个参数的商
     */
    public static double div(double v1,double v2,int scale){
        if(scale<0){
            throw new IllegalArgumentException(
                "The scale must be a positive integer or zero");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2,scale,BigDecimal.ROUND_HALF_UP).doubleValue();
    }
 
    /**
     * 提供精确的小数位四舍五入处理。
     * @param v 需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(double v,int scale){
        if(scale<0){
            throw new IllegalArgumentException(
                "The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(Double.toString(v));
        BigDecimal one = new BigDecimal("1");
        return b.divide(one,scale,BigDecimal.ROUND_HALF_UP).doubleValue();
    }
    
    /**
     * 提供两个double型数字比较方法<br>
     * 		返回值1： v1 > v2<br>
     *  	返回值0：v1 = v2<br>
     *  	返回值-1：v1 < v2<br>
     * @param v1 
     * @param v2
     * @return 比较结果
     */
    public static int compareTo(double v1,double v2){
    	BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.compareTo(b2);
    }
    
    /**
     * 将String类型转为Double
     * @param v
     * @return
     */
    public static Double parseDouble(String v){
    	return new BigDecimal(v).doubleValue();
    }
    
    /**
     * 将Double类型转为String，四舍五入保留两位小数
     * @param v
     * @return
     */
    public static String toString(Double v){
    	return toString(v, 2);
    }
    
    /**
     * 将Double类型转为String
     * @param v
     * @param scale 小数点后保留几位
     * @return
     */
    public static String toString(Double v, int scale){
    	return String.format(new StringBuilder("%.").append(scale).append("f").toString() , v);
    }
    
    /**
     * 取得相反数
     * @param v
     * @return
     */
    public static Double negate(Double v){
    	return new BigDecimal(v).negate().doubleValue();
    }
    
    /**
     * 与0的比较<br>
     * 		返回值1： v > 0<br>
     *  	返回值0：v = 0<br>
     *  	返回值-1：v < 0<br>
     * @return 比较结果
     */
    public static int compareToZero(double v){
    	return compareTo(v, 0.0);
    }
    
    /**
     * 计算税额
     * @param money 金额
     * @param taxRate 税率（如果大于1会自动除以100）
     * @return
     */
    public static Double getRax(Double money, Double taxRate){
    	if(compareTo(taxRate, 1) == 1){
    		taxRate = div(taxRate, 100);
    	}
    	return mul(div(money, 1 + taxRate), taxRate);
    }

    public static void main(String[] args) {

//        String v1="290215.51";
//        String v2="0.3";
//        BigDecimal b1 = new BigDecimal(v1);
//        BigDecimal b2 = new BigDecimal(v2);
//        System.out.println(mulString(v2,v1).toString());
//        System.out.println(b1.compareTo(BigDecimal.ZERO)<=0);

//        String v3="376";
//        System.out.println(v2+v3);
//        System.out.println(divString(divString(v1,v3.toString()),v2.toString()));
        BigDecimal b1 = new BigDecimal("7.00");
        BigDecimal b2 = new BigDecimal("134.10");
        String v1="153.506244";
        System.out.println(divString(divString(v1,b1.toString()),b2.toString()));
    }
    
}
