package com.pinq.bluegray;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.pinq.bluegray.data.DelayState;
import com.pinq.bluegray.data.GoNextOfEvent;
import com.pinq.bluegray.data.ItemProvider;
import com.pinq.bluegray.data.PreferenceHandler;
import com.pinq.bluegray.data.State;
import com.pinq.bluegray.listener.NotificationParseListener;
import com.pinq.bluegray.listener.ParseListener;
import com.pinq.bluegray.ui.StateHandler;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by Arda on 7.01.2017.
 */
public class NotificationService extends Service {
    public NotificationService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        State state = PreferenceHandler.getLastState(this);

        if(!(state instanceof DelayState)) {
            Log.e("nof", state.mName);
            return Service.START_NOT_STICKY;
        }

        state.mNextState = ((DelayState)state).mDelayedNextState;

        PreferenceHandler.saveState(this, state);

        if( !BlueGray.isForeground() ){
            PreferenceHandler.setLastState(this, state.mNextState);

            State nextState = ItemProvider.getInstance(this).loadStateData(this, state.mNextState);
            StateHandler handler = new StateHandler(this, nextState, null);

            ParseListener listener = new NotificationParseListener(this, nextState);

            handler.parse(nextState.mRaw.split(System.getProperty("line.separator")), listener);
        } else {
            EventBus.getDefault().post(new GoNextOfEvent((DelayState) state));
        }

        return Service.START_NOT_STICKY;
    }

    public boolean isAwake(){
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            return pm.isInteractive();
        } else {
            return pm.isScreenOn();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
