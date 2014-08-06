package com.odoo.addons.crm.receivers;

import java.util.List;

import odoo.ODomain;
import odoo.Odoo;
import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

import com.odoo.App;
import com.odoo.base.res.ResPartner;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OSyncHelper;

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
			ResPartner mPartner = new ResPartner(mContext);
			String where = "phone like ? or phone like ? or mobile like ? or mobile like ?";
			String[] args = new String[] { "%" + mcontactLast2Chars,
					"%" + mcontactLast3Chars, "%" + mcontactLast2Chars,
					"%" + mcontactLast3Chars };
			// getting from Local DB
			List<ODataRow> partners = mPartner.select(where, args);
			if (partners != null && partners.size() > 0) {
				mPartnerInfo = partners.get(0);
			} else {
				// getting from server
				App app = (App) mContext.getApplicationContext();
				Odoo odoo = app.getOdoo();
				ODomain domain = new ODomain();
				domain.add("|");
				domain.add("|");
				domain.add("phone", "=like", "%" + mcontactLast2Chars);
				domain.add("phone", "=like", "%" + mcontactLast3Chars);
				domain.add("|");
				domain.add("mobile", "=like", "%" + mcontactLast2Chars);
				domain.add("mobile", "=like", "%" + mcontactLast3Chars);
				try {
					OSyncHelper sync = mPartner.getSyncHelper();
					sync.getServerData(mPartner, domain);
					List<ODataRow> partner = mPartner
							.select(where,args);
					for (ODataRow row : partner) {
						String phone = row.getString("phone").trim();
						String mobile = row.getString("mobile").trim();
						String contact = (phone.equals("false") || TextUtils
								.isEmpty(phone)) ? mobile : phone;
						if (contact.replaceAll(" ", "")
								.contains(mContactNumber)
								|| mContactNumber.contains(contact.replaceAll(
										" ", ""))) {
							mPartnerInfo = new ODataRow();
							mPartnerInfo = row;
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
