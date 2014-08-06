package com.odoo.addons.crm.receivers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.odoo.MainActivity;
import com.odoo.addons.crm.receivers.CRMPartnerFinder.onPartnerResult;
import com.odoo.orm.ODataRow;

public class PhoneStateReceiver extends BroadcastReceiver {
	public static final String ACTION_LOG_SCHEDULE_CALL = "com.odoo.addons.crm.receivers.ACTION_LOG_SCHEDULE_CALL";
	public static final String TAG = PhoneStateReceiver.class.getSimpleName();

	Context mContext = null;
	public static CRMPartnerFinder mPartnerFinder = null;
	public static CRMCallerWindow mCallerWindow = null;

	Boolean isCallReceived = false;
	Boolean incommingCall = false;
	Boolean outgoingCall = false;
	String incomingNumber = null;
	String outgoingNumber = null;
	ODataRow mPartner = null;

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;
		if (mCallerWindow == null)
			mCallerWindow = new CRMCallerWindow(context);
		if (mPartnerFinder == null)
			mPartnerFinder = new CRMPartnerFinder(mContext);
		Bundle bundle = intent.getExtras();
		incomingNumber = bundle
				.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
		String sip_number = SipURI.parse(incomingNumber);

		TelephonyManager telyphony = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (intent.hasExtra(Intent.EXTRA_PHONE_NUMBER)) {
			outgoingNumber = bundle.getString(Intent.EXTRA_PHONE_NUMBER);
			telyphony.listen(mPhoneStateListener,
					PhoneStateListener.LISTEN_CALL_STATE);
		} else {
			outgoingNumber = null;
			if (bundle.containsKey(TelephonyManager.EXTRA_INCOMING_NUMBER)) {
				incomingNumber = bundle
						.getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
			}
			if (sip_number == null) {
				telyphony.listen(mPhoneStateListener,
						PhoneStateListener.LISTEN_CALL_STATE);
			} else {
				phoneSipStateListener(sip_number,
						bundle.getString(TelephonyManager.EXTRA_STATE), true);
			}
		}

	}

	public static boolean inReadyMode = false;

	private void phoneSipStateListener(String contactNumber, String state,
			boolean isSipNumber) {

		if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK) && inReadyMode) {
			phoneStateOffHook(contactNumber, null);
		}
		if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
			phoneStateRinging(contactNumber);
			inReadyMode = true;
		}
		if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
			inReadyMode = false;
			phoneStateIdle(null);
		}

	}

	PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		// Ringing
		public void onCallStateChanged(int state, String incoming_Number) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				phoneStateRinging(incomingNumber);
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				phoneStateOffHook(outgoingNumber, incomingNumber);
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				phoneStateIdle(incomingNumber);
				break;
			default:
				break;
			}

		};
	};

	private void phoneStateRinging(String incomingNumber) {
		incommingCall = true;
		if (!mPartnerFinder.isFinding())
			mPartnerFinder.findPartnerByNumber(incomingNumber, mPartnerResults);
	}

	private void phoneStateOffHook(String outGoingNumber, String incomingNumber) {
		// Call Started
		if (outgoingNumber != null) {
			outgoingCall = true;
		}
	}

	CRMPartnerFinder.onPartnerResult mPartnerResults = new CRMPartnerFinder.onPartnerResult() {

		@Override
		public void onResult(ODataRow partner) {
			if (partner != null && !mCallerWindow.isActive()) {
				mPartner = partner;
				mCallerWindow.showCallerWindow(partner);
			} else {
				isCallReceived = false;
				incommingCall = false;
			}
		}
	};

	private void phoneStateIdle(String outgoingNumber) {
		if (mCallerWindow.isActive()) {
			mCallerWindow.removeCallerWindow();
			outgoingNumber = null;
		}
		if (isCallReceived) {
			Log.v(TAG, "LogCallActivity()");
			isCallReceived = false;
			startLogCallActivity();
		}
		if (incommingCall && !isCallReceived) {
			incommingCall = false;
			startLogCallActivity();
		}
	}

	private void startLogCallActivity() {
		Intent log_call = new Intent(mContext, MainActivity.class);
		log_call.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		log_call.setAction(ACTION_LOG_SCHEDULE_CALL);
		// log_call.putExtra("call_duration", mRecodingDuration);
		// log_call.putExtra("media_file", mMediaFile);
		if (mPartner != null) {
			log_call.putExtra("partner_id", mPartner.getInt("id"));
			Log.v(TAG, "Call Duration sending : "
					+ log_call.getExtras().getFloat("call_duration"));
			mContext.startActivity(log_call);
		}
	}

	static class SipURI {
		private final static String SIP_SCHEME_RULE = "sip(?:s)?|tel";
		private final static Pattern SIP_CONTACT_ADDRESS_PATTERN = Pattern
				.compile("^([^@:]+)@([^@]+)$");
		private final static Pattern SIP_CONTACT_PATTERN = Pattern
				.compile("^(?:\")?([^<\"]*)(?:\")?[ ]*(?:<)?("
						+ SIP_SCHEME_RULE + "):([^@]+)@([^>]+)(?:>)?$");
		private final static Pattern SIP_HOST_PATTERN = Pattern
				.compile("^(?:\")?([^<\"]*)(?:\")?[ ]*(?:<)?("
						+ SIP_SCHEME_RULE + "):([^@>]+)(?:>)?$");

		public static String parse(String sipUri) {
			String sip_number = null;
			if (!TextUtils.isEmpty(sipUri)) {
				Matcher m = SIP_CONTACT_PATTERN.matcher(sipUri);
				if (m.matches()) {
					sip_number = Uri.decode(m.group(3));
				} else {
					m = SIP_HOST_PATTERN.matcher(sipUri);
					if (m.matches()) {
						sip_number = null;
					} else {
						m = SIP_CONTACT_ADDRESS_PATTERN.matcher(sipUri);
						if (m.matches()) {
							sip_number = Uri.decode(m.group(1));
						}
					}
				}
			}
			return sip_number;
		}
	}
}
