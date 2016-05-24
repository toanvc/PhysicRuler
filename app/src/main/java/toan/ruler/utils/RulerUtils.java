package toan.ruler.utils;

/**
 * Created by Toan Vu on 4/1/16.
 */
public class RulerUtils {

    public static String formatNumber(double d) {
        if (d == (long) d)
            return String.format("%d", (long) d);
        else
            return String.format("%.2f", d);
    }
}