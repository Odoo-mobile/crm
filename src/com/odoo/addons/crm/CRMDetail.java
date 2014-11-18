package com.odoo.addons.crm;

import java.util.List;

import odoo.OArguments;
import odoo.ODomain;
import odoo.Odoo;
import odoo.controls.OField;
import odoo.controls.OForm;
import odoo.controls.OSearchableMany2One.DialogListRowViewListener;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.odoo.addons.crm.CRM.Keys;
import com.odoo.addons.crm.model.CRMLead;
import com.odoo.base.res.ResUsers;
import com.odoo.crm.R;
import com.odoo.orm.OColumn;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OValues;
import com.odoo.support.ODialog;
import com.odoo.support.fragment.BaseFragment;
import com.odoo.util.OControls;
import com.odoo.util.ODate;
import com.odoo.util.drawer.DrawerItem;

public class CRMDetail extends BaseFragment implements OnClickListener,
		DialogListRowViewListener {

	private View mView = null;
	private Keys mKey = null;
	private Integer mId = null;
	Menu mMenu = null;
	Context mContext = null;
	private Boolean mEditMode = true;
	private OForm mForm = null;
	ODataRow mRecord = null;
	Bundle arg = null;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		initArgs();
		setHasOptionsMenu(true);
		mContext = getActivity();
		mView = inflater.inflate(R.layout.crm_detail_view, container, false);
		return mView;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		init();
	}

	private void init() {
		switch (mKey) {
		case Leads:
			OControls.setVisible(mView, R.id.crmLeadDetail);
			mForm = (OForm) mView.findViewById(R.id.crmLeadDetail);
			if (mId != null) {
				mForm.findViewById(R.id.btnConvertToOpportunity)
						.setOnClickListener(this);
				mForm.findViewById(R.id.btnCancelCase).setOnClickListener(this);
				mForm.findViewById(R.id.btnLeadReset).setOnClickListener(this);
			} else {
				mForm.findViewById(R.id.btnLayout).setVisibility(View.GONE);
			}
			OField partner_id = (OField) mForm.findViewById(R.id.partner_id);
			partner_id.setManyToOneSearchableCallbacks(this);
			break;
		case Opportunities:
			OControls.setVisible(mView, R.id.crmOppDetail);
			mForm = (OForm) mView.findViewById(R.id.crmOppDetail);
			mForm.findViewById(R.id.btnConvertToQuotation).setOnClickListener(
					this);
			partner_id = (OField) mForm.findViewById(R.id.partner_id_opp);
			partner_id.setManyToOneSearchableCallbacks(this);
			break;
		}
		CRMLead crmLead = new CRMLead(getActivity());
		if (mId != null) {
			mRecord = crmLead.select(mId);
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
		arg = getArguments();
		mKey = Keys.valueOf(arg.getString(CRM.KEY_CRM_LEAD_TYPE));
		if (arg.containsKey(OColumn.ROW_ID)) {
			mId = arg.getInt(OColumn.ROW_ID);
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
						new CRMLead(getActivity()).update(values, mId);
						break;
					case Opportunities:
						new CRMLead(getActivity()).update(values, mId);
						break;
					}
				} else {
					ResUsers users = new ResUsers(getActivity());
					values.put("user_id",
							users.selectRowId(scope.User().getUser_id()));
					values.put("assignee_name", "Me");
					CRMLead.CRMCaseStage stages = new CRMLead.CRMCaseStage(
							getActivity());
					List<ODataRow> stage = stages.select(
							"type = ? and name = ?", new String[] { "both",
									"New" });
					if (stage.size() > 0) {
						values.put("stage_id",
								stage.get(0).getInt(OColumn.ROW_ID));
					}
					values.put("create_date",
							ODate.getUTCDate(ODate.DEFAULT_FORMAT));
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

	@Override
	public void onClick(View v) {
		Bundle bundle = new Bundle();
		switch (v.getId()) {
		case R.id.btnConvertToOpportunity:
			CRMConvertToOpp convertToOpportunity = new CRMConvertToOpp();
			if (arg.getInt("id") != 0) {
				if (app().inNetwork()) {
					bundle.putInt("lead_id", mId);
					bundle.putInt("index", getArguments().getInt("index"));
					convertToOpportunity.setArguments(bundle);
					startFragment(convertToOpportunity, true);
				} else {
					Toast.makeText(mContext, _s(R.string.toast_no_netowrk),
							Toast.LENGTH_SHORT).show();
				}
			} else {
				Toast.makeText(mContext, _s(R.string.toast_sync_before),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.btnConvertToQuotation:
			if (mRecord.getM2ORecord("partner_id").browse() != null) {
				ConvertToQuotation converToQuotation = new ConvertToQuotation(
						mRecord);
				converToQuotation.execute();
			}
		default:
			break;
		}
	}

	class ConvertToQuotation extends AsyncTask<Void, Void, Void> {
		ODataRow mLead = null;
		ODialog mDialog = null;
		String mToast = null;

		public ConvertToQuotation(ODataRow lead) {
			mLead = lead;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mDialog = new ODialog(getActivity(), false, "Converting....");
			mDialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					try {
						Odoo odoo = app().getOdoo();
						int version = odoo.getOdooVersion().getVersion_number();
						OArguments args = new OArguments();
						JSONArray fields = new JSONArray();
						fields.put("close");
						if (version == 7)
							fields.put("shop_id");
						fields.put("partner_id");
						args.add(fields);
						JSONObject kwargs = new JSONObject();
						JSONObject context = new JSONObject();
						if (version == 7)
							context.put("stage_type", "opportunity");
						context.put("active_model", "crm.lead");
						context.put("active_id", mLead.getInt("id"));
						context.put("active_ids",
								new JSONArray().put(mLead.getInt("id")));
						kwargs.put("context", context);
						JSONObject response = (JSONObject) odoo.call_kw(
								"crm.make.sale", "default_get",
								new JSONArray().put(fields), kwargs);
						JSONObject result = response.getJSONObject("result");
						JSONObject arguments = new JSONObject();
						arguments.put("partner_id", result.get("partner_id"));
						if (version == 7)
							arguments.put("shop_id", result.get("shop_id"));
						arguments.put("close", false);
						JSONObject newContext = odoo.updateContext(context);
						JSONObject res = odoo.createNew("crm.make.sale",
								arguments);
						int id = res.getInt("result");

						// makeOrder
						OArguments make_order_args = new OArguments();
						make_order_args.add(id);
						make_order_args.add(newContext);
						JSONObject order = odoo.call_kw("crm.make.sale",
								"makeOrder", make_order_args.get());
						if (order.getJSONObject("result").has("res_id")) {
							mToast = "Quotation SO"
									+ order.getJSONObject("result").getInt(
											"res_id") + " created";
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			return null;
		}
	}

	@Override
	public View onDialogListRowGetView(ODataRow data, int position, View view,
			ViewGroup parent) {
		return null;
	}

	@Override
	public ODomain onDialogSearchChange(String filter) {
		ODomain domain = new ODomain();
		domain.add("name", "=ilike", filter + "%");
		return domain;
	}

	@Override
	public void bindDisplayLayoutLoad(ODataRow data, View layout) {
		if (data != null) {
			TextView txvName = (TextView) layout;
			txvName.setText(data.getString("name"));
		}
	}

	@Override
	public boolean onBackPressed() {
		// TODO Auto-generated method stub
		return false;
	}

}