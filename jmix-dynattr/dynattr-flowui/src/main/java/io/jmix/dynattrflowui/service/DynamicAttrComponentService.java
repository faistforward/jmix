package io.jmix.dynattrflowui.service;

import com.vaadin.flow.component.Component;

public interface DynamicAttrComponentService {
    void register(Object entity, String attributeName, Component component);

    void visible(String attributeName, boolean visible);

    void visible(Object entity, String attributeName, boolean visible);
}
