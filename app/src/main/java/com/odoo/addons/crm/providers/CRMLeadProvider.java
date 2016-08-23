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
 * Created on 13/1/15 11:28 AM
 */
package com.odoo.addons.crm.providers;

import android.net.Uri;

import com.odoo.addons.crm.models.CRMLead;
import com.odoo.core.orm.provider.BaseModelProvider;

public class CRMLeadProvider extends BaseModelProvider {
    public static final String TAG = CRMLeadProvider.class.getSimpleName();

    @Override
    public void setModel(Uri uri) {
        super.setModel(uri);
        mModel = new CRMLead(getContext(), getUser(uri));
    }

    @Override
    public String authority() {
        return CRMLead.AUTHORITY;
    }
}
