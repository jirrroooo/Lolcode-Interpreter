package octavo.cmsc124.lolcode_program.model;

public class Arithmetic {
    public static float sumOf(float a, float b){
        return a+b;
    }

    public static float diffOf(float a, float b){
        return a-b;
    }

    public static float produktOf(float a, float b){
        return a*b;
    }

    public static float quoshuntOf(float a, float b){
        return a/b;
    }

    public static float modOf(float a, float b){
        return a%b;
    }

    public static float biggrOf(float a, float b){
        return Math.max(a, b);
    }

    public static float smallrOf(float a, float b){
        return Math.min(a, b);
    }
}
