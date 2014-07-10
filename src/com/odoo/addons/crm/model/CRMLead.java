package com.odoo.addons.crm.model;

import android.content.Context;

import com.odoo.base.res.ResCompany;
import com.odoo.base.res.ResCountry;
import com.odoo.base.res.ResPartner;
import com.odoo.base.res.ResUsers;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.OModel;
import com.odoo.orm.types.OBoolean;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OInteger;
import com.odoo.orm.types.OReal;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;
import com.odoo.util.ODate;

public class CRMLead extends OModel {

	OColumn partner_id = new OColumn("Partner", ResPartner.class,
			RelationType.ManyToOne);
	OColumn name = new OColumn("Subject", OVarchar.class, 64).setRequired(true);
	OColumn email_from = new OColumn("Email", OVarchar.class, 128);
	OColumn create_date = new OColumn("Creation Date", ODateTime.class);
	OColumn description = new OColumn("Note", OText.class);
	OColumn write_date = new OColumn("Update Date", ODateTime.class);
	OColumn categ_ids = new OColumn("Tags", CRMCaseCateg.class,
			RelationType.ManyToMany);
	OColumn contact_name = new OColumn("Contact Name", OVarchar.class, 64);
	OColumn partner_name = new OColumn("Partner Name", ODateTime.class, 64);
	OColumn opt_out = new OColumn("Opt-Out", OBoolean.class);
	OColumn type = new OColumn("Type", OVarchar.class, 64);
	OColumn priority = new OColumn("Priority", OVarchar.class, 64);
	OColumn date_open = new OColumn("Assigned", ODateTime.class);
	OColumn date_closed = new OColumn("Closed", ODateTime.class);
	OColumn stage_id = new OColumn("Stage", CRMCaseStage.class,
			RelationType.ManyToOne);
	OColumn user_id = new OColumn("Salesperson", ResUsers.class,
			RelationType.ManyToOne);
	OColumn referred = new OColumn("Refferd By", OVarchar.class);
	OColumn date_last_stage_update = new OColumn("Last Stage Update",
			ODateTime.class);
	OColumn company_id = new OColumn("Company", ResCompany.class,
			RelationType.ManyToOne);
	OColumn country_id = new OColumn("Country", ResCountry.class,
			RelationType.ManyToOne);
	OColumn probability = new OColumn("Success Rate (%)", OReal.class, 20);
	OColumn planned_revenue = new OColumn("Expected Revenue", OReal.class, 20);
	OColumn ref = new OColumn("Reference", OVarchar.class, 64);
	OColumn ref2 = new OColumn("Reference 2", OVarchar.class, 64);
	OColumn date_deadline = new OColumn("Expected Closing", ODateTime.class).setParsePatter(ODate.DEFAULT_FORMAT);
	OColumn title_action = new OColumn("Next Action", OVarchar.class, 64);
	OColumn payment_mode = new OColumn("Payment Mode", CRMPaymentMode.class,
			RelationType.ManyToOne);
	OColumn planned_cost = new OColumn("Planned Cost", OReal.class, 20);

	public CRMLead(Context context) {
		super(context, "crm.lead");
	}

	public static class CRMCaseCateg extends OModel {

		OColumn name = new OColumn("Name", OVarchar.class);

		public CRMCaseCateg(Context context) {
			super(context, "crm.case.categ");
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

	}

	public static class CRMPaymentMode extends OModel {

		OColumn name = new OColumn("Name", OVarchar.class, 64);

		public CRMPaymentMode(Context context) {
			super(context, "crm.payment.mode");
		}

	}

//	public static class CRMPhoneCall extends OModel {
//
//
//		public CRMPhoneCall(Context context) {
//			super(context, "crm.phonecall");
//		}
//
//	}
	
	//public class static CRMProduct extends 
}
