package com.beesham.popularmovies;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by Beesham on 10/12/2016.
 * Expands the trailers/reviews list to match the item count
 * preserving the listview mechanics
 */

public class ExpandedListView extends ListView {
    private android.view.ViewGroup.LayoutParams params;
    private int oldCount = 0;

    public ExpandedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(getCount() != oldCount){
            oldCount = getCount();
            params = getLayoutParams();
            params.height = getCount() * (oldCount > 0 ? getChildAt(0).getHeight() : 0);
            setLayoutParams(params);
        }

        super.onDraw(canvas);
    }
}
