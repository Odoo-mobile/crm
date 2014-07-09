package com.odoo.addons.sale.model;

import android.content.Context;

import com.odoo.base.res.ResCurrency;
import com.odoo.base.res.ResPartner;
import com.odoo.base.res.ResUsers;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.OModel;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OInteger;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;

public class SalesDB extends OModel {

	OColumn name = new OColumn("name", OVarchar.class, 64).setRequired(true)
			.setRequired(true);
	OColumn date_order = new OColumn("timestamp without time zone",
			ODateTime.class).setRequired(true);
	OColumn partner_id = new OColumn("Customer", ResPartner.class,
			RelationType.ManyToOne).setRequired(true).setRequired(true);
	OColumn user_id = new OColumn("Salesperson", ResUsers.class,
			RelationType.ManyToOne);
	OColumn amount_total = new OColumn("Total", OInteger.class);
	OColumn amount_untaxed = new OColumn("Untaxed", OInteger.class);
	OColumn amount_tax = new OColumn("Tax", OInteger.class);
	OColumn client_order_ref = new OColumn("Client Order Reference",
			OVarchar.class, 100);
	OColumn state = new OColumn("status", OVarchar.class, 10);
	OColumn currency_id = new OColumn("currency", ResCurrency.class,
			RelationType.ManyToOne).setRequired(true);
	OColumn order_line = new OColumn("Order Lines", SalesOrderLine.class,
			RelationType.OneToMany);

	public SalesDB(Context context) {
		super(context, "sale.order");
	}

	public static class SalesOrderLine extends OModel {

		OColumn product_id = new OColumn("Product Id", ProductDB.class,
				RelationType.ManyToOne);
		OColumn name = new OColumn("Name", OText.class);
		OColumn product_uom_qty = new OColumn("Quantity", OInteger.class);
		OColumn price_unit = new OColumn("Unit Price", OInteger.class);
		OColumn price_subtotal = new OColumn("Sub Total", OInteger.class);
		OColumn order_id = new OColumn("ID", SalesDB.class,
				RelationType.ManyToOne);

		public SalesOrderLine(Context context, String model_name) {
			super(context, "sale.order_line");
		}
	}

	public static class ProductDB extends OModel {

		OColumn lst_price = new OColumn("Public price", OInteger.class);
		OColumn name_template = new OColumn("Product Name", OText.class);

		public ProductDB(Context context, String model_name) {
			super(context, "product.product");
		}
	}
}
