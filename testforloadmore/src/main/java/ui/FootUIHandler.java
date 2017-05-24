package ui;

import assit.FootAndHeaderIndicator;

/**
 * Created by jackypeng on 2017/5/20.
 */

interface FootUIHandler {
     void onUIPositionChange(LoadmoreLayout frame, boolean isUnderTouch, byte status, FootAndHeaderIndicator ptrIndicator);

     void onUILoadMorePrepare(LoadmoreLayout frame);

     void onUILoadMoreBegin(LoadmoreLayout frame);

     void onUILoadMoreCompleted(LoadmoreLayout frame);

     void onUIReset(LoadmoreLayout loadmoreLayout);
}
