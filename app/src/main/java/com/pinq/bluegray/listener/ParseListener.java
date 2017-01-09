package com.pinq.bluegray.listener;

import com.pinq.bluegray.data.Answer;
import com.pinq.bluegray.data.Message;

/**
 * Created by Arda on 8.01.2017.
 */

public interface ParseListener {
    void onMessage(Message message);
    void onAnswer(Answer answer);
    void onDelay(String[] delayData);
    void onGoTo(String stateName);
    void onVariable(String[] variableData);
    void onStatement(String[] statementData);
    void finish();
}
