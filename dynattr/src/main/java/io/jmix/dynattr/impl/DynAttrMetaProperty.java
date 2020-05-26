/*
 * Copyright 2020 Haulmont.
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

package io.jmix.dynattr.impl;

import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.model.*;
import io.jmix.core.metamodel.model.impl.ClassRange;
import io.jmix.core.metamodel.model.impl.DatatypeRange;
import io.jmix.core.metamodel.model.impl.MetadataObjectImpl;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Objects;

public class DynAttrMetaProperty extends MetadataObjectImpl implements MetaProperty {
    private static final long serialVersionUID = 839160118855669248L;

    private final MetaClass ownerMetaClass;
    private final Range range;
    private final Class<?> javaClass;

    protected final AnnotatedElement annotatedElement = new FakeAnnotatedElement();
    protected final Type type;

    public DynAttrMetaProperty(String name,
                               MetaClass ownerMetaClass,
                               Class<?> javaClass,
                               @Nullable MetaClass propertyMetaClass,
                               @Nullable Datatype<?> datatype) {

        this.ownerMetaClass = ownerMetaClass;
        this.javaClass = javaClass;
        this.name = name;

        if (propertyMetaClass != null) {
            this.range = new ClassRange(propertyMetaClass);
            this.type = Type.ASSOCIATION;
        } else {
            assert datatype != null;
            this.range = new DatatypeRange(datatype);
            this.type = Type.DATATYPE;
        }
    }

    @Nullable
    @Override
    public Session getSession() {
        return ownerMetaClass.getSession();
    }

    @Override
    public MetaClass getDomain() {
        return ownerMetaClass;
    }

    @Override
    public Range getRange() {
        return range;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public boolean isMandatory() {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public MetaProperty getInverse() {
        return null;
    }

    @Override
    public AnnotatedElement getAnnotatedElement() {
        return annotatedElement;
    }

    @Override
    public Class<?> getJavaType() {
        return javaClass;
    }

    @Override
    public Class<?> getDeclaringClass() {
        return null;
    }

    @Override
    public Store getStore() {
        return ownerMetaClass.getStore();
    }

    private static class FakeAnnotatedElement implements AnnotatedElement, Serializable {

        @Override
        public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
            return false;
        }

        @Override
        public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
            return null;
        }

        @Override
        public Annotation[] getAnnotations() {
            return new Annotation[0];
        }

        @Override
        public Annotation[] getDeclaredAnnotations() {
            return new Annotation[0];
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof DynAttrMetaProperty)) return false;

        DynAttrMetaProperty that = (DynAttrMetaProperty) o;

        return Objects.equals(ownerMetaClass, that.ownerMetaClass) && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return 31 * ownerMetaClass.hashCode() + name.hashCode();
    }
}
