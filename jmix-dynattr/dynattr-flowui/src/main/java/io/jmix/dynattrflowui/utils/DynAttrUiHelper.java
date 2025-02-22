/*
 * Copyright 2022 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.dynattrflowui.utils;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.icon.VaadinIcon;
import io.jmix.core.entity.HasManagedEntity;
import io.jmix.dynattrflowui.panel.dynamicwizard.DynamicFormPanel;
import io.jmix.flowui.Dialogs;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.model.CollectionContainer;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;

@Component("dynat_DynAttrUiHelper")
public class DynAttrUiHelper {

    private final Dialogs dialogs;
    private final UiComponents uiComponents;

    public DynAttrUiHelper(Dialogs dialogs, UiComponents uiComponents) {
        this.dialogs = dialogs;
        this.uiComponents = uiComponents;
    }

    /**
     * Recursively traverses the component tree starting from the given root component
     * and returns the first component that implements the HasManagedEntity interface.
     *
     * @param component the starting point in the component tree
     * @return the found component as a HasManagedEntity, or null if none is found
     */
    public HasManagedEntity findManagedEntity(com.vaadin.flow.component.Component component) {
        return findComponent(component, HasManagedEntity.class);
    }

    /**
     * Recursively traverses the component tree starting from the given root component
     * and returns the first component that is an instance of DynamicFormPanel.
     *
     * @param component the starting point in the component tree
     * @return the found component as a DynamicFormPanel, or null if none is found
     */
    public DynamicFormPanel findDynamicFormPanel(com.vaadin.flow.component.Component component) {
        return findComponent(component, DynamicFormPanel.class);
    }

    public <T> void moveTableItemUp(CollectionContainer<T> collectionContainer, Grid<T> table, Runnable afterMoveCommand) {
        List<T> items = collectionContainer.getMutableItems();
        T currentItem = collectionContainer.getItemOrNull();
        if (currentItem == null) {
            currentItem = table.getSelectedItems().iterator().next();
        }
        int idx = items.indexOf(currentItem);
        if (idx == 0) return;
        items.remove(currentItem);
        items.add(idx - 1, currentItem);
        table.select(currentItem);
        collectionContainer.setItem(currentItem);
        if (afterMoveCommand != null) {
            afterMoveCommand.run();
        }
        table.getDataProvider().refreshAll();
    }

    public <T> void moveTableItemDown(CollectionContainer<T> collectionContainer, Grid<T> table, Runnable afterMoveCommand) {
        List<T> items = collectionContainer.getMutableItems();
        T currentItem = collectionContainer.getItemOrNull();
        if (currentItem == null) {
            currentItem = table.getSelectedItems().iterator().next();
        }
        int idx = items.indexOf(currentItem);
        if (idx == items.size() - 1) return;
        items.remove(currentItem);
        items.add(idx + 1, currentItem);
        table.select(currentItem);
        collectionContainer.setItem(currentItem);
        if (afterMoveCommand != null) {
            afterMoveCommand.run();
        }
        table.getDataProvider().refreshAll();
    }

    public JmixButton createHelperButton(String message) {
        JmixButton helperButton = uiComponents.create(JmixButton.class);
        helperButton.setIcon(VaadinIcon.QUESTION_CIRCLE.create());
        helperButton.addThemeVariants(ButtonVariant.LUMO_ICON, ButtonVariant.LUMO_TERTIARY_INLINE);
        helperButton.addClickListener(event ->
                dialogs.createMessageDialog()
                        .withMinWidth("10em")
                        .withMaxWidth("40em")
                        .withMinHeight("10em")
                        .withContent(new Html(MessageFormat.format("<div>{0}</div>", message)))
                        .open());
        return helperButton;
    }

    /**
     * Recursively traverses the component tree starting from the given root component
     * and returns the first component that is an instance of the specified target class.
     *
     * @param component   the starting point in the component tree
     * @param targetClass the class to search for
     * @param <T>         the type parameter representing the target class
     * @return the found component as type T, or null if no matching component is found
     */
    public <T> T findComponent(com.vaadin.flow.component.Component component, Class<T> targetClass) {
        if (targetClass.isInstance(component)) {
            return targetClass.cast(component);
        }

        // Iterate over all child components
        for (com.vaadin.flow.component.Component child : component.getChildren().toList()) {
            T found = findComponent(child, targetClass);
            if (found != null) {
                return found;
            }
        }

        return null;
    }
}
