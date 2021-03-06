package com.tlulybluemonochrome.minimarurss;

import java.util.HashMap;
import java.util.LinkedHashMap;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class EfectViewPager extends ViewPager {

	private static final boolean mEnabled = true;
	private static final boolean mFadeEnabled = false;
	public static final int sOutlineColor = Color.WHITE;
	private static int mEffect = 0;

	private static final HashMap<Integer, Object> mObjs = new LinkedHashMap<Integer, Object>();

	private static final float SCALE_MAX = 0.5f;
	private static final float ZOOM_MAX = 0.5f;
	private static final float ROT_MAX = 15.0f;

	/*
	 * public static enum TransitionEffect { Standard,0 Tablet,1 CubeIn,2
	 * CubeOut,3 FlipVertical,4 FlipHorizontal,5 Stack,6 ZoomIn,7 ZoomOut,8
	 * RotateUp,9 RotateDown,10 Accordion11 }
	 */

	public EfectViewPager(final Context context) {
		this(context, null);
	}

	public EfectViewPager(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		setClipChildren(false);
		// TypedArray ta = context.obtainStyledAttributes(attrs,
		// R.styleable.JazzyViewPager);
		// ta.recycle();
	}

	public static void setTransitionEffect(final int effect) {
		mEffect = effect;
		// reset();
	}

	private View wrapChild(final View child) {
		return child;
	}

	public void addView(final View child) {
		super.addView(wrapChild(child));
	}

	public void addView(final View child, final int index) {
		super.addView(wrapChild(child), index);
	}

	public void addView(final View child, final LayoutParams params) {
		super.addView(wrapChild(child), params);
	}

	public void addView(final View child, final int width, final int height) {
		super.addView(wrapChild(child), width, height);
	}

	public void addView(final View child, final int index,
			final LayoutParams params) {
		super.addView(wrapChild(child), index, params);
	}

	@Override
	public boolean onInterceptTouchEvent(final MotionEvent arg0) {
		return mEnabled ? super.onInterceptTouchEvent(arg0) : false;
	}

	private static int mState;// 0 IDLE,1 LEFT,2 RIGTH
	private int oldPage;

	// private View mLeft;
	// private View mRight;
	// private static float mRot;
	// private static float mTrans;
	// private static float mScale;

	/*
	 * private static enum State { IDLE,0 GOING_LEFT,1 GOING_RIGHT2 }
	 */

	protected static void animateTablet(final View left, final View right,
			final float positionOffset) {
		if (mState != 0) {
			if (left != null) {
				manageLayer(left, true);
				final float mRot = 30.0f * positionOffset;
				final float mTrans = getOffsetXForRotation(mRot,
						left.getMeasuredWidth(), left.getMeasuredHeight());
				left.setPivotX(left.getMeasuredWidth() / 2);
				left.setPivotY(left.getMeasuredHeight() / 2);
				left.setTranslationX(mTrans);
				left.setRotationY(mRot);
				// logState(left, "Left");
			}
			if (right != null) {
				manageLayer(right, true);
				final float mRot = -30.0f * (1 - positionOffset);
				final float mTrans = getOffsetXForRotation(mRot,
						right.getMeasuredWidth(), right.getMeasuredHeight());
				right.setPivotX(right.getMeasuredWidth() * 0.5f);
				right.setPivotY(right.getMeasuredHeight() * 0.5f);
				right.setTranslationX(mTrans);
				right.setRotationY(mRot);
				// logState(right, "Right");
			}
		}
	}

	private static void animateCube(final View left, final View right,
			final float positionOffset, final boolean in) {
		if (mState != 0) {
			if (left != null) {
				manageLayer(left, true);
				final float mRot = (in ? 90.0f : -90.0f) * positionOffset;
				left.setPivotX(left.getMeasuredWidth());
				left.setPivotY(left.getMeasuredHeight() * 0.5f);
				left.setRotationY(mRot);
			}
			if (right != null) {
				manageLayer(right, true);
				final float mRot = -(in ? 90.0f : -90.0f)
						* (1 - positionOffset);
				right.setPivotX(0);
				right.setPivotY(right.getMeasuredHeight() * 0.5f);
				right.setRotationY(mRot);
			}
		}
	}

	private static void animateAccordion(final View left, final View right,
			final float positionOffset) {
		if (mState != 0) {
			if (left != null) {
				manageLayer(left, true);
				left.setPivotX(left.getMeasuredWidth());
				left.setPivotY(0);
				left.setScaleX(1 - positionOffset);
			}
			if (right != null) {
				manageLayer(right, true);
				right.setPivotX(0);
				right.setPivotY(0);
				right.setScaleX(positionOffset);
			}
		}
	}

	private static void animateZoom(final View left, final View right,
			final float positionOffset, final boolean in) {
		if (mState != 0) {
			if (left != null) {
				manageLayer(left, true);
				final float mScale = in ? ZOOM_MAX + (1 - ZOOM_MAX)
						* (1 - positionOffset) : 1 + ZOOM_MAX - ZOOM_MAX
						* (1 - positionOffset);
				left.setPivotX(left.getMeasuredWidth() * 0.5f);
				left.setPivotY(left.getMeasuredHeight() * 0.5f);
				left.setScaleX(mScale);
				left.setScaleY(mScale);
			}
			if (right != null) {
				manageLayer(right, true);
				final float mScale = in ? ZOOM_MAX + (1 - ZOOM_MAX)
						* positionOffset : 1 + ZOOM_MAX - ZOOM_MAX
						* positionOffset;
				right.setPivotX(right.getMeasuredWidth() * 0.5f);
				right.setPivotY(right.getMeasuredHeight() * 0.5f);
				right.setScaleX(mScale);
				right.setScaleY(mScale);
			}
		}
	}

	private static void animateRotate(final View left, final View right,
			final float positionOffset, final boolean up, final int height) {
		if (mState != 0) {
			if (left != null) {
				manageLayer(left, true);
				final float mRot = (up ? 1 : -1) * (ROT_MAX * positionOffset);
				final float mTrans = (up ? -1 : 1)
						* (float) (height - height
								* Math.cos(mRot * Math.PI / 180.0f));
				left.setPivotX(left.getMeasuredWidth() * 0.5f);
				left.setPivotY(up ? 0 : left.getMeasuredHeight());
				left.setTranslationY(mTrans);
				left.setRotation(mRot);
			}
			if (right != null) {
				manageLayer(right, true);
				final float mRot = (up ? 1 : -1)
						* (-ROT_MAX + ROT_MAX * positionOffset);
				final float mTrans = (up ? -1 : 1)
						* (float) (height - height
								* Math.cos(mRot * Math.PI / 180.0f));
				right.setPivotX(right.getMeasuredWidth() * 0.5f);
				right.setPivotY(up ? 0 : right.getMeasuredHeight());
				right.setTranslationY(mTrans);
				right.setRotation(mRot);
			}
		}
	}

	private static void animateFlipHorizontal(final View left,
			final View right, final float positionOffset,
			final int positionOffsetPixels, final int width, final int margin) {
		if (mState != 0) {
			if (left != null) {
				manageLayer(left, true);
				final float mRot = 180.0f * positionOffset;
				if (mRot > 90.0f) {
					left.setVisibility(View.INVISIBLE);
				} else {
					if (left.getVisibility() == View.INVISIBLE)
						left.setVisibility(View.VISIBLE);
					final float mTrans = positionOffsetPixels;
					left.setPivotX(left.getMeasuredWidth() * 0.5f);
					left.setPivotY(left.getMeasuredHeight() * 0.5f);
					left.setTranslationX(mTrans);
					left.setRotationY(mRot);
				}
			}
			if (right != null) {
				manageLayer(right, true);
				final float mRot = -180.0f * (1 - positionOffset);
				if (mRot < -90.0f) {
					right.setVisibility(View.INVISIBLE);
				} else {
					if (right.getVisibility() == View.INVISIBLE)
						right.setVisibility(View.VISIBLE);
					final float mTrans = -width - margin + positionOffsetPixels;
					right.setPivotX(right.getMeasuredWidth() * 0.5f);
					right.setPivotY(right.getMeasuredHeight() * 0.5f);
					right.setTranslationX(mTrans);
					right.setRotationY(mRot);
				}
			}
		}
	}

	private static void animateFlipVertical(final View left, final View right,
			final float positionOffset, final int positionOffsetPixels,
			final int width, final int margin) {
		if (mState != 0) {
			if (left != null) {
				manageLayer(left, true);
				final float mRot = 180.0f * positionOffset;
				if (mRot > 90.0f) {
					left.setVisibility(View.INVISIBLE);
				} else {
					if (left.getVisibility() == View.INVISIBLE)
						left.setVisibility(View.VISIBLE);
					final float mTrans = positionOffsetPixels;
					left.setPivotX(left.getMeasuredWidth() * 0.5f);
					left.setPivotY(left.getMeasuredHeight() * 0.5f);
					left.setTranslationX(mTrans);
					left.setRotationX(mRot);
				}
			}
			if (right != null) {
				manageLayer(right, true);
				final float mRot = -180.0f * (1 - positionOffset);
				if (mRot < -90.0f) {
					right.setVisibility(View.INVISIBLE);
				} else {
					if (right.getVisibility() == View.INVISIBLE)
						right.setVisibility(View.VISIBLE);
					final float mTrans = -width - margin + positionOffsetPixels;
					right.setPivotX(right.getMeasuredWidth() * 0.5f);
					right.setPivotY(right.getMeasuredHeight() * 0.5f);
					right.setTranslationX(mTrans);
					right.setRotationX(mRot);
				}
			}
		}
	}

	protected static void animateStack(final View left, final View right,
			final float positionOffset, final int positionOffsetPixels,
			final int width, final int margin) {
		if (mState != 0) {
			if (right != null) {
				manageLayer(right, true);
				final float mScale = (1 - SCALE_MAX) * positionOffset
						+ SCALE_MAX;
				final float mTrans = -width - margin + positionOffsetPixels;
				right.setScaleX(mScale);
				right.setScaleY(mScale);
				right.setTranslationX(mTrans);
			}
		}
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void manageLayer(final View v, final boolean enableHardware) {
		// if (!API_11) return;
		int layerType = enableHardware ? View.LAYER_TYPE_HARDWARE
				: View.LAYER_TYPE_NONE;
		if (layerType != v.getLayerType())
			v.setLayerType(layerType, null);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void disableHardwareLayer() {
		// if (!API_11) return;
		View v;
		for (int i = 0; i < getChildCount(); i++) {
			v = getChildAt(i);
			if (v.getLayerType() != View.LAYER_TYPE_NONE)
				v.setLayerType(View.LAYER_TYPE_NONE, null);
		}
	}

	private static final Matrix mMatrix = new Matrix();
	private static final Camera mCamera = new Camera();
	private static final float[] mTempFloat2 = new float[2];

	protected static float getOffsetXForRotation(final float degrees,
			final int width, final int height) {
		mMatrix.reset();
		mCamera.save();
		mCamera.rotateY(Math.abs(degrees));
		mCamera.getMatrix(mMatrix);
		mCamera.restore();

		mMatrix.preTranslate(-width * 0.5f, -height * 0.5f);
		mMatrix.postTranslate(width * 0.5f, height * 0.5f);
		mTempFloat2[0] = width;
		mTempFloat2[1] = height;
		mMatrix.mapPoints(mTempFloat2);
		return (width - mTempFloat2[0]) * (degrees > 0.0f ? 1.0f : -1.0f);
	}

	protected static void animateFade(final View left, final View right,
			final float positionOffset) {
		if (left != null) {
			left.setAlpha(1 - positionOffset);
		}
		if (right != null) {
			right.setAlpha(positionOffset);
		}
	}

	@Override
	public void onPageScrolled(final int position, final float positionOffset,
			final int positionOffsetPixels) {
		if (mState == 0 && positionOffset > 0) {
			oldPage = getCurrentItem();
			mState = position == oldPage ? 2 : 1;
		}
		final boolean goingRight = position == oldPage;
		if (mState == 2 && !goingRight)
			mState = 1;
		else if (mState == 1 && goingRight)
			mState = 2;

		final float effectOffset = isSmall(positionOffset) ? 0 : positionOffset;

		// mLeft = getChildAt(position);
		// mRight = getChildAt(position+1);
		final View mLeft = findViewFromObject(position);
		final View mRight = findViewFromObject(position + 1);

		if (mFadeEnabled)
			animateFade(mLeft, mRight, effectOffset);

		switch (mEffect) {
		case 0:// Standard:
			break;
		case 1:// Tablet:
			animateTablet(mLeft, mRight, effectOffset);
			break;
		case 2:// CubeIn:
			animateCube(mLeft, mRight, effectOffset, true);
			break;
		case 3:// CubeOut:
			animateCube(mLeft, mRight, effectOffset, false);
			break;
		case 4:// FlipVertical:
			animateFlipVertical(mLeft, mRight, positionOffset,
					positionOffsetPixels, getWidth(), getPageMargin());
			break;
		case 5:// FlipHorizontal:
			animateFlipHorizontal(mLeft, mRight, effectOffset,
					positionOffsetPixels, getWidth(), getPageMargin());
		case 6:// Stack:
			animateStack(mLeft, mRight, effectOffset, positionOffsetPixels,
					getWidth(), getPageMargin());
			break;
		case 7:// ZoomIn:
			animateZoom(mLeft, mRight, effectOffset, true);
			break;
		case 8:// ZoomOut:
			animateZoom(mLeft, mRight, effectOffset, false);
			break;
		case 9:// RotateUp:
			animateRotate(mLeft, mRight, effectOffset, true,
					getMeasuredHeight());
			break;
		case 10:// RotateDown:
			animateRotate(mLeft, mRight, effectOffset, false,
					getMeasuredHeight());
			break;
		case 11:// Accordion:
			animateAccordion(mLeft, mRight, effectOffset);
			break;
		}

		super.onPageScrolled(position, positionOffset, positionOffsetPixels);

		if (effectOffset == 0) {
			disableHardwareLayer();
			mState = 0;
		}

	}

	private static boolean isSmall(final float positionOffset) {
		return false; // Math.abs(positionOffset) < 0.0001;
	}

	public static void setObjectForPosition(final Object obj, final int position) {
		mObjs.put(position, obj);
	}

	private View findViewFromObject(final int position) {
		final Object o = mObjs.get(position);
		if (o != null) {
			final int j = getChildCount();
			final PagerAdapter a = getAdapter();
			for (int i = 0; i < j; i++) {
				if (a.isViewFromObject(getChildAt(i), o))
					return getChildAt(i);
			}
		}
		return null;
	}

}
