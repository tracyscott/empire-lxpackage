package org.projectempire.lx.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class EmpireUtils {
    /**
     * Returns a floating-point rounded value of the sin function to 8 decimal places.
     * This is often useful because Math.sin(Math.PI) is NOT actually 0.
     *
     * @param radians Radians to take sin of
     * @return Result rounded to 8 decimal places
     */
    public static float sinf(float radians) {
        return new BigDecimal(String.valueOf(Math.sin(radians))).setScale(8, RoundingMode.HALF_UP).floatValue();
    }

    /**
     * Returns a floating-point rounded value of the cos function to 8 decimal places.
     * This is often useful because Math.sin(Math.PI) is NOT actually 0.
     *
     * @param radians Radians to take cos of
     * @return Result rounded to 8 decimal places
     */
    public static float cosf(double radians) {
        return new BigDecimal(String.valueOf(Math.cos(radians))).setScale(8, RoundingMode.HALF_UP).floatValue();
    }
}
