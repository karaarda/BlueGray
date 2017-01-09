package com.pinq.bluegray.data;

/**
 * Created by Arda on 7.01.2017.
 */

public class DelayState extends State{
    public long mDueDate = 0;
    public String mDelayedNextState;

    public DelayState(){

    }

    public DelayState(String name, String raw, String language) {
        super(name, raw, language);
    }
}
