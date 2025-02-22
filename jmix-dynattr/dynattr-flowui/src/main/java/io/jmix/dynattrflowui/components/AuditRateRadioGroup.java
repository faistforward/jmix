package io.jmix.dynattrflowui.components;

import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup;

public class AuditRateRadioGroup extends JmixRadioButtonGroup<Integer> {

    public AuditRateRadioGroup() {

    }

    public void init(String label) {
        setLabel(label);
        setItems(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        setValue(5);
    }
}
