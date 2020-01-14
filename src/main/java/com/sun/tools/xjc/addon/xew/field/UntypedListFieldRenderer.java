package com.sun.tools.xjc.addon.xew.field;

import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.xjc.addon.xew.config.AbstractConfigurablePlugin;
import com.sun.tools.xjc.addon.xew.config.GlobalConfiguration;
import com.sun.tools.xjc.generator.bean.ClassOutlineImpl;
import com.sun.tools.xjc.generator.bean.field.FieldRenderer;
import com.sun.tools.xjc.model.CPropertyInfo;
import com.sun.tools.xjc.outline.FieldOutline;
import org.jvnet.jaxb2_commons.util.CustomizationUtils;

import java.util.Collection;


public class UntypedListFieldRenderer implements FieldRenderer {
    private boolean dummy;
    private boolean content;

    public UntypedListFieldRenderer() {
        this(false, false);
    }

    public UntypedListFieldRenderer(boolean dummy, boolean content) {
        this.dummy = dummy;
        this.content = content;
    }

    @Override
    public FieldOutline generate(ClassOutlineImpl context, CPropertyInfo prop) {
        try {
            GlobalConfiguration configuration = new GlobalConfiguration();

            AbstractConfigurablePlugin.applyConfigurationFromCustomizations(configuration,
                    CustomizationUtils.getCustomizations(context.parent().getModel()), false);

            AbstractConfigurablePlugin.applyConfigurationFromCustomizations(configuration,
                    CustomizationUtils.getCustomizations(context), false);

            JDefinedClass interfaceList;
            try {
                interfaceList = context.parent().getCodeModel()._class(configuration.getCollectionInterfaceClass());
                interfaceList.hide();
                interfaceList._extends(Collection.class);
            } catch (JClassAlreadyExistsException e) {
                interfaceList = e.getExistingClass();
            }

            JDefinedClass implementationList;
            try {
                implementationList = context.parent().getCodeModel()._class(configuration.getCollectionImplClass());
                implementationList.hide();
                implementationList._extends(Collection.class);
            } catch (JClassAlreadyExistsException e) {
                implementationList = e.getExistingClass();
            }

            if (dummy) {
                return new DummyListField(context, prop, interfaceList, implementationList);
            } else if (content) {
                return new ContentListField(context, prop, interfaceList, implementationList);
            } else {
                return new UntypedListField(context, prop, interfaceList, implementationList);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
