package ui;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by pj on 2017/5/22.
 */
public class ClassicRefreshAndLoadMoreLayout extends LoadmoreLayout {

    private ClassicFootView classicFootView;
    private ClassicHeaderView classicHeaderView;

    public ClassicRefreshAndLoadMoreLayout(Context context) {
        this(context, null);
    }

    public ClassicRefreshAndLoadMoreLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClassicRefreshAndLoadMoreLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initViews();
    }

    private void initViews() {
        classicFootView = new ClassicFootView(getContext());
        classicHeaderView = new ClassicHeaderView(getContext());
        setFootView(classicFootView);
        addFootUIHandler(classicFootView);

        setHeaderView(classicHeaderView);
        addHeaderUIHandler(classicHeaderView);

    }

}
