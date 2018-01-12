package com.tcl.browser;

import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.view.ViewTreeObserver.OnWindowFocusChangeListener;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * 
 * 焦点光标，为View加上焦点光标和移动动画。
 * 
 * @author king
 *
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class FocusCursor {

	private ViewGroup mHostView;

	private Drawable mCursorDrawable;

	private ValueAnimator mAnima;

	private OnGlobalFocusChangeListener mGlobalFocusChangeListener;
 
	private TimeInterpolator mInterpolator;

	private long mAimaTime = 400;

	private OnWindowFocusChangeListener mWindowFocusChangeListener;

	private AnimatorUpdateListener mAnimationUpdataListener;

	private int offset = 10;
	
	private onPointEvaluatorChangeListener mOnPointEvaluatorChangeListener;
	
	private PointEvaluator mPointEvaluator;


	public FocusCursor(Activity ac, Drawable drawable) {
		this((ViewGroup) ac.getWindow().getDecorView(), drawable);
	}

	public FocusCursor(ViewGroup vg, Drawable drawable) {
		mHostView = vg;
		mCursorDrawable = drawable;
		init();
	}

	public void addTrackView(final ViewGroup vg) {
		
		vg.post(new Runnable() {
			
			@Override
			public void run() {
				
					if (vg instanceof AdapterView) {
			dealWithAdapterView(vg);
		} else {
			iteratorApplyView(vg);
		}
			}
		});
		
		
	
	}

	@SuppressLint("NewApi")
	public void startTrack() {
		mHostView.getOverlay().clear();
		mHostView.getOverlay().add(mCursorDrawable);
		mHostView.getViewTreeObserver().addOnGlobalFocusChangeListener(mGlobalFocusChangeListener);
		mHostView.getViewTreeObserver().addOnWindowFocusChangeListener(mWindowFocusChangeListener);
		

	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	@SuppressLint("NewApi")
	public void stop() {
		mHostView.getOverlay().remove(mCursorDrawable);
		mHostView.getViewTreeObserver().removeOnGlobalFocusChangeListener(mGlobalFocusChangeListener);
		mHostView.getViewTreeObserver().removeOnWindowFocusChangeListener(
		mWindowFocusChangeListener);
		

	}

	public void setOnPointEvaluatorChangeListener(
			onPointEvaluatorChangeListener mpe) {
		mOnPointEvaluatorChangeListener = mpe;
	}

	public void iteratorApplyView(View v) {

		if (v instanceof ViewGroup && !(v instanceof AdapterView)) {

			final int childCount = ((ViewGroup) v).getChildCount();

			for (int i = 0; i < childCount; i++) {
				iteratorApplyView(((ViewGroup) v).getChildAt(i));
			}
		} else {
			if (v instanceof AdapterView) {
				dealWithAdapterView(v);
			}

		}
	}

	@SuppressLint("NewApi")
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	private void init() {

		mWindowFocusChangeListener = new OnWindowFocusChangeListener() {

			@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
			@SuppressLint("NewApi")
			@Override
			public void onWindowFocusChanged(boolean focus) {
				if (focus) {
					mHostView.getOverlay().clear();
					mHostView.getOverlay().add(mCursorDrawable);
					mHostView.getViewTreeObserver()
							.addOnGlobalFocusChangeListener(mGlobalFocusChangeListener);

				} else {
					mHostView.getOverlay().remove(mCursorDrawable);
					mHostView.getViewTreeObserver()
							.removeOnGlobalFocusChangeListener(mGlobalFocusChangeListener);
				}

			}
		};

		mAnimationUpdataListener = new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {

				Rect rect = (Rect) animation.getAnimatedValue();
 
				mCursorDrawable.setBounds(rect);
				mCursorDrawable.invalidateSelf();
			}
		};
 
		mInterpolator = new AccelerateDecelerateInterpolator();

		mPointEvaluator = new PointEvaluator();

		iteratorApplyView(mHostView);
		
		generateMFI();
	}
	public	int[] oldLocallocalOnScreen = new int[2];
	public	int[] parentlocalOnScreen = new int[2];
	public	int[] newlocalOnScreen = new int[2];
		Rect defaultR = new Rect();
	public void onAdapterItemSelect(AdapterView<?> ihost, View target) {

		if (target == null || !ihost.isFocused()) {
			return;
		}
		//ViewGroup host = (ViewGroup)target.getParent();
		mHostView.getLocationOnScreen(parentlocalOnScreen);
		target.getLocationOnScreen(newlocalOnScreen);

		if (mAnima != null && mAnima.isRunning()) {
			mAnima.cancel();
			mRealAnimaTime = (long) (mAimaTime *(1-mAnima.getAnimatedFraction()));
			startpoint = (Rect) mAnima.getAnimatedValue();
		} else {
			mRealAnimaTime = mAimaTime;
			startpoint = mCursorDrawable.getBounds();
		}
		if (startpoint.equals(defaultR)) {
			mHostView.getLocalVisibleRect(startpoint);
		}

		endPoint.set(newlocalOnScreen[0] - parentlocalOnScreen[0] - offset, newlocalOnScreen[1]-parentlocalOnScreen[1] - offset,
				target.getMeasuredWidth() + newlocalOnScreen[0] - parentlocalOnScreen[0] + offset, target.getMeasuredHeight()
						+ newlocalOnScreen[1] - parentlocalOnScreen[1] + offset);
		startAnimation();
	}

	public void onAdapterViewFocusChange(final AdapterView<?> tmview,
			boolean hasFocus) {
		if (hasFocus) {
			final int chilcount = tmview.getChildCount();

			for (int i = 0; i < chilcount; i++) {

				final View child = tmview.getChildAt(i);

				if (child.isSelected()) {
					onAdapterItemSelect(tmview, child);
					break;
				}

			}

		}
	}

	private class OnFocusChangeListenerWrap  implements OnFocusChangeListener
	{
		private OnFocusChangeListener ofc;
		
		

		public OnFocusChangeListenerWrap(OnFocusChangeListener ofc) {
			super();
			this.ofc = ofc;
		}



		@Override
		public void onFocusChange(View arg0, boolean arg1) {
			ofc.onFocusChange(arg0, arg1);
			FocusCursor.this.onAdapterViewFocusChange((AdapterView<?>)arg0, arg1);
			
		}
		
	}
	private class OnItemSelectedListenerWrap implements OnItemSelectedListener
	{
		private OnItemSelectedListener oist;
		
		public OnItemSelectedListenerWrap(OnItemSelectedListener oist) {
			super();
			this.oist = oist;
		}

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			oist.onItemSelected(arg0, arg1, arg2, arg3);
			FocusCursor.this.onAdapterItemSelect(arg0, arg1);
			
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			
			
		}
		
	}
	
	public void dealWithAdapterView(View adpaterview) {
		final AdapterView<?> tmview = (AdapterView<?>) adpaterview;

		if (tmview.getOnFocusChangeListener() == null) {
			tmview.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View hostview, boolean hasFocus) {
					onAdapterViewFocusChange(tmview, hasFocus);
				}
			});
		}else if (!(tmview.getOnFocusChangeListener() instanceof OnFocusChangeListenerWrap)) {
			tmview.
			setOnFocusChangeListener(new OnFocusChangeListenerWrap(tmview.getOnFocusChangeListener()));
		}

		if (tmview.getOnItemSelectedListener() == null) {
			tmview.setOnItemSelectedListener(new OnItemSelectedListener() {

				@Override
				public void onItemSelected(AdapterView<?> host, View target,
						int index, long id) {

					onAdapterItemSelect(host, target);

				}
 
				@Override
				public void onNothingSelected(AdapterView<?> hostview) {
					      
				}
			});
		}else if (!(tmview.getOnItemSelectedListener() instanceof OnItemSelectedListenerWrap)) {
			tmview.setOnItemSelectedListener(new OnItemSelectedListenerWrap(tmview.getOnItemSelectedListener()));
		}
		
	}

	public Rect startpoint;
	public Rect endPoint = new Rect();;

	private View mOldFoucs;
	private View mNewFocus;
	private Rect defR = new Rect();
	
	
	private void generateMFI() {

		mGlobalFocusChangeListener = new OnGlobalFocusChangeListener() {

			@Override
			public void onGlobalFocusChanged(View oldFocus, View newFocus) {
				NotAdapterViewFocus(
						oldFocus, newFocus);

			}

			private void NotAdapterViewFocus(
					View oldFocus, View newFocus) {
			
				if ((newFocus == null)||(mOldFoucs !=null && mOldFoucs.equals(oldFocus))  
						&&(mNewFocus != null && mNewFocus.equals(newFocus))) return;
				
				mOldFoucs = oldFocus;
				mNewFocus = newFocus;

				if (oldFocus == null) {
					oldFocus = mHostView;
					
				}
				
				oldFocus.getLocationOnScreen(oldLocallocalOnScreen);
				
				newFocus.getLocationOnScreen(newlocalOnScreen);
				mHostView.getLocationOnScreen(parentlocalOnScreen);

				endPoint.set(newlocalOnScreen[0] - parentlocalOnScreen[0] - offset,
						newlocalOnScreen[1] - parentlocalOnScreen[1] - offset, newlocalOnScreen[0]
								- parentlocalOnScreen[0] + newFocus.getWidth() + offset,
						offset + newlocalOnScreen[1] - parentlocalOnScreen[1]
								+ newFocus.getHeight());

				
				
				if (mAnima != null && mAnima.isRunning()) {
					startpoint = (Rect) mAnima.getAnimatedValue();
					mRealAnimaTime = (long) (mAimaTime *(1-mAnima.getAnimatedFraction()));
					mAnima.cancel();
					mAnima = null;
				} else {
					mRealAnimaTime = mAimaTime;
					startpoint = mCursorDrawable.getBounds();
				}

				if (startpoint.equals(defR)) {
					mHostView.getLocalVisibleRect(startpoint);
				}
				startAnimation();

			}

		};
	}
	
	
	private Rect customPstart;
	private Rect customPend;
	
 public void setPoint(Rect start,Rect end){
	 
	 if (start != null) {
		 customPstart = start;
	}
	 if (end != null) {
		 customPend = end;
	}
 }
	
	
	public void startAnimation() {
		
		if (customPstart != null) {
			startpoint.set(customPstart);
			customPstart = null;
		}
		if (customPend != null) {
			endPoint.set(customPend);
			customPend = null;
		}
		mAnima = ObjectAnimator.ofObject(mPointEvaluator, startpoint, endPoint);
		mAnima.addUpdateListener(mAnimationUpdataListener);
		mAnima.setDuration(mRealAnimaTime);
		mAnima.setInterpolator(mInterpolator);
		mAnima.start();
	}

	public TimeInterpolator getTimeInterpolator() {
		return mInterpolator;
	}

	public void setTimeInterpolator(TimeInterpolator mIp) {
		this.mInterpolator = mIp;
	}

	public long getAimaTime() {
		return mAimaTime;
	}

	public void setAimaTime(long mAimaTime) {
		this.mAimaTime = mAimaTime;
	}

	interface onPointEvaluatorChangeListener {
		Rect onPointEvaluatorChange(float fraction, Rect startPoint,
				Rect endPoint);
	}
	
	private long mRealAnimaTime;
	

	private class PointEvaluator implements TypeEvaluator<Object> {
		private Rect result = new Rect();

		@Override
		public Object evaluate(float fraction, Object startValue,
				Object endValue) {
			Rect startPoint = (Rect) startValue;
			Rect endPoint = (Rect) endValue;

			if (mOnPointEvaluatorChangeListener != null
					&& (result = mOnPointEvaluatorChangeListener.onPointEvaluatorChange(fraction,
							startPoint, endPoint)) != null) {
				return result;
			}

			final float offsetleft = endPoint.left - startPoint.left;
			final float offsetRight = endPoint.right - startPoint.right;
			final float offsetTop = endPoint.top - startPoint.top;
			final float offsetButtom = endPoint.bottom - startPoint.bottom;

			result.set((int) (startPoint.left + fraction * offsetleft),
					(int) (startPoint.top + fraction * offsetTop),
					(int) (startPoint.right + fraction * offsetRight),
					(int) (startPoint.bottom + fraction * offsetButtom));

			return result;
		}

	}

}
