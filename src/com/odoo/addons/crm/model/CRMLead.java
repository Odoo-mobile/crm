package com.odoo.addons.crm.model;

import org.json.JSONArray;

import android.content.Context;
import android.text.TextUtils;

import com.odoo.addons.crm.providers.crm.CRMProvider;
import com.odoo.addons.crm.providers.crm.CrmCaseCategProvider;
import com.odoo.addons.crm.providers.crm.CrmCaseStageProvider;
import com.odoo.addons.crm.providers.crm.CrmPaymentModeProvider;
import com.odoo.base.res.ResCompany;
import com.odoo.base.res.ResCountry;
import com.odoo.base.res.ResCurrency;
import com.odoo.base.res.ResPartner;
import com.odoo.base.res.ResUsers;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.orm.OValues;
import com.odoo.orm.annotations.Odoo;
import com.odoo.orm.annotations.Odoo.Functional;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OInteger;
import com.odoo.orm.types.OReal;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;
import com.odoo.support.provider.OContentProvider;
import com.odoo.util.ODate;

public class CRMLead extends OModel {

	Context mContext = null;

	@Odoo.onChange(method = "partner_id_on_change")
	OColumn partner_id = new OColumn("Customer", ResPartner.class,
			RelationType.ManyToOne);
	OColumn name = new OColumn("Name", OVarchar.class, 64).setRequired(true);
	OColumn email_from = new OColumn("Email", OVarchar.class, 128);
	OColumn street = new OColumn("Street", OText.class);
	OColumn street2 = new OColumn("Street2", OText.class);
	OColumn city = new OColumn("City", OVarchar.class, 100);
	OColumn zip = new OColumn("Zip", OVarchar.class, 20);
	OColumn phone = new OColumn("Phone", OVarchar.class, 20);

	OColumn create_date = new OColumn("Creation Date", ODateTime.class)
			.setParsePattern(ODate.DEFAULT_FORMAT);
	OColumn description = new OColumn("Internal Notes", OText.class);
	OColumn categ_ids = new OColumn("Tags", CRMCaseCateg.class,
			RelationType.ManyToMany);
	OColumn contact_name = new OColumn("Contact Name", OVarchar.class, 64);
	OColumn partner_name = new OColumn("Partner Name", OVarchar.class, 64);
	OColumn opt_out = new OColumn("Opt-Out", OBoolean.class);
	OColumn type = new OColumn("Type", OVarchar.class, 64).setDefault("lead");
	OColumn priority = new OColumn("Priority", OVarchar.class, 10);
	OColumn date_open = new OColumn("Assigned", ODateTime.class)
			.setParsePattern(ODate.DEFAULT_FORMAT);
	OColumn date_closed = new OColumn("Closed", ODateTime.class)
			.setParsePattern(ODate.DEFAULT_FORMAT);
	OColumn stage_id = new OColumn("Stage", CRMCaseStage.class,
			RelationType.ManyToOne);
	OColumn user_id = new OColumn("Salesperson", ResUsers.class,
			RelationType.ManyToOne);
	OColumn referred = new OColumn("Refferd By", OVarchar.class);
	OColumn company_id = new OColumn("Company", ResCompany.class,
			RelationType.ManyToOne);
	OColumn country_id = new OColumn("Country", ResCountry.class,
			RelationType.ManyToOne);
	OColumn company_currency = new OColumn("Company Currency",
			ResCurrency.class, RelationType.ManyToOne);

	/**
	 * Only used for type opportunity
	 */

	OColumn probability = new OColumn("Success Rate (%)", OReal.class, 20);
	OColumn planned_revenue = new OColumn("Expected Revenue", OReal.class, 20);
	OColumn ref = new OColumn("Reference", OVarchar.class, 64);
	OColumn ref2 = new OColumn("Reference 2", OVarchar.class, 64);
	OColumn date_deadline = new OColumn("Expected Closing", ODateTime.class)
			.setParsePattern(ODate.DEFAULT_FORMAT);
	OColumn date_action = new OColumn("Next Action Date", ODateTime.class)
			.setParsePattern(ODate.DEFAULT_DATE_FORMAT);
	OColumn title_action = new OColumn("Next Action", OVarchar.class, 64);
	OColumn payment_mode = new OColumn("Payment Mode", CRMPaymentMode.class,
			RelationType.ManyToOne);
	OColumn planned_cost = new OColumn("Planned Cost", OReal.class, 20);

	/**
	 * Extra functional fields
	 */
	@Functional(method = "getDisplayName", store = true, depends = {
			"partner_id", "contact_name", "partner_name" })
	OColumn display_name = new OColumn("Display Name", OVarchar.class, 64)
			.setLocalColumn();
	@Functional(method = "storeAssigneeName", store = true, depends = { "user_id" })
	OColumn assignee_name = new OColumn("Assignee", OVarchar.class, 100)
			.setLocalColumn();

	public CRMLead(Context context) {
		super(context, "crm.lead");
		mContext = context;
	}

	@Override
	public OContentProvider getContentProvider() {
		return new CRMProvider();
	}

	public String getDisplayName(OValues row) {
		String name = "";
		try {
			if (!row.getString("partner_id").equals("false")) {
				JSONArray partner_id = new JSONArray(
						row.getString("partner_id"));
				name = partner_id.getString(1);
			} else if (!row.getString("partner_name").equals("false")) {
				name = row.getString("partner_name");
			}
			if (!row.getString("contact_name").equals("false")) {
				name += (TextUtils.isEmpty(name)) ? row
						.getString("contact_name") : " ("
						+ row.getString("contact_name") + ")";
			}
			if (TextUtils.isEmpty(name)) {
				name = "No Parnter";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	public String storeAssigneeName(OValues vals) {
		return (!vals.getString("user_id").equals("false")) ? "Me"
				: "Unassigned";
	}

	/**
	 * OnChange methods
	 */

	public ODataRow partner_id_on_change(ODataRow row) {
		ODataRow rec = new ODataRow();
		String display_name = "";
		ResCountry country = new ResCountry(mContext);
		try {
			rec.put("partner_name", row.getString("name"));
			rec.put("partner_name", rec.getString("partner_name"));
			if (!row.getString("parent_id").equals("false")) {
				if (row.get("parent_id") instanceof JSONArray) {
					JSONArray parent_id = new JSONArray(
							row.getString("parent_id"));
					rec.put("partner_name", parent_id.get(1));
					display_name = parent_id.getString(1);
				} else {
					ODataRow parent_id = row.getM2ORecord("parent_id").browse();
					if (parent_id != null) {
						rec.put("partner_name", parent_id.getString("name"));
						display_name = parent_id.getString("name");
					}
				}
				if (!TextUtils.isEmpty(display_name))
					display_name += " (" + row.getString("name") + ")";
				else
					display_name += row.getString("name");
			} else {
				display_name = row.getString("name");
			}
			Integer country_id = 0;
			if (!row.getString("country_id").equals("false")) {
				if (row.get("country_id") instanceof JSONArray) {
					JSONArray country_data = new JSONArray(
							row.getString("country_id"));
					country_id = country.selectRowId(country_data.getInt(0));
					if (country_id == null) {
						country_id = 0;
					}
				} else {
					ODataRow country_data = row.getM2ORecord("country_id")
							.browse();
					if (country_data != null) {
						country_id = country_data.getInt(OColumn.ROW_ID);
					}
				}
				if (country_id != 0)
					rec.put("country_id", country_id);
			}
			rec.put("display_name", display_name);
			rec.put("street", row.getString("street"));
			rec.put("street2", row.getString("street2"));
			rec.put("city", row.getString("city"));
			rec.put("zip", row.getString("zip"));
			rec.put("email_from", row.getString("email"));
			rec.put("phone", row.getString("phone"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rec;
	}

	public static class CRMCaseCateg extends OModel {

		OColumn name = new OColumn("Name", OVarchar.class);

		public CRMCaseCateg(Context context) {
			super(context, "crm.case.categ");
			if (getOdooVersion() != null) {
				int version = getOdooVersion().getVersion_number();
				if (version >= 9) {
					setModelName("crm.categ");
				}
			}
		}

		@Override
		public OContentProvider getContentProvider() {
			return new CrmCaseCategProvider();
		}
	}

	public static class CRMCaseStage extends OModel {

		OColumn name = new OColumn("Name", OVarchar.class, 64);
		OColumn sequence = new OColumn("Sequence", OInteger.class);
		OColumn probability = new OColumn("Probability (%)", OReal.class, 20);
		OColumn case_default = new OColumn("Default to New Sales Team",
				OBoolean.class);
		OColumn type = new OColumn("Type", OVarchar.class, 64);

		public CRMCaseStage(Context context) {
			super(context, "crm.case.stage");
			if (getOdooVersion() != null) {
				int version = getOdooVersion().getVersion_number();
				if (version >= 9) {
					setModelName("crm.stage");
				}
			}
		}

		@Override
		public OContentProvider getContentProvider() {
			return new CrmCaseStageProvider();
		}
	}

	public static class CRMPaymentMode extends OModel {

		OColumn name = new OColumn("Name", OVarchar.class, 64);

		public CRMPaymentMode(Context context) {
			super(context, "crm.payment.mode");
		}

		@Override
		public OContentProvider getContentProvider() {
			return new CrmPaymentModeProvider();
		}
	}
}
