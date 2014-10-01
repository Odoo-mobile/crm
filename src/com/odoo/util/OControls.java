package com.odoo.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.odoo.crm.R;

public class OControls {

	public static void setText(View parent_view, int textview_id, Object value) {
		TextView textView = (TextView) parent_view.findViewById(textview_id);
		if (value instanceof String || value instanceof CharSequence)
			textView.setText(value.toString());
		if (value instanceof Integer)
			textView.setText(Integer.parseInt(value.toString()));
	}

	public static String getText(View parent_view, int textview_id) {
		TextView textView = (TextView) parent_view.findViewById(textview_id);
		return textView.getText().toString();
	}

	public static void toggleViewVisibility(View parent_view, int view_id,
			Boolean visible) {
		int view_visibility = (visible) ? View.VISIBLE : View.GONE;
		parent_view.findViewById(view_id).setVisibility(view_visibility);

	}

	public static void setImage(View parent_view, int imageview_id,
			Bitmap bitmap) {
		ImageView imgView = (ImageView) parent_view.findViewById(imageview_id);
		imgView.setImageBitmap(bitmap);
	}

	public static void setImage(View parent_view, int imageview_id,
			int drawable_id) {
		ImageView imgView = (ImageView) parent_view.findViewById(imageview_id);
		imgView.setImageResource(drawable_id);
	}

	public static void setVisible(View parent_view, int resource_id) {
		OControls.setVisible(parent_view, resource_id, false, null);
	}

	public static void setVisible(View parent_view, int resource_id,
			boolean animate, Context context) {
		View view = parent_view.findViewById(resource_id);
		if (!animate) {
			view.setVisibility(View.VISIBLE);
		} else {
			OControls.slideInFromRight(parent_view, resource_id, 500, context);
		}
	}

	public static void slideInFromRight(View parent_view, int resource_id,
			long duration, Context context) {
		View view = parent_view.findViewById(resource_id);
		view.setVisibility(View.VISIBLE);
		Animation animate = AnimationUtils.loadAnimation(context,
				R.anim.slide_in_from_left);
		view.startAnimation(animate);

	}

	public static void slideOutToRight(View parent_view, int resource_id,
			long duration, Context context) {
		View view = parent_view.findViewById(resource_id);
		view.setVisibility(View.VISIBLE);
		Animation animate = AnimationUtils.loadAnimation(context,
				R.anim.slide_in_from_left);
		view.startAnimation(animate);

	}

	public static void setInvisible(View parent_view, int resource_id) {
		parent_view.findViewById(resource_id).setVisibility(View.INVISIBLE);
	}

	public static void setGone(View parent_view, int resource_id) {
		OControls.setGone(parent_view, resource_id, false, null);
	}

	public static void setGone(View parent_view, int resource_id,
			boolean animate, Context context) {
		View view = parent_view.findViewById(resource_id);
		if (!animate) {
			view.setVisibility(View.GONE);
		} else {
			OControls.slideOutToRight(parent_view, resource_id, 500, context);
		}
	}

}
