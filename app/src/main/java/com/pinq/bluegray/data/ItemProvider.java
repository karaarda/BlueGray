package com.pinq.bluegray.data;

import android.content.Context;
import android.util.Log;

import com.pinq.bluegray.GameAdapter;
import com.pinq.bluegray.R;
import com.pinq.bluegray.ui.StateHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

/**
 * Created by Arda on 28.12.2016.
 */

public class ItemProvider {
    private static ItemProvider mInstance;

    private Context mContext;

    private ItemProvider(Context ctx){
        mContext = ctx;
    }

    public static ItemProvider getInstance(Context ctx){
        if(mInstance == null)
            mInstance = new ItemProvider(ctx);

        return mInstance;
    }

    public void start(GameAdapter game){
        State startState = loadStateData(mContext, "start");
        if(startState.mNextState == null) {
            new StateHandler(mContext, startState, game).populate(true);

            PreferenceHandler.setLastState(mContext, startState.mName);
            PreferenceHandler.saveState(mContext, startState);
        } else
            populateWithNext(game, startState);
    }
   /*
    public void start(GameAdapter game){
        State lastState = PreferenceHandler.getLastState(mContext);
        if(lastState == null) {
            lastState = loadStateData(mContext, "start");
            PreferenceHandler.setLastState(mContext, lastState.mName);
        }
        populateWithPrevious(game, lastState);
    } */

    public void goToNextOf(GameAdapter game, State state){
        goToNextOf(game, state, true);
    }

    public void goToNextOf(GameAdapter game, State state, boolean override){
        State nextState = loadStateData(mContext, state.mNextState, override);

        nextState.mPreviousState = state.mName;
        nextState.mVariables = (HashMap<String, String>) state.mVariables.clone();
        new StateHandler(mContext, nextState, game).populate(true); //TODO change to true before release

        if(nextState.mNextState == null)
            PreferenceHandler.setLastState(mContext, nextState.mName);

        PreferenceHandler.saveState(mContext, state);
        PreferenceHandler.saveState(mContext, nextState);
    }

    private void populateWithPrevious(GameAdapter game, State state){
        new StateHandler(mContext, state, game).populate(false);

        if(state.mPreviousState != null)
            populateWithPrevious(game, loadStateData(mContext, state.mPreviousState));
        else
            game.scrollToEnd();
    }

    private void populateWithNext(GameAdapter game, State state){
        boolean hasNext = state.mNextState != null;

        new StateHandler(mContext, state, game).populate(false);

        if(hasNext)
            populateWithNext(game, loadStateData(mContext, state.mNextState));
        else
            game.scrollToEnd();
    }

    public State loadStateData(Context ctx, String stateName){
        return loadStateData(ctx, stateName, false);
    }

    public State loadStateData(Context ctx, String stateName, boolean override){
        State savedState = PreferenceHandler.getState(ctx, stateName);

        if(savedState != null && !override && savedState.mLanguage.equals(PreferenceHandler.getLanguage(mContext)))
            return savedState;

        boolean update = false;

        if(savedState != null && !savedState.mLanguage.equals(PreferenceHandler.getLanguage(mContext)))
            update = true;

        String raw = getRaw(ctx, stateName);

        if(!update){
        State result = new State(stateName, raw, PreferenceHandler.getLanguage(mContext));

        PreferenceHandler.saveState(mContext, result);

        return result;
        } else {
            savedState.mLanguage = PreferenceHandler.getLanguage(mContext);
            savedState.mRaw = raw;
            PreferenceHandler.saveState(mContext, savedState);

            return savedState;
        }
    }

    private String getRaw(Context ctx, String stateName){
        StringBuilder text = new StringBuilder();

        boolean read = false;

        try {
            int language;

            switch (PreferenceHandler.getLanguage(mContext)){
                default:
                case "en":
                    language = R.raw.storydata_en;
                    break;
                case "de":
                    language = R.raw.storydata_de;
                    break;
                case "tr":
                    language = R.raw.storydata_tr;
                    break;
                case "az":
                    language = R.raw.storydata_az;
                    break;
            }

            InputStream inputStream =  ctx.getResources().openRawResource(language);

            InputStreamReader inputreader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputreader);
            String line;

            while ((line = br.readLine()) != null) {
                if(line.trim().equals("::" + stateName)){
                    read = true;
                }

                if(read){
                    if(line.trim().isEmpty())
                        break;
                    text.append(line);
                    text.append('\n');
                }
            }
            br.close();
        }
        catch (IOException e) {
            //TODO You'll need to add proper error handling here
            e.printStackTrace();
        }

        Log.e("raw", text.toString());

        String raw = text.toString();

        return raw;
    }
}