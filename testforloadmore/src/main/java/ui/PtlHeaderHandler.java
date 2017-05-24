package ui;

import android.view.View;

/**
 * Created by pj on 2017/5/23.
 */
public interface PtlHeaderHandler {
    void onRefreshBegin(LoadmoreLayout frame);

    boolean checkCanDoRefresh(final LoadmoreLayout frame, final View content, final View header);
}
