package com.odoo.addons.partners;

import java.util.List;

import odoo.controls.OForm;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.base.res.ResPartner;
import com.odoo.crm.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;

public class ResDetail extends BaseFragment {
	private View mView = null;
	Context mContext = null;
	private Integer mId = null;
	private OForm mForm = null;
	private Boolean mEditMode = false;
	private ODataRow mRecord = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		initArgs();
		mContext = getActivity();
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.res_detail, container, false);
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init();
	}

	private void initArgs() {
		Bundle args = getArguments();
		if (args.containsKey(OColumn.ROW_ID)) {
			mId = args.getInt(OColumn.ROW_ID);
		} else
			mEditMode = true;
	}

	private void init() {
		OControls.setVisible(mView, R.id.odooFormRes);
		mForm = (OForm) mView.findViewById(R.id.odooFormRes);
		ResPartner res = new ResPartner(getActivity());
		if (mId != null) {
			mRecord = res.select(mId);
			mForm.initForm(mRecord);
		} else {
			mForm.setModel(res);
			mForm.setEditable(mEditMode);
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

}
