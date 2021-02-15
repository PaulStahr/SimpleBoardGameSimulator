package util;

public class Calculate {
    public static int clip(int in, int low, int high) {
        return in < low ? low : in > high ? high : in;
    }
}
