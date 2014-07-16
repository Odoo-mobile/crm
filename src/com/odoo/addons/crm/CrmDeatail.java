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

import com.odoo.addons.crm.CRM.Keys;
import com.odoo.addons.crm.model.CRMLead;
import com.odoo.crm.R;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OValues;
import com.odoo.support.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;

public class CrmDeatail extends BaseFragment {

	private View mView = null;
	private Keys mKey = null;
	private Integer mId = null;
	private Boolean mLocalRecord = false;
	Menu mMenu = null;
	private Boolean mEditMode = true;
	private OForm mForm = null;
	ODataRow mRecord = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		initArgs();
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.crm_lead_detail_view, container,
				false);
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init();
	}

	private void init() {
		// updateMenu(mEditMode);
		switch (mKey) {
		case Leads:
			OControls.setVisible(mView, R.id.crmLeadDetail);
			mForm = (OForm) mView.findViewById(R.id.crmLeadDetail);
			break;
		case Opportunities:
			OControls.setVisible(mView, R.id.crmOppDetail);
			mForm = (OForm) mView.findViewById(R.id.crmOppDetail);
			break;
		}
		CRMLead crmLead = new CRMLead(getActivity());
		if (mId != null) {
			mRecord = crmLead.select(mId, mLocalRecord);
			mForm.initForm(mRecord);
		} else {
			mForm.setModel(crmLead);
		}
		mForm.setEditable(mEditMode);
	}

	private void updateMenu(boolean edit_mode) {
		mMenu.findItem(R.id.menu_crm_detail_save).setVisible(edit_mode);
		mMenu.findItem(R.id.menu_crm_detail_edit).setVisible(!edit_mode);
	}

	public void initArgs() {
		Bundle arg = getArguments();
		mKey = Keys.valueOf(arg.getString("key"));
		if (arg.containsKey("id")) {
			mLocalRecord = arg.getBoolean("local_record");
			if (mLocalRecord) {
				mId = arg.getInt("local_id");
			} else
				mId = arg.getInt("id");
		}
	}

	@Override
	public Object databaseHelper(Context context) {
		return null;
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_crm_detail, menu);
		mMenu = menu;
		updateMenu(mEditMode);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_crm_detail_edit:
			mEditMode = !mEditMode;
			updateMenu(mEditMode);
			mForm.setEditable(mEditMode);
			break;
		case R.id.menu_crm_detail_save:
			mEditMode = false;
			OValues values = mForm.getFormValues();
			if (values != null) {
				updateMenu(mEditMode);
				if (mId != null) {
					switch (mKey) {
					case Leads:
						new CRMLead(getActivity()).update(values, mId,
								mLocalRecord);
						break;
					case Opportunities:
						new CRMLead(getActivity()).update(values, mId,
								mLocalRecord);
						break;
					}
				} else {
					switch (mKey) {
					case Leads:
						new CRMLead(getActivity()).create(values);
						break;
					case Opportunities:
						new CRMLead(getActivity()).create(values);
						break;
					}
				}
				getActivity().getSupportFragmentManager().popBackStack();
			}
			break;
		case R.id.menu_crm_detail_delete:
				new CRMLead(getActivity()).delete(mId);
				getActivity().getSupportFragmentManager().popBackStack();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
}