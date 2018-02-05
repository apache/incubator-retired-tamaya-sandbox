/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tamaya.collections;


import java.io.Serializable;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

/**
 * <p>Class for instantiation of objects that represent parameterized types
 * with current parameters.</p>
 *
 * <p>An object that represents a parameterized type may be obtained by
 * subclassing <tt>TypeLiteral</tt>.</p>
 *
 * <pre>
 * TypeLiteral&lt;List&lt;Integer&gt;&gt; stringListType = new TypeLiteral&lt;List&lt;Integer&gt;&gt;() {};
 * </pre>
 *
 * @param <T> the type, including all type parameters
 */
public class TypeLiteral<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Type[] EMPTY_TYPE_ARRAY = new Type[0];
    /** The current defined type. */
    private final Type definedType;

    /**
     * Constructor.
     * @param definedType the defined type.
     */
    public TypeLiteral(Type definedType) {
        Objects.requireNonNull(definedType, "Type must be given");

        this.definedType = definedType;
    }

    /**
     * Constructor only for directly implemeting a TypeLiteral hereby dynamically implementing a generic interface.
     */
    public TypeLiteral() {
        this.definedType = getDefinedType(this.getClass());
    }

    /**
     * Creates a new TypeLiteral based on a given type.
     *
     * @param type the type , not {@code null}.
     * @param <R>  the literal generic type.
     * @return the corresponding TypeLiteral, never {@code null}.
     */
    public static <R> TypeLiteral<R> of(Type type) {
        Objects.requireNonNull(type, "Type must be given.");

        return new TypeLiteral<R>(type);
    }

    public final Type getType() {
        return definedType;
    }

    /**
     * Returns basic raw Java type.
     *
     * @return the actual type represented by this object
     */
    @SuppressWarnings("unchecked")
	public final Class<T> getRawType() {
        Class<T> rawType;

        if (this.definedType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) this.definedType;
            rawType = (Class<T>) pt.getRawType();
        } else if (this.definedType instanceof GenericArrayType) {
            rawType = (Class<T>) Object[].class;
        } else if (this.definedType instanceof Class) {
            rawType = (Class<T>) this.definedType;
        } else {
            throw new RuntimeException("Illegal type for the Type Literal Class");
        }

        return rawType;
    }

    /**
     * Returns actual type arguments, if present.
     *
     * @return the actual type represented by defined class, or an empty array.
     */
    public final Type[] getActualTypeArguments() {
        if (this.definedType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) this.definedType;
            return pt.getActualTypeArguments();
        }
        return new Type[0];
    }


    protected Type getDefinedType(Class<?> clazz) {
        Type type;

        if (clazz == null) {
            throw new RuntimeException("Class parameter clazz can not be null");
        }

        Type superClazz = clazz.getGenericSuperclass();

        if (superClazz instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) superClazz;
            Type[] actualArgs = pt.getActualTypeArguments();

            if (actualArgs.length == 1) {
                type = actualArgs[0];

            } else {
                throw new RuntimeException("More than one parametric type");
            }

        } else if (superClazz.equals(Object.class)) {
            throw new RuntimeException("Super class must be parametrized type");
        } else {
            type = getDefinedType((Class<?>) superClazz);
        }

        return type;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((definedType == null) ? 0 : definedType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TypeLiteral<?> other = (TypeLiteral<?>) obj;
        if (definedType == null) {
            if (other.definedType != null) {
                return false;
            }
        } else if (!definedType.equals(other.definedType)) {
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        return "TypeLiteral{" +
                "type=" + definedType +
                '}';
    }

}
