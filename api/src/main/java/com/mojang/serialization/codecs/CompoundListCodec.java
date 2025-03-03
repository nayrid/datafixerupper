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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.RecordBuilder;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public final class CompoundListCodec<K, V> implements Codec<List<Pair<K, V>>> {

    private final Codec<K> keyCodec;
    private final Codec<V> elementCodec;

    public CompoundListCodec(final Codec<K> keyCodec, final Codec<V> elementCodec) {
        this.keyCodec = keyCodec;
        this.elementCodec = elementCodec;
    }

    @Override
    public <T> DataResult<Pair<List<Pair<K, V>>, T>> decode(final DynamicOps<T> ops,
                                                            final T input) {
        return ops.getMapEntries(input).flatMap(map -> {
            final ImmutableList.Builder<Pair<K, V>> read = ImmutableList.builder();
            final ImmutableMap.Builder<T, T> failed = ImmutableMap.builder();

            final AtomicReference<DataResult<Unit>> result = new AtomicReference<>(DataResult.success(
                Unit.INSTANCE,
                Lifecycle.experimental()
            ));

            map.accept((key, value) -> {
                final DataResult<K> k = keyCodec.parse(ops, key);
                final DataResult<V> v = elementCodec.parse(ops, value);

                final DataResult<Pair<K, V>> readEntry = k.apply2stable(Pair::new, v);

                readEntry.error().ifPresent(e -> failed.put(key, value));

                result.setPlain(result.getPlain().apply2stable((u, e) -> {
                    read.add(e);
                    return u;
                }, readEntry));
            });

            final ImmutableList<Pair<K, V>> elements = read.build();
            final T errors = ops.createMap(failed.build());

            final Pair<List<Pair<K, V>>, T> pair = Pair.of(elements, errors);

            return result.getPlain().map(unit -> pair).setPartial(pair);
        });
    }

    @Override
    public <T> DataResult<T> encode(final List<Pair<K, V>> input,
                                    final DynamicOps<T> ops,
                                    final T prefix) {
        final RecordBuilder<T> builder = ops.mapBuilder();

        for (final Pair<K, V> pair : input) {
            builder.add(keyCodec.encodeStart(ops, pair.first()),
                elementCodec.encodeStart(ops, pair.second())
            );
        }

        return builder.build(prefix);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CompoundListCodec<?, ?> that = (CompoundListCodec<?, ?>) o;
        return Objects.equals(keyCodec, that.keyCodec) && Objects.equals(elementCodec,
            that.elementCodec
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(keyCodec, elementCodec);
    }

    @Override
    public String toString() {
        return "CompoundListCodec[" + keyCodec + " -> " + elementCodec + ']';
    }

}
