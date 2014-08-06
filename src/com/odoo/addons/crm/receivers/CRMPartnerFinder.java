package com.odoo.addons.crm.receivers;

import java.util.List;

import odoo.ODomain;
import odoo.Odoo;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.odoo.App;
import com.odoo.base.res.ResPartner;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OFieldsHelper;
import com.odoo.util.logger.OLog;

public class CRMPartnerFinder {

	public static final String TAG = CRMPartnerFinder.class.getSimpleName();

	Context mContext = null;
	Boolean isFinding = false;
	PartnerFinder mFinder = null;

	public CRMPartnerFinder(Context context) {
		mContext = context;
	}

	public void findPartnerByNumber(String contactNumber,
			onPartnerResult resultCallback) {
		Log.v(TAG, "findPartnerByNumber() -> Contact Number : " + contactNumber);
		if (mFinder != null) {
			mFinder.cancel(true);
			mFinder = null;
		}
		mFinder = new PartnerFinder(contactNumber, resultCallback);
		mFinder.execute();
		isFinding = true;

	}

	public boolean isFinding() {
		return isFinding;
	}

	class PartnerFinder extends AsyncTask<Void, Void, Void> {
		onPartnerResult mCallBack = null;
		ODataRow mPartnerInfo = null;
		String mContactNumber = null;
		String mcontactLast2Chars = "";
		String mcontactLast3Chars = "";

		public PartnerFinder(String contact_number, onPartnerResult callback) {
			mCallBack = callback;
			mContactNumber = contact_number;
			mcontactLast2Chars = mContactNumber.substring(mContactNumber
					.length() - 2);
			mcontactLast3Chars = mContactNumber.substring(mContactNumber
					.length() - 3);
			mPartnerInfo = null;
		}

		@Override
		protected Void doInBackground(Void... params) {
			OLog.log(">>>>>>>>>>>");
			ResPartner mPartner = new ResPartner(mContext);
			// getting from Local DB
			OLog.log(">> " + mcontactLast2Chars + " : " + mcontactLast3Chars);
			List<ODataRow> partners = mPartner
					.select("phone like ? or phone like ? or mobile like ? or mobile like ?",
							new String[] { "%" + mcontactLast2Chars,
									"%" + mcontactLast3Chars,
									"%" + mcontactLast2Chars,
									"%" + mcontactLast3Chars });
			if (partners != null && partners.size() > 0) {
				mPartnerInfo = partners.get(0);
				OLog.log(partners.size() + " got from local");
			}

			// getting from server
			if (mPartnerInfo == null) {
				App app = (App) mContext.getApplicationContext();
				Odoo odoo = app.getOdoo();
				OFieldsHelper fields = new OFieldsHelper(mPartner.getColumns());
				ODomain domain = new ODomain();
				domain.add("|");
				domain.add("|");
				domain.add("phone", "=like", "%" + mcontactLast2Chars);
				domain.add("phone", "=like", "%" + mcontactLast3Chars);
				domain.add("|");
				domain.add("mobile", "=like", "%" + mcontactLast2Chars);
				domain.add("mobile", "=like", "%" + mcontactLast3Chars);
				try {
					odoo.debug(true);
					JSONObject result = odoo
							.search_read(mPartner.getModelName(), fields.get(),
									domain.get());
					for (int i = 0; i < result.getJSONArray("records").length(); i++) {
						JSONObject row = result.getJSONArray("records")
								.getJSONObject(i);
						String phone = row.getString("phone").trim();
						String mobile = row.getString("mobile").trim();
						String contact = (phone.equals("false") || TextUtils
								.isEmpty(phone)) ? mobile : phone;
						OLog.log(contact);
						OLog.log(contact.replaceAll(" ", "").contains(
								mContactNumber)
								+ " || "
								+ mContactNumber.contains(contact.replaceAll(
										" ", "")));
						if (contact.replaceAll(" ", "")
								.contains(mContactNumber)
								|| mContactNumber.contains(contact.replaceAll(
										" ", ""))) {
							// Fixing

							// Log.e("fields",
							// "$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
							// mPartnerInfo = new ODataRow();
							// List<OColumn> columns = (List<OColumn>) row;
							// fields.addAll(columns);
						}
					}
				} catch (Exception e) {

				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			mCallBack.onResult(mPartnerInfo);
			isFinding = false;
		}

	}

	public interface onPartnerResult {
		public void onResult(ODataRow partner);
	}
}
