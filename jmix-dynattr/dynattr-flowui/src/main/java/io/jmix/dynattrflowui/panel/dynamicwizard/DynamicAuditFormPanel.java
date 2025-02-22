package io.jmix.dynattrflowui.panel.dynamicwizard;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.details.DetailsVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattrflowui.service.DynamicAttrComponentService;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.UiComponentsGenerator;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.ViewValidation;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DynamicAuditFormPanel extends DynamicFormPanel {
    private DynamicAttrComponentService dynamicAttrComponentService;

    public DynamicAuditFormPanel(UiComponentsGenerator uiComponentsGenerator, UiComponents uiComponents, Messages messages, DynAttrMetadata dynAttrMetadata, List<CategoryAttribute> attributesToShowInGroup, ViewValidation viewValidation, InstanceContainer<?> instanceContainer, Category category, DynamicAttrComponentService dynamicAttrComponentService, Boolean readOnly) {
        super(uiComponentsGenerator, uiComponents, messages, dynAttrMetadata, attributesToShowInGroup, viewValidation, instanceContainer, category, readOnly, dynamicAttrComponentService);
    }

    @Override
    protected Component generateFieldComponent(AttributeDefinition attribute) {
        return attributesToShowInGroup.stream()
                .filter(attr -> attr.getCode().equals(attribute.getCode()))
                .filter(attr -> Boolean.TRUE.equals(attr.getWithRate()) || Boolean.TRUE.equals(attr.getWithNote()))
                .findFirst()
                .map(extAttr -> generateExtendedFieldComponent(extAttr, attribute))
                .or(() ->
                        attributesToShowInGroup.stream().filter(attr -> attr.getCode().equals(attribute.getCode())).findFirst()
                                .map(attr -> addToStandardLayout(List.of(super.generateFieldComponent(attribute)), attr.getVisible()))

                ).orElse(new Span());
    }

    private Component generateExtendedFieldComponent(CategoryAttribute attribute, AttributeDefinition attributeDef) {
        List<Component> componentList = new ArrayList<>();
        componentList.add(super.generateFieldComponent(attributeDef));

        if (attribute.getWithRate()) {
            var rateing = generaetCustomDynComponente(attributeDef, DynRateConstants.RATE);
            componentList.add(rateing);
        }

        if (attribute.getWithNote()) {
            Details details = new Details();
            details.setWidthFull();
            details.addThemeVariants(DetailsVariant.REVERSE);
            var textArea = generaetCustomDynComponente(attributeDef, DynRateConstants.NOTE);
            if (StringUtils.isNotBlank(attribute.getDescription())) {
                var descSpan = new Span(attribute.getDescription());
                descSpan.getElement().getStyle().set("font-size", "small");
                descSpan.getElement().getStyle().set("padding", "5px");
                details.add(descSpan);
            }
            details.add(textArea);
            componentList.add(details);
        }
        return addToStandardLayout(componentList, attribute.getVisible());
    }

    private VerticalLayout addToStandardLayout(List<Component> components, Boolean visible) {
        VerticalLayout vr = new VerticalLayout();
        vr.setWidthFull();
        vr.add(components);
        vr.getStyle().set("border-bottom", "1px solid gray");
        vr.setVisible(visible);
        return vr;
    }

    private Component generaetCustomDynComponente(AttributeDefinition attributeDef, String dynRateConstants) {

        MetaProperty metaProperty = attributeDef.getMetaProperty();
        ValueSource<?> valueSource = new ContainerValueSource<>(instanceContainer, metaProperty.getName() + dynRateConstants);

        ComponentGenerationContext componentContext =
                new ComponentGenerationContext(instanceContainer.getEntityMetaClass(), metaProperty.getName() + dynRateConstants);
        componentContext.setValueSource(valueSource);
        return uiComponentsGenerator.generate(componentContext);
    }

}
