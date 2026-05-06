package cn.nodesoft.utils;

import java.math.BigDecimal;

public class DecimalZeroChargeUtil {

    public static String getPrettyNumber(String dec) {
        BigDecimal decimal = new BigDecimal(BigDecimal.valueOf(Double.parseDouble(dec)).stripTrailingZeros().toPlainString());
        return String.valueOf(decimal);
    }
}
