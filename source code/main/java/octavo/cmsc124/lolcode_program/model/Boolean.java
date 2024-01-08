package octavo.cmsc124.lolcode_program.model;

import java.util.ArrayList;

public class Boolean {
    public static boolean bothOF(boolean a, boolean b){
        return a & b;
    }

    public static boolean eitherOF(boolean a, boolean b){
        return a | b;
    }

    public static boolean wonOF(boolean a, boolean b){
        return a ^ b;
    }

    public static boolean not(boolean x){
        return !x;
    }

    public static boolean allOf(ArrayList<java.lang.Boolean> troofList){
        boolean isValid = true;

        for(boolean item : troofList){
            if(!item){
                isValid = false;
                break;
            }
        }
        return isValid;
    }

    public static boolean anyOf(ArrayList<java.lang.Boolean> troofList){
        boolean isValid = false;

        for(boolean item : troofList){
            if(item){
                isValid = true;
                break;
            }
        }
        return isValid;
    }
}
