package com.odoo.addons.crm.model;

import odoo.ODomain;
import android.content.Context;

import com.odoo.addons.crm.providers.crm.CRMProvider;
import com.odoo.addons.crm.providers.crm.CrmCaseCategProvider;
import com.odoo.addons.crm.providers.crm.CrmCaseStageProvider;
import com.odoo.addons.crm.providers.crm.CrmPaymentModeProvider;
import com.odoo.base.res.ResCompany;
import com.odoo.base.res.ResCountry;
import com.odoo.base.res.ResPartner;
import com.odoo.base.res.ResUsers;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.ODataRow;
import com.odoo.orm.OModel;
import com.odoo.orm.annotations.Odoo.Functional;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OInteger;
import com.odoo.orm.types.OReal;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;
import com.odoo.support.OUser;
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
			.setParsePattern(ODate.DEFAULT_FORMAT);
	OColumn title_action = new OColumn("Next Action", OVarchar.class, 64);
	OColumn payment_mode = new OColumn("Payment Mode", CRMPaymentMode.class,
			RelationType.ManyToOne);
	OColumn planned_cost = new OColumn("Planned Cost", OReal.class, 20);
	@Functional(method = "plannedProbability")
	OColumn plannedProbabilityTotal = new OColumn("Total Amount", OText.class);

	public CRMLead(Context context) {
		super(context, "crm.lead");
		mContext = context;
	}

	@Override
	public OContentProvider getContentProvider() {
		return new CRMProvider();
	}

	public String plannedProbability(ODataRow row) {
		if (!row.getString("planned_revenue").equals("false")
				&& Double.parseDouble(row.getString("planned_revenue")) > 0)
			return row.getString("planned_revenue") + " at "
					+ row.getString("probability") + "%";
		else
			return "";
	}

	@Override
	public ODomain defaultDomain() {
		ODomain domain = new ODomain();
		domain.add("user_id", "=", OUser.current(mContext).getUser_id());
		// domain.add("user_id", "=", false);
		return domain;
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
