package assit;

import android.view.View;
import android.widget.AbsListView;

import ui.LoadmoreLayout;
import ui.PtlHeaderHandler;

/**
 * Created by pj on 2017/5/24.
 */
public abstract class PtlDefaultHeadHandler implements PtlHeaderHandler {
    @Override
    public boolean checkCanDoRefresh(LoadmoreLayout frame, View content, View header) {
        return checkContentCanBePullDown(frame, content, header);
    }

    public static boolean checkContentCanBePullDown(LoadmoreLayout frame, View content, View header) {
        return !canChildScrollUp(content);
    }

    public static boolean canChildScrollUp(View view) {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (view instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) view;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return view.getScrollY() > 0;
            }
        } else {
            return view.canScrollVertically(-1);
        }
    }
}
