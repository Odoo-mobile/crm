/*
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
 */
package com.odoo.config;

import com.odoo.addons.crm.CRM;
import com.odoo.addons.crm.CRMPhoneCalls;
import com.odoo.addons.customers.Customers;
import com.odoo.addons.sale.Sales;
import com.odoo.support.OModule;
import com.odoo.support.OModulesHelper;

/**
 * The Class ModulesConfig.
 */
public class OModules extends OModulesHelper {

	OModule a_res_partner = new OModule(Customers.class).setDefault();
	OModule b_crm = new OModule(CRM.class);
	OModule c_sale = new OModule(Sales.class);
	OModule d_crm_phone_call = new OModule(CRMPhoneCalls.class);
}
