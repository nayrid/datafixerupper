/*
 * MIT License
 *
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Documentation Copyright (c) kvverti, 2020-2021. All rights reserved.
 * Further modifications Copyright (c) nayrid, 2025. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.mojang.serialization;

import com.google.common.collect.ImmutableMap;
import java.util.function.UnaryOperator;

public interface RecordBuilder<T> {

    DynamicOps<T> ops();

    RecordBuilder<T> add(T key, T value);

    RecordBuilder<T> add(T key, DataResult<T> value);

    RecordBuilder<T> add(DataResult<T> key, DataResult<T> value);

    RecordBuilder<T> withErrorsFrom(final DataResult<?> result);

    RecordBuilder<T> setLifecycle(Lifecycle lifecycle);

    RecordBuilder<T> mapError(UnaryOperator<String> onError);

    DataResult<T> build(T prefix);

    default DataResult<T> build(final DataResult<T> prefix) {
        return prefix.flatMap(this::build);
    }

    default RecordBuilder<T> add(final String key, final T value) {
        return add(ops().createString(key), value);
    }

    default RecordBuilder<T> add(final String key, final DataResult<T> value) {
        return add(ops().createString(key), value);
    }

    default <E> RecordBuilder<T> add(final String key, final E value, final Encoder<E> encoder) {
        return add(key, encoder.encodeStart(ops(), value));
    }

    abstract class AbstractBuilder<T, R> implements RecordBuilder<T> {

        private final DynamicOps<T> ops;
        protected DataResult<R> builder = DataResult.success(initBuilder(), Lifecycle.stable());

        protected AbstractBuilder(final DynamicOps<T> ops) {
            this.ops = ops;
        }

        @Override
        public DynamicOps<T> ops() {
            return ops;
        }

        protected abstract R initBuilder();

        protected abstract DataResult<T> build(final R builder, final T prefix);

        @Override
        public DataResult<T> build(final T prefix) {
            final DataResult<T> result = builder.flatMap(b -> build(b, prefix));
            builder = DataResult.success(initBuilder(), Lifecycle.stable());
            return result;
        }

        @Override
        public RecordBuilder<T> withErrorsFrom(final DataResult<?> result) {
            builder = builder.flatMap(v -> result.map(r -> v));
            return this;
        }

        @Override
        public RecordBuilder<T> setLifecycle(final Lifecycle lifecycle) {
            builder = builder.setLifecycle(lifecycle);
            return this;
        }

        @Override
        public RecordBuilder<T> mapError(final UnaryOperator<String> onError) {
            builder = builder.mapError(onError);
            return this;
        }

    }

    abstract class AbstractStringBuilder<T, R> extends AbstractBuilder<T, R> {

        protected AbstractStringBuilder(final DynamicOps<T> ops) {
            super(ops);
        }

        protected abstract R append(String key, T value, R builder);

        @Override
        public RecordBuilder<T> add(final String key, final T value) {
            builder = builder.map(b -> append(key, value, b));
            return this;
        }

        @Override
        public RecordBuilder<T> add(final String key, final DataResult<T> value) {
            builder = builder.apply2stable((b, v) -> append(key, v, b), value);
            return this;
        }

        @Override
        public RecordBuilder<T> add(final T key, final T value) {
            builder = ops().getStringValue(key).flatMap(k -> {
                add(k, value);
                return builder;
            });
            return this;
        }

        @Override
        public RecordBuilder<T> add(final T key, final DataResult<T> value) {
            builder = ops().getStringValue(key).flatMap(k -> {
                add(k, value);
                return builder;
            });
            return this;
        }

        @Override
        public RecordBuilder<T> add(final DataResult<T> key, final DataResult<T> value) {
            builder = key.flatMap(ops()::getStringValue).flatMap(k -> {
                add(k, value);
                return builder;
            });
            return this;
        }

    }

    abstract class AbstractUniversalBuilder<T, R> extends AbstractBuilder<T, R> {

        protected AbstractUniversalBuilder(final DynamicOps<T> ops) {
            super(ops);
        }

        protected abstract R append(T key, T value, R builder);

        @Override
        public RecordBuilder<T> add(final T key, final T value) {
            builder = builder.map(b -> append(key, value, b));
            return this;
        }

        @Override
        public RecordBuilder<T> add(final T key, final DataResult<T> value) {
            builder = builder.apply2stable((b, v) -> append(key, v, b), value);
            return this;
        }

        @Override
        public RecordBuilder<T> add(final DataResult<T> key, final DataResult<T> value) {
            builder = builder.ap(key.apply2stable((k, v) -> b -> append(k, v, b), value));
            return this;
        }

    }

    final class MapBuilder<T> extends AbstractUniversalBuilder<T, ImmutableMap.Builder<T, T>> {

        public MapBuilder(final DynamicOps<T> ops) {
            super(ops);
        }

        @Override
        protected ImmutableMap.Builder<T, T> initBuilder() {
            return ImmutableMap.builder();
        }

        @Override
        protected ImmutableMap.Builder<T, T> append(final T key,
                                                    final T value,
                                                    final ImmutableMap.Builder<T, T> builder) {
            return builder.put(key, value);
        }

        @Override
        protected DataResult<T> build(final ImmutableMap.Builder<T, T> builder, final T prefix) {
            return ops().mergeToMap(prefix, builder.buildKeepingLast());
        }

    }

}
