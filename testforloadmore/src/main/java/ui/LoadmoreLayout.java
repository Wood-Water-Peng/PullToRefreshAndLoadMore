package ui;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Scroller;

import assit.FootAndHeaderIndicator;


/**
 * Created by jackypeng on 2017/5/20.
 */

public class LoadmoreLayout extends ViewGroup {
    private static final String TAG = "LoadmoreLayout";
    //上拉加载状态
    public static final byte PTL_STATUS_INIT = 0;
    public static final byte PTL_STATUS_PREPARE = 1;
    public static final byte PTL_STATUS_LOADING = 2;
    public static final byte PTL_STATUS_COMPLETE = 3;
    //下拉刷新状态
    public static final byte REFRESH_STATUS_INIT = 4;
    public static final byte REFRESH_STATUS_PREPARE = 5;
    public static final byte REFRESH_STATUS_REFRESHING = 6;
    public static final byte REFRESH_STATUS_COMPLETE = 7;

    private final int mPagingTouchSlop;
    private final FootScrollChecker mFootScrollChecker;
    private byte mFootStatus = PTL_STATUS_INIT;
    private byte mHeaderStatus = REFRESH_STATUS_INIT;
    private View mContent;
    private View mFootView;
    private int mFootHeight;
    private FootAndHeaderIndicator mFootAndHeaderIndicator;
    private HeadScrollChecker mHeadScrollChecker;
    private int mDurationToClose = 200;
    private int mDurationToCloseHeader = 1000;
    private int mDurationToCloseFooter = 1000;
    private PtlFootHandler mPtlFootHandler;
    private PtlHeaderHandler mPtlHeaderHandler;
    private View mHeaderView;
    private HeaderUIHandler mHeaderUIHandler;
    private int mHeaderHeight;
    private MotionEvent mLastMoveEvent;
    private boolean mHasSendCancelEvent;

    public void loadMoreComplete() {
        performLoadmoreComplete();
    }

    public void refreshCompleted() {
        performRefreshComplete();
    }

    private void performRefreshComplete() {
        mHeaderStatus = REFRESH_STATUS_COMPLETE;
        notifyUIRefreshComplete();
    }


    private void performLoadmoreComplete() {
        // 刷新完毕
        mFootStatus = PTL_STATUS_COMPLETE;
        notifyUILoadMoreComplete();
    }

    private void tryScrollBackToTopAfterComplete() {
        Log.i(TAG, "---tryScrollBackToTopAfterComplete---");
        tryScrollBackToTop();
    }

    private void notifyUIRefreshComplete() {
        if (mHeaderUIHandler != null) {
            mHeaderUIHandler.onUIRefreshCompleted(this);
        }
        mFootAndHeaderIndicator.onUIRefreshComplete();
        tryScrollBackToTopAfterComplete();
        tryToNotifyHeaderReset();
    }

    private void notifyUILoadMoreComplete() {
        if (mFootUIHandler != null) {
            mFootUIHandler.onUILoadMoreCompleted(this);
        }
        mFootAndHeaderIndicator.onUIRefreshComplete();
        tryScrollBackToBottomAfterComplete();
        tryToNotifyFooterReset();
    }

    private void tryScrollBackToBottomAfterComplete() {
        tryScrollBackToBottom();
    }

    public int getOffsetToRefresh() {
        return mFootAndHeaderIndicator.getOffsetToRefresh();
    }

    public void setPtlHeadHandler(PtlHeaderHandler ptlHeadHandler) {
        mPtlHeaderHandler = ptlHeadHandler;
    }

    public static class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        @SuppressWarnings({"unused"})
        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }

    private FootUIHandler mFootUIHandler;

    public void addFootUIHandler(FootUIHandler footUIHandler) {
        mFootUIHandler = footUIHandler;
    }

    public void addHeaderUIHandler(HeaderUIHandler headerUIHandler) {
        mHeaderUIHandler = headerUIHandler;
    }

    public void setHeaderView(View header) {
        if (header == null) {
            return;
        }

        if (mHeaderView != null && header != null && mHeaderView != header) {
            removeView(header);
        }

        ViewGroup.LayoutParams lp = header.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(-1, -2);
            header.setLayoutParams(lp);
        }
        mHeaderView = header;
        addView(header);
    }

    public void setFootView(View footer) {
        if (footer == null) {
            return;
        }

        if (mFootView != null && footer != null && mFootView != footer) {
            removeView(footer);
        }

        ViewGroup.LayoutParams lp = footer.getLayoutParams();
        if (lp == null) {
            lp = new LayoutParams(-1, -2);
            footer.setLayoutParams(lp);
        }
        mFootView = footer;
        addView(footer);
    }

    public void setPtlFootHandler(PtlFootHandler ptlHandler) {
        mPtlFootHandler = ptlHandler;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p != null && p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    public LoadmoreLayout(Context context) {
        this(context, null);
    }

    public LoadmoreLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadmoreLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mFootAndHeaderIndicator = new FootAndHeaderIndicator();
        mHeadScrollChecker = new HeadScrollChecker();
        mFootScrollChecker = new FootScrollChecker();
        final ViewConfiguration conf = ViewConfiguration.get(getContext());
        mPagingTouchSlop = conf.getScaledTouchSlop() * 2;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int headOffsetY = mFootAndHeaderIndicator.getCurrentHeadPosY();
        int footOffsetY = mFootAndHeaderIndicator.getCurrentFootPosY();

        if (mHeaderView != null && mContent != null) {
            Log.i(TAG, "headHeight:" + mHeaderView.getMeasuredHeight());
            int top = t + headOffsetY - mHeaderView.getMeasuredHeight();
            int bottom = mHeaderView.getMeasuredHeight() + top;
            mHeaderView.layout(l, top, l + mHeaderView.getMeasuredWidth(), bottom);
        }

        if (mContent != null) {
            int top = t + headOffsetY + footOffsetY;
            int bottom = mContent.getMeasuredHeight() + top;
            mContent.layout(l, top, l + mContent.getMeasuredWidth(), bottom);
        }

        if (mFootView != null && mContent != null) {
            Log.i(TAG, "footHeight:" + mFootView.getMeasuredHeight());
            int top = mContent.getMeasuredHeight() + footOffsetY;
            int bottom = top + mFootView.getMeasuredHeight();
            mFootView.layout(l, top, l + mFootView.getMeasuredWidth(), bottom);
        }
//        Log.i(TAG, "after release--footTop:" + mFootView.getTop() + "footBottom:" + mFootView.getBottom());
    }

    @Override
    protected void onFinishInflate() {
        /**
         * when inflation finished,there will be only two children allowed,one wil be the listview and
         * another will be the footview
         */
        final int count = getChildCount();
        Log.i(TAG, "child_count:" + count);
        if (count > 3) {
            throw new IllegalArgumentException("LoadmoreLayout can only contain 3 children");
        } else if (count == 2) {
            View child01 = getChildAt(0);
            View child02 = getChildAt(1);
            if (child02 instanceof FootUIHandler) {
                mContent = child01;
                mFootView = child02;
            } else if (child01 instanceof FootUIHandler) {
                mFootView = child01;
                mContent = child02;
            }
            if (child02 instanceof HeaderUIHandler) {
                mContent = child01;
                mHeaderView = child02;
            } else if (child01 instanceof HeaderUIHandler) {
                mHeaderView = child01;
                mContent = child02;
            }

        } else if (count == 3) {  //judging the footview by whether it implements the FootUIHandler
            View child01 = getChildAt(0);
            View child02 = getChildAt(1);
            View child03 = getChildAt(2);
            if (child02 instanceof FootUIHandler) {
                mFootView = child02;
                if (child01 instanceof HeaderUIHandler) {
                    mHeaderView = child01;
                    mContent = child03;
                } else {
                    mContent = child01;
                    mHeaderView = child03;
                }
            } else if (child02 instanceof HeaderUIHandler) {
                mHeaderView = child02;
                if (child01 instanceof FootUIHandler) {
                    mFootView = child01;
                    mContent = child03;
                } else {
                    mContent = child01;
                    mFootView = child03;
                }
            } else {
                mContent = child02;
                if (child01 instanceof FootUIHandler) {
                    mHeaderView = child01;
                    mFootView = child03;
                } else {
                    mFootView = child01;
                    mHeaderView = child03;
                }
            }
        }

        if (mFootView != null) {
            mFootView.bringToFront();
        }
        if (mHeaderView != null) {
            mHeaderView.bringToFront();
        }
        super.onFinishInflate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mHeaderView != null) {
            measureChildWithMargins(mHeaderView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mHeaderHeight = mHeaderView.getMeasuredHeight();
            mFootAndHeaderIndicator.setOffsetToRefresh(mHeaderHeight + 10);
            mFootAndHeaderIndicator.setHeadHeight(mHeaderHeight);
            Log.i(TAG, "mHeaderHeight:" + mHeaderHeight);
        }

        if (mContent != null) {
            measureContentView(mContent, widthMeasureSpec, heightMeasureSpec);
        }

        if (mFootView != null) {
            measureChildWithMargins(mFootView, widthMeasureSpec, 0, heightMeasureSpec, 0);
            mFootHeight = mFootView.getMeasuredHeight();
            mFootAndHeaderIndicator.setOffsetToLoadMore(mFootHeight + 10);
            mFootAndHeaderIndicator.setFooterHeight(mFootHeight);
            Log.i(TAG, "mFootHeight:" + mFootHeight);
        }
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mContent.getMeasuredHeight());
    }

    private void measureContentView(View mContent, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
        final MarginLayoutParams lp = (MarginLayoutParams) mContent.getLayoutParams();

        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
                getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
        final int childHeightMeasureSpec = getChildMeasureSpec(parentHeightMeasureSpec,
                getPaddingTop() + getPaddingBottom() + lp.topMargin, lp.height);

        mContent.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if ((mContent == null && mFootView == null) || (mContent == null && mHeaderView == null)) {
            return super.dispatchTouchEvent(ev);
        }

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHasSendCancelEvent = false;
                mFootAndHeaderIndicator.onPressDown(ev.getX(), ev.getY());
                mFootScrollChecker.abortIfWorking();
                mHeadScrollChecker.abortIfWorking();
                /**
                 * 判断Scroller的执行状态
                 */
                break;
            case MotionEvent.ACTION_MOVE:
                mLastMoveEvent = ev;
                mFootAndHeaderIndicator.onMove(ev.getX(), ev.getY());
                float offsetY = mFootAndHeaderIndicator.getOffsetY();
                float offsetX = mFootAndHeaderIndicator.getOffsetX();

                if (Math.abs(offsetY) > mPagingTouchSlop && Math.abs(offsetX) > Math.abs(offsetY)) {
                    return super.dispatchTouchEvent(ev);
                }

                boolean scrollDown = offsetY > 0;
                boolean scrollUp = !scrollDown;
                boolean canMoveDown = mFootAndHeaderIndicator.hasFootLeftStartPosition();
                boolean canMoveUp = mFootAndHeaderIndicator.hasHeadLeftStartPosition();

//                Log.i(TAG, "canMoveDown:" + canMoveDown + "---canMoveUp:" + canMoveUp);
//                Log.i(TAG, "curHeadPos:" + mFootAndHeaderIndicator.getCurrentHeadPosY() + "---curFootPos:" + mFootAndHeaderIndicator.getCurrentFootPosY());

//                if (scrollDown && mPtlHeaderHandler != null && mPtlHeaderHandler.checkCanDoRefresh(this, mContent, mHeaderView)) {
//                    moveHeaderPos(offsetY);
//                    return true;
//                }
//
//                if (scrollDown || (scrollUp && canMoveUp)) {
//                    moveHeaderPos(offsetY);
//                    return true;
//                }

                if (scrollDown && !canChildScrollDown()) {
                    moveHeaderPos(offsetY);
                    return true;
                }
                if (canMoveUp && scrollUp) {
                    moveHeaderPos(offsetY);
                    return true;
                }
//
                if (!canChildScrollUp() && scrollUp) {
                    moveFootPos(offsetY);
                    return true;
                } else if (scrollDown && canMoveDown) {
                    moveFootPos(offsetY);
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mFootAndHeaderIndicator.onRelease(); // 表示滑动动作已经取消
                if (mFootAndHeaderIndicator.hasFootLeftStartPosition()) {
                    onRelease(false);
                    if (mFootAndHeaderIndicator.hasFootMovedAfterPressDown()) {
                        Log.i(TAG, "---hasMovedAfterPressDown---");
                        sendCancelEvent();
                        return true;
                    }
                }
                if (mFootAndHeaderIndicator.hasHeadLeftStartPosition()) {
                    onRelease(false);
                    if (mFootAndHeaderIndicator.hasHeadMovedAfterPressDown()) {
                        Log.i(TAG, "---hasMovedAfterPressDown---");
                        sendCancelEvent();
                        return true;
                    }
                }
                break;

        }
        return super.dispatchTouchEvent(ev);
    }


    private void onRelease(boolean stayForLoading) {
        tryToPerformRefresh();
        tryToPerformLoadMore();  //更新状态
        Log.i(TAG, "onRelease--footStatus:" + mFootStatus + "headStatus:" + mHeaderStatus);

        if (mHeaderStatus == REFRESH_STATUS_REFRESHING) {
            //keep foot for loading
            mHeadScrollChecker.tryToScrollHeadTo(
                    mFootAndHeaderIndicator.getOffsetToKeepHeaderWhileLoading(),
                    mDurationToClose);
        } else {
            if (mHeaderStatus == REFRESH_STATUS_PREPARE) {
                tryScrollBackToTop();
            } else if (mHeaderStatus == REFRESH_STATUS_COMPLETE) {
                notifyUIRefreshComplete();
            }
        }

        if (mFootStatus == PTL_STATUS_LOADING) {
            //keep foot for loading
            mFootScrollChecker.tryToScrollFootTo(
                    mFootAndHeaderIndicator.getOffsetToKeepFootWhileLoading(),
                    mDurationToClose);
        } else {
            if (mFootStatus == PTL_STATUS_PREPARE) {
                tryScrollBackToBottom();
            } else if (mFootStatus == PTL_STATUS_COMPLETE) {
                notifyUILoadMoreComplete();
            }
        }
    }


    private boolean tryToPerformRefresh() {

        if (mHeaderStatus != REFRESH_STATUS_PREPARE) {
            return false;
        }

        if (mFootAndHeaderIndicator.isOverOffsetToRefresh()) {
            //距离超过了加载距离
            mHeaderStatus = REFRESH_STATUS_REFRESHING;
            performRefresh();
        }
        return false;
    }

    private void tryScrollBackToTop() {
        if (!mFootAndHeaderIndicator.isUnderTouch()) {
            mHeadScrollChecker.tryToScrollHeadTo(mFootAndHeaderIndicator.POS_START,
                    mDurationToCloseHeader);
        }
    }


    private void tryScrollBackToBottom() {
        if (!mFootAndHeaderIndicator.isUnderTouch()) {
            mFootScrollChecker.tryToScrollFootTo(mFootAndHeaderIndicator.POS_START,
                    mDurationToCloseHeader);
        }
    }

    private boolean tryToPerformLoadMore() {

        if (mFootStatus != PTL_STATUS_PREPARE) {
            return false;
        }

        if (mFootAndHeaderIndicator.isOverOffsetToLoadMore()) {
            //距离超过了加载距离
//            Log.i(TAG, "---isOverOffsetToLoadMore---");
            mFootStatus = PTL_STATUS_LOADING;
            performLoadMore();
        }
        return false;
    }

    private void performLoadMore() {
        if (mFootUIHandler != null) {
            mFootUIHandler.onUILoadMoreBegin(this);
        }
        if (mPtlFootHandler != null) {
            mPtlFootHandler.onLoadMoreBegin(this);
        }
    }

    private void performRefresh() {
        if (mHeaderUIHandler != null) {
            mHeaderUIHandler.onUIRefreshBegin(this);
        }
        if (mPtlHeaderHandler != null) {
            mPtlHeaderHandler.onRefreshBegin(this);
        }
    }

    private void moveHeaderPos(float offsetY) {

        if (mHeaderView == null) {
            return;
        }
        if (offsetY < 0 && mFootAndHeaderIndicator.isHeaderInStartPosition()) {
            return;
        }
        // 之前保存的Y值+偏移量
        int to = mFootAndHeaderIndicator.getCurrentHeadPosY() + (int) offsetY;

        if (mFootAndHeaderIndicator.willOverTop(to)) {
            to = FootAndHeaderIndicator.POS_START;
        }

        mFootAndHeaderIndicator.setCurrentHeaderPos(to);
        int change = to - mFootAndHeaderIndicator.getLastHeadPosY();

        updateHeadPos(change);
    }

    private void moveFootPos(float offsetY) {

        if (mFootView == null) {
            return;
        }
        // 之前保存的Y值+偏移量
        int to = mFootAndHeaderIndicator.getCurrentFootPosY() + (int) offsetY;

        if (mFootAndHeaderIndicator.willOverTop(to)) {
            to = mFootAndHeaderIndicator.POS_START;
            Log.i(TAG, "---willOverTop---");
        }

        mFootAndHeaderIndicator.setCurrentFootPos(to);
        int change = to - mFootAndHeaderIndicator.getLastFootPosY();
        updateFootPos(change);
    }

    private void updateHeadPos(int change) {

        if (change == 0) {
            return;
        }

        boolean underTouch = mFootAndHeaderIndicator.isUnderTouch();
        //once moved,cancel event will be sent to child
        if (underTouch && !mHasSendCancelEvent && mFootAndHeaderIndicator.hasHeadMovedAfterPressDown()) {
            mHasSendCancelEvent = true;
            sendCancelEvent();
        }
        //leave initial position or just refresh complete
        if (mFootAndHeaderIndicator.hasHeadJustLeftStartPosition() && mHeaderStatus == REFRESH_STATUS_INIT) {
            mHeaderStatus = REFRESH_STATUS_PREPARE;
            mHeaderUIHandler.onUIRefreshPrepare(this);
        }
        //back to initial position
        if (mFootAndHeaderIndicator.hasHeadJustBackToStartPosition()) {
            tryToNotifyHeaderReset();
            if (underTouch) {
                sendDownEvent();
            }
        }

        mContent.offsetTopAndBottom(change);
        mHeaderView.offsetTopAndBottom(change);

        if (mHeaderUIHandler != null) {
            mHeaderUIHandler.onUIPositionChange(this, underTouch, mHeaderStatus, mFootAndHeaderIndicator);
        }
    }

    private void updateFootPos(int change) {
        if (change == 0) {
            return;
        }

        boolean underTouch = mFootAndHeaderIndicator.isUnderTouch();

        //once moved,cancel event will be sent to child
        if (underTouch && !mHasSendCancelEvent && mFootAndHeaderIndicator.hasFootMovedAfterPressDown()) {
//            mHasSendCancelEvent=true;
        }


        if (mFootAndHeaderIndicator.hasFootLeftStartPosition() && mFootStatus == PTL_STATUS_INIT) {
            mFootStatus = PTL_STATUS_PREPARE;
            mFootUIHandler.onUILoadMorePrepare(this);
        }

        //foot back to initial position
        if (mFootAndHeaderIndicator.hasFootJustBackToStartPosition()) {
            tryToNotifyFooterReset();
            if (underTouch) {
                sendDownEvent();
            }
        }

        mContent.offsetTopAndBottom(change);
        mFootView.offsetTopAndBottom(change);

        if (mFootUIHandler != null) {
            mFootUIHandler.onUIPositionChange(this, underTouch, mFootStatus, mFootAndHeaderIndicator);
        }

    }

    private void sendCancelEvent() {
        // The ScrollChecker will update position and lead to send cancel event
        // when mLastMoveEvent is null.
        // fix #104, #80, #92
        if (mLastMoveEvent == null) {
            return;
        }
        MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(),
                last.getEventTime() + ViewConfiguration.getLongPressTimeout(),
                MotionEvent.ACTION_CANCEL, last.getX(), last.getY(),
                last.getMetaState());
        dispatchTouchEventSupper(e);
    }

    private void sendDownEvent() {
        final MotionEvent last = mLastMoveEvent;
        MotionEvent e = MotionEvent.obtain(last.getDownTime(),
                last.getEventTime(), MotionEvent.ACTION_DOWN, last.getX(),
                last.getY(), last.getMetaState());
        dispatchTouchEventSupper(e);
    }

    public boolean dispatchTouchEventSupper(MotionEvent e) {
        return super.dispatchTouchEvent(e);
    }


    private void tryToNotifyHeaderReset() {
        Log.i(TAG, "---tryToNotifyFooterReset---");
        if ((mHeaderStatus == REFRESH_STATUS_PREPARE || mHeaderStatus == REFRESH_STATUS_COMPLETE) && mFootAndHeaderIndicator.isHeaderInStartPosition()) {
            if (mHeaderUIHandler != null) {
                mHeaderUIHandler.onUIReset(this);
            }
            mHeaderStatus = REFRESH_STATUS_INIT;
        }
    }

    private void tryToNotifyFooterReset() {
        Log.i(TAG, "---tryToNotifyFooterReset---");
        if ((mFootStatus == PTL_STATUS_PREPARE || mFootStatus == PTL_STATUS_COMPLETE) && mFootAndHeaderIndicator.isFooterInStartPosition()) {
            if (mFootUIHandler != null) {
                mFootUIHandler.onUIReset(this);
            }
            mFootStatus = PTL_STATUS_INIT;
        }
    }

    protected boolean canChildScrollDown() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mContent instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mContent;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mContent, -1) || mContent.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mContent, -1);
        }
    }

    protected boolean canChildScrollUp() {
        AbsListView absListView = (AbsListView) mContent;
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mContent instanceof AbsListView) {
                //absListView = (AbsListView) mContent;
                return absListView.getChildCount() > 0
                        && (absListView.getLastVisiblePosition() < absListView.getChildCount() - 1
                        || absListView.getChildAt(absListView.getChildCount() - 1).getBottom() > absListView.getPaddingBottom());
            } else {
                return ViewCompat.canScrollVertically(mContent, 1);
            }
        } else {
            return ViewCompat.canScrollVertically(mContent, 1);
        }
    }


    private void onPtrScrollAbort() {

    }

    private class HeadScrollChecker implements Runnable {

        private final Scroller mScroller;
        private int mStart;
        private int mTo;
        private int mLastFlingY;
        private boolean mIsRunning = false;

        public HeadScrollChecker() {
            mScroller = new Scroller(getContext());
        }

        @Override
        public void run() {
            boolean finish = !mScroller.computeScrollOffset() || mScroller.isFinished();
            int curY = mScroller.getCurrY();
            int deltaY = curY - mLastFlingY;
            Log.i(TAG, "head_deltaY:" + deltaY);
            if (!finish) {
                mLastFlingY = curY;
                moveHeaderPos(deltaY);
                post(this);
            } else {
                tryToNotifyFooterReset();
                finish();
            }
        }

        private void finish() {
            reset();
        }

        private void reset() {
            mIsRunning = false;
            mLastFlingY = 0;
            removeCallbacks(this);
        }

        public void tryToScrollHeadTo(int to, int duration) {

            mStart = mFootAndHeaderIndicator.getCurrentHeadPosY();
            mTo = to;
            int distance = to - mStart;
//            Log.i(TAG, "mStart:" + mStart + "---to:" + to);
            removeCallbacks(this);
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            mLastFlingY = 0;
            mScroller.startScroll(0, 0, 0, distance, duration);
            //将该任务添加到主线程的消息队列
            post(this);
            mIsRunning = true;
        }

        public void abortIfWorking() {
            if (mIsRunning) {
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                onPtrScrollAbort();
                reset();
            }
        }
    }

    private class FootScrollChecker implements Runnable {

        private final Scroller mScroller;
        private int mStart;
        private int mTo;
        private int mLastFlingY;
        private boolean mIsRunning = false;

        public FootScrollChecker() {
            mScroller = new Scroller(getContext());
        }

        @Override
        public void run() {
            boolean finish = !mScroller.computeScrollOffset() || mScroller.isFinished();
            int curY = mScroller.getCurrY();
            int deltaY = curY - mLastFlingY;
            Log.i(TAG, "foot_deltaY:" + deltaY);
            if (!finish) {
                mLastFlingY = curY;
                moveFootPos(deltaY);
                post(this);
            } else {
                tryToNotifyFooterReset();
                finish();
            }
        }

        private void finish() {
            reset();
        }

        private void reset() {
            mIsRunning = false;
            mLastFlingY = 0;
            removeCallbacks(this);
        }

        //在一定时间内滑动到某个位置
        public void tryToScrollFootTo(int to, int duration) {
            mStart = mFootAndHeaderIndicator.getCurrentFootPosY();
            mTo = to;
            int distance = -(to + mStart);
//            Log.i(TAG, "mStart:" + mStart + "---to:" + to);
            removeCallbacks(this);
            if (!mScroller.isFinished()) {
                mScroller.forceFinished(true);
            }
            mLastFlingY = 0;
            mScroller.startScroll(0, 0, 0, distance, duration);
            //将该任务添加到主线程的消息队列
            post(this);
            mIsRunning = true;
        }

        public void abortIfWorking() {
            if (mIsRunning) {
                if (!mScroller.isFinished()) {
                    mScroller.forceFinished(true);
                }
                onPtrScrollAbort();
                reset();
            }
        }
    }


}
