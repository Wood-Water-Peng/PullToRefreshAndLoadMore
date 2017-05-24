package ui;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.jackypeng.testforloadmore.R;

import assit.FootAndHeaderIndicator;

/**
 * Created by jackypeng on 2017/5/20.
 */

public class ClassicFootView extends FrameLayout implements FootUIHandler {


    private static final String TAG = "ClassicFootView";
    private RotateAnimation mFlipAnimation;
    private long mRotateAniTime = 500l;
    private RotateAnimation mReverseAnimation;
    private View mRotateView;
    private TextView mPrepareTv;
    private TextView mCompleteTv;
    private View mPrepareContainer;
    private View mLoadingContainer;
    private View mCompletedContainer;

    public ClassicFootView(@NonNull Context context) {
        this(context, null);
    }

    public ClassicFootView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClassicFootView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews(attrs);
    }

    private void initViews(AttributeSet attrs) {
        View footer = LayoutInflater.from(getContext()).inflate(R.layout.item_custom_footview, this);
        buildAnimation();
        mRotateView = footer.findViewById(R.id.item_foot_view_prepare_container_iv);
        mPrepareTv = (TextView) footer.findViewById(R.id.item_foot_view_prepare_container_tv);
        mPrepareContainer = footer.findViewById(R.id.item_foot_view_prepare_container);
        mLoadingContainer = footer.findViewById(R.id.item_foot_view_loading_container);
        mCompletedContainer = footer.findViewById(R.id.item_foot_view_completed_container);
        mCompleteTv = (TextView) footer.findViewById(R.id.item_foot_view_completed_container_tv);

        resetView();

    }

    private void resetView() {
        mLoadingContainer.setVisibility(GONE);
        mCompletedContainer.setVisibility(GONE);
    }

    private void buildAnimation() {
        mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(mRotateAniTime);
        mFlipAnimation.setFillAfter(true);

        mReverseAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseAnimation.setInterpolator(new LinearInterpolator());
        mReverseAnimation.setDuration(mRotateAniTime);
        mReverseAnimation.setFillAfter(true);
    }

    @Override
    public void onUIPositionChange(LoadmoreLayout frame, boolean isUnderTouch, byte status, FootAndHeaderIndicator ptrIndicator) {
        int offsetToLoadmore = ptrIndicator.getOffsetToLoadmore();
        int currentPosY = Math.abs(ptrIndicator.getCurrentFootPosY());
        int lastPosY = Math.abs(ptrIndicator.getLastFootPosY());
        Log.i(TAG, "currentPosY:" + currentPosY + "---lastPosY:" + lastPosY + "---offsetToLoadmore:" + offsetToLoadmore);
        if (currentPosY < offsetToLoadmore && lastPosY > offsetToLoadmore) {
            if (isUnderTouch && status == LoadmoreLayout.PTL_STATUS_PREPARE) {
                crossRotateLineFromBottomUnderTouch(frame);
                if (mRotateView != null) {
                    mRotateView.clearAnimation();
                    mRotateView.startAnimation(mReverseAnimation);
                }
            }
        } else if (currentPosY > offsetToLoadmore && lastPosY < offsetToLoadmore) {
            if (isUnderTouch && status == LoadmoreLayout.PTL_STATUS_PREPARE) {
                crossRotateLineFromTopUnderTouch(frame);
                if (mRotateView != null) {
                    mRotateView.clearAnimation();
                    mRotateView.startAnimation(mFlipAnimation);
                }
            }
        }

    }

    private void crossRotateLineFromTopUnderTouch(LoadmoreLayout frame) {
        mPrepareTv.setText("Release to loadmore");
    }

    private void crossRotateLineFromBottomUnderTouch(LoadmoreLayout frame) {
        mPrepareTv.setText("pull up to loadmore");
    }

    @Override
    public void onUILoadMorePrepare(LoadmoreLayout frame) {
        mLoadingContainer.setVisibility(GONE);
        mPrepareContainer.setVisibility(VISIBLE);
        mCompletedContainer.setVisibility(GONE);

    }

    @Override
    public void onUILoadMoreBegin(LoadmoreLayout frame) {
        mLoadingContainer.setVisibility(VISIBLE);
        mPrepareContainer.setVisibility(GONE);
        mCompletedContainer.setVisibility(GONE);
    }

    @Override
    public void onUILoadMoreCompleted(LoadmoreLayout frame) {
        mLoadingContainer.setVisibility(GONE);
        mPrepareContainer.setVisibility(GONE);
        mCompletedContainer.setVisibility(VISIBLE);
        mCompleteTv.setText("Load Completed");
    }

    @Override
    public void onUIReset(LoadmoreLayout loadmoreLayout) {

    }


}
