package com.pinq.bluegray.listener;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.pinq.bluegray.GameAdapter;
import com.pinq.bluegray.NotificationService;
import com.pinq.bluegray.R;
import com.pinq.bluegray.data.Answer;
import com.pinq.bluegray.data.DelayState;
import com.pinq.bluegray.data.ItemProvider;
import com.pinq.bluegray.data.Message;
import com.pinq.bluegray.data.PreferenceHandler;
import com.pinq.bluegray.data.State;

import java.util.HashMap;

/**
 * Created by Arda on 8.01.2017.
 */

public class ViewParseListener implements ParseListener{
    GameAdapter mAdapter;
    State mState;
    Context mContext;

    boolean mAnimate = true;

    public ViewParseListener(State pState, GameAdapter pAdapter, Context pContext){
        mState = pState;
        mAdapter = pAdapter;
        mContext = pContext;
    }

    public void setAnimate(boolean animate){
        mAnimate = animate;
    }

    @Override
    public void onMessage(Message message) {
        mAdapter.addMessage(message, mAnimate);
    }

    @Override
    public void onAnswer(Answer answer) {
        mAdapter.addAnswer(answer, mAnimate);
    }

    @Override
    public void onDelay(String[] delayData) {
        DelayState delayState = new DelayState();
        delayState.mName = "delay" + "_" + mState.mName;
        delayState.mLanguage = PreferenceHandler.getLanguage(mContext);

        delayData[0] = delayData[0].substring(0, delayData[0].length()-1);

        long delay = Integer.parseInt(delayData[0]) * 1000 * 60;

        delayState.mRaw = "::" + delayState.mName + "\n<<" + mContext.getString(R.string.busy);
        delayState.mDueDate = System.currentTimeMillis() + delay;
        delayState.mDelayedNextState = delayData[1];
        delayState.mVariables = (HashMap<String, String>) mState.mVariables.clone();

        Intent intent = new Intent(mContext, NotificationService.class);
        PendingIntent pintent = PendingIntent.getService(mContext, 0, intent, 0);

        AlarmManager alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarm.setExact(AlarmManager.RTC, delayState.mDueDate, pintent); //TODO change second argument for debugging
        } else {
            alarm.set(AlarmManager.RTC_WAKEUP, delayState.mDueDate, pintent);
        }

        mState.mNextState = delayState.mName;

        PreferenceHandler.saveState(mContext, delayState);

        ItemProvider.getInstance(mContext).goToNextOf(mAdapter, mState, false);
    }

    @Override
    public void onGoTo(String stateName) {
        mState.mNextState = stateName;
        ItemProvider.getInstance(mContext).goToNextOf(mAdapter, mState);
    }

    @Override
    public void onVariable(String[] variableData) {
        mState.mVariables.put(variableData[0], variableData[1]);

        Log.e("set", variableData[0] + " to " + variableData[1]);
    }

    @Override
    public void onStatement(String[]statementData) {

    }

    @Override
    public void finish() {
        PreferenceHandler.saveState(mContext, mState);
    }
}
