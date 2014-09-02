package com.odoo.addons.crm.receivers;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.odoo.addons.crm.model.CRMLead;
import com.odoo.crm.R;
import com.odoo.orm.ODataRow;
import com.odoo.util.Base64Helper;
import com.odoo.util.OControls;

public class CRMCallerWindow implements OnTouchListener {

	WindowManager wm = null;
	Context mContext = null;
	View myView = null;
	Boolean mActiveWindow = false;

	public CRMCallerWindow(Context context) {
		mContext = context;
	}

	public boolean isActive() {
		return mActiveWindow;
	}

	public void showCallerWindow(ODataRow callerInfo) {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		wm = (WindowManager) mContext.getSystemService(Activity.WINDOW_SERVICE);
		myView = inflater.inflate(R.layout.caller_window_layout, null);
		fillPartnerInfo(myView, callerInfo);
		myView.setOnTouchListener(this);
		wm.addView(myView, getWindowParams());
		mActiveWindow = true;
	}

	private void fillPartnerInfo(View view, ODataRow partner) {
		if (!partner.getString("image_small").equals("false")) {
			OControls.setImage(
					view,
					R.id.callerImage,
					Base64Helper.getBitmapImage(mContext,
							partner.getString("image_small")));
		} else {
			OControls.setImage(view, R.id.callerImage, R.drawable.avatar);
		}
		OControls.setText(view, R.id.callerName, partner.getString("name"));
		ODataRow company = partner.getM2ORecord("company_id").browse();
		if (company != null) {
			OControls.setText(view, R.id.callerCompany,
					company.getString("name"));
		} else {
			OControls.setInvisible(view, R.id.callerCompany);
		}

		CRMLead crm = new CRMLead(mContext);
		List<ODataRow> leads = crm.select("partner_id = ?",
				new String[] { partner.getString("id") });
		if (leads.size() > 0) {
			OControls.setText(view, R.id.callerLeadInfo, leads.get(0)
					.getString("type"));
			OControls.setText(view, R.id.callerLeadTitle, leads.get(0)
					.getString("name"));

			if (leads.get(0).getString("type").equals("opportunity")) {

				ODataRow lead_company = leads.get(0).getM2ORecord("compny_id")
						.browse();
				String symbol = lead_company.getM2ORecord("currency_id")
						.browse().getString("symbol");

				OControls.setVisible(view, R.id.callerRevenue);
				OControls.setText(view, R.id.callerPlanned, leads.get(0)
						.getString("planned_revenue") + " " + symbol);
				OControls.setText(view, R.id.callerProbability, leads.get(0)
						.getString("probability") + "%");
			} else {
				OControls.setGone(view, R.id.callerRevenue);
			}
		} else {
			OControls.setGone(view, R.id.callerLeadInfo);
			OControls.setText(view, R.id.callerLeadTitle, "No Lead found");
			OControls.setGone(view, R.id.callerRevenue);
		}
	}

	private LayoutParams getWindowParams() {
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(
				WindowManager.LayoutParams.MATCH_PARENT,
				WindowManager.LayoutParams.WRAP_CONTENT,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
						| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
						| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
				PixelFormat.TRANSLUCENT);

		params.gravity = Gravity.TOP;
		return params;
	}

	public void removeCallerWindow() {
		Log.v("CRMCallerWindow", "removeCallerWindow()");
		try {
			wm.removeViewImmediate(myView);
			mActiveWindow = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_MOVE:
			v.setBackgroundColor(Color.parseColor("#55f5f5f5"));
			break;
		default:
			v.setBackgroundColor(Color.parseColor("#f5f5f5"));
			break;
		}
		return false;
	}

}
