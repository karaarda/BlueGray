package com.pinq.bluegray.data;

import android.view.Gravity;

/**
 * Created by Arda on 30.12.2016.
 */
public class Message {
    public String mText;
    public int mGravity = Gravity.CENTER_VERTICAL;

    public Message(String text) {
        super();
        mText = text;
    }
}
