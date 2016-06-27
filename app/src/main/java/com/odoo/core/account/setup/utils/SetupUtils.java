package com.odoo.core.account.setup.utils;

import android.content.Context;
import android.util.Log;

import com.odoo.App;
import com.odoo.core.orm.OModel;
import com.odoo.core.support.OUser;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import dalvik.system.DexFile;

public class SetupUtils {

    public static final String TAG = SetupUtils.class.getCanonicalName();
    private Context mContext;
    private OUser user;

    public SetupUtils(Context context, OUser user) {
        mContext = context;
        this.user = user;
    }

    public HashMap<Priority, List<Class<? extends OModel>>> getSetupModels() {
        Log.d(TAG, "Creating setup model lists");
        HashMap<Priority, List<Class<? extends OModel>>> setupModels = new HashMap<>();

        for (Class<? extends OModel> cls : getModels().values()) {
            OdooSetup.Model setupModel = cls.getAnnotation(OdooSetup.Model.class);
            if (setupModel != null) {
                List<Class<? extends OModel>> models = new ArrayList<>();
                if (setupModels.containsKey(setupModel.value())) {
                    models.addAll(setupModels.get(setupModel.value()));
                }
                models.add(cls);
                setupModels.put(setupModel.value(), models);
            }
        }
        return setupModels;
    }

    public HashMap<String, Class<? extends OModel>> getModels() {
        HashMap<String, Class<? extends OModel>> models = new HashMap<>();
        try {
            DexFile dexFile = new DexFile(mContext.getPackageCodePath());
            String packageName = App.class.getPackage().getName();
            for (Enumeration<String> item = dexFile.entries(); item.hasMoreElements(); ) {
                String element = item.nextElement();
                if (element.startsWith(packageName)) {
                    try {
                        Class<? extends OModel> cls = (Class<? extends OModel>) Class.forName(element);
                        if (cls.getSuperclass() == OModel.class) {
                            Constructor constructor = cls.getConstructor(Context.class, OUser.class);
                            OModel model = (OModel) constructor.newInstance(mContext, user);
                            models.put(model.getModelName(), cls);
                        }

                    } catch (Exception e) {
                        Log.d(TAG, "Unable to load class : " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return models;
    }
}
