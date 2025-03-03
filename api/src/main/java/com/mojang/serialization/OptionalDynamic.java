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

import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public final class OptionalDynamic<T> extends DynamicLike<T> {

    private final DataResult<Dynamic<T>> delegate;

    public OptionalDynamic(final DynamicOps<T> ops, final DataResult<Dynamic<T>> delegate) {
        super(ops);
        this.delegate = delegate;
    }

    public DataResult<Dynamic<T>> get() {
        return delegate;
    }

    public Optional<Dynamic<T>> result() {
        return delegate.result();
    }

    public <U> DataResult<U> map(final Function<? super Dynamic<T>, U> mapper) {
        return delegate.map(mapper);
    }

    public <U> DataResult<U> flatMap(final Function<? super Dynamic<T>, ? extends DataResult<U>> mapper) {
        return delegate.flatMap(mapper);
    }

    @Override
    public DataResult<Number> asNumber() {
        return flatMap(DynamicLike::asNumber);
    }

    @Override
    public DataResult<String> asString() {
        return flatMap(DynamicLike::asString);
    }

    @Override
    public DataResult<Boolean> asBoolean() {
        return flatMap(DynamicLike::asBoolean);
    }

    @Override
    public DataResult<Stream<Dynamic<T>>> asStreamOpt() {
        return flatMap(DynamicLike::asStreamOpt);
    }

    @Override
    public DataResult<Stream<Pair<Dynamic<T>, Dynamic<T>>>> asMapOpt() {
        return flatMap(DynamicLike::asMapOpt);
    }

    @Override
    public DataResult<ByteBuffer> asByteBufferOpt() {
        return flatMap(DynamicLike::asByteBufferOpt);
    }

    @Override
    public DataResult<IntStream> asIntStreamOpt() {
        return flatMap(DynamicLike::asIntStreamOpt);
    }

    @Override
    public DataResult<LongStream> asLongStreamOpt() {
        return flatMap(DynamicLike::asLongStreamOpt);
    }

    @Override
    public OptionalDynamic<T> get(final String key) {
        return new OptionalDynamic<>(ops, delegate.flatMap(k -> k.get(key).delegate));
    }

    @Override
    public DataResult<T> getGeneric(final T key) {
        return flatMap(v -> v.getGeneric(key));
    }

    @Override
    public DataResult<T> getElement(final String key) {
        return flatMap(v -> v.getElement(key));
    }

    @Override
    public DataResult<T> getElementGeneric(final T key) {
        return flatMap(v -> v.getElementGeneric(key));
    }

    public Dynamic<T> orElseEmptyMap() {
        return result().orElseGet(this::emptyMap);
    }

    public Dynamic<T> orElseEmptyList() {
        return result().orElseGet(this::emptyList);
    }

    public <V> DataResult<V> into(final Function<? super Dynamic<T>, ? extends V> action) {
        return delegate.map(action);
    }

    @Override
    public <A> DataResult<Pair<A, T>> decode(final Decoder<? extends A> decoder) {
        return delegate.flatMap(t -> t.decode(decoder));
    }

}
