package io.jmix.dynattrflowui.panel.dynamicwizard;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.Messages;

public class DanamicWizardSaveTab extends VerticalLayout {

    protected Messages messages;

    public DanamicWizardSaveTab(Messages messages) {
        add(messages.getMessage(getClass(), "dynattrWizardSaveTabTitle"));
    }
}
