package com.pinq.bluegray;

import android.app.Activity;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.pinq.bluegray.data.Answer;
import com.pinq.bluegray.data.GoNextOfEvent;
import com.pinq.bluegray.data.ItemProvider;
import com.pinq.bluegray.data.Message;
import com.pinq.bluegray.data.State;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

/**
 * Created by Arda on 30.12.2016.
 */
public class GameAdapter extends RecyclerView.Adapter {
    final static int sMessage = 0;
    final static int sAnswer = 1;

    Activity mActivity;

    RecyclerView mRecyclerView;

    ArrayList<Integer> mTypes;
    LinkedHashMap<Integer, Message> mMessages;
    LinkedHashMap<Integer, Answer> mAnswers;

    ArrayList<Object> mAddingList;

    int mLastAnimatedPosition = 0;
    boolean mAnimating = false;

    View mAnimatedView;

    float mAlpha = 0.0f;
    int mLastAdded = -1;

    View mFooter;

    Thread mAnimator = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true)
                if (mAnimating) try {
                    mAnimator.sleep(100);
                    mAlpha += 0.10f;

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(mAnimatedView != null) {
                            mAnimatedView.setAlpha(mAlpha);
                            }
                        }
                    });

                    float alpha;

                    if(mAddingList.get(mLastAdded == -1 ? 1 : mLastAdded) instanceof Message)
                        alpha = 1.2f + ((Message)mAddingList.get(mLastAdded == -1 ? 1 : mLastAdded)).mText.length() / 40f;
                    else
                        alpha = 1;

                    Log.e("alpha", alpha + "");

                    if(mAlpha >= alpha){
                        mAlpha = 0;
                        mAnimatedView = null;

                        if(mLastAdded == mAddingList.size() - 1){
                            mAnimating = false;
                            mLastAdded = 0;
                            mAddingList.clear();
                            continue;
                        }
                        if(mAddingList.get(++mLastAdded) instanceof Message) {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addMessage((Message) mAddingList.get(mLastAdded));
                                    mAnimating = true;
                                }
                            });
                            mAnimating = false;
                        }
                        else {
                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addAnswer((Answer) mAddingList.get(mLastAdded));
                                }
                            });

                            mAnimating = false;
                            continue;
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        }
    });

    public GameAdapter(Activity activity, RecyclerView recyclerView) {
        super();
        mActivity = activity;
        mRecyclerView = recyclerView;

        mTypes = new ArrayList<>();
        mMessages = new LinkedHashMap<>();
        mAnswers = new LinkedHashMap<>();
        mAddingList = new ArrayList<>();

        mAnimator.start();

        EventBus.getDefault().register(this);

        ItemProvider.getInstance(activity).start(this);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = null;

        switch (viewType){
            default:
            case sMessage: //Message;
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_message, parent, false);
                return new MessageViewHolder(itemView);
            case sAnswer: //Answer;
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_answer, parent, false);
                return new AnswerViewHolder( itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        switch (getItemViewType(position)){
            case sMessage:
                if(mAnimating && position == getItemCount() - 1) {
                    mAnimatedView = holder.itemView;
                    mAnimatedView.setAlpha(mAlpha);
                }
                ((MessageViewHolder)holder).mText.setText(Html.fromHtml(mMessages.get(position).mText));
                ((MessageViewHolder)holder).mText.setGravity(mMessages.get(position).mGravity);
                break;
            case sAnswer:
                ((AnswerViewHolder)holder).mOption1.setText(mAnswers.get(position).option1);
                ((AnswerViewHolder)holder).mOption1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State state = mAnswers.get(position).state;
                        state.mNextState = mAnswers.get(position).next1;
                        state.mSelectedChoice = 1;
                        ItemProvider.getInstance(mActivity).goToNextOf(GameAdapter.this, state);
                    }
                });

                ((AnswerViewHolder)holder).mOption2.setText(mAnswers.get(position).option2);
                ((AnswerViewHolder)holder).mOption2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        State state = mAnswers.get(position).state;
                        state.mNextState = mAnswers.get(position).next2;
                        state.mSelectedChoice = 2;
                        ItemProvider.getInstance(mActivity).goToNextOf(GameAdapter.this, state);
                    }
                });

                switch(mAnswers.get(position).state.mSelectedChoice){
                    case -1:
                        ((AnswerViewHolder)holder).mOption1.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.button_selector));
                        ((AnswerViewHolder)holder).mOption2.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.button_selector));
                        ((AnswerViewHolder)holder).mOption1.setSelected(false);
                        ((AnswerViewHolder)holder).mOption2.setSelected(false);
                        ((AnswerViewHolder)holder).mOption1.setClickable(true);
                        ((AnswerViewHolder)holder).mOption2.setClickable(true);
                        ((AnswerViewHolder)holder).itemView.findViewById(R.id.separator).setVisibility(View.GONE);
                        break;
                    case 1:
                        ((AnswerViewHolder)holder).mOption1.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.past_button_selector));
                        ((AnswerViewHolder)holder).mOption2.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.past_button_selector));
                        ((AnswerViewHolder)holder).mOption1.setSelected(true);
                        ((AnswerViewHolder)holder).mOption2.setSelected(false);
                        ((AnswerViewHolder)holder).mOption1.setClickable(false);
                        ((AnswerViewHolder)holder).mOption2.setClickable(false);
                        ((AnswerViewHolder)holder).mOption1.setOnClickListener(null);
                        ((AnswerViewHolder)holder).mOption2.setOnClickListener(null);
                        ((AnswerViewHolder)holder).itemView.findViewById(R.id.separator).setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        ((AnswerViewHolder)holder).mOption1.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.past_button_selector));
                        ((AnswerViewHolder)holder).mOption2.setBackgroundDrawable(mActivity.getResources().getDrawable(R.drawable.past_button_selector));
                        ((AnswerViewHolder)holder).mOption1.setSelected(false);
                        ((AnswerViewHolder)holder).mOption2.setSelected(true);
                        ((AnswerViewHolder)holder).mOption1.setClickable(false);
                        ((AnswerViewHolder)holder).mOption2.setClickable(false);
                        ((AnswerViewHolder)holder).mOption1.setOnClickListener(null);
                        ((AnswerViewHolder)holder).mOption2.setOnClickListener(null);
                        ((AnswerViewHolder)holder).itemView.findViewById(R.id.separator).setVisibility(View.VISIBLE);
                        break;
                }

                break;
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        if(mAnimatedView == holder.itemView) {
            mAnimatedView = null;
            holder.itemView.setAlpha(1);
        }
    }

    @Override
    public int getItemCount() {
        return mMessages.size() + mAnswers.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mTypes.get(position);
    }

    public void addMessage(Message message, boolean animate) {
        if(animate) {
            if (mAnimating)
                mAddingList.add(message);
            else {
                mAlpha = 0.0f;
                mAnimatedView = null;
                mLastAnimatedPosition = getItemCount() - 1;
                addMessage(message);
                mAnimating = true;
            }
        } else
            addMessage(message);
    }

    public void addMessage(Message message) {
        boolean scrollToEnd = false;

        if(((LinearLayoutManager)mRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() == getItemCount()-1)
            scrollToEnd = true;

        if(message.mText.charAt(0) == '[' && message.mText.charAt(message.mText.length()-1) == ']') {
            message.mText = "<font color='#2e6d46'>" + message.mText + "</font>";
            message.mGravity = Gravity.CENTER;
        }
        else
     //       message.mText = "<font color='#3ba7cc'>" + message.mText + "</font>";
            message.mText = "<font color='#ffffff'>" + message.mText + "</font>";

        mTypes.add(sMessage);
        mMessages.put(getItemCount(), message);

        notifyDataSetChanged();

        if(scrollToEnd)
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.smoothScrollToPosition(getItemCount());
                        }
                    }, 200);
                }
            });
    }

    public void addAnswer(Answer answer, boolean animate) {
        if(animate) {
            if (mAnimating)
                mAddingList.add(answer);
            else {
                mAnimating = true;
                mAnimator.start();
                mLastAnimatedPosition = getItemCount() - 1;
                addAnswer(answer);
            }
        } else
            addAnswer(answer);
    }

    public void addAnswer(Answer answer) {

        boolean scrollToEnd = false;

        if(((LinearLayoutManager)mRecyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() == getItemCount()-1)
            scrollToEnd = true;

        mTypes.add(sAnswer);
        mAnswers.put(getItemCount(), answer);

        notifyDataSetChanged();

        if(scrollToEnd)
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mRecyclerView.smoothScrollToPosition(getItemCount());
                        }
                    }, 200);
                }
            });
    }

    public void scrollToEnd() {
        mRecyclerView.smoothScrollToPosition(getItemCount() - 1);
    }


    public void languageChanged() {
        mTypes = new ArrayList<>();
        mMessages = new LinkedHashMap<>();
        mAnswers = new LinkedHashMap<>();
        mAddingList = new ArrayList<>();

        ItemProvider.getInstance(mActivity).start(this);

        mAddingList = new ArrayList<>();

        mLastAnimatedPosition = 0;
        mAnimating = false;

        mAnimatedView = null;

        mAlpha = 0.0f;
        mLastAdded = 0;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onGoNextOfEvent(GoNextOfEvent event) {
        ItemProvider.getInstance(mActivity).goToNextOf(this, event.state);
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView mText;

        MessageViewHolder(View root){
            super(root);

            mText = (TextView) root.findViewById(R.id.text);
        }
    }

    class AnswerViewHolder extends RecyclerView.ViewHolder {
        TextView mOption1, mOption2;

        AnswerViewHolder(View root){
            super(root);
            mOption1 = (TextView) root.findViewById(R.id.option1);
            mOption2 = (TextView) root.findViewById(R.id.option2);
        }
    }
}
