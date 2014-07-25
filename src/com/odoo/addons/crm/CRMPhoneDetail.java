package com.odoo.addons.crm;

import java.util.List;

import odoo.controls.OForm;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.addons.crm.model.CRMPhoneCall;
import com.odoo.crm.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OValues;
import com.odoo.support.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;

public class CRMPhoneDetail extends BaseFragment {

	private View mView = null;
	private Integer mId = null;
	Menu mMenu = null;
	private Boolean mEditMode = true;
	private OForm mForm = null;
	ODataRow mRecord = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		initArgs();
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.crm_phone_detail, container, false);
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init();
	}

	private void init() {
		// updateMenu(mEditMode);
		OControls.setVisible(mView, R.id.crmPhoneDetail);
		mForm = (OForm) mView.findViewById(R.id.crmPhoneDetail);
		CRMPhoneCall crmPhone = new CRMPhoneCall(getActivity());
		if (mId != null) {
			mRecord = crmPhone.select(mId);
			mForm.initForm(mRecord);
		} else {
			mForm.setModel(crmPhone);
		}
		mForm.setEditable(mEditMode);
	}

	public void initArgs() {
		Bundle arg = getArguments();
		if (arg.containsKey(OColumn.ROW_ID)) {
			mId = arg.getInt(OColumn.ROW_ID);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_crm_phone_detail, menu);
		mMenu = menu;
		updateMenu(mEditMode);
	}

	private void updateMenu(boolean edit_mode) {
		mMenu.findItem(R.id.menu_phone_detail_save).setVisible(edit_mode);
		mMenu.findItem(R.id.menu_phone_detail_edit).setVisible(!edit_mode);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_phone_detail_edit:
			mEditMode = !mEditMode;
			updateMenu(mEditMode);
			mForm.setEditable(mEditMode);
			break;
		case R.id.menu_phone_detail_save:
			mEditMode = false;
			OValues values = mForm.getFormValues();
			if (values != null) {
				updateMenu(mEditMode);
				if (mId != null) {
					new CRMPhoneCall(getActivity()).update(values, mId);
				} else {
					new CRMPhoneCall(getActivity()).create(values);
				}
			}
			getActivity().getSupportFragmentManager().popBackStack();
			break;
		case R.id.menu_phone_detail_delete:
			new CRMPhoneCall(getActivity()).delete(mId);
			getActivity().getSupportFragmentManager().popBackStack();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Object databaseHelper(Context context) {
		return null;
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

}
