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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * Deserializes (decodes) a fixed set of record fields from a serialized form.
 *
 * <p>A {@link MapDecoder} is a specialization of a {@link Decoder} that operates on some defined set of record keys.
 * While a {@link MapDecoder} does not implement {@link Decoder}, any {@link MapDecoder} may be turned into a
 * {@link Decoder} by calling the {@link #decoder()} method.
 *
 * <p>Implementations of {@link MapDecoder} are immutable once created. The methods defined in this interface never
 * mutate the decoder in a way visible to the outside.
 *
 * @param <A> The type that this {@link MapDecoder} deserializes.
 * @implNote The default methods in this interface that return map decoders or {@linkplain Decoder decoders}
 *     wrap this decoder without adding any debugging context. These methods should be overridden if deeply nested
 *     decoders are undesirable or additional debugging context is desired.
 * @see Decoder
 * @see MapCodec
 */
public interface MapDecoder<A> extends Keyable {

    /**
     * Decodes an object from the given map of serialized entries. If decoding fails (for example, if a key is absent
     * or has an incompatible value), returns an error {@link DataResult}.
     *
     * @param ops   The {@link DynamicOps} instance defining the serialized form.
     * @param input A map or map-like object containing the serialized entries.
     * @param <T>   The type of the serialized form.
     * @return A {@link DataResult} containing the decoded object, or an error if no object could be decoded.
     * @see #compressedDecode(DynamicOps, Object)
     */
    <T> DataResult<A> decode(DynamicOps<T> ops, MapLike<T> input);

    /**
     * Decodes an object from the given serialized form. If the {@link DynamicOps} uses compressed keys, the input
     * should also contain compressed keys. Likewise, if the {@link DynamicOps} does not use compressed keys, the input
     * should also not contain compressed keys.
     *
     * @param ops   The {@link DynamicOps} instance defining the serialized form.
     * @param input The serialized value that contains the record data to deserialize.
     * @param <T>   The type of the serialized form.
     * @return A {@link DataResult} containing the decoded object, or an error if no object could be decoded.
     * @implSpec The default implementation determines whether the serialized keys are compressed. Then, it deserializes
     *     the input into a {@link MapLike} that is then passed to {@link #decode(DynamicOps, MapLike)}.
     */
    default <T> DataResult<A> compressedDecode(final DynamicOps<T> ops, final T input) {
        if (ops.compressMaps()) {
            final Optional<Consumer<Consumer<T>>> inputList = ops.getList(input).result();

            if (inputList.isEmpty()) {
                return DataResult.error(() -> "Input is not a list");
            }

            final KeyCompressor<T> compressor = compressor(ops);
            final List<T> entries = new ArrayList<>();
            inputList.get().accept(entries::add);

            final MapLike<T> map = new MapLike<T>() {
                @Override
                public @Nullable T get(final T key) {
                    return entries.get(compressor.compress(key));
                }

                @Override
                public @Nullable T get(final String key) {
                    return entries.get(compressor.compress(key));
                }

                @Override
                public Stream<Pair<T, T>> entries() {
                    return IntStream.range(0, entries.size())
                        .mapToObj(i -> Pair.of(compressor.decompress(i), entries.get(i)))
                        .filter(p -> p.second() != null);
                }
            };
            return decode(ops, map);
        }
        // will use the lifecycle of decode
        return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap(map -> decode(ops, map));
    }

    /**
     * Returns the {@link KeyCompressor} used to associate record keys with indices.
     *
     * @param ops The {@link DynamicOps} instance defining the serialized form.
     * @param <T> The type of the serialized form.
     * @return The {@link KeyCompressor} for this map decoder.
     */
    <T> KeyCompressor<T> compressor(DynamicOps<T> ops);

    /**
     * Returns a {@link Decoder} that implements the operations defined in this map decoder.
     *
     * @implNote The default implementation returns a {@link Decoder} that implements the {@link Decoder#decode(DynamicOps, Object)}
     *     method to call {@link #decode(DynamicOps, MapLike)}.
     */
    default Decoder<A> decoder() {
        return new Decoder<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return compressedDecode(ops, input).map(r -> Pair.of(r, input));
            }

            @Override
            public String toString() {
                return MapDecoder.this.toString();
            }
        };
    }

    /**
     * Transforms this map decoder to operate on a different type using the given partial mapping function. Any errors
     * the mapping function returns are merged with errors from deserialization.
     *
     * @param function A function from the current type to the new type.
     * @param <B>      The new type.
     * @return A map decoder that decodes using this map decoder, then transforms the output using the mapping function.
     * @implSpec The default implementation returns a {@link MapDecoder.Implementation} that wraps this map decoder.
     */
    default <B> MapDecoder<B> flatMap(final Function<? super A, ? extends DataResult<? extends B>> function) {
        return new Implementation<B>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapDecoder.this.keys(ops);
            }

            @Override
            public <T> DataResult<B> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return MapDecoder.this.decode(ops, input)
                    .flatMap(b -> function.apply(b).map(Function.identity()));
            }

            @Override
            public String toString() {
                return MapDecoder.this + "[flatMapped]";
            }
        };
    }

    /**
     * Transforms this map decoder to operate on a different type using the given mapping function.
     *
     * @param function A function from the current type to the new type.
     * @param <B>      The new type.
     * @return A map decoder that decodes using this map decoder, then transforms the output using the mapping function.
     * @implSpec The default implementation returns a {@link MapDecoder.Implementation} that wraps this map decoder.
     */
    default <B> MapDecoder<B> map(final Function<? super A, ? extends B> function) {
        return new Implementation<B>() {
            @Override
            public <T> DataResult<B> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return MapDecoder.this.decode(ops, input).map(function);
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapDecoder.this.keys(ops);
            }

            @Override
            public String toString() {
                return MapDecoder.this + "[mapped]";
            }
        };
    }

    /**
     * Applies a transformation decoded from the given map decoder to decoded values. Errors from the transformation
     * decoder are merged with errors from this decoder.
     *
     * @param decoder A decoder that deserializes mapping functions.
     * @param <E>     The new type.
     * @return A map decoder that deserializes a value, then applies a transformation deserialized from the given
     *     map decoder.
     * @apiNote This method implements the <em>applicative operator</em> for {@link MapDecoder}.
     * @implSpec The default implementation returns a {@link MapDecoder.Implementation} that wraps this map decoder.
     * @see #map(Function)
     * @see com.mojang.datafixers.kinds.Applicative
     */
    default <E> MapDecoder<E> ap(final MapDecoder<Function<? super A, ? extends E>> decoder) {
        return new Implementation<E>() {
            @Override
            public <T> DataResult<E> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return MapDecoder.this.decode(ops, input)
                    .flatMap(f -> decoder.decode(ops, input).map(e -> e.apply(f)));
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return Stream.concat(MapDecoder.this.keys(ops), decoder.keys(ops));
            }

            @Override
            public String toString() {
                return decoder.toString() + " * " + MapDecoder.this;
            }
        };
    }

    /**
     * Sets the {@link Lifecycle} of the results this map decoder deserializes.
     *
     * @param lifecycle The lifecycle to use.
     * @return A map decoder equivalent to this map decoder, but using the given lifecycle for the deserialized
     *     results.
     * @implSpec The default implementation returns a {@link MapDecoder.Implementation} that wraps this map decoder.
     */
    default MapDecoder<A> withLifecycle(final Lifecycle lifecycle) {
        return new Implementation<A>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return MapDecoder.this.keys(ops);
            }

            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return MapDecoder.this.decode(ops, input).setLifecycle(lifecycle);
            }

            @Override
            public String toString() {
                return MapDecoder.this.toString();
            }
        };
    }

    /**
     * A class that implements both {@link MapDecoder} and {@link Compressable}.
     *
     * <p>The method {@link MapDecoder#compressor(DynamicOps)} is implemented via {@link CompressorHolder#compressor(DynamicOps)}.
     *
     * @param <A> The type that this map decoder deserializes.
     */
    abstract class Implementation<A> extends CompressorHolder implements MapDecoder<A> {

    }

}
