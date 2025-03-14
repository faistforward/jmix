/*
 * Copyright 2025 Haulmont.
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

package io.jmix.tabbedmode.builder;

import io.jmix.flowui.view.View;

import java.util.function.Consumer;
import java.util.function.Function;

public class ViewBuilder<V extends View<?>> extends AbstractViewBuilder<V, ViewBuilder<V>> {

    public ViewBuilder(View<?> origin,
                       String viewId,
                       Function<ViewBuilder<V>, V> handler,
                       Consumer<ViewOpeningContext> openHandler) {
        super(origin, handler, openHandler);

        this.viewId = viewId;
    }

    public ViewBuilder(View<?> origin,
                       Class<V> viewClass,
                       Function<ViewBuilder<V>, V> handler,
                       Consumer<ViewOpeningContext> openHandler) {
        super(origin, handler, openHandler);

        this.viewClass = viewClass;
    }
}
