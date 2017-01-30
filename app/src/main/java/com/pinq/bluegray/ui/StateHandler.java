package com.pinq.bluegray.ui;

import android.content.Context;
import android.util.Log;

import com.pinq.bluegray.GameAdapter;
import com.pinq.bluegray.data.Answer;
import com.pinq.bluegray.listener.ViewParseListener;
import com.pinq.bluegray.data.Message;
import com.pinq.bluegray.listener.ParseListener;
import com.pinq.bluegray.data.State;

import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * Created by Arda on 28.12.2016.
 */

public class StateHandler {
    private Context mContext;
    State mState;
    GameAdapter mAdapter;

    public StateHandler(Context ctx, State state, GameAdapter adapter){
        Log.e("name", state.mName);
        Log.e("raw", state.mRaw);

        mContext = ctx;
        mState = state;
        mAdapter = adapter;
    }

    public void populate(boolean animate){

        Log.e("name", mState.mName);
        Log.e("raw", mState.mRaw);

        if(!animate){
            String[] lines = mState.mRaw.split(System.getProperty("line.separator"));
            parse(lines);
        } else {
            final String[] lines = mState.mRaw.split(System.getProperty("line.separator"));
            Thread parser = new Thread(new Runnable() {
                @Override
                public void run() {
                    parse(lines, true);
                }
            });
            //parser.start();
            parse(lines, true);
        }
    }

    private void parse(String[] lines){
        parse(lines, false);
    }

    private void parse(String[] lines, boolean animate){
        ViewParseListener listener = new ViewParseListener(mState, mAdapter, mContext);
        listener.setAnimate(animate);
        parse(lines, listener);
    }

    public void parse(String[] lines, ParseListener listener){
        Log.e("parse", Arrays.toString(lines));

        for(int i = 0; i < lines.length; ++i) switch(lines[i].substring(0, 2)){
            case "$$":
                if(mState.mNextState != null)
                    break;
                String[] values = lines[i].substring(2, lines[i].length()).split((Pattern.quote("=")));

                listener.onVariable(values);
                break;
            case "<<":
                String text = lines[i].substring(2,lines[i].length());

                listener.onMessage(new Message(text));
                break;
            case "??":
                String[] option = lines[i].substring(2,lines[i].length()).split((Pattern.quote("||")));
                Answer answer = new Answer();

                answer.state = mState;

                answer.option1 = option[0];
                answer.next1 = option[1];

                Log.e("option", option[0] + " leads to " + option[1]);

                option = lines[++i].substring(2,lines[i].length()).split((Pattern.quote("||")));

                answer.option2 = option[0];
                answer.next2 = option[1];

                listener.onAnswer(answer);
                break;
            case "--":
                if(lines[i].substring(2).trim().isEmpty())
                    continue;

                String[] values1 = lines[i].substring(2).split((Pattern.quote("=")));

                Log.e("if", values1[0] + " is " + values1[1]);

                Log.e("Variables", mState.mVariables.toString());

                if(mState.mVariables.get(values1[0]).equals(values1[1])){
                    int k = i;

                    int lineCount = 0;

                    Log.e("line", lines[k]);
                    while(!lines[++k].substring(0,2).equals("--")) {
                        ++lineCount;
                        Log.e("line", lines[k]);
                    }

                    String[] innerLines = new String[lineCount];

                    k = i + 1;

                    while(!lines[++i].substring(0,2).equals("--"))
                        innerLines[i-k] = lines[i];

                    parse(innerLines, listener);
                } else {
                    while(!lines[++i].substring(0,2).equals("--"));
                }

                break;
            case "++":
                if(mState.mNextState != null)
                    break;

                String[] delayData = lines[i].substring(2).split(Pattern.quote("||"));

                listener.onDelay(delayData);
                break;
            case "||":
                if(mState.mNextState != null)
                    break;

                listener.onGoTo(lines[i].substring(2));
                break;
        }

        listener.finish();
    }
}
