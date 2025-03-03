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

import com.google.common.collect.ImmutableList;
import java.util.function.UnaryOperator;

public interface ListBuilder<T> {

    DynamicOps<T> ops();

    DataResult<T> build(T prefix);

    ListBuilder<T> add(final T value);

    ListBuilder<T> add(final DataResult<T> value);

    ListBuilder<T> withErrorsFrom(final DataResult<?> result);

    ListBuilder<T> mapError(UnaryOperator<String> onError);

    default DataResult<T> build(final DataResult<T> prefix) {
        return prefix.flatMap(this::build);
    }

    default <E> ListBuilder<T> add(final E value, final Encoder<E> encoder) {
        return add(encoder.encodeStart(ops(), value));
    }

    default <E> ListBuilder<T> addAll(final Iterable<E> values, final Encoder<E> encoder) {
        values.forEach(v -> encoder.encode(v, ops(), ops().empty()));
        return this;
    }

    final class Builder<T> implements ListBuilder<T> {

        private final DynamicOps<T> ops;
        private DataResult<ImmutableList.Builder<T>> builder = DataResult.success(ImmutableList.builder(),
            Lifecycle.stable()
        );

        public Builder(final DynamicOps<T> ops) {
            this.ops = ops;
        }

        @Override
        public DynamicOps<T> ops() {
            return ops;
        }

        @Override
        public ListBuilder<T> add(final T value) {
            builder = builder.map(b -> b.add(value));
            return this;
        }

        @Override
        public ListBuilder<T> add(final DataResult<T> value) {
            builder = builder.apply2stable(ImmutableList.Builder::add, value);
            return this;
        }

        @Override
        public ListBuilder<T> withErrorsFrom(final DataResult<?> result) {
            builder = builder.flatMap(r -> result.map(v -> r));
            return this;
        }

        @Override
        public ListBuilder<T> mapError(final UnaryOperator<String> onError) {
            builder = builder.mapError(onError);
            return this;
        }

        @Override
        public DataResult<T> build(final T prefix) {
            final DataResult<T> result = builder.flatMap(b -> ops.mergeToList(prefix, b.build()));
            builder = DataResult.success(ImmutableList.builder(), Lifecycle.stable());
            return result;
        }

    }

}
