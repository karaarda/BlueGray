package com.pinq.bluegray;

/**
 * Created by Arda on 7.01.2017.
 */
public class BlueGray {
    static boolean foreground = false;

    static boolean isForeground(){
        return foreground;
    }

    static void setForeground(boolean value){
        foreground = value;
    }
}
