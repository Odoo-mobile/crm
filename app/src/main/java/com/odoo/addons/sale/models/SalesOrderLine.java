/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 *
 * Created on 13/1/15 11:08 AM
 */
package com.odoo.addons.sale.models;

import android.content.Context;

import com.odoo.core.orm.OModel;
import com.odoo.core.orm.fields.OColumn;
import com.odoo.core.orm.fields.types.OFloat;
import com.odoo.core.orm.fields.types.OInteger;
import com.odoo.core.orm.fields.types.OText;
import com.odoo.core.support.OUser;

public class SalesOrderLine extends OModel {
    public static final String TAG = SalesOrderLine.class.getSimpleName();
    OColumn product_id = new OColumn("Product", ProductProduct.class,
            OColumn.RelationType.ManyToOne);
    OColumn name = new OColumn("Description ", OText.class);
    OColumn product_uom_qty = new OColumn("Quantity", OInteger.class);
    OColumn price_unit = new OColumn("Unit Price", OFloat.class);
    OColumn price_subtotal = new OColumn("Sub Total", OFloat.class);
    OColumn order_id = new OColumn("ID", SaleOrder.class,
            OColumn.RelationType.ManyToOne);

    public SalesOrderLine(Context context, OUser user) {
        super(context, "sale.order.line", user);
    }
}
