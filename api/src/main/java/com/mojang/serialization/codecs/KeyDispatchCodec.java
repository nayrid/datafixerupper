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
import com.mojang.serialization.MapDecoder;
import com.mojang.serialization.MapEncoder;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import java.util.function.Function;
import java.util.stream.Stream;

public class KeyDispatchCodec<K, V> extends MapCodec<V> {

    private static final String COMPRESSED_VALUE_KEY = "value";
    private final String typeKey;
    private final Codec<K> keyCodec;
    private final Function<? super V, ? extends DataResult<? extends K>> type;
    private final Function<? super K, ? extends DataResult<? extends MapDecoder<? extends V>>> decoder;
    private final Function<? super V, ? extends DataResult<? extends MapEncoder<V>>> encoder;

    protected KeyDispatchCodec(final String typeKey,
                               final Codec<K> keyCodec,
                               final Function<? super V, ? extends DataResult<? extends K>> type,
                               final Function<? super K, ? extends DataResult<? extends MapDecoder<? extends V>>> decoder,
                               final Function<? super V, ? extends DataResult<? extends MapEncoder<V>>> encoder) {
        this.typeKey = typeKey;
        this.keyCodec = keyCodec;
        this.type = type;
        this.decoder = decoder;
        this.encoder = encoder;
    }

    /**
     * Assumes codec(type(V)) is {@code MapCodec<V>}.
     */
    public KeyDispatchCodec(final String typeKey,
                            final Codec<K> keyCodec,
                            final Function<? super V, ? extends DataResult<? extends K>> type,
                            final Function<? super K, ? extends DataResult<? extends MapCodec<? extends V>>> codec) {
        this(typeKey, keyCodec, type, codec, v -> getCodec(type, codec, v));
    }

    @SuppressWarnings("unchecked")
    private static <K, V> DataResult<? extends MapEncoder<V>> getCodec(final Function<? super V, ? extends DataResult<? extends K>> type,
                                                                       final Function<? super K, ? extends DataResult<? extends MapEncoder<? extends V>>> encoder,
                                                                       final V input) {
        return type.apply(input)
            .<MapEncoder<? extends V>>flatMap(key -> encoder.apply(key).map(Function.identity()))
            .map(c -> ((MapEncoder<V>) c));
    }

    @Override
    public <T> DataResult<V> decode(final DynamicOps<T> ops, final MapLike<T> input) {
        final T elementName = input.get(typeKey);
        if (elementName == null) {
            return DataResult.error(() -> "Input does not contain a key [" + typeKey + "]: " + input);
        }

        return keyCodec.decode(ops, elementName)
            .flatMap(type -> decoder.apply(type.first()).flatMap(elementDecoder -> {
                if (ops.compressMaps()) {
                    final T value = input.get(ops.createString(COMPRESSED_VALUE_KEY));
                    if (value == null) {
                        return DataResult.error(() -> "Input does not have a \"value\" entry: " + input);
                    }
                    return elementDecoder.decoder().parse(ops, value).map(Function.identity());
                }
                return elementDecoder.decode(ops, input).map(Function.identity());
            }));
    }

    @Override
    public <T> RecordBuilder<T> encode(final V input,
                                       final DynamicOps<T> ops,
                                       final RecordBuilder<T> prefix) {
        final DataResult<? extends MapEncoder<V>> encoderResult = encoder.apply(input);
        final RecordBuilder<T> builder = prefix.withErrorsFrom(encoderResult);
        if (encoderResult.isError()) {
            return builder;
        }

        final MapEncoder<V> elementEncoder = encoderResult.result().get();
        if (ops.compressMaps()) {
            return prefix.add(typeKey, type.apply(input).flatMap(t -> keyCodec.encodeStart(ops, t)))
                .add(COMPRESSED_VALUE_KEY, elementEncoder.encoder().encodeStart(ops, input));
        }

        return elementEncoder.encode(input, ops, prefix)
            .add(typeKey, type.apply(input).flatMap(t -> keyCodec.encodeStart(ops, t)));
    }

    @Override
    public <T> Stream<T> keys(final DynamicOps<T> ops) {
        return Stream.of(typeKey, COMPRESSED_VALUE_KEY).map(ops::createString);
    }

    @Override
    public String toString() {
        return "KeyDispatchCodec[" + keyCodec.toString() + " " + type + " " + decoder + "]";
    }

}
