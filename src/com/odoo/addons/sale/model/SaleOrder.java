package com.odoo.addons.sale.model;

import odoo.ODomain;
import android.content.Context;

import com.odoo.base.res.ResCurrency;
import com.odoo.base.res.ResPartner;
import com.odoo.base.res.ResUsers;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.OModel;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OInteger;
import com.odoo.orm.types.OReal;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;
import com.odoo.support.OUser;

public class SaleOrder extends OModel {
	Context mContext = null;
	OColumn name = new OColumn("name", OVarchar.class, 64);
	OColumn date_order = new OColumn("Date", ODateTime.class);
	OColumn partner_id = new OColumn("Customer", ResPartner.class,
			RelationType.ManyToOne);
	OColumn user_id = new OColumn("Salesperson", ResUsers.class,
			RelationType.ManyToOne);
	OColumn amount_total = new OColumn("Total", OInteger.class);
	OColumn amount_untaxed = new OColumn("Untaxed", OInteger.class);
	OColumn amount_tax = new OColumn("Tax", OInteger.class);

	// OColumn pricelist_id = new OColumn("Pricelist",,RelationType.ManyToOne);
	// OColumn partner_shipping_id = new
	// OColumn("Delivery Address",ResPartners.class,RelationType.ManyToOne);
	// OColumn partner_invoice_id = new
	// OColumn("Invoice Address",ResPartners.class,RelationType.ManyToOne);
	OColumn client_order_ref = new OColumn("Client Order Reference",
			OVarchar.class, 100);
	OColumn state = new OColumn("status", OVarchar.class, 10)
			.setDefault("draft");
	OColumn currency_id = new OColumn("currency", ResCurrency.class,
			RelationType.ManyToOne).setRequired(true);
	OColumn order_line = new OColumn("Order Lines", SalesOrderLine.class,
			RelationType.OneToMany).setRelatedColumn("order_id");

	public SaleOrder(Context context) {
		super(context, "sale.order");
		mContext = context;
	}

	@Override
	public ODomain defaultDomain() {
		ODomain domain = new ODomain();
		domain.add("user_id", "=", OUser.current(mContext).getUser_id());
		return domain;
	}

	public static class SalesOrderLine extends OModel {

		OColumn product_id = new OColumn("Product Id", ProductProduct.class,
				RelationType.ManyToOne);
		OColumn name = new OColumn("Name", OText.class);
		OColumn product_uom_qty = new OColumn("Quantity", OInteger.class);
		// OColumn tax_id = new OColumn("Tax Id",,RelationType.ManyToMany);
		OColumn price_unit = new OColumn("Unit Price", OReal.class);
		OColumn price_subtotal = new OColumn("Sub Total", OInteger.class);
		// OColumn sequence = new OColumn("Sequence", OInteger.class);
		OColumn order_id = new OColumn("ID", SaleOrder.class,
				RelationType.ManyToOne);

		// OColumn product_uom = new OColumn("Unit of Measure",,
		// RelationType.ManyToOne);
		public SalesOrderLine(Context context) {
			super(context, "sale.order.line");
		}
	}

	public static class ProductProduct extends OModel {

		OColumn lst_price = new OColumn("Public price", OInteger.class);
		OColumn name_template = new OColumn("Product Name", OText.class);

		public ProductProduct(Context context) {
			super(context, "product.product");
		}
	}
}
