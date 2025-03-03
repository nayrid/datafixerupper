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

import com.mojang.serialization.codecs.FieldEncoder;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Serializes (encodes) objects of a given type to a serialized form.
 *
 * <p>An {@link Encoder} is used to transform objects of a specific type to some serialized representation using
 * a supplied {@link DynamicOps}. Encoders encapsulate the serialization routine for a specific type, regardless
 * of the desired serialization form.
 *
 * <p>Implementations of {@link Encoder} are immutable once created. The methods defined in this interface never
 * mutate the encoder in a way visible to the outside.
 *
 * @param <A> The type this {@link Encoder} serializes.
 * @implNote The default methods in this interface that return encoders or {@linkplain MapEncoder map encoders}
 *     wrap this encoder without adding any debugging context. These methods should be overridden if deeply nested
 *     encoders are undesirable or additional debugging context is desired.
 * @see Decoder
 * @see Codec
 */
public interface Encoder<A> {

    /**
     * A {@link MapEncoder} that performs no serialization. Its {@linkplain MapEncoder#encode(Object, DynamicOps, RecordBuilder) encoding}
     * method returns the prefix unchanged and it has an empty {@linkplain MapEncoder#keys(DynamicOps) key stream}.
     *
     * @param <A> The type the returned {@link MapEncoder} operates on.
     * @return A {@link MapEncoder} that performs no serialization.
     */
    static <A> MapEncoder<A> empty() {
        return new MapEncoder.Implementation<A>() {
            @Override
            public <T> RecordBuilder<T> encode(final A input,
                                               final DynamicOps<T> ops,
                                               final RecordBuilder<T> prefix) {
                return prefix;
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return Stream.empty();
            }

            @Override
            public String toString() {
                return "EmptyEncoder";
            }
        };
    }

    static <A> Encoder<A> error(final String error) {
        return new Encoder<A>() {
            @Override
            public <T> DataResult<T> encode(final A input,
                                            final DynamicOps<T> ops,
                                            final T prefix) {
                return DataResult.error(() -> error + " " + input);
            }

            @Override
            public String toString() {
                return "ErrorEncoder[" + error + "]";
            }
        };
    }

    /**
     * Encodes an object into the specified serialized data. If encoding fails, returns an error {@link DataResult}.
     *
     * @param input  The object to serialize.
     * @param ops    The {@link DynamicOps} instance defining the serialized form.
     * @param prefix The existing serialized data to append to.
     * @param <T>    The type of the serialized form.
     * @return A {@link DataResult} wrapping the serialized form of {@code input}, appended to {@code prefix}.
     */
    <T> DataResult<T> encode(final A input, final DynamicOps<T> ops, final T prefix);

    /**
     * Encodes an object. If encoding fails, returns an error {@link DataResult}.
     *
     * <p>For merging the input into existing serialized data, use {@link #encode(Object, DynamicOps, Object)}.
     *
     * @param input The object to serialize.
     * @param ops   The {@link DynamicOps} instance defining the serialized form.
     * @param <T>   The type of the serialized form.
     * @return A {@link DataResult} wrapping the serialized form of {@code input}.
     * @implSpec The default implementation is equivalent to {@code encode(input, ops, ops.empty())}.
     * @see #encode(Object, DynamicOps, Object)
     */
    default <T> DataResult<T> encodeStart(final DynamicOps<T> ops, final A input) {
        return encode(input, ops, ops.empty());
    }

    /**
     * Returns a {@link MapEncoder} that encodes objects in a record under a field with the given name. The returned
     * {@link MapEncoder} may be used in conjunction with a {@link RecordBuilder} to serialize many different fields,
     * all using different encoders, to a single record.
     *
     * @param name The field to encode objects into.
     * @return A {@link MapEncoder} that performs the same encoding as this encoder, but places the serialized value
     *     in a record under the given field.
     * @implSpec The default implementation returns a {@link FieldEncoder} wrapping this encoder.
     * @see RecordBuilder
     */
    default MapEncoder<A> fieldOf(final String name) {
        return new FieldEncoder<>(name, this);
    }

    /**
     * Transforms this encoder to operate on a different type using the given mapping function.
     *
     * @param function A function transforming the new type to the old type.
     * @param <B>      The new type of the encoder.
     * @return An encoder that operates on the type {@code B}.
     * @apiNote This method implements a <em>contravariant map</em> for {@link Encoder}.
     * @implSpec The default implementation returns another {@link Encoder} that wraps this encoder.
     * @see <a href="https://en.wikipedia.org/wiki/Covariance_and_contravariance_(computer_science)">Covariance and Contravariance in Subtyping</a>
     */
    default <B> Encoder<B> comap(final Function<? super B, ? extends A> function) {
        return new Encoder<B>() {
            @Override
            public <T> DataResult<T> encode(final B input,
                                            final DynamicOps<T> ops,
                                            final T prefix) {
                return Encoder.this.encode(function.apply(input), ops, prefix);
            }

            @Override
            public String toString() {
                return Encoder.this + "[comapped]";
            }
        };
    }

    /**
     * Transforms this encoder to operate on a different type using the given {@link DataResult}-producing mapping function.
     *
     * @param function A function transforming the new type to the old type. Errors from this encoder and from the function
     *                 are merged.
     * @param <B>      The new type of the encoder.
     * @return An encoder that operates on the type {@code B}.
     * @apiNote This method implements a <em>contravariant flat map</em> for {@link Encoder}.
     * @implSpec The default implementation returns another {@link Encoder} that wraps this encoder.
     * @see #comap(Function)
     */
    default <B> Encoder<B> flatComap(final Function<? super B, ? extends DataResult<? extends A>> function) {
        return new Encoder<B>() {
            @Override
            public <T> DataResult<T> encode(final B input,
                                            final DynamicOps<T> ops,
                                            final T prefix) {
                return function.apply(input).flatMap(a -> Encoder.this.encode(a, ops, prefix));
            }

            @Override
            public String toString() {
                return Encoder.this + "[flatComapped]";
            }
        };
    }

    /**
     * Sets the {@link Lifecycle} for the serialized data this encoder produces.
     *
     * @param lifecycle the lifecycle to use.
     * @return An encoder equivalent to this encoder but with the given lifecycle.
     * @implSpec The default implementation returns another {@link Encoder} that wraps this encoder.
     * @see Lifecycle
     */
    default Encoder<A> withLifecycle(final Lifecycle lifecycle) {
        return new Encoder<A>() {
            @Override
            public <T> DataResult<T> encode(final A input,
                                            final DynamicOps<T> ops,
                                            final T prefix) {
                return Encoder.this.encode(input, ops, prefix).setLifecycle(lifecycle);
            }

            @Override
            public String toString() {
                return Encoder.this.toString();
            }
        };
    }

}
