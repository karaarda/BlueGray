package com.pinq.bluegray.data;

import android.support.annotation.NonNull;

import junit.framework.Assert;

import java.util.HashMap;

/**
 * Created by Arda on 28.12.2016.
 */

public class State {
    public State(){
    }

    public State(String name, String raw, @NonNull String language){
        mName = name;
        mRaw = raw;
        Assert.assertNotNull(language);

        mLanguage = language;
    }

    public HashMap<String, String> mVariables = new HashMap<>();
    public int mSelectedChoice = -1;
    public String mName;
    public String mRaw;
    public String mNextState = null;
    public String mPreviousState = null;
    public String mLanguage;
}
