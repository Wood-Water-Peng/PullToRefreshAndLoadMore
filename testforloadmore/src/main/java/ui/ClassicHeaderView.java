package ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.example.jackypeng.testforloadmore.R;

import assit.FootAndHeaderIndicator;

/**
 * Created by pj on 2017/5/23.
 */
public class ClassicHeaderView extends FrameLayout implements HeaderUIHandler {
    private View mRotateView;
    private TextView mTitleTextView;
    private View mProgressBar;
    private int mRotateAniTime = 150;
    private RotateAnimation mFlipAnimation;
    private RotateAnimation mReverseFlipAnimation;

    public ClassicHeaderView(Context context) {
        this(context, null);
    }

    public ClassicHeaderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClassicHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        buildAnimation();
        View header = LayoutInflater.from(getContext()).inflate(R.layout.ptl_classic_default_header, this);
        mRotateView = header.findViewById(R.id.ptl_classic_header_rotate_view);
        mTitleTextView = (TextView) header.findViewById(R.id.ptl_classic_header_rotate_view_header_title);
        mProgressBar = header.findViewById(R.id.ptl_classic_header_rotate_view_progressbar);

        resetView();
    }

    private void buildAnimation() {
        mFlipAnimation = new RotateAnimation(0, -180, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mFlipAnimation.setInterpolator(new LinearInterpolator());
        mFlipAnimation.setDuration(mRotateAniTime);
        mFlipAnimation.setFillAfter(true);

        mReverseFlipAnimation = new RotateAnimation(-180, 0, RotateAnimation.RELATIVE_TO_SELF, 0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f);
        mReverseFlipAnimation.setInterpolator(new LinearInterpolator());
        mReverseFlipAnimation.setDuration(mRotateAniTime);
        mReverseFlipAnimation.setFillAfter(true);
    }

    private void resetView() {
        hideRotateView();
        mProgressBar.setVisibility(INVISIBLE);
    }

    private void hideRotateView() {
        mRotateView.clearAnimation();
        mRotateView.setVisibility(INVISIBLE);
    }

    private void crossRotateLineFromTopUnderTouch(LoadmoreLayout frame) {
        mTitleTextView.setVisibility(VISIBLE);
        mTitleTextView.setText("Release To Refresh");
    }

    @Override
    public void onUIPositionChange(LoadmoreLayout frame, boolean isUnderTouch, byte status, FootAndHeaderIndicator ptrIndicator) {
        final int mOffsetToRefresh = frame.getOffsetToRefresh();
        final int currentPos = ptrIndicator.getCurrentHeadPosY();
        final int lastPos = ptrIndicator.getLastHeadPosY();

        if (currentPos < mOffsetToRefresh && lastPos >= mOffsetToRefresh) {
            if (isUnderTouch && status == LoadmoreLayout.REFRESH_STATUS_PREPARE) {
                crossRotateLineFromBottomUnderTouch(frame);
                if (mRotateView != null) {
                    mRotateView.clearAnimation();
                    mRotateView.startAnimation(mReverseFlipAnimation);
                }
            }
        } else if (currentPos > mOffsetToRefresh && lastPos <= mOffsetToRefresh) {
            if (isUnderTouch && status == LoadmoreLayout.REFRESH_STATUS_PREPARE) {
                crossRotateLineFromTopUnderTouch(frame);
                if (mRotateView != null) {
                    mRotateView.clearAnimation();
                    mRotateView.startAnimation(mFlipAnimation);
                }
            }
        }
    }

    private void crossRotateLineFromBottomUnderTouch(LoadmoreLayout frame) {
        mTitleTextView.setVisibility(VISIBLE);
        mTitleTextView.setText("Pull to refresh");
    }

    @Override
    public void onUIRefreshPrepare(LoadmoreLayout frame) {
        mProgressBar.setVisibility(INVISIBLE);

        mRotateView.setVisibility(VISIBLE);
        mTitleTextView.setVisibility(VISIBLE);
        mTitleTextView.setText("Pull to refresh");
    }

    @Override
    public void onUIRefreshBegin(LoadmoreLayout frame) {
        hideRotateView();
        mProgressBar.setVisibility(VISIBLE);
        mTitleTextView.setVisibility(VISIBLE);
        mTitleTextView.setText("Updating...");
    }

    @Override
    public void onUIRefreshCompleted(LoadmoreLayout frame) {
        hideRotateView();
        mProgressBar.setVisibility(INVISIBLE);

        mTitleTextView.setVisibility(VISIBLE);
        mTitleTextView.setText("Completed");
    }

    @Override
    public void onUIReset(LoadmoreLayout loadmoreLayout) {
        resetView();
    }
}
