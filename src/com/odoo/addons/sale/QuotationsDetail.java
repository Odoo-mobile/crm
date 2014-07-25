package com.odoo.addons.sale;

import java.util.List;

import odoo.controls.OForm;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.odoo.addons.sale.Sales.Keys;
import com.odoo.addons.sale.model.SaleOrder;
import com.odoo.addons.sale.model.SaleOrder.SalesOrderLine;
import com.odoo.crm.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OValues;
import com.odoo.support.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.drawer.DrawerItem;

public class QuotationsDetail extends BaseFragment {
	private View mView = null;
	Context mContext = null;
	private Integer mId = null;
	private OForm mForm = null;
	private OForm mFormLine = null;
	private Boolean mEditMode = false;
	private ODataRow mRecord = null;
	private ODataRow mRecordLine = null;
	Menu mMenu = null;
	private Keys mKey = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		initArgs();
		mContext = getActivity();
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.quotations_detail, container, false);
		mContext = getActivity();

		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init();
	}

	private void initArgs() {
		Bundle args = getArguments();
		mKey = Keys.valueOf(args.getString("key"));
		if (args.containsKey(OColumn.ROW_ID)) {
			mId = args.getInt("local_id");
		} else
			mEditMode = true;
	}

	private void init() {
		updateMenu(mEditMode);
		// switch (mKey) {
		// case Note:
		// case Archive:
		// case Reminders:
		OControls.setVisible(mView, R.id.odooFormQuotations);

		mForm = (OForm) mView.findViewById(R.id.odooFormQuotations);
		mFormLine = (OForm) mView.findViewById(R.id.odooFormOrderLine);
		// SaleOrder saleOrder = new SaleOrder(getActivity());
		SalesOrderLine saleOrderLine = new SalesOrderLine(getActivity());
		if (mId != null) {
			mRecord = db().select(mId);
			mForm.initForm(mRecord);
			mRecordLine = saleOrderLine.select(mId);
			mFormLine.initForm(mRecordLine);
		} else {
			mForm.setModel(db());
			mForm.setEditable(mEditMode);
//			mFormLine.setVisibility(View.VISIBLE);
//			mFormLine.setModel(saleOrderLine);
//			mFormLine.setEditable(mEditMode);
		}
		// break;
		// }

	}

	private void updateMenu(boolean edit_mode) {
		mMenu.findItem(R.id.menu_sales_detail_save).setVisible(edit_mode);
		mMenu.findItem(R.id.menu_sales_detail_edit).setVisible(!edit_mode);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_sales_detail_edit:
			mEditMode = !mEditMode;
			updateMenu(mEditMode);
			mForm.setEditable(mEditMode);
			mFormLine.setEditable(mEditMode);
			break;
		case R.id.menu_sales_detail_save:
			mEditMode = false;
			OValues values = mForm.getFormValues();
			if (values != null) {
				updateMenu(mEditMode);
				if (mId != null) {
					// switch (mKey) {
					// case Note:
					db().update(values, mId);
					// break;
					// }
				} else {
					// switch (mKey) {
					// case Note:
					db().create(values);
					// break;
					// }
				}
				getActivity().getSupportFragmentManager().popBackStack();
			}
			break;
		case R.id.menu_sales_detail_delete:
			if (mId != null)
				db().delete(mId);
			Log.e("Delete", mId + "");
			getActivity().getSupportFragmentManager().popBackStack();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_quotations_detail, menu);
		mMenu = menu;
		updateMenu(mEditMode);
	}

	@Override
	public Object databaseHelper(Context context) {
		return new SaleOrder(context);
	}

	@Override
	public List<DrawerItem> drawerMenus(Context context) {
		return null;
	}

}
