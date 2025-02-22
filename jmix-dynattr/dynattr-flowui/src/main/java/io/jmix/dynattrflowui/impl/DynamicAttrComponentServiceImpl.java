package io.jmix.dynattrflowui.impl;


import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.spring.annotation.UIScope;
import io.jmix.core.annotation.GroovyContextBean;
import io.jmix.core.entity.EntityValues;
import io.jmix.dynattrflowui.service.DynamicAttrComponentService;
import io.jmix.dynattrflowui.utils.DynAttrUiHelper;
import org.springframework.stereotype.Service;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service("dynAttributeService")
@UIScope
@GroovyContextBean
public class DynamicAttrComponentServiceImpl implements DynamicAttrComponentService {
    private final Map<EntityAttributeKey, Component> uiScopedDynamicAttributeComponents = new HashMap<>();
    private final DynAttrUiHelper dynAttrUiHelper;

    public DynamicAttrComponentServiceImpl(DynAttrUiHelper dynAttrUiHelper) {
        this.dynAttrUiHelper = dynAttrUiHelper;
    }

    @Override
    public void register(Object entity, String attributeCode, Component component) {
        uiScopedDynamicAttributeComponents.put(new EntityAttributeKey(entity, attributeCode), component);
    }

    @Override
    public void visible(String attributeCode, boolean visible) {
        var componentWithManagedEntity = dynAttrUiHelper.findManagedEntity(UI.getCurrent());
        if (componentWithManagedEntity != null) {
            visible(dynAttrUiHelper.findManagedEntity(UI.getCurrent()).getManagedEntity(), attributeCode, visible);
        }
    }

    @Override
    public void visible(Object entity, String attributeCode, boolean visible) {
        var component = uiScopedDynamicAttributeComponents.get(new EntityAttributeKey(entity, attributeCode));
        component.setVisible(visible);
        component.getChildren().forEach(comp -> comp.setVisible(visible));
        if (!visible) {
            EntityValues.setValue(entity, "+" + attributeCode, null);
        }
    }

    public final class EntityAttributeKey {
        private final WeakReference<Object> entityRef;
        private final int entityHash;
        private final String attributeCode;

        public EntityAttributeKey(Object entity, String attributeCode) {
            // Speichere eine schwache Referenz auf die Entity
            this.entityRef = new WeakReference<>(entity);
            // Berechne und speichere den Hash-Code der Entity zum Zeitpunkt der Erstellung
            this.entityHash = System.identityHashCode(entity);
            this.attributeCode = attributeCode;
        }

        @Override
        public int hashCode() {
            return 31 * entityHash + Objects.hashCode(attributeCode);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof EntityAttributeKey other)) return false;
            // Hole die referenzierten Entities
            Object thisEntity = this.entityRef.get();
            Object otherEntity = other.entityRef.get();
            // Falls eine der Entity-Instanzen schon gesammelt wurde, sind die Schlüssel nicht gleich
            if (thisEntity == null || otherEntity == null) return false;
            // Vergleiche Entities per Referenz (oder passe das an, falls eine andere Vergleichslogik benötigt wird)
            return thisEntity == otherEntity && Objects.equals(this.attributeCode, other.attributeCode);
        }
    }
}
