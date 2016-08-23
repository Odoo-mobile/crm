/**
 * Odoo, Open Source Management Solution
 * Copyright (C) 2012-today Odoo SA (<http:www.odoo.com>)
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details
 * <p/>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http:www.gnu.org/licenses/>
 * <p/>
 * Created on 5/1/15 5:55 PM
 */
package com.odoo.config;

public class BaseConfig {
    public static final boolean DEVELOPER_MODE = true;

    // Odoo module dependencies
    public static String[] DEPENDS_ON_MODULES = {"sale", "crm"};

    // User groups required for access application
    public static String[] USER_GROUPS = {
            "base.group_sale_manager",
            "base.group_sale_salesman_all_leads",
            "base.group_sale_salesman"
    };
}


