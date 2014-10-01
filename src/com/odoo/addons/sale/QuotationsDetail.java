package com.odoo.addons.sale;

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
import android.widget.Toast;

import com.odoo.addons.sale.Sales.Keys;
import com.odoo.addons.sale.model.SaleOrder;
import com.odoo.addons.sale.model.SaleOrder.SalesOrderLine;
import com.odoo.crm.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OValues;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.ODate;
import com.odoo.util.drawer.DrawerItem;

public class QuotationsDetail extends BaseFragment {
	private View mView = null;
	Context mContext = null;
	private Integer mId = null;
	private OForm mForm = null;
	private OForm mFormLine = null;
	private ODataRow mRecord = null;
	private ODataRow mRecordLine = null;
	private Keys mKey = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		initArgs();
		mContext = getActivity();
		setHasOptionsMenu(true);
		mView = inflater.inflate(R.layout.quotations_detail, container, false);
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
			mId = args.getInt(OColumn.ROW_ID);
		}

	}

	private void init() {
		OControls.setVisible(mView, R.id.odooFormQuotations);
		mForm = (OForm) mView.findViewById(R.id.odooFormQuotations);
		mFormLine = (OForm) mView.findViewById(R.id.odooFormOrderLine);
		SalesOrderLine saleOrderLine = new SalesOrderLine(getActivity());
		if (mId != null) {
			mRecord = db().select(mId);
			mForm.initForm(mRecord);
			mRecordLine = saleOrderLine.select(mId);
			// mFormLine.initForm(mRecordLine);
			// Log.e("Quotations", ":>>>> " + mRecord);
			if (mRecord.getString("state").equals("draft")) {
				mForm.setEditable(true);
				// mFormLine.setEditable(true);
			} else {
				mForm.setEditable(false);
				// mFormLine.setEditable(false);
			}
		} else {
			mForm.setModel(db());
			mForm.setEditable(false);
			// mFormLine.setEditable(false);
		}

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_sales_detail_save:
			// OValues values = mForm.getFormValues();
			// if (values != null) {
			OValues values = new OValues();
			if (mId != null && mRecord.getString("state").equals("draft")) {
				values = mForm.getFormValues();
				db().update(values, mId);
			} else if (mId == null) {
				values = new OValues();
				values.put("date_order", ODate.getUTCDate(ODate.DEFAULT_FORMAT));
				values.put("partner_id",
						mForm.getFormValues().getInt("partner_id"));
				values.put("is_dirty", true);
				values.put("name", "SO");
				values.put("state", "draft");
				db().create(values);
			}
			// }
			break;
		case R.id.menu_sales_detail_delete:
			if (mId != null && mKey.toString() == "Quotation")
				db().delete(mId);
			else
				Toast.makeText(mContext, "No Delete", Toast.LENGTH_LONG).show();
			break;
		}
		getActivity().getSupportFragmentManager().popBackStack();
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		menu.clear();
		inflater.inflate(R.menu.menu_quotations_detail, menu);
		if (mKey.toString() == "Quotation")
			menu.findItem(R.id.menu_sales_detail_delete).setVisible(true);
		else
			menu.findItem(R.id.menu_sales_detail_delete).setVisible(false);
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
