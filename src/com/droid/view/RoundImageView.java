package com.droid.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

/**
 * 自定义圆形imageview
 * 
 * @author zhaolu
 *
 */
public class RoundImageView extends ImageView {

	public RoundImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		//setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		init();
	}

	public RoundImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public RoundImageView(Context context) {
		super(context);
		init();
	}

	@TargetApi(11)
	private void init()
	{
//		if (Build.VERSION.SDK_INT >= 11)
//		{
//			setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//		}
	}
	private Path mPath = new Path();

	public interface LockScreenLayoutListener {
		public void onUnLock();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		float cx = getMeasuredWidth() / 2;
		float cy = getMeasuredHeight() / 2;
		float cr = cx < cy ? cx : cy;

		mPath.reset();
		mPath.addCircle(cx, cy, cr, Path.Direction.CCW);
		canvas.clipPath(mPath);
		super.onDraw(canvas);
	}
}
