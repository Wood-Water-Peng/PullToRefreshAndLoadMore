package assit;

import android.graphics.PointF;
import android.util.Log;

/**
 * Created by jackypeng on 2017/5/21.
 */

public class FootAndHeaderIndicator {

    public static final int POS_START = 0;
    private static final String TAG = "FootAndHeaderIndicator";
    private PointF mFootLastMove = new PointF();
    private PointF mHeaderLastMove = new PointF();
    private boolean mIsUnderTouch;
    private float mResistance = 1.7f;
    private float mFootOffsetX;
    private float mFootOffsetY;
    private int mPressedPos;
    private int mFooterHeight;
    private int mOffsetToLoadmore;
    private int mLastFootPosY;
    private int mLastHeadPosY;
    private int mRefreshCompleteY;
    private int mHeaderHeight;
    private int mOffsetToRefresh;
    private int mCurrentHeadPosY;
    private int mCurrentFootPosY;
    private float mHeaderOffsetX;
    private float mHeaderOffsetY;
    private PointF mLastMove = new PointF();
    private float mOffsetY;
    private float mOffsetX;

    public void onPressDown(float x, float y) {
        mIsUnderTouch = true;
        mPressedPos = mCurrentFootPosY;
        mLastMove.set(x, y);
    }

    public void setFooterHeight(int footerHeight) {
        this.mFooterHeight = footerHeight;
    }

    public final void onFootMove(float x, float y) {
        float offsetX = x - mFootLastMove.x;
        float offsetY = (y - mFootLastMove.y);
        processFooterOnMove(x, y, offsetX, offsetY);
        mFootLastMove.set(x, y);
    }

    public final void onHeaderMove(float x, float y) {
        float offsetX = x - mHeaderLastMove.x;
        float offsetY = (y - mHeaderLastMove.y);
        processHeaderOnMove(x, y, offsetX, offsetY);
        mHeaderLastMove.set(x, y);
    }

    protected void processFooterOnMove(float currentX, float currentY, float offsetX, float offsetY) {
        setFooterOffset(offsetX, offsetY / mResistance);
    }

    protected void processHeaderOnMove(float currentX, float currentY, float offsetX, float offsetY) {
        setHeaderOffset(offsetX, offsetY / mResistance);
    }

    private void setHeaderOffset(float x, float y) {
        mHeaderOffsetX = x;
        mHeaderOffsetY = y;
    }

    protected void setFooterOffset(float x, float y) {
        mFootOffsetX = x;
        mFootOffsetY = y;
    }

    public float getFootOffsetX() {
        return mFootOffsetX;
    }

    public float getFootOffsetY() {
        return mFootOffsetY;
    }

    public int getOffsetToLoadmore() {
        return mOffsetToLoadmore;
    }

    public final void setCurrentFootPos(int current) {
        mLastFootPosY = mCurrentFootPosY;
        mCurrentFootPosY = current;
    }

    public final void setCurrentHeaderPos(int current) {
        mLastHeadPosY = mCurrentHeadPosY;
        mCurrentHeadPosY = current;
    }

    public int getCurrentFootPosY() {
        return mCurrentFootPosY;
    }

    public int getCurrentHeadPosY() {
        return mCurrentHeadPosY;
    }

    public int getLastFootPosY() {
        return mLastFootPosY;
    }

    public int getLastHeadPosY() {
        return mLastHeadPosY;
    }

    public boolean hasHeadLeftStartPosition() {
        return mCurrentHeadPosY > POS_START;
    }

    public boolean hasFootLeftStartPosition() {
        return mCurrentFootPosY < POS_START;
    }

    public void setOffsetToLoadMore(float offset) {
        mOffsetToLoadmore = (int) offset;
    }

    public boolean isUnderTouch() {
        return mIsUnderTouch;
    }

    public void onRelease() {
        mIsUnderTouch = false;
    }


    public boolean isOverOffsetToLoadMore() {
        Log.i(TAG, "mCurrentFootPos:" + mCurrentFootPosY + "---offsetToLoadmore:" + getOffsetToLoadmore());
        return Math.abs(mCurrentFootPosY) > Math.abs(getOffsetToLoadmore());
    }

    public int getOffsetToKeepHeaderWhileLoading() {
        return mHeaderHeight;
    }

    public void onUIRefreshComplete() {
        mRefreshCompleteY = mCurrentFootPosY;
    }

    public boolean hasFootMovedAfterPressDown() {
        return mCurrentFootPosY != mPressedPos;
    }

    public boolean willOverTop(int to) {
        return Math.abs(to) < POS_START;
    }

    public boolean isHeaderInStartPosition() {
        return mCurrentHeadPosY == POS_START;
    }

    public boolean isFooterInStartPosition() {
        return mCurrentFootPosY == POS_START;
    }

    public boolean hasFootJustBackToStartPosition() {
        return mLastFootPosY != POS_START && isFooterInStartPosition();
    }

    public boolean hasHeadJustBackToStartPosition() {
        return mLastHeadPosY != POS_START && isHeaderInStartPosition();
    }

    public void setHeadHeight(int mHeaderHeight) {
        this.mHeaderHeight = mHeaderHeight;
    }

    public void setOffsetToRefresh(int offset) {
        mOffsetToRefresh = (int) offset;
    }

    public int getOffsetToRefresh() {
        return mOffsetToRefresh;
    }

    public void onMove(float x, float y) {
        float offsetX = x - mLastMove.x;
        float offsetY = (y - mLastMove.y);
        processOnMove(x, y, offsetX, offsetY);
        mLastMove.set(x, y);
    }

    private void processOnMove(float x, float y, float offsetX, float offsetY) {
        setOffset(offsetX, offsetY / mResistance);
    }

    protected void setOffset(float x, float y) {
        mOffsetY = y;
        mOffsetX = x;
    }

    public float getOffsetY() {
        return mOffsetY;
    }

    public float getOffsetX() {
        return mOffsetX;
    }

    public boolean isOverOffsetToRefresh() {
        return Math.abs(mCurrentHeadPosY) > Math.abs(getOffsetToRefresh());
    }

    public int getOffsetToKeepFootWhileLoading() {
        return mFooterHeight;
    }

    public boolean hasHeadMovedAfterPressDown() {
        return mCurrentHeadPosY != mPressedPos;
    }

    public boolean hasHeadJustLeftStartPosition() {
        return mLastHeadPosY == POS_START && hasHeadLeftStartPosition();
    }

    public boolean hasFootJustLeftStartPosition() {
        return mLastFootPosY == POS_START && hasFootLeftStartPosition();
    }
}
