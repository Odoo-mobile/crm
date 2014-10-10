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
import com.odoo.orm.OModel;
import com.odoo.orm.OValues;
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

	OColumn partner_id = new OColumn("Customer", ResPartner.class,
			RelationType.ManyToOne);
	OColumn name = new OColumn("Subject", OVarchar.class, 64).setRequired(true);
	OColumn email_from = new OColumn("Email", OVarchar.class, 128);
	OColumn create_date = new OColumn("Creation Date", ODateTime.class)
			.setParsePattern(ODate.DEFAULT_FORMAT);
	OColumn description = new OColumn("Note", OText.class);
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

	public static class CRMCaseCateg extends OModel {

		OColumn name = new OColumn("Name", OVarchar.class);

		public CRMCaseCateg(Context context) {
			super(context, "crm.case.categ");
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
