package com.odoo.addons.sale;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.odoo.addons.sale.models.ProductProduct;
import com.odoo.core.orm.ODataRow;
import com.odoo.core.utils.OActionBarUtils;
import com.odoo.core.utils.logger.OLog;
import com.odoo.crm.R;

public class AddProductLineWizard extends ActionBarActivity implements AdapterView.OnItemSelectedListener, TextWatcher {

    TextView txvUnitPrice, txvSubTotal;
    EditText edtQty;
    Spinner spnProduct;
    ProductProduct mProduct;
    ArrayAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sale_add_item);
        OActionBarUtils.setActionBar(this, true);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add Order Line");
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        setResult(RESULT_CANCELED);
        init();
    }

    private void init() {
        mProduct = new ProductProduct(this, null);
        txvUnitPrice = (TextView) findViewById(R.id.txvPrice);
        txvSubTotal = (TextView) findViewById(R.id.txvSubTotal);
        edtQty = (EditText) findViewById(R.id.edtQty);
        edtQty.addTextChangedListener(this);
        spnProduct = (Spinner) findViewById(R.id.spnProduct);
        mAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item);
        for (ODataRow row : mProduct.select()) {
            mAdapter.add(row.getString("name_template"));
        }
        spnProduct.setAdapter(mAdapter);
        spnProduct.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sale_add_item, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_sale_add_item_done) {
            OLog.log(">>>>>>>>>>> Submit");
            Intent intent = new Intent();
            intent.putExtra("Item", "Done");
            setResult(RESULT_OK, intent);
        }
        finish();
        return true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        txvUnitPrice.setText("500.00");
        edtQty.setText("1");
        txvSubTotal.setText("500.00");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s != null && count != 0)
            txvSubTotal.setText("" + Integer.parseInt(s + "") * 500);
        else
            txvSubTotal.setText("");
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
