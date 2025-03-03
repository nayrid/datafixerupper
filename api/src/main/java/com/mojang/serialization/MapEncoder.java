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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Serializes (encodes) a fixed set of record fields to a serialized form.
 *
 * <p>A {@link MapEncoder} is a specialization of a {@link Encoder} that operates on some defined set of record keys.
 * While a {@link MapEncoder} does not implement {@link Encoder}, any {@link MapEncoder} may be turned into a
 * {@link Encoder} by calling the {@link #encoder()} method.
 *
 * <p>Implementations of {@link MapEncoder} are immutable once created. The methods defined in this interface never
 * mutate the decoder in a way visible to the outside.
 *
 * @param <A> The type that this {@link MapEncoder} serializes.
 * @implNote The default methods in this interface that return map encoders or {@linkplain Encoder encoders}
 *     wrap this encoder without adding any debugging context. These methods should be overridden if deeply nested
 *     encoders are undesirable or additional debugging context is desired.
 * @see Encoder
 * @see MapCodec
 */
public interface MapEncoder<A> extends Keyable {

    /**
     * Creates a {@link RecordBuilder} that serializes a fixed set of keys defined by the {@link KeyCompressor}.
     *
     * @param ops        The {@link DynamicOps} instance defining the serialized form.
     * @param compressor A {@link KeyCompressor} the returned builder uses to compress valid keys.
     * @param <T>        The type of the serialized form.
     * @return A {@link RecordBuilder} that uses the given compressor to serialize compressed keys.
     */
    static <T> RecordBuilder<T> makeCompressedBuilder(final DynamicOps<T> ops,
                                                      final KeyCompressor<T> compressor) {
        class CompressedRecordBuilder extends RecordBuilder.AbstractUniversalBuilder<T, List<T>> {

            private CompressedRecordBuilder() {
                super(ops);
            }

            @Override
            protected List<T> initBuilder() {
                final List<T> list = new ArrayList<>(compressor.size());
                for (int i = 0;
                     i < compressor.size();
                     i++) {
                    list.add(null);
                }
                return list;
            }

            @Override
            protected List<T> append(final T key, final T value, final List<T> builder) {
                builder.set(compressor.compress(key), value);
                return builder;
            }

            @Override
            protected DataResult<T> build(final List<T> builder, final T prefix) {
                return ops().mergeToList(prefix, builder);
            }

        }

        return new CompressedRecordBuilder();
    }

    /**
     * Encodes the input into a set of record fields that are added to the given builder. If a record field cannot
     * be serialized, it is not added to the builder.
     *
     * @param input  The value to serialize.
     * @param ops    The {@link DynamicOps} instance defining the serialized form.
     * @param prefix A {@link RecordBuilder} to add the serialized fields to.
     * @param <T>    The type of the serialized form.
     * @return The builder, with the serialized fields added.
     */
    <T> RecordBuilder<T> encode(A input, DynamicOps<T> ops, RecordBuilder<T> prefix);

    /**
     * Creates a new, empty {@link RecordBuilder} that accepts values of the given serialized type. The returned
     * builder will used compressed keys if and only if the serialized type uses compressed keys.
     *
     * @param ops The {@link DynamicOps} instance defining the serialized form.
     * @param <T> The type of the serialized form.
     * @return An empty {@link RecordBuilder}.
     * @implSpec The default implementation returns {@link #makeCompressedBuilder(DynamicOps, KeyCompressor)} if
     *     the given {@link DynamicOps} prefers compressed keys, and returns {@link DynamicOps#mapBuilder()} otherwise.
     * @see DynamicOps#compressMaps()
     */
    default <T> RecordBuilder<T> compressedBuilder(final DynamicOps<T> ops) {
        if (ops.compressMaps()) {
            return makeCompressedBuilder(ops, compressor(ops));
        }
        return ops.mapBuilder();
    }

    /**
     * Returns the {@link KeyCompressor} this map encoder uses to associate valid record keys with indices.
     *
     * @param ops The {@link DynamicOps} instance defining the serialized form.
     * @param <T> The type of the serialized form.
     * @return The {@link KeyCompressor} for this map encoder.
     */
    <T> KeyCompressor<T> compressor(final DynamicOps<T> ops);

    /**
     * Transforms this map encoder to operate on a different type using the given mapping function.
     *
     * @param function A function from the new type to the current type.
     * @param <B>      The new type.
     * @return A map encoder that first transforms the input using the mapping function, then encodes the result
     *     using this map encoder.
     * @implSpec The default implementation returns a {@link MapEncoder.Implementation} that wraps this map encoder.
     */
    default <B> MapEncoder<B> comap(final Function<? super B, ? extends A> function) {
        return new Implementation<B>() {
            @Override
            public <T> RecordBuilder<T> encode(final B input,
                                               final DynamicOps<T> ops,
                                               final RecordBuilder<T> prefix) {
                return MapEncoder.this.encode(function.apply(input), ops, prefix);
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapEncoder.this.keys(ops);
            }

            @Override
            public String toString() {
                return MapEncoder.this + "[comapped]";
            }
        };
    }

    /**
     * Transforms this map encoder to operate on a different type using the given partial mapping function. Any errors
     * the mapping function returns prevent further serialization.
     *
     * @param function A function from the new type to the current type.
     * @param <B>      The new type.
     * @return A map encoder that first transforms the input using the mapping function, then encodes the result
     *     using this map encoder.
     * @implSpec The default implementation returns a {@link MapEncoder.Implementation} that wraps this map encoder.
     */
    default <B> MapEncoder<B> flatComap(final Function<? super B, ? extends DataResult<? extends A>> function) {
        return new Implementation<B>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapEncoder.this.keys(ops);
            }

            @Override
            public <T> RecordBuilder<T> encode(final B input,
                                               final DynamicOps<T> ops,
                                               final RecordBuilder<T> prefix) {
                final DataResult<? extends A> aResult = function.apply(input);
                final RecordBuilder<T> builder = prefix.withErrorsFrom(aResult);
                return aResult.map(r -> MapEncoder.this.encode(r, ops, builder))
                    .result()
                    .orElse(builder);
            }

            @Override
            public String toString() {
                return MapEncoder.this + "[flatComapped]";
            }
        };
    }

    /**
     * Returns the {@link Encoder} that implements the operations defined in this {@link MapEncoder}.
     *
     * @implNote The default implementation returns a {@link Encoder} that implements the {@link Encoder#encode(Object, DynamicOps, Object)}
     *     method to call {@link #encode(Object, DynamicOps, RecordBuilder)}.
     */
    default Encoder<A> encoder() {
        return new Encoder<A>() {
            @Override
            public <T> DataResult<T> encode(final A input,
                                            final DynamicOps<T> ops,
                                            final T prefix) {
                return MapEncoder.this.encode(input, ops, compressedBuilder(ops)).build(prefix);
            }

            @Override
            public String toString() {
                return MapEncoder.this.toString();
            }
        };
    }

    /**
     * Sets the {@link Lifecycle} of the results this map encoder serializes.
     *
     * @param lifecycle The lifecycle to use.
     * @return A map encoder equivalent to this map encoder, but using the given lifecycle for the serialized
     *     results.
     * @implSpec The default implementation returns a {@link MapEncoder.Implementation} that wraps this map decoder.
     */
    default MapEncoder<A> withLifecycle(final Lifecycle lifecycle) {
        return new Implementation<A>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapEncoder.this.keys(ops);
            }

            @Override
            public <T> RecordBuilder<T> encode(final A input,
                                               final DynamicOps<T> ops,
                                               final RecordBuilder<T> prefix) {
                return MapEncoder.this.encode(input, ops, prefix).setLifecycle(lifecycle);
            }

            @Override
            public String toString() {
                return MapEncoder.this.toString();
            }
        };
    }

    /**
     * A class that implements both {@link MapEncoder} and {@link Compressable}.
     *
     * <p>The method {@link MapEncoder#compressor(DynamicOps)} is implemented via {@link CompressorHolder#compressor(DynamicOps)}.
     *
     * @param <A> The type that this map encoder serializes.
     */
    abstract class Implementation<A> extends CompressorHolder implements MapEncoder<A> {

    }

}
