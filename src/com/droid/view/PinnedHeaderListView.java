package com.droid.view;


import com.droid.PinnedHeaderAdapter;
import com.droid.R;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

public class PinnedHeaderListView extends ListView {


	private PinnedHeaderAdapter mAdapter;
	private View mHeaderView;
	private boolean mHeaderViewVisible;
	private int mHeaderViewWidth;
	private int mHeaderViewHeight;

	public PinnedHeaderListView(Context context) {
		super(context);
	}

	public PinnedHeaderListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PinnedHeaderListView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mHeaderView != null) {
            mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
            configureHeaderView(getFirstVisiblePosition());
        }
    }
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mHeaderView != null) {
            measureChild(mHeaderView, widthMeasureSpec, heightMeasureSpec);
            mHeaderViewWidth = mHeaderView.getMeasuredWidth();
            mHeaderViewHeight = mHeaderView.getMeasuredHeight();
        }
    }
	public void setPinnedHeaderView(View view) {
		mHeaderView = view;
		if (mHeaderView != null) {
			setFadingEdgeLength(0);
		}
		requestLayout();//当view确定自身已经不再适合现有的区域时，该view本身调用这个方法要求parent view重新调用他的onMeasure onLayout来对重新设置自己位置
	}
    public void setAdapter(ListAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = (PinnedHeaderAdapter)adapter;
    }
 
	public void configureHeaderView(int position) {
		if (mHeaderView == null || mAdapter==null) {
			return;
		}
		
		/*position根据开通啪啪奇的好友数量确定*/
		if(position==2){
			int t=getChildAt(0).getTop();
			if(t<0 && -1*t>=mHeaderView.getHeight())mHeaderViewVisible = true;
			else{
				mHeaderViewVisible = false;
				return;
			}
		}
		/*position 根据开通啪啪奇的好友数量确定*/
		
		int state = mAdapter.getPinnedHeaderState(position);
		
		switch (state) {
		case PinnedHeaderAdapter.PINNED_HEADER_GONE: {
			Log.i("fuck", "gone");
			mHeaderViewVisible = false;
			break;
		}

		case PinnedHeaderAdapter.PINNED_HEADER_VISIBLE: {
	
			mAdapter.configurePinnedHeader(mHeaderView, position);
			
			if (mHeaderView.getTop() != 0) {
				mHeaderView.layout(0, 0, mHeaderViewWidth, mHeaderViewHeight);
			}
			mHeaderViewVisible = true;
			break;
		}

		case PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP: {

			View firstView = getChildAt(0);
			int bottom = firstView.getBottom();
			int headerHeight = mHeaderView.getHeight();
			int y;
			if (bottom < headerHeight) {
				y = (bottom - headerHeight);
			} else {
				y = 0;
			}
			mAdapter.configurePinnedHeader(mHeaderView, position);
			if (mHeaderView.getTop() != y) {
				mHeaderView.layout(0, y, mHeaderViewWidth, mHeaderViewHeight
						+ y);
			}
			mHeaderViewVisible = true;
			break;
		}
		}
	}
	
	//onDraw()方法绘制视图本身  ,dispatchDraw ()方法绘制子视图
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHeaderViewVisible) {
            drawChild(canvas, mHeaderView, getDrawingTime());
        }
    }
    
}
