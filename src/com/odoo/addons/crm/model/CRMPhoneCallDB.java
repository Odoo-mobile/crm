package com.odoo.addons.crm.model;

import android.content.Context;

import com.odoo.addons.crm.model.CRMdb.CRMCaseCateg;
import com.odoo.base.res.ResPartner;
import com.odoo.base.res.ResUsers;
import com.odoo.orm.OColumn;
import com.odoo.orm.OModel;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OReal;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;

public class CRMPhoneCallDB extends OModel{


	OColumn user_id = new OColumn("Responsible", ResUsers.class,
			RelationType.ManyToOne);
	OColumn partner_id = new OColumn("Contact", ResPartner.class,
			RelationType.ManyToOne);
	OColumn description = new OColumn("Description", OText.class);
	OColumn state = new OColumn("status", OVarchar.class, 64);
	OColumn name = new OColumn("Call summary", OVarchar.class, 64);
	OColumn duration = new OColumn("Duration", OReal.class);
	OColumn categ_id = new OColumn("Category", CRMCaseCateg.class,
			RelationType.ManyToOne);
	OColumn date = new OColumn("Date", ODateTime.class);
	OColumn opportunity_id = new OColumn("Lead/Opportunity", CRMdb.class,
			RelationType.ManyToOne);
	OColumn call_audio_file = new OColumn("recorded audio file",
			OVarchar.class, 200).setLocalColumn();
	
	public CRMPhoneCallDB(Context context) {
		super(context,"crm.phonecall");
	}

}
