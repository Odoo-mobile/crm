package com.odoo.widgets.bottomsheet;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.odoo.widgets.bottomsheet.BottomSheetListeners.OnSheetActionClickListener;
import com.odoo.widgets.bottomsheet.BottomSheetListeners.OnSheetItemClickListener;
import com.odoo.widgets.bottomsheet.BottomSheetListeners.OnSheetMenuCreateListener;

/**
 * Bottom Sheet Library
 * 
 * @author Dharmang Soni (dpr@odoo.com)
 * 
 */
public class BottomSheet extends RelativeLayout {

	public static final String TAG = BottomSheet.class.getSimpleName();
	private Context mContext;
	private Builder mBuilder;
	private Menu mMenu;
	private Boolean mShowing = false;
	private Boolean mIsDismissing = false;
	private OnSheetItemClickListener mItemListener = null;
	private OnSheetMenuCreateListener mOnSheetMenuCreateListener = null;

	public BottomSheet(Context context) {
		super(context);
		mContext = context;
	}

	public BottomSheet setBuilder(Builder builder) {
		mBuilder = builder;
		mMenu = new MenuBuilder(mContext);
		MenuInflater inflator = ((Activity) mContext).getMenuInflater();
		inflator.inflate(mBuilder.getSheetMenu(), mMenu);
		if (mBuilder.getOnSheetMenuCreateListener() != null) {
			mOnSheetMenuCreateListener = mBuilder
					.getOnSheetMenuCreateListener();
			mOnSheetMenuCreateListener.onSheetMenuCreate(mMenu,
					mBuilder.getExtraData());
		}
		mItemListener = mBuilder.getSheetItemListener();
		return this;
	}

	public boolean isShowing() {
		return mShowing;
	}

	public void show() {
		init(mContext);
	}

	public void dismiss() {
		if (mIsDismissing) {
			return;
		}
		Animation slideOut = AnimationUtils.loadAnimation(getContext(),
				R.anim.sheet_out);
		slideOut.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				mIsDismissing = true;
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				post(new Runnable() {
					@Override
					public void run() {
						finish();
					}
				});
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		startAnimation(slideOut);
	}

	private void finish() {
		clearAnimation();
		ViewGroup parent = (ViewGroup) getParent();
		if (parent != null) {
			parent.removeView(this);
			parent.removeView(parent.findViewById(R.id.sheet_overlay));
		}
		mShowing = false;
		mIsDismissing = false;
	}

	private void prepareMenus(RelativeLayout layout) {
		LinearLayout sheet_view = (LinearLayout) layout
				.findViewById(R.id.sheet_rows);
		sheet_view.removeAllViews();
		int menus = mMenu.size();
        int columns = 3;
		int rows = (menus <= columns) ? 1 : ((int) Math.floor(menus / columns))
				+ ((menus % columns != 0) ? 1 : 0);

		int index = 0;
		for (int i = 0; i < rows; i++) {
			LinearLayout row_view = (LinearLayout) LayoutInflater
					.from(mContext).inflate(R.layout.sheet_row, sheet_view,
							false);
			for (int j = 0; j < 3; j++) {
				if (index < mMenu.size())
					row_view.addView(getMenuView(mMenu.getItem(index), row_view));
				else
					row_view.addView(getDummyMenuView(row_view));
				index++;
			}
			sheet_view.addView(row_view);
		}
	}

	private View getDummyMenuView(ViewGroup parent) {
		LinearLayout view = (LinearLayout) LayoutInflater.from(mContext)
				.inflate(R.layout.sheet_row_item, parent, false);
		view.setVisibility(View.INVISIBLE);
		return view;
	}

	private View getMenuView(final MenuItem item, ViewGroup parent) {
		LinearLayout view = (LinearLayout) LayoutInflater.from(mContext)
				.inflate(R.layout.sheet_row_item, parent, false);
		ImageView menu_icon = (ImageView) view.findViewById(R.id.menu_icon);
		TextView menu_title = (TextView) view.findViewById(R.id.menu_title);
		menu_icon.setImageDrawable(item.getIcon());
		menu_title.setText(item.getTitle());

		menu_title.setTextColor(mBuilder.getTextColor());
		menu_icon.setColorFilter(mBuilder.getIconColor());

		if (mItemListener != null) {
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					new Handler().postDelayed(new Runnable() {

						@Override
						public void run() {
							mItemListener.onItemClick(BottomSheet.this, item,
									mBuilder.getExtraData());
						}
					}, 100);
				}
			});
		}
		return view;
	}

	private void prepareTitle(RelativeLayout layout) {
		String title = mBuilder.getSheetTitle();
		final OnSheetActionClickListener actionListener = mBuilder
				.getActionListener();
		TextView sheet_title = (TextView) layout.findViewById(R.id.sheet_title);
		ImageView sheet_action = (ImageView) layout
				.findViewById(R.id.sheet_action);
		if (actionListener != null) {
			sheet_action.setVisibility(View.VISIBLE);
			sheet_action.setColorFilter(mBuilder.getIconColor());
			if (mBuilder.getActionIcon() != 0) {
				sheet_action.setImageResource(mBuilder.getActionIcon());
			} else {
				sheet_action.setImageResource(R.drawable.ic_launcher);
			}
			sheet_action.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					actionListener.onSheetActionClick(BottomSheet.this,
							mBuilder.getExtraData());
				}
			});
		} else {
			sheet_action.setVisibility(View.INVISIBLE);
		}
		if (title != null) {

			layout.findViewById(R.id.sheet_title_view).setVisibility(
					View.VISIBLE);
			layout.findViewById(R.id.sheet_title_divider).setVisibility(
					View.VISIBLE);
			sheet_title.setText(title);
			sheet_title.setTextColor(mBuilder.getTextColor());
		} else {
			layout.findViewById(R.id.sheet_title_view).setVisibility(View.GONE);
			layout.findViewById(R.id.sheet_title_divider).setVisibility(
					View.GONE);
		}
	}

	private void init(Context context) {

		RelativeLayout layout = (RelativeLayout) LayoutInflater.from(context)
				.inflate(R.layout.sheet, this, true);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				getScreenWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
		layout.setLayoutParams(params);

		prepareTitle(layout);
		prepareMenus(layout);
		// Showing
		ViewGroup root = (ViewGroup) ((Activity) context)
				.findViewById(android.R.id.content);
		FrameLayout.LayoutParams frame_params = new FrameLayout.LayoutParams(
				getScreenWidth(), ViewGroup.LayoutParams.WRAP_CONTENT);
		frame_params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

		// Adding overlay
		FrameLayout overlay = (FrameLayout) LayoutInflater.from(context)
				.inflate(R.layout.sheet_overlay, root, false);
		root.addView(overlay);
		overlay.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		// adding sheet
		root.addView(this, frame_params);

		mShowing = true;

		Animation slideIn = AnimationUtils.loadAnimation(getContext(),
				R.anim.sheet_in);
		slideIn.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
		startAnimation(slideIn);
	}

	/**
	 * Bottom Sheet Builder
	 * 
	 * @author Dharmang Soni (dpr@odoo.com)
	 * 
	 */
	public static class Builder {
		private Context mContext;
		private Resources mRes;

		private String mSheetTitle = null;
		private Integer mSheetMenu;
		private OnSheetItemClickListener mItemListener = null;
		private OnSheetActionClickListener mOnSheetActionClickListener = null;
		private OnSheetMenuCreateListener mOnSheetMenuCreateListener = null;
		private Integer textColor = Color.BLACK;
		private Integer iconColor = Color.BLACK;
		private Object data = null;
		private Integer actionIcon = 0;

		public Builder(Context context) {
			mContext = context;
			mRes = mContext.getResources();
		}

		public Builder title(int res_id) {
			try {
				title(mRes.getString(res_id));
			} catch (Exception e) {
				Log.e(TAG, e.getMessage());
			}
			return this;
		}

		public Builder setData(Object extra_data) {
			data = extra_data;
			return this;
		}

		public Builder setActionIcon(int res_id) {
			actionIcon = res_id;
			return this;
		}

		public int getActionIcon() {
			return actionIcon;
		}

		public Object getExtraData() {
			return data;
		}

		public Builder title(CharSequence title) {
			mSheetTitle = title.toString();
			return this;
		}

		public Builder actionListener(OnSheetActionClickListener listener) {
			mOnSheetActionClickListener = listener;
			return this;
		}

		public OnSheetActionClickListener getActionListener() {
			return mOnSheetActionClickListener;
		}

		public Builder setIconColor(int color) {
			iconColor = color;
			return this;
		}

		public Builder setTextColor(int color) {
			textColor = color;
			return this;
		}

		public int getTextColor() {
			return textColor;
		}

		public int getIconColor() {
			return iconColor;
		}

		public Builder menu(int menu_res_id) {
			mSheetMenu = menu_res_id;
			return this;
		}

		public int getSheetMenu() {
			return mSheetMenu;
		}

		public Builder listener(
				BottomSheetListeners.OnSheetItemClickListener listener) {
			mItemListener = listener;
			return this;
		}

		public BottomSheetListeners.OnSheetItemClickListener getSheetItemListener() {
			return mItemListener;
		}

		public String getSheetTitle() {
			return mSheetTitle;
		}

		public BottomSheet create() {
			return new BottomSheet(mContext).setBuilder(this);
		}

		public Builder setOnSheetMenuCreateListener(
				OnSheetMenuCreateListener listener) {
			mOnSheetMenuCreateListener = listener;
			return this;
		}

		public OnSheetMenuCreateListener getOnSheetMenuCreateListener() {
			return mOnSheetMenuCreateListener;
		}
	}

	private int getScreenWidth() {
		WindowManager wm = (WindowManager) mContext
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point point = new Point();
		display.getSize(point);
		int orientation = mContext.getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			return point.y;
		}
		return point.x;
	}
}
