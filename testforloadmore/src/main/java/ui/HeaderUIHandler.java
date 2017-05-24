package ui;

import assit.FootAndHeaderIndicator;

/**
 * Created by pj on 2017/5/23.
 */
public interface HeaderUIHandler {
    void onUIPositionChange(LoadmoreLayout frame, boolean isUnderTouch, byte status, FootAndHeaderIndicator ptrIndicator);

    void onUIRefreshPrepare(LoadmoreLayout frame);

    void onUIRefreshBegin(LoadmoreLayout frame);

    void onUIRefreshCompleted(LoadmoreLayout frame);

    void onUIReset(LoadmoreLayout loadmoreLayout);
}
