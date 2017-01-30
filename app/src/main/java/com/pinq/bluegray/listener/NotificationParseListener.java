package com.pinq.bluegray.listener;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.pinq.bluegray.MainActivity;
import com.pinq.bluegray.NotificationService;
import com.pinq.bluegray.R;
import com.pinq.bluegray.data.Answer;
import com.pinq.bluegray.data.DelayState;
import com.pinq.bluegray.data.ItemProvider;
import com.pinq.bluegray.data.Message;
import com.pinq.bluegray.data.PreferenceHandler;
import com.pinq.bluegray.data.State;
import com.pinq.bluegray.ui.StateHandler;

import java.util.Date;
import java.util.HashMap;

/**
 * Created by Arda on 8.01.2017.
 */

public class NotificationParseListener implements ParseListener{
    Context mContext;
    State mState;
    String mNotification = "";

    boolean finished = false;

    Answer mAnswer;

    public NotificationParseListener(Context pContext, State pState){
        mContext = pContext;
        mState = pState;
    }

    @Override
    public void onMessage(Message message) {
        mNotification += message.mText + "\n";
    }

    @Override
    public void onAnswer(Answer answer) {
        mAnswer = answer;
    }

    @Override
    public void onDelay(String[] delayData) {
        DelayState delayState = new DelayState();
        delayState.mName = "delay" + "_" + mState.mName;
        delayState.mLanguage = PreferenceHandler.getLanguage(mContext);

        delayData[0] = delayData[0].substring(0, delayData[0].length()-1);

        long delay = Integer.parseInt(delayData[0]) * 1000; //TODO * 60 to make it minutes

        delayState.mRaw = "::" + delayState.mName + "\n<<" + mContext.getString(R.string.busy);
        delayState.mDueDate = System.currentTimeMillis() + delay;
        delayState.mDelayedNextState = delayData[1];
        delayState.mVariables = (HashMap<String, String>) mState.mVariables.clone();

        Intent intent = new Intent(mContext, NotificationService.class);
        PendingIntent pintent = PendingIntent.getService(mContext, 0, intent, 0);

        AlarmManager alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            alarm.setExact(AlarmManager.RTC, System.currentTimeMillis() + 1000 * 10, pintent);
            alarm.setExact(AlarmManager.RTC, delayState.mDueDate, pintent); //TODO change second argument for debugging
        } else {
//            alarm.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000 * 10, pintent);
            alarm.set(AlarmManager.RTC_WAKEUP, delayState.mDueDate, pintent);
        }

        mState.mNextState = delayState.mName;

        PreferenceHandler.saveState(mContext, delayState);
        PreferenceHandler.saveState(mContext, mState);

        StateHandler handler = new StateHandler(mContext, delayState, null);
        handler.parse(delayState.mRaw.split(System.getProperty("line.separator")), this);

        PreferenceHandler.setLastState(mContext, delayState.mName);
    }

    @Override
    public void onGoTo(String stateName) {
        mState.mNextState = stateName;

        //TODO PARSE NEXT STATE
        State nextState = ItemProvider.getInstance(mContext).loadStateData(mContext, stateName, false);

        nextState.mPreviousState = mState.mName;
        nextState.mVariables = (HashMap<String, String>) mState.mVariables.clone();

        StateHandler handler = new StateHandler(mContext, nextState, null);
        handler.parse(nextState.mRaw.split(System.getProperty("line.separator")), this);

        if(nextState.mNextState == null)
            PreferenceHandler.setLastState(mContext, nextState.mName);

        PreferenceHandler.saveState(mContext, mState);
        PreferenceHandler.saveState(mContext, nextState);
    }

    @Override
    public void onVariable(String[] variableData) {
        mState.mVariables.put(variableData[0], variableData[1]);

        Log.e("set", variableData[0] + " to " + variableData[1]);
    }

    @Override
    public void onStatement(String[] statementData) {

    }

    @Override
    public void finish() {
        PreferenceHandler.saveState(mContext, mState);

        if(finished)
            return;

        finished = true;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.drawable.ic_nof);
        mBuilder.setContentTitle(mContext.getString(R.string.app_name));
        mBuilder.setContentText(mNotification);
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(mNotification));
        mBuilder.setGroup("blue-gray");
        mBuilder.setAutoCancel(true);

        mBuilder.setColor(mContext.getResources().getColor(R.color.bluegray));

        Intent resultIntent = new Intent(mContext, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(MainActivity.class);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        int id = (int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

        mNotificationManager.notify(id, mBuilder.build());
    }
}
