package toan.ruler.utils;

import android.annotation.SuppressLint;

/**
 * Created by Toan Vu on 4/1/16.
 */
public class RulerUtils {

    @SuppressLint("DefaultLocale") public static String formatNumber(float d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.format("%.2f", d);
    }
}