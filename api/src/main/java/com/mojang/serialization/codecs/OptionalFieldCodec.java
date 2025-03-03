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
package com.mojang.serialization.codecs;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Optimization of `Codec.either(someCodec.field(name), Codec.EMPTY)`
 */
public class OptionalFieldCodec<A> extends MapCodec<Optional<A>> {

    private final String name;
    private final Codec<A> elementCodec;
    private final boolean lenient;

    public OptionalFieldCodec(final String name,
                              final Codec<A> elementCodec,
                              final boolean lenient) {
        this.name = name;
        this.elementCodec = elementCodec;
        this.lenient = lenient;
    }

    @Override
    public <T> DataResult<Optional<A>> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final T value = input.get(name);
        if (value == null) {
            return DataResult.success(Optional.empty());
        }
        final DataResult<A> parsed = elementCodec.parse(ops, value);
        if (parsed.isError() && lenient) {
            return DataResult.success(Optional.empty());
        }
        return parsed.map(Optional::of).setPartial(parsed.resultOrPartial());
    }

    @Override
    public <T> RecordBuilder<T> encode(final Optional<A> input,
                                       final DynamicOps<T> ops,
                                       final RecordBuilder<T> prefix) {
        if (input.isPresent()) {
            return prefix.add(name, elementCodec.encodeStart(ops, input.get()));
        }
        return prefix;
    }

    @Override
    public <T> Stream<T> keys(final DynamicOps<T> ops) {
        return Stream.of(ops.createString(name));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final OptionalFieldCodec<?> that = (OptionalFieldCodec<?>) o;
        return Objects.equals(name, that.name) && Objects.equals(elementCodec,
            that.elementCodec
        ) && lenient == that.lenient;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, elementCodec, lenient);
    }

    @Override
    public String toString() {
        return "OptionalFieldCodec[" + name + ": " + elementCodec + ']';
    }

}
