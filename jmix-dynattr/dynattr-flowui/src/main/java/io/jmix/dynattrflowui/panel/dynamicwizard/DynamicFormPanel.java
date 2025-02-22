package io.jmix.dynattrflowui.panel.dynamicwizard;

import com.google.common.base.Strings;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.dynattr.AttributeDefinition;
import io.jmix.dynattr.CategoryDefinition;
import io.jmix.dynattr.DynAttrMetadata;
import io.jmix.dynattr.model.Categorized;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import io.jmix.dynattrflowui.service.DynamicAttrComponentService;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.component.ComponentContainer;
import io.jmix.flowui.component.ComponentGenerationContext;
import io.jmix.flowui.component.UiComponentUtils;
import io.jmix.flowui.component.UiComponentsGenerator;
import io.jmix.flowui.component.formlayout.JmixFormLayout;
import io.jmix.flowui.data.ValueSource;
import io.jmix.flowui.data.value.ContainerValueSource;
import io.jmix.flowui.model.InstanceContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.View;
import io.jmix.flowui.view.ViewControllerUtils;
import io.jmix.flowui.view.ViewValidation;
import org.springframework.lang.Nullable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.jmix.flowui.component.UiComponentUtils.sameId;

public class DynamicFormPanel extends Composite<VerticalLayout> implements HasSize, ComponentContainer {

    public static final String NAME = "dynamicAttributesPanel";

    public static final String DEFAULT_FIELD_WIDTH = "100%";

    protected final UiComponentsGenerator uiComponentsGenerator;
    protected final UiComponents uiComponents;
    protected final Messages messages;
    protected final DynAttrMetadata dynAttrMetadata;
    protected final ViewValidation viewValidation;

    protected InstanceContainer<?> instanceContainer;
    protected String fieldWidth = DEFAULT_FIELD_WIDTH;
    protected VerticalLayout rootPanel;
    protected VerticalLayout categoryFieldBox;
    protected H3 categoryFieldLabel;
    protected JmixFormLayout propertiesForm;
    protected List<CategoryAttribute> attributesToShowInGroup;
    protected Category category;
    protected DynamicAttrComponentService dynamicAttrComponentService;

    public DynamicFormPanel(UiComponentsGenerator uiComponentsGenerator,
                            UiComponents uiComponents,
                            Messages messages,
                            DynAttrMetadata dynAttrMetadata,
                            List<CategoryAttribute> attributesToShowInGroup,
                            ViewValidation viewValidation,
                            InstanceContainer<?> instanceContainer,
                            Category category,
                            Boolean readOnly,
                            DynamicAttrComponentService dynamicAttrComponentService
    ) {
        this.dynamicAttrComponentService = dynamicAttrComponentService;
        this.uiComponentsGenerator = uiComponentsGenerator;
        this.uiComponents = uiComponents;
        this.messages = messages;
        this.dynAttrMetadata = dynAttrMetadata;
        this.attributesToShowInGroup = attributesToShowInGroup;
        this.viewValidation = viewValidation;
        this.instanceContainer = instanceContainer;
        this.category = category;

        rootPanel = uiComponents.create(VerticalLayout.class);
        rootPanel.setPadding(false);
        rootPanel.setSpacing(true);
        rootPanel.setWidth("100%");

        categoryFieldBox = uiComponents.create(VerticalLayout.class);
        categoryFieldBox.setPadding(false);
        categoryFieldBox.setMargin(false);
        categoryFieldBox.setWidth("100%");
        categoryFieldBox.setSpacing(true);

        categoryFieldLabel = uiComponents.create(H3.class);
        categoryFieldLabel.setText(messages.getMessage(getClass(), "category"));


        propertiesForm = uiComponents.create(JmixFormLayout.class);
        propertiesForm.setResponsiveSteps(new FormLayout.ResponsiveStep("250px", 1));
        propertiesForm.setWidth("100%");
        rootPanel.add(categoryFieldBox, propertiesForm);
        rootPanel.expand(propertiesForm);

        initPropertiesForm();
        if (readOnly) {
            applyReadOnlyMode();
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        View<?> view = UiComponentUtils.findView(this);
        if (view instanceof StandardDetailView<?> detailView) {
            ViewControllerUtils.addValidationEventListener(detailView, this::onValidation);
        }

        super.onAttach(attachEvent);
    }

    protected void onValidation(StandardDetailView.ValidationEvent validationEvent) {
        validationEvent.addErrors(viewValidation.validateUiComponents(propertiesForm));
    }


    @Override
    protected VerticalLayout initContent() {
        VerticalLayout content = super.initContent();
        content.setMargin(false);
        content.setPadding(false);
        content.setId("dynAttrPanelLayout");
        content.add(rootPanel);
        return content;
    }

    protected void initPropertiesForm() {
        propertiesForm.removeAll();

        Map<AttributeDefinition, Component> fields = new HashMap<>();
        for (AttributeDefinition attribute : getAttributesByCategory()) {
            if (attributesToShowInGroup.stream().anyMatch(a -> a.getCode().equals(attribute.getCode()))) {
                Component resultComponent = generateFieldComponent(attribute);
                dynamicAttrComponentService.register(instanceContainer.getItem(), attribute.getCode(), resultComponent);
                fields.put(attribute, resultComponent);
            }
        }

        addFieldsToForm(propertiesForm, fields);
    }

    protected void addFieldsToForm(FormLayout newPropertiesForm, Map<AttributeDefinition, Component> fields) {
        if (fields.keySet().stream().anyMatch(attr -> attr.getConfiguration().getColumnNumber() != null
                && attr.getConfiguration().getRowNumber() != null)) {
            List<AttributeDefinition> attributesToAdd = fields.keySet().stream()
                    .filter(attr -> attr.getConfiguration().getColumnNumber() != null
                            && attr.getConfiguration().getRowNumber() != null)
                    .toList();

            int maxColumnIndex = attributesToAdd.stream()
                    .mapToInt(attr -> attr.getConfiguration().getColumnNumber())
                    .max()
                    .orElse(0);
            newPropertiesForm.setResponsiveSteps(Stream.of(maxColumnIndex)
                    .map(i -> new FormLayout.ResponsiveStep("0px", i))
                    .toList());
            for (int i = 0; i <= maxColumnIndex; i++) {
                int columnIndex = i;
                List<AttributeDefinition> columnAttributes = attributesToAdd.stream()
                        .filter(attr -> attr.getConfiguration().getColumnNumber() != null && attr.getConfiguration().getRowNumber() != null)
                        .filter(attr -> columnIndex == attr.getConfiguration().getColumnNumber())
                        .sorted(Comparator.comparing(attr -> attr.getConfiguration().getRowNumber()))
                        .toList();
                int currentRowNumber = 0;
                for (AttributeDefinition attr : columnAttributes) {
                    //noinspection DataFlowIssue
                    while (attr.getConfiguration().getRowNumber() > currentRowNumber) {
                        //add empty row
                        newPropertiesForm.add(createEmptyComponent());
                        currentRowNumber++;
                    }

                    newPropertiesForm.add(fields.get(attr));
                    currentRowNumber++;
                }
            }
        } else {
            List<Component> sortedAttributeFields = fields.entrySet().stream()
                    .sorted(Comparator.comparingInt(e -> e.getKey().getOrderNo()))
                    .map(Map.Entry::getValue)
                    .toList();
            for (Component field : sortedAttributeFields) {
                newPropertiesForm.add(field);
            }
        }
    }

    private Component createEmptyComponent() {
        Text component = uiComponents.create(Text.class);
        component.setText("\u2060");
        return component;
    }

    protected Collection<AttributeDefinition> getAttributesByCategory() {
        if (category != null) {
            for (CategoryDefinition categoryDefinition : dynAttrMetadata.getCategories(instanceContainer.getEntityMetaClass())) {
                if (category.equals(categoryDefinition.getSource())) {
                    return categoryDefinition.getAttributeDefinitions();
                }
            }
        }
        return Collections.emptyList();
    }

    protected Component generateFieldComponent(AttributeDefinition attribute) {
        MetaProperty metaProperty = attribute.getMetaProperty();
        ValueSource<?> valueSource = new ContainerValueSource<>(instanceContainer, metaProperty.getName());

        ComponentGenerationContext componentContext =
                new ComponentGenerationContext(instanceContainer.getEntityMetaClass(), metaProperty.getName());
        componentContext.setValueSource(valueSource);

        Component resultComponent = uiComponentsGenerator.generate(componentContext);

        setWidth(resultComponent, attribute);

        return resultComponent;
    }

    protected void setWidth(Component component, AttributeDefinition attribute) {
        String formWidth = attribute.getConfiguration().getFormWidth();
        if (!Strings.isNullOrEmpty(formWidth) && component instanceof HasSize) {
            ((HasSize) component).setWidth(formWidth);
        } else {
            ((HasSize) component).setWidth(fieldWidth);
        }
    }


    @Nullable
    protected Category getDefaultCategory() {
        for (CategoryDefinition category : getCategoryDefinitions()) {
            if (category != null && category.isDefault()) {
                return (Category) category.getSource();
            }
        }
        return null;
    }

    protected Collection<CategoryDefinition> getCategoryDefinitions() {
        return dynAttrMetadata.getCategories(instanceContainer.getEntityMetaClass());
    }

    protected List<Category> getCategoriesOptionsList() {
        Collection<CategoryDefinition> options = getCategoryDefinitions();
        return options.stream().
                map(definition -> (Category) definition.getSource())
                .collect(Collectors.toList());
    }

    protected void onInstanceContainerItemChangeEvent(InstanceContainer.ItemChangeEvent<?> event) {
        if (event.getItem() instanceof Categorized
                && ((Categorized) event.getItem()).getCategory() == null) {
            ((Categorized) event.getItem()).setCategory(getDefaultCategory());
        }
        if (event.getItem() == null) {
            propertiesForm.removeAll();
        }
    }

    @Override
    public Optional<Component> findOwnComponent(String id) {
        return getOwnComponents().stream()
                .filter(component -> sameId(component, id))
                .findAny();
    }

    @Override
    public Collection<Component> getOwnComponents() {
        return List.of(categoryFieldLabel, propertiesForm);
    }

    /**
     * Sets the visibility of the category field.
     *
     * @param visible true to make the category field visible, false otherwise
     */
    public void setCategoryFieldVisible(boolean visible) {
        categoryFieldBox.setVisible(visible);
    }


    /**
     * Sets the width of the fields. This parameter is used if some dynamic attribute does not have own width value.
     *
     * @param fieldWidth width of the fields
     */
    public void setFieldWidth(String fieldWidth) {
        this.fieldWidth = fieldWidth;
    }

    /**
     * Prüft, ob alle Felder im Formular die Validierungen bestehen.
     *
     * @return true, wenn keine Validierungsfehler vorliegen, ansonsten false.
     */
    public boolean isValid() {
        // Die Methode validateUiComponents liefert eine Collection von Validierungsfehlern zurück.
        // Ist diese Collection leer, sind alle Felder gültig.
        return viewValidation.validateUiComponents(propertiesForm).isEmpty();
    }


    /**
     * Applies the global read-only mode to all fields in the form recursively.
     */
    private void applyReadOnlyMode() {
        // Apply read-only mode to all children of propertiesForm recursively
        propertiesForm.getChildren().forEach(this::applyReadOnlyRecursively);
    }

    /**
     * Recursively sets read-only mode for the given component and all its children.
     *
     * @param component the component to process
     */
    private void applyReadOnlyRecursively(Component component) {
        // If the component supports value input, set it to read-only
        if (component instanceof HasValue) {
            ((HasValue<?, ?>) component).setReadOnly(true);
        }
        // Recursively process all child components
        component.getChildren().forEach(this::applyReadOnlyRecursively);
    }
}
