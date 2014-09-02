package com.odoo.addons.crm.model;

import odoo.ODomain;
import android.content.Context;

import com.odoo.addons.crm.model.CRMLead.CRMCaseCateg;
import com.odoo.addons.crm.providers.crm.PhoneCallProvider;
import com.odoo.addons.sale.providers.sale.SalesProvider;
import com.odoo.base.res.ResPartner;
import com.odoo.base.res.ResUsers;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.OModel;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OReal;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;
import com.odoo.support.OUser;
import com.odoo.support.provider.OContentProvider;
import com.odoo.util.ODate;

public class CRMPhoneCall extends OModel {

	Context mContext = null;
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
	OColumn date = new OColumn("Date", ODateTime.class)
			.setParsePattern(ODate.DEFAULT_DATE_FORMAT);
	OColumn opportunity_id = new OColumn("Lead/Opportunity", CRMLead.class,
			RelationType.ManyToOne);
	OColumn call_audio_file = new OColumn("recorded audio file",
			OVarchar.class, 200).setLocalColumn();

	public CRMPhoneCall(Context context) {
		super(context, "crm.phonecall");
		mContext = context;
	}

	@Override
	public OContentProvider getContentProvider() {
		return new PhoneCallProvider();
	}

	@Override
	public ODomain defaultDomain() {
		ODomain domain = new ODomain();
		domain.add("|");
		domain.add("user_id", "=", OUser.current(mContext).getUser_id());
		// domain.add("user_id", "=", false);
		return domain;
	}

}
