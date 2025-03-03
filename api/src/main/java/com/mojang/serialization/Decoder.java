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
import com.mojang.serialization.codecs.FieldDecoder;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Deserializes (decodes) objects of a given type from a serialized form.
 *
 * <p>A {@link Decoder} is used to transform objects from some serialized representation to a specific type using
 * a supplied {@link DynamicOps}. Decoders encapsulate the deserialization routine for a specific type, regardless
 * of the input serialization form.
 *
 * <p>Implementations of {@link Decoder} are immutable once created. The methods defined in this interface never
 * mutate the decoder in a way visible to the outside.
 *
 * @param <A> The type this {@link Decoder} deserializes.
 * @implNote The default methods in this interface that return decoders or {@linkplain MapDecoder map decoders}
 *     wrap this decoder without adding any debugging context. These methods should be overridden if deeply nested
 *     decoders are undesirable or additional debugging context is desired.
 * @see Encoder
 * @see Codec
 */
public interface Decoder<A> {

    /**
     * Creates a {@link Decoder} from the given {@link Decoder.Terminal}.
     *
     * @param terminal The terminal decoder.
     * @param <A>      The type to decode into.
     * @return A {@link Decoder} from the given terminal decoder.
     * @see Terminal#decoder()
     */
    static <A> Decoder<A> ofTerminal(final Terminal<? extends A> terminal) {
        return terminal.decoder().map(Function.identity());
    }

    /**
     * Creates a {@link Decoder} from the given {@link Decoder.Boxed}.
     *
     * @param boxed The boxed decoder.
     * @param <A>   The type to decode into.
     * @return A {@link Decoder} from the given boxed decoder.
     * @see Boxed#decoder()
     */
    static <A> Decoder<A> ofBoxed(final Boxed<? extends A> boxed) {
        return boxed.decoder().map(Function.identity());
    }

    /**
     * Creates a {@link Decoder} from the given {@link Decoder.Simple}.
     *
     * @param simple The simple decoder.
     * @param <A>    The type to decode into.
     * @return A {@link Decoder} from the given simple decoder.
     * @see Simple#decoder()
     */
    static <A> Decoder<A> ofSimple(final Simple<? extends A> simple) {
        return simple.decoder().map(Function.identity());
    }

    /**
     * A {@link MapDecoder} that performs no deserialization and always returns the given value. Its
     * {@linkplain MapDecoder#decode(DynamicOps, MapLike)} decoding} method always returns the instance and its
     * {@linkplain MapDecoder#keys(DynamicOps) key stream} is always empty.
     *
     * @param instance The instance to return from the decoder.
     * @param <A>      The type of the instance.
     * @return A {@link MapDecoder} that always returns the given value.
     * @see #unit(Supplier)
     */
    static <A> MapDecoder<A> unit(final A instance) {
        return unit(() -> instance);
    }

    /**
     * A {@link MapDecoder} that performs no deserialization and always returns a supplied value. Its
     * {@linkplain MapDecoder#decode(DynamicOps, MapLike)} decoding} method always returns a value generated from
     * the supplier and its {@linkplain MapDecoder#keys(DynamicOps) key stream} is always empty.
     *
     * @param instance A {@link Supplier} that returns instances to return from the decoder.
     * @param <A>      The type of values to decode.
     * @return A {@link MapDecoder} that always returns supplied values.
     */
    static <A> MapDecoder<A> unit(final Supplier<A> instance) {
        return new MapDecoder.Implementation<A>() {
            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return DataResult.success(instance.get());
            }

            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return Stream.empty();
            }

            @Override
            public String toString() {
                return "UnitDecoder[" + instance.get() + "]";
            }
        };
    }

    /**
     * An {@link Decoder} that performs no deserialization. Its {@linkplain #decode(DynamicOps, Object) decoding}
     * method always returns the given error.
     *
     * @param error The error the returned decoder should produce.
     * @param <A>   The type the returned decoder operates on.
     * @return An {@link Decoder} that performs no deserialization.
     */
    static <A> Decoder<A> error(final String error) {
        return new Decoder<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return DataResult.error(() -> error);
            }

            @Override
            public String toString() {
                return "ErrorDecoder[" + error + ']';
            }
        };
    }

    /**
     * Decodes an object from the specified serialized data. If decoding fails, returns an error {@link DataResult}.
     *
     * @param ops   The {@link DynamicOps} instance defining the serialized form.
     * @param input The serialized data.
     * @param <T>   The type of the serialized form.
     * @return A {@link Pair} containing the decoded object and the remaining serialized data, wrapped in a {@link DataResult}.
     */
    <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input);

    /**
     * Decodes an object from the specified serialized data, discarding any remaining data. If decoding fails,
     * returns an error {@link DataResult}.
     *
     * <p>For preserving remaining serialize data, use {@link #decode(DynamicOps, Object)}.
     *
     * @param ops   The {@link DynamicOps} instance defining the serialized form.
     * @param input The serialized data.
     * @param <T>   The type of the serialized form.
     * @return A {@link DataResult} containing the decoded object.
     * @implSpec The default implementation is equivalent to {@code decode(ops, input).map(Pair::getFirst)}.
     */
    // TODO: rename to read after Type.read is no more
    default <T> DataResult<A> parse(final DynamicOps<T> ops, final T input) {
        return decode(ops, input).map(Pair::first);
    }

    /**
     * Decodes an object from the specified {@link Dynamic} data. If decoding fails, returns an error {@link DataResult}.
     *
     * @param input The serialized data.
     * @param <T>   The type of the serialized form.
     * @return A {@link DataResult} containing a pair or the decoded object and any remaining serialized data.
     * @implSpec The default implementation is equivalent to calling {@link #decode(DynamicOps, Object)} on the
     *     wrapped ops and value.
     * @see #decode(DynamicOps, Object)
     */
    default <T> DataResult<Pair<A, T>> decode(final Dynamic<T> input) {
        return decode(input.getOps(), input.getValue());
    }

    /**
     * Decodes an object from the specified {@link Dynamic} data, discarding any remaining data. If decoding fails,
     * returns an error {@link DataResult}.
     *
     * @param input The serialized data.
     * @param <T>   The type of the serialized form.
     * @return A {@link DataResult} containing the decoded object.
     * @implSpec The default implementation is equivalent to {@code decode(input).map(Pair::getFirst)}.
     */
    default <T> DataResult<A> parse(final Dynamic<T> input) {
        return decode(input).map(Pair::first);
    }

    /**
     * Creates a {@link Decoder.Terminal} from this {@link Decoder}.
     *
     * @implSpec The default implementation returns a {@link Decoder.Terminal} based on {@link #parse(DynamicOps, Object)}.
     */
    default Terminal<A> terminal() {
        return this::parse;
    }

    /**
     * Creates a {@link Decoder.Boxed} from this {@link Decoder}.
     *
     * @implSpec The default implementation returns a {@link Decoder.Boxed} based on {@link #decode(Dynamic)}.
     */
    default Boxed<A> boxed() {
        return this::decode;
    }

    /**
     * Creates a {@link Decoder.Simple} from this {@link Decoder}.
     *
     * @implSpec The default implementation returns a {@link Decoder.Simple} based on {@link #parse(Dynamic)}.
     */
    default Simple<A> simple() {
        return this::parse;
    }

    /**
     * Returns a {@link MapDecoder} that decodes objects in a record under a field with the given name. The returned
     * {@link MapDecoder} may be used in conjunction with a partially deserialized {@link MapLike} to deserialize
     * many different fields, all using different decoders, from a single record.
     *
     * @param name The field to decode objects from.
     * @return A {@link MapDecoder} that performs the same decoding as this decoder, but takes the serialized value
     *     from a record under the given field.
     * @implSpec The default implementation returns a {@link FieldDecoder} wrapping this decoder.
     * @see MapLike
     */
    default MapDecoder<A> fieldOf(final String name) {
        return new FieldDecoder<>(name, this);
    }

    /**
     * Transforms this decoder to operate on a different type using the given {@link DataResult}-producing mapping function.
     *
     * @param function A function transforming the old type to the new type. Errors from this decoder and from the function
     *                 are merged.
     * @param <B>      The new type of the decoder.
     * @return A decoder that operates on the type {@code B}.
     * @apiNote This method implements a <em>flat map</em> for {@link Decoder}.
     * @implSpec The default implementation returns another {@link Decoder} that wraps this decoder.
     * @see #map(Function)
     */
    default <B> Decoder<B> flatMap(final Function<? super A, ? extends DataResult<? extends B>> function) {
        return new Decoder<B>() {
            @Override
            public <T> DataResult<Pair<B, T>> decode(final DynamicOps<T> ops, final T input) {
                return Decoder.this.decode(ops, input)
                    .flatMap(p -> function.apply(p.first()).map(r -> Pair.of(r, p.second())));
            }

            @Override
            public String toString() {
                return Decoder.this + "[flatMapped]";
            }
        };
    }

    /**
     * Transforms this decoder to operate on a different type using the given mapping function.
     *
     * @param function A function transforming the old type to the new type.
     * @param <B>      The new type of the decoder.
     * @return A decoder that operates on the type {@code B}.
     * @apiNote This method implements a <em>map</em> for {@link Decoder}.
     * @implSpec The default implementation returns another {@link Decoder} that wraps this decoder.
     */
    default <B> Decoder<B> map(final Function<? super A, ? extends B> function) {
        return new Decoder<B>() {
            @Override
            public <T> DataResult<Pair<B, T>> decode(final DynamicOps<T> ops, final T input) {
                return Decoder.this.decode(ops, input).map(p -> p.mapFirst(function));
            }

            @Override
            public String toString() {
                return Decoder.this + "[mapped]";
            }
        };
    }

    /**
     * Returns a {@link Decoder} that returns the partial decoded result in this decoder returns an error.
     *
     * @param onError A callback to run if an error occurs when decoding. The function receives the error message
     *                as its argument.
     * @return A {@link Decoder} that promotes any partial result to a successful decoded result.
     * @implSpec The default implementation returns another {@link Decoder} that wraps this decoder.
     * @see DataResult#promotePartial(Consumer)
     */
    default Decoder<A> promotePartial(final Consumer<String> onError) {
        return new Decoder<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return Decoder.this.decode(ops, input).promotePartial(onError);
            }

            @Override
            public String toString() {
                return Decoder.this + "[promotePartial]";
            }
        };
    }

    /**
     * Sets the {@link Lifecycle} for the serialized data this decoder produces.
     *
     * @param lifecycle the lifecycle to use.
     * @return A decoder equivalent to this decoder but with the given lifecycle.
     * @implSpec The default implementation returns another {@link Decoder} that wraps this decoder.
     * @see Lifecycle
     */
    default Decoder<A> withLifecycle(final Lifecycle lifecycle) {
        return new Decoder<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return Decoder.this.decode(ops, input).setLifecycle(lifecycle);
            }

            @Override
            public String toString() {
                return Decoder.this.toString();
            }
        };
    }

    /**
     * A simple decoder interface that discards any serialized input that was not used to decode the object. In
     * practice, there is not expected to be any remaining serialized input when using objects of this type.
     *
     * <p>This interface allows one to implement a {@link Decoder} based on {@link Decoder#parse(DynamicOps, Object)}.
     *
     * @param <A> The type that this decoder deserializes.
     */
    interface Terminal<A> {

        /**
         * Completely decodes the given serialized input and returns the decoded object in a {@link DataResult}.
         *
         * @param ops   The {@link DynamicOps} instance defining the serialized form.
         * @param input The serialized data.
         * @param <T>   The type of the serialized form.
         * @return A {@link DataResult} containing the decoded object.
         * @see Decoder#parse(DynamicOps, Object)
         */
        <T> DataResult<A> decode(final DynamicOps<T> ops, final T input);

        /**
         * Returns a {@link Decoder} that performs the same actions as this terminal decoder.
         *
         * @implSpec The default implementation returns a {@link Decoder} that returns the result of
         *     {@link #decode(DynamicOps, Object)} with an empty remainder.
         * @implNote The returned {@link Decoder} is implemented via {@link Decoder#decode(DynamicOps, Object)},
         *     not via {@link Decoder#parse(DynamicOps, Object)}.
         */
        default Decoder<A> decoder() {
            return new Decoder<A>() {
                @Override
                public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                    return Terminal.this.decode(ops, input).map(a -> Pair.of(a, ops.empty()));
                }

                @Override
                public String toString() {
                    return "TerminalDecoder[" + Terminal.this + "]";
                }
            };
        }

    }

    /**
     * A simple decoder interface that decodes an object from a {@link Dynamic}.
     *
     * <p>This interface allows one to implement a {@link Decoder} based on {@link Decoder#decode(Dynamic)}.
     *
     * @param <A> The type of object this decoder deserializes.
     */
    interface Boxed<A> {

        /**
         * Decodes the input into an object and returns the decoded object and remaining serialized data in a
         * {@link DataResult}.
         *
         * @param input The serialized data.
         * @param <T>   The type of the serialized form.
         * @return A {@link DataResult} containing the decoded object and the remaining serialized data.
         * @see Decoder#decode(Dynamic)
         */
        <T> DataResult<Pair<A, T>> decode(final Dynamic<T> input);

        /**
         * Returns a {@link Decoder} that performs the same actions as this boxed decoder.
         *
         * @implSpec The default implementation returns a {@link Decoder} that returns the result of
         *     {@link #decode(Dynamic)}.
         * @implNote The returned {@link Decoder} is implemented via {@link Decoder#decode(DynamicOps, Object)},
         *     not via {@link Decoder#decode(Dynamic)}.
         */
        default Decoder<A> decoder() {
            return new Decoder<A>() {
                @Override
                public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                    return Boxed.this.decode(new Dynamic<>(ops, input));
                }

                @Override
                public String toString() {
                    return "BoxedDecoder[" + Boxed.this + "]";
                }
            };
        }

    }

    /**
     * A simple decoder interface that completely decodes an object from a {@link Dynamic}, discarding any remaining
     * serialized data.
     *
     * <p>This interface is a combination of {@link Decoder.Terminal} and {@link Decoder.Boxed}, and allows one
     * to implement a {@link Decoder} based on {@link Decoder#parse(Dynamic)}.
     *
     * @param <A> The type that this decoder deserializes.
     */
    interface Simple<A> {

        /**
         * Completely decodes an object from the given {@link Dynamic} data. Any remaining serialized data is
         * discarded.
         *
         * @param input The serialized data.
         * @param <T>   The type of the serialized form.
         * @return A {@link DataResult} containing the decoded object.
         * @see Decoder#parse(Dynamic)
         */
        <T> DataResult<A> decode(final Dynamic<T> input);

        /**
         * Returns a {@link Decoder} that performs the same actions as this simple decoder.
         *
         * @implSpec The default implementation returns a {@link Decoder} that returns the result of
         *     {@link #decode(Dynamic)}.
         * @implNote The returned {@link Decoder} is implemented via {@link Decoder#decode(DynamicOps, Object)},
         *     not via {@link Decoder#parse(Dynamic)}.
         */
        default Decoder<A> decoder() {
            return new Decoder<A>() {
                @Override
                public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                    return Simple.this.decode(new Dynamic<>(ops, input))
                        .map(a -> Pair.of(a, ops.empty()));
                }

                @Override
                public String toString() {
                    return "SimpleDecoder[" + Simple.this + "]";
                }
            };
        }

    }

}
