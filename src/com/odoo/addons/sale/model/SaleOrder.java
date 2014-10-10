package com.odoo.addons.sale.model;

import java.util.HashMap;

import org.json.JSONArray;

import android.content.Context;

import com.odoo.addons.sale.providers.sale.ProductProvider;
import com.odoo.addons.sale.providers.sale.SalesOrderLineProvider;
import com.odoo.addons.sale.providers.sale.SalesProvider;
import com.odoo.base.res.ResCurrency;
import com.odoo.base.res.ResPartner;
import com.odoo.base.res.ResUsers;
import com.odoo.orm.OColumn;
import com.odoo.orm.OColumn.RelationType;
import com.odoo.orm.OModel;
import com.odoo.orm.OValues;
import com.odoo.orm.annotations.Odoo.Functional;
import com.odoo.orm.types.ODateTime;
import com.odoo.orm.types.OInteger;
import com.odoo.orm.types.OReal;
import com.odoo.orm.types.OText;
import com.odoo.orm.types.OVarchar;
import com.odoo.support.provider.OContentProvider;
import com.odoo.util.ODate;

public class SaleOrder extends OModel {
	Context mContext = null;
	OColumn name = new OColumn("name", OVarchar.class, 64);
	OColumn date_order = new OColumn("Date", ODateTime.class)
			.setParsePattern(ODate.DEFAULT_FORMAT);
	OColumn partner_id = new OColumn("Customer", ResPartner.class,
			RelationType.ManyToOne);
	OColumn user_id = new OColumn("Salesperson", ResUsers.class,
			RelationType.ManyToOne);
	OColumn amount_total = new OColumn("Total", OReal.class);
	OColumn amount_untaxed = new OColumn("Untaxed", OInteger.class);
	OColumn amount_tax = new OColumn("Tax", OInteger.class);
	OColumn client_order_ref = new OColumn("Client Order Reference",
			OVarchar.class, 100);
	OColumn state = new OColumn("status", OVarchar.class, 10)
			.setDefault("draft");
	@Functional(method = "getStateTitle", store = true, depends = { "state" })
	OColumn state_title = new OColumn("State Title", OVarchar.class)
			.setLocalColumn();
	OColumn currency_id = new OColumn("currency", ResCurrency.class,
			RelationType.ManyToOne);
	OColumn order_line = new OColumn("Order Lines", SalesOrderLine.class,
			RelationType.OneToMany).setRelatedColumn("order_id");
	@Functional(method = "countOrderLines", store = true, depends = { "order_line" })
	OColumn order_line_count = new OColumn("Total Lines", OVarchar.class, 64)
			.setLocalColumn();

	public SaleOrder(Context context) {
		super(context, "sale.order");
		mContext = context;
		if (user() != null && user().getVersion_number() == 7) {
			date_order.setParsePattern(ODate.DEFAULT_DATE_FORMAT);
		}
	}

	@Override
	public OContentProvider getContentProvider() {
		return new SalesProvider();
	}

	public String getStateTitle(OValues row) {
		HashMap<String, String> mStates = new HashMap<String, String>();
		mStates.put("draft", "Draft Quotation");
		mStates.put("sent", "Quotation Sent");
		mStates.put("cancel", "Cancelled");
		mStates.put("waiting_date", "Waiting Schedule");
		mStates.put("progress", "Sales Order");
		mStates.put("manual", "Sale to Invoice");
		mStates.put("shipping_except", "Shipping Exception");
		mStates.put("invoice_except", "Invoice Exception");
		mStates.put("done", "Done");
		return mStates.get(row.getString("state"));
	}

	public String countOrderLines(OValues vals) {
		try {
			JSONArray order_line = new JSONArray(vals.getString("order_line"));
			if (order_line.length() > 0) {
				return " (" + order_line.length() + " lines)";
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return " (No lines)";
	}

	public static class SalesOrderLine extends OModel {
		OColumn product_id = new OColumn("Product Id", ProductProduct.class,
				RelationType.ManyToOne);
		OColumn name = new OColumn("Description ", OText.class);
		OColumn product_uom_qty = new OColumn("Quantity", OInteger.class);
		OColumn price_unit = new OColumn("Unit Price", OReal.class);
		OColumn price_subtotal = new OColumn("Sub Total", OReal.class);
		OColumn order_id = new OColumn("ID", SaleOrder.class,
				RelationType.ManyToOne);

		public SalesOrderLine(Context context) {
			super(context, "sale.order.line");
		}

		@Override
		public OContentProvider getContentProvider() {
			return new SalesOrderLineProvider();
		}
	}

	public static class ProductProduct extends OModel {

		OColumn lst_price = new OColumn("Public price", OInteger.class);
		OColumn name_template = new OColumn("Product Name", OText.class);

		public ProductProduct(Context context) {
			super(context, "product.product");
		}

		@Override
		public OContentProvider getContentProvider() {
			return new ProductProvider();
		}
	}
}
