/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplatesflowui.kit.component.event.dom;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.DomEvent;
import com.vaadin.flow.component.EventData;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJs;

/**
 * The DOM event is fired at the moment of updating the {@link GrapesJs} template on the client-side.
 */
@DomEvent("value-changed")
public class GrapesJsValueChangedDomEvent extends ComponentEvent<GrapesJs> {

    protected final String value;

    public GrapesJsValueChangedDomEvent(GrapesJs source, boolean fromClient,
                                        @EventData("event.detail.value") String value) {
        super(source, fromClient);
        this.value = value;
    }

    /**
     * @return updated GrapesJs template value
     */
    public String getValue() {
        return value;
    }
}
