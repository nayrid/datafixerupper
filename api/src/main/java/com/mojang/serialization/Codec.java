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

import com.google.common.base.Suppliers;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.codecs.CompoundListCodec;
import com.mojang.serialization.codecs.DispatchedMapCodec;
import com.mojang.serialization.codecs.EitherCodec;
import com.mojang.serialization.codecs.EitherMapCodec;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import com.mojang.serialization.codecs.ListCodec;
import com.mojang.serialization.codecs.OptionalFieldCodec;
import com.mojang.serialization.codecs.PairCodec;
import com.mojang.serialization.codecs.PairMapCodec;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.SimpleMapCodec;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import com.mojang.serialization.codecs.XorCodec;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * A combined {@link Decoder} and {@link Encoder}.
 *
 * <p>A {@link Codec} handles transformation of a specific type to and from a provided serialized form. Codecs
 * encapsulate both the behaviors of an encoder and those of a decoder in the same object.
 *
 * <p>Implementations of {@link Codec} are immutable once created. The methods defined in this interface never
 * mutate the codec in a way visible to the outside.
 *
 * @param <A> The type this {@link Codec} serializes and deserializes.
 * @implSpec An implementation must include, at a minimum, definitions of the {@link #encode(Object, DynamicOps, Object)}
 *     and {@link #decode(DynamicOps, Object)} methods.
 * @implNote The default methods in this interface that return codecs or {@linkplain MapCodec map codecs}
 *     wrap this codec without adding any debugging context. These methods should be overridden if deeply nested
 *     codecs are undesirable or additional debugging context is desired.
 * @see MapCodec
 */
public interface Codec<A> extends Encoder<A>, Decoder<A> {

    /**
     * A {@link PrimitiveCodec} for the type {@code boolean}.
     */
    PrimitiveCodec<Boolean> BOOL = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Boolean> read(final DynamicOps<T> ops, final T input) {
            return ops.getBooleanValue(input);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Boolean value) {
            return ops.createBoolean(value);
        }

        @Override
        public String toString() {
            return "Bool";
        }
    };

    /**
     * A {@link PrimitiveCodec} for the type {@code byte}.
     */
    PrimitiveCodec<Byte> BYTE = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Byte> read(final DynamicOps<T> ops, final T input) {
            return ops.getNumberValue(input).map(Number::byteValue);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Byte value) {
            return ops.createByte(value);
        }

        @Override
        public String toString() {
            return "Byte";
        }
    };

    /**
     * A {@link PrimitiveCodec} for the type {@code short}.
     */
    PrimitiveCodec<Short> SHORT = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Short> read(final DynamicOps<T> ops, final T input) {
            return ops.getNumberValue(input).map(Number::shortValue);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Short value) {
            return ops.createShort(value);
        }

        @Override
        public String toString() {
            return "Short";
        }
    };

    /**
     * A {@link PrimitiveCodec} for the type {@code int}.
     */
    PrimitiveCodec<Integer> INT = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Integer> read(final DynamicOps<T> ops, final T input) {
            return ops.getNumberValue(input).map(Number::intValue);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Integer value) {
            return ops.createInt(value);
        }

        @Override
        public String toString() {
            return "Int";
        }
    };

    /**
     * A {@link PrimitiveCodec} for the type {@code long}.
     */
    PrimitiveCodec<Long> LONG = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Long> read(final DynamicOps<T> ops, final T input) {
            return ops.getNumberValue(input).map(Number::longValue);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Long value) {
            return ops.createLong(value);
        }

        @Override
        public String toString() {
            return "Long";
        }
    };

    /**
     * A {@link PrimitiveCodec} for the type {@code float}.
     */
    PrimitiveCodec<Float> FLOAT = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Float> read(final DynamicOps<T> ops, final T input) {
            return ops.getNumberValue(input).map(Number::floatValue);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Float value) {
            return ops.createFloat(value);
        }

        @Override
        public String toString() {
            return "Float";
        }
    };

    /**
     * A {@link PrimitiveCodec} for the type {@code double}.
     */
    PrimitiveCodec<Double> DOUBLE = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<Double> read(final DynamicOps<T> ops, final T input) {
            return ops.getNumberValue(input).map(Number::doubleValue);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final Double value) {
            return ops.createDouble(value);
        }

        @Override
        public String toString() {
            return "Double";
        }
    };

    /**
     * A {@link PrimitiveCodec} for the type {@link String}.
     */
    PrimitiveCodec<String> STRING = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<String> read(final DynamicOps<T> ops, final T input) {
            return ops.getStringValue(input);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final String value) {
            return ops.createString(value);
        }

        @Override
        public String toString() {
            return "String";
        }
    };

    /**
     * A {@link PrimitiveCodec} for the type {@link ByteBuffer} (a byte sequence).
     */
    PrimitiveCodec<ByteBuffer> BYTE_BUFFER = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<ByteBuffer> read(final DynamicOps<T> ops, final T input) {
            return ops.getByteBuffer(input);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final ByteBuffer value) {
            return ops.createByteList(value);
        }

        @Override
        public String toString() {
            return "ByteBuffer";
        }
    };

    /**
     * A {@link PrimitiveCodec} for the type {@link IntStream} (an int sequence).
     */
    PrimitiveCodec<IntStream> INT_STREAM = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<IntStream> read(final DynamicOps<T> ops, final T input) {
            return ops.getIntStream(input);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final IntStream value) {
            return ops.createIntList(value);
        }

        @Override
        public String toString() {
            return "IntStream";
        }
    };

    /**
     * A {@link PrimitiveCodec} for the type {@link LongStream} (a long sequence).
     */
    PrimitiveCodec<LongStream> LONG_STREAM = new PrimitiveCodec<>() {
        @Override
        public <T> DataResult<LongStream> read(final DynamicOps<T> ops, final T input) {
            return ops.getLongStream(input);
        }

        @Override
        public <T> T write(final DynamicOps<T> ops, final LongStream value) {
            return ops.createLongList(value);
        }

        @Override
        public String toString() {
            return "LongStream";
        }
    };

    /**
     * A codec that "passes through" serialized forms unchanged.
     */
    Codec<Dynamic<?>> PASSTHROUGH = new Codec<>() {
        @Override
        public <T> DataResult<Pair<Dynamic<?>, T>> decode(final DynamicOps<T> ops, final T input) {
            return DataResult.success(Pair.of(new Dynamic<>(ops, input), ops.empty()));
        }

        @Override
        public <T> DataResult<T> encode(final Dynamic<?> input,
                                        final DynamicOps<T> ops,
                                        final T prefix) {
            if (input.getValue() == input.getOps().empty()) {
                // nothing to merge, return rest
                return DataResult.success(prefix, Lifecycle.experimental());
            }

            final T casted = input.convert(ops).getValue();
            if (prefix == ops.empty()) {
                // no need to merge anything, return the old value
                return DataResult.success(casted, Lifecycle.experimental());
            }

            final DataResult<T> toMap = ops.getMap(casted)
                .flatMap(map -> ops.mergeToMap(prefix, map));
            return toMap.result().map(DataResult::success).orElseGet(() -> {
                final DataResult<T> toList = ops.getStream(casted)
                    .flatMap(stream -> ops.mergeToList(prefix,
                        stream.collect(Collectors.toList())
                    ));
                return toList.result()
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Don't know how to merge " + prefix + " and " + casted,
                        prefix,
                        Lifecycle.experimental()
                    ));
            });
        }

        @Override
        public String toString() {
            return "passthrough";
        }
    };

    /**
     * A {@link MapCodec} that performs no encoding nor decoding.
     */
    MapCodec<Unit> EMPTY = MapCodec.of(Encoder.empty(), Decoder.unit(Unit.INSTANCE));

    /**
     * Combines an {@link Encoder} and a {@link Decoder} into a {@link Codec}.
     *
     * @param encoder The {@link Encoder} defining the returned codec.
     * @param decoder The {@link Decoder} defining the returned codec.
     * @param <A>     The type the returned codec operates on.
     * @return A codec that combines the given encoder and decoder.
     */
    static <A> Codec<A> of(final Encoder<A> encoder, final Decoder<A> decoder) {
        return of(encoder, decoder, "Codec[" + encoder + " " + decoder + "]");
    }

    /**
     * Combines an {@link Encoder} and a {@link Decoder} into a named {@link Codec}.
     *
     * @param encoder The {@link Encoder} defining the returned codec.
     * @param decoder The {@link Decoder} defining the returned codec.
     * @param name    The name given in the string representation of the returned codec.
     * @param <A>     The type the returned codec operates on.
     * @return A codec that combines the given encoder and decoder.
     * @see #of(Encoder, Decoder)
     */
    static <A> Codec<A> of(final Encoder<A> encoder, final Decoder<A> decoder, final String name) {
        return new Codec<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return decoder.decode(ops, input);
            }

            @Override
            public <T> DataResult<T> encode(final A input,
                                            final DynamicOps<T> ops,
                                            final T prefix) {
                return encoder.encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }

    /**
     * Combines a {@link MapEncoder} and a {@link MapDecoder} into a {@link MapCodec}.
     *
     * @param encoder The {@link MapEncoder} defining the returned {@link MapCodec}.
     * @param decoder The {@link MapDecoder} defining the returned {@link MapCodec}.
     * @param <A>     The type the returned codec operates on.
     * @return A {@link MapCodec} combining the given encoder and decoder.
     */
    static <A> MapCodec<A> of(final MapEncoder<A> encoder, final MapDecoder<A> decoder) {
        return of(encoder, decoder, () -> "MapCodec[" + encoder + " " + decoder + "]");
    }

    /**
     * Combines a {@link MapEncoder} and a {@link MapDecoder} into a named {@link MapCodec}. The key stream
     * of the returned codec is the concatenation of the key stream of the encoder and decoder.
     *
     * @param encoder The {@link MapEncoder} defining the returned {@link MapCodec}.
     * @param decoder The {@link MapDecoder} defining the returned {@link MapCodec}.
     * @param name    The name given in the string representation of the returned {@link MapCodec}.
     * @param <A>     The type the returned codec operates on.
     * @return A {@link MapCodec} combining the given encoder and decoder.
     * @see #of(MapEncoder, MapDecoder)
     */
    static <A> MapCodec<A> of(final MapEncoder<A> encoder,
                              final MapDecoder<A> decoder,
                              final Supplier<String> name) {
        return new MapCodec<A>() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return Stream.concat(encoder.keys(ops), decoder.keys(ops));
            }

            @Override
            public <T> DataResult<A> decode(final DynamicOps<T> ops, final MapLike<T> input) {
                return decoder.decode(ops, input);
            }

            @Override
            public <T> RecordBuilder<T> encode(final A input,
                                               final DynamicOps<T> ops,
                                               final RecordBuilder<T> prefix) {
                return encoder.encode(input, ops, prefix);
            }

            @Override
            public String toString() {
                return name.get();
            }
        };
    }

    /**
     * Creates a codec for a {@link Pair} from codecs for its elements.
     *
     * @param first  A codec for the first element type.
     * @param second A codec for the second element type.
     * @param <F>    The first element type.
     * @param <S>    The second element type.
     * @return A codec for a {@link Pair} of the element types.
     */
    static <F, S> Codec<Pair<F, S>> pair(final Codec<F> first, final Codec<S> second) {
        return new PairCodec<>(first, second);
    }

    /**
     * Creates a codec for an {@link Either} from codecs for its elements.
     *
     * @param first  A codec for the first element type.
     * @param second A codec for the second element type.
     * @param <F>    The first element type.
     * @param <S>    The second element type.
     * @return A codec for an {@link Either} of the element types.
     */
    static <F, S> Codec<Either<F, S>> either(final Codec<F> first, final Codec<S> second) {
        return new EitherCodec<>(first, second);
    }

    static <F, S> Codec<Either<F, S>> xor(final Codec<F> first, final Codec<S> second) {
        return new XorCodec<>(first, second);
    }

    static <T> Codec<T> withAlternative(final Codec<T> primary,
                                        final Codec<? extends T> alternative) {
        return Codec.either(primary, alternative).xmap(Either::unwrap, Either::left);
    }

    static <T, U> Codec<T> withAlternative(final Codec<T> primary,
                                           final Codec<U> alternative,
                                           final Function<U, T> converter) {
        return Codec.either(primary, alternative)
            .xmap(either -> either.map(v -> v, converter), Either::left);
    }

    /**
     * Creates a map codec for a {@link Pair} from map codecs for its elements.
     *
     * @param first  A codec for the first element type.
     * @param second A codec for the second element type.
     * @param <F>    The first element type.
     * @param <S>    The second element type.
     * @return A codec for a {@link Pair} of the element types.
     */
    static <F, S> MapCodec<Pair<F, S>> mapPair(final MapCodec<F> first, final MapCodec<S> second) {
        return new PairMapCodec<>(first, second);
    }

    /**
     * Creates a map codec for an {@link Either} from map codecs for its elements.
     *
     * @param first  A codec for the first element type.
     * @param second A codec for the second element type.
     * @param <F>    The first element type.
     * @param <S>    The second element type.
     * @return A codec for an {@link Either} of the element types.
     */
    static <F, S> MapCodec<Either<F, S>> mapEither(final MapCodec<F> first,
                                                   final MapCodec<S> second) {
        return new EitherMapCodec<>(first, second);
    }

    /**
     * Creates a codec for a {@link List} from a codec for the element type.
     *
     * @param elementCodec A codec for the element type of the list.
     * @param <E>          The element type of the list.
     * @return A codec for a {@link List} of the element type.
     * @see #listOf()
     */
    static <E> Codec<List<E>> list(final Codec<E> elementCodec) {
        return list(elementCodec, 0, Integer.MAX_VALUE);
    }

    static <E> Codec<List<E>> list(final Codec<E> elementCodec,
                                   final int minSize,
                                   final int maxSize) {
        return new ListCodec<>(elementCodec, minSize, maxSize);
    }

    /**
     * Creates a codec for a list of key-value pairs from codecs for the element types.
     *
     * @param keyCodec     A codec for the key type.
     * @param elementCodec A codec for the element type.
     * @param <K>          The key type.
     * @param <V>          The element type.
     * @return A codec for a list of key-value pairs.
     * @implNote This implementation is likely more efficient than calling {@link #pair(Codec, Codec)} and
     *     {@link #list(Codec)} in sequence.
     * @see #pair(Codec, Codec)
     * @see #list(Codec)
     */
    static <K, V> Codec<List<Pair<K, V>>> compoundList(final Codec<K> keyCodec,
                                                       final Codec<V> elementCodec) {
        return new CompoundListCodec<>(keyCodec, elementCodec);
    }

    /**
     * Creates a map codec for a {@link java.util.Map} with a fixed key set.
     *
     * <p>For creating a map codec with an unbounded key set, see {@link #unboundedMap(Codec, Codec)}.
     *
     * @param keyCodec     A codec for the key type.
     * @param elementCodec A codec for the element type.
     * @param keys         A {@link Keyable} defining the set of allowed keys.
     * @param <K>          The key type.
     * @param <V>          The element type.
     * @return A {@link MapCodec} for a map with fixed keys.
     */
    static <K, V> SimpleMapCodec<K, V> simpleMap(final Codec<K> keyCodec,
                                                 final Codec<V> elementCodec,
                                                 final Keyable keys) {
        return new SimpleMapCodec<>(keyCodec, elementCodec, keys);
    }

    /**
     * Creates a map codec for a {@link java.util.Map} with arbitrary keys.
     *
     * <p>For creating a map codec with a fixed key set, see {@link #simpleMap(Codec, Codec, Keyable)}.
     *
     * @param keyCodec     A codec for the key type.
     * @param elementCodec A codec for the element type.
     * @param <K>          The key type.
     * @param <V>          The element type.
     * @return A {@link MapCodec} for a map with arbitrary keys.
     */
    static <K, V> UnboundedMapCodec<K, V> unboundedMap(final Codec<K> keyCodec,
                                                       final Codec<V> elementCodec) {
        return new UnboundedMapCodec<>(keyCodec, elementCodec);
    }

    static <K, V> Codec<Map<K, V>> dispatchedMap(final Codec<K> keyCodec,
                                                 final Function<K, Codec<? extends V>> valueCodecFunction) {
        return new DispatchedMapCodec<>(keyCodec, valueCodecFunction);
    }

    static <E> Codec<E> stringResolver(final Function<E, String> toString,
                                       final Function<String, E> fromString) {
        return Codec.STRING.flatXmap(name -> Optional.ofNullable(fromString.apply(name))
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "Unknown element name:" + name)),
            e -> Optional.ofNullable(toString.apply(e))
                .map(DataResult::success)
                .orElseGet(() -> DataResult.error(() -> "Element with unknown name: " + e))
        );
    }

    /**
     * Creates a {@link MapCodec} that encodes and decodes an optional record field. Absent {@link Optional} values
     * will not be serialized and visa-versa.
     *
     * @param name         The name of the field.
     * @param elementCodec A codec for the element type.
     * @param <F>          The element type.
     * @return A map codec for the optional field {@code name}.
     * @see #optionalFieldOf(String)
     * @see #fieldOf(String)
     */
    static <F> MapCodec<Optional<F>> optionalField(final String name,
                                                   final Codec<F> elementCodec,
                                                   final boolean lenient) {
        return new OptionalFieldCodec<>(name, elementCodec, lenient);
    }

    static <A> Codec<A> recursive(final String name, final Function<Codec<A>, Codec<A>> wrapped) {
        return new RecursiveCodec<>(name, wrapped);
    }

    static <A> Codec<A> lazyInitialized(final Supplier<Codec<A>> delegate) {
        return new RecursiveCodec<>(delegate.toString(), self -> delegate.get());
    }

    /**
     * Returns a codec that always decodes the given value and always encodes an empty value.
     *
     * @param defaultValue The value to decode.
     * @param <A>          The type of the value.
     * @return A codec that always decodes the given value.
     */
    static <A> Codec<A> unit(final A defaultValue) {
        return unit(() -> defaultValue);
    }

    /**
     * Returns a codec that always decodes a supplied value and always encodes an empty value.
     *
     * @param defaultValue A supplier of the value to decode.
     * @param <A>          The type of the value.
     * @return A codec that always decodes the given value.
     */
    static <A> Codec<A> unit(final Supplier<A> defaultValue) {
        return MapCodec.unit(defaultValue).codec();
    }

    // private
    static <N extends Number & Comparable<N>> Function<N, DataResult<N>> checkRange(final N minInclusive,
                                                                                    final N maxInclusive) {
        return value -> {
            if (value.compareTo(minInclusive) >= 0 && value.compareTo(maxInclusive) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error(() -> "Value " + value + " outside of range [" + minInclusive + ":" + maxInclusive + "]");
        };
    }

    static Codec<Integer> intRange(final int minInclusive, final int maxInclusive) {
        final Function<Integer, DataResult<Integer>> checker = checkRange(minInclusive,
            maxInclusive
        );
        return Codec.INT.flatXmap(checker, checker);
    }

    static Codec<Float> floatRange(final float minInclusive, final float maxInclusive) {
        final Function<Float, DataResult<Float>> checker = checkRange(minInclusive, maxInclusive);
        return Codec.FLOAT.flatXmap(checker, checker);
    }

    static Codec<Double> doubleRange(final double minInclusive, final double maxInclusive) {
        final Function<Double, DataResult<Double>> checker = checkRange(minInclusive, maxInclusive);
        return Codec.DOUBLE.flatXmap(checker, checker);
    }

    static Codec<String> string(final int minSize, final int maxSize) {
        return Codec.STRING.validate(value -> {
            final int length = value.length();
            if (length < minSize) {
                return DataResult.error(() -> "String \"" + value + "\" is too short: " + length + ", expected range [" + minSize + "-" + maxSize + "]");
            }
            if (length > maxSize) {
                return DataResult.error(() -> "String \"" + value + "\" is too long: " + length + ", expected range [" + minSize + "-" + maxSize + "]");
            }
            return DataResult.success(value);
        });
    }

    static Codec<String> sizeLimitedString(final int maxSize) {
        return string(0, maxSize);
    }

    /**
     * Sets the {@link Lifecycle} for any result data this codec produces.
     *
     * @param lifecycle the lifecycle to use.
     * @return An codec equivalent to this codec but with the given lifecycle.
     * @implSpec The default implementation returns another {@link Codec} that wraps this codec.
     * @see Lifecycle
     */
    @Override
    default Codec<A> withLifecycle(final Lifecycle lifecycle) {
        return new Codec<A>() {
            @Override
            public <T> DataResult<T> encode(final A input,
                                            final DynamicOps<T> ops,
                                            final T prefix) {
                return Codec.this.encode(input, ops, prefix).setLifecycle(lifecycle);
            }

            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return Codec.this.decode(ops, input).setLifecycle(lifecycle);
            }

            @Override
            public String toString() {
                return Codec.this.toString();
            }
        };
    }

    /**
     * Returns an equivalent {@link Codec} with the {@linkplain Lifecycle#stable() stable} lifecycle.
     *
     * @see Lifecycle#stable()
     */
    default Codec<A> stable() {
        return withLifecycle(Lifecycle.stable());
    }

    /**
     * Returns an equivalent {@link Codec} with a {@linkplain Lifecycle#deprecated(int) deprecated} lifecycle.
     *
     * @param since The deprecation version.
     * @see Lifecycle#deprecated(int)
     */
    default Codec<A> deprecated(final int since) {
        return withLifecycle(Lifecycle.deprecated(since));
    }

    /**
     * Returns a codec for a {@link List} of elements defined by this codec.
     *
     * @implSpec The default implementation returns {@code list(this)}.
     */
    default Codec<List<A>> listOf() {
        return list(this);
    }

    default Codec<List<A>> listOf(final int minSize, final int maxSize) {
        return list(this, minSize, maxSize);
    }

    default Codec<List<A>> sizeLimitedListOf(final int maxSize) {
        return listOf(0, maxSize);
    }

    /**
     * Transforms this codec into another codec using the given invertible mapping functions.
     *
     * <p>This method performs a {@code map} operation on both {@link Decoder} and {@link Encoder}.
     *
     * @param to   A function from this codec's type to the new type.
     * @param from A function from the new type to this codec's type.
     * @param <S>  The new type.
     * @return A codec for the new type.
     * @implSpec The default implementation calls {@link #comap(Function)} and {@link #map(Function)} on this codec.
     * @see #map(Function)
     * @see #comap(Function)
     */
    default <S> Codec<S> xmap(final Function<? super A, ? extends S> to,
                              final Function<? super S, ? extends A> from) {
        return Codec.of(comap(from), map(to), this + "[xmapped]");
    }

    /**
     * Transforms this codec into another codec using the given invertible partial function.
     *
     * <p>This method performs a {@code flatMap} operation on {@link Decoder} and a {@code map} operation on {@link Encoder}.
     *
     * @param to   A partial function form this codec's type to the new type. The value and any errors are wrapped in
     *             a {@link DataResult}.
     * @param from A function from the new type to this codec's type.
     * @param <S>  The new type.
     * @return A codec for the new type.
     * @implSpec The default implementation calls {@link #comap(Function)} and {@link #flatMap(Function)}.
     * @see #flatMap(Function)
     * @see #comap(Function)
     */
    default <S> Codec<S> comapFlatMap(final Function<? super A, ? extends DataResult<? extends S>> to,
                                      final Function<? super S, ? extends A> from) {
        return Codec.of(comap(from), flatMap(to), this + "[comapFlatMapped]");
    }

    /**
     * Transforms this codec into another codec using the given partially invertible function.
     *
     * <p>This method performs a {@code map} operation on {@link Decoder} and a {@code flatMap} operation on {@link Encoder}.
     *
     * @param to   A function form this codec's type to the new type.
     * @param from A partial function from the new type to this codec's type. The value and any errors are wrapped in
     *             a {@link DataResult}.
     * @param <S>  The new type.
     * @return A codec for the new type.
     * @implSpec The default implementation calls {@link #flatComap(Function)} and {@link #map(Function)}.
     * @see #map(Function)
     * @see #flatComap(Function)
     */
    default <S> Codec<S> flatComapMap(final Function<? super A, ? extends S> to,
                                      final Function<? super S, ? extends DataResult<? extends A>> from) {
        return Codec.of(flatComap(from), map(to), this + "[flatComapMapped]");
    }

    /**
     * Transforms this codec into another codec using the given partially invertible partial function.
     *
     * <p>This method performs a {@code flatMap} operation on both {@link Decoder} and {@link Encoder}.
     *
     * @param to   A partial function form this codec's type to the new type.The value and any errors are wrapped in
     *             a {@link DataResult}.
     * @param from A partial function from the new type to this codec's type. The value and any errors are wrapped in
     *             a {@link DataResult}.
     * @param <S>  The new type.
     * @return A codec for the new type.
     * @implSpec The default implementation calls {@link #flatComap(Function)} and {@link #flatMap(Function)}.
     * @see #flatMap(Function)
     * @see #flatComap(Function)
     */
    default <S> Codec<S> flatXmap(final Function<? super A, ? extends DataResult<? extends S>> to,
                                  final Function<? super S, ? extends DataResult<? extends A>> from) {
        return Codec.of(flatComap(from), flatMap(to), this + "[flatXmapped]");
    }

    /**
     * Returns a {@link MapCodec} that encodes and decodes objects in a record under a field with the given name.
     * The returned {@link MapCodec} may be used to serialize and deserialize many different fields, all using
     * different codecs, from a single record.
     *
     * @param name The field to encode and decode objects from.
     * @return A {@link MapCodec} that performs the same operations as this codec, but takes the serialized value
     *     from a record under the given field.
     * @implSpec The default implementation calls the superinterface default implementations
     *     {@link Encoder#fieldOf(String)} and {@link Decoder#fieldOf(String)} and combines them into a {@link MapCodec}.
     * @see com.mojang.serialization.codecs.RecordCodecBuilder
     * @see MapLike
     * @see MapCodec#of(MapEncoder, MapDecoder)
     */
    @Override
    default MapCodec<A> fieldOf(final String name) {
        return MapCodec.of(Encoder.super.fieldOf(name),
            Decoder.super.fieldOf(name),
            () -> "Field[" + name + ": " + this + "]"
        );
    }

    /**
     * Returns a {@link MapCodec} that encodes and decodes objects the may optionally appear in a record. Absent
     * {@link Optional} values will not be serialized to the record, and visa-versa.
     *
     * @param name The field to encode and decode objects from.
     * @return A {@link MapCodec} that performs the same operations as this codec, but taking the optional value
     *     from a record field.
     * @implSpec The default implementation returns {@code optionalField(name, this)}.
     * @see #optionalField(String, Codec, boolean)
     * @see #fieldOf(String)
     * @see #optionalFieldOf(String, Object)
     */
    default MapCodec<Optional<A>> optionalFieldOf(final String name) {
        return optionalField(name, this, false);
    }

    /**
     * Returns a {@link MapCodec} that encodes and decodes objects that may optionally appear in a record. Absent
     * values will be decoded as the default value, and the default value will be encoded as absent.
     *
     * @param name         The field to encode and decode objects from.
     * @param defaultValue The default value to use if the value is absent in the serialized form.
     * @return A {@link MapCodec} that performs operations on an optional record field.
     * @implSpec The default implementation calls {@link #optionalField(String, Codec, boolean)}, then maps absent optionals
     *     to the default value.
     */
    default MapCodec<A> optionalFieldOf(final String name, final A defaultValue) {
        return optionalFieldOf(name, defaultValue, false);
    }

    /**
     * Returns a {@link MapCodec} that encodes and decodes objects that may optionally appear in a record, and
     * additionally allows the caller to provide a lifecycle for the default value. Absent values will be decoded
     * as the default value, and the default value will be encoded as absent.
     *
     * @param name               The field to encode and decode objects from.
     * @param defaultValue       The default value to use if the value is absent in the serialized form.
     * @param lifecycleOfDefault The lifecycle of the default value.
     * @return A {@link MapCodec} that performs operations on an optional record field.
     * @implSpec The default implementation calls {@link #optionalFieldOf(String, Lifecycle, Object, Lifecycle)}
     *     with a field lifecycle of {@link Lifecycle#experimental()}.
     * @see #optionalFieldOf(String, Object)
     */
    default MapCodec<A> optionalFieldOf(final String name,
                                        final A defaultValue,
                                        final Lifecycle lifecycleOfDefault) {
        return optionalFieldOf(name, Lifecycle.experimental(), defaultValue, lifecycleOfDefault);
    }

    /**
     * Returns a {@link MapCodec} that encodes and decodes objects that may optionally appear in a record, and
     * additionally allows the caller to provide lifecycles for the present field values and the default value.
     * Absent values will be decoded as the default value, and the default value will be encoded as absent.
     *
     * @param name               The field to encode and decode objects from.
     * @param defaultValue       The default value to use if the value is absent in the serialized form.
     * @param fieldLifecycle     The lifecycle of the encoded or decoded fields.
     * @param lifecycleOfDefault The lifecycle of the default value.
     * @return A {@link MapCodec} that performs operations on an optional record field.
     * @implSpec The default implementation calls {@link #optionalField(String, Codec, boolean)}, then maps absent optionals
     *     to the default value.
     * @see #optionalFieldOf(String, Object)
     */
    default MapCodec<A> optionalFieldOf(final String name,
                                        final Lifecycle fieldLifecycle,
                                        final A defaultValue,
                                        final Lifecycle lifecycleOfDefault) {
        // setting lifecycle to stable on the outside since it will be overriden by the passed parameters
        return optionalFieldOf(name, fieldLifecycle, defaultValue, lifecycleOfDefault, false);
    }

    default MapCodec<Optional<A>> lenientOptionalFieldOf(final String name) {
        return optionalField(name, this, true);
    }

    default MapCodec<A> lenientOptionalFieldOf(final String name, final A defaultValue) {
        return optionalFieldOf(name, defaultValue, true);
    }

    default MapCodec<A> lenientOptionalFieldOf(final String name,
                                               final A defaultValue,
                                               final Lifecycle lifecycleOfDefault) {
        return lenientOptionalFieldOf(name,
            Lifecycle.experimental(),
            defaultValue,
            lifecycleOfDefault
        );
    }

    default MapCodec<A> lenientOptionalFieldOf(final String name,
                                               final Lifecycle fieldLifecycle,
                                               final A defaultValue,
                                               final Lifecycle lifecycleOfDefault) {
        return optionalFieldOf(name, fieldLifecycle, defaultValue, lifecycleOfDefault, true);
    }

    private MapCodec<A> optionalFieldOf(final String name,
                                        final A defaultValue,
                                        final boolean lenient) {
        return optionalField(name, this, lenient).xmap(o -> o.orElse(defaultValue),
            a -> Objects.equals(a, defaultValue) ? Optional.empty() : Optional.of(a)
        );
    }

    private MapCodec<A> optionalFieldOf(final String name,
                                        final Lifecycle fieldLifecycle,
                                        final A defaultValue,
                                        final Lifecycle lifecycleOfDefault,
                                        final boolean lenient) {
        // setting lifecycle to stable on the outside since it will be overriden by the passed parameters
        return optionalField(name, this, lenient).stable()
            .flatXmap(o -> o.map(v -> DataResult.success(v, fieldLifecycle))
                    .orElse(DataResult.success(defaultValue, lifecycleOfDefault)),
                a -> Objects.equals(a, defaultValue) ? DataResult.success(Optional.empty(),
                    lifecycleOfDefault
                ) : DataResult.success(Optional.of(a), fieldLifecycle)
            );
    }

    /**
     * Transforms the {@link DataResult} produced by this code's decode and encode methods.
     *
     * @param function The function to run on the produced {@link DataResult}.
     * @return A codec that runs the given function after decoding or encoding.
     * @implSpec The default implementation returns a new codec that wraps this codec.
     * @see #decode(DynamicOps, Object)
     * @see #encode(Object, DynamicOps, Object)
     */
    default Codec<A> mapResult(final ResultFunction<A> function) {
        return new Codec<A>() {
            @Override
            public <T> DataResult<T> encode(final A input,
                                            final DynamicOps<T> ops,
                                            final T prefix) {
                return function.coApply(ops, input, Codec.this.encode(input, ops, prefix));
            }

            @Override
            public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                return function.apply(ops, input, Codec.this.decode(ops, input));
            }

            @Override
            public String toString() {
                return Codec.this + "[mapResult " + function + "]";
            }
        };
    }

    /**
     * Provides a default value for decoding in case of error, and runs the given callback on error.
     *
     * @param onError The callback to run if decoding or encoding fails.
     * @param value   The default value to return if decoding fails.
     * @return A codec that implements the error handling behavior.
     * @implSpec The default implementation calls {@link #orElse(UnaryOperator, Object)}, passing an error
     *     function that returns its input after calling {@code onError}.
     * @see #orElse(Object)
     */
    default Codec<A> orElse(final Consumer<String> onError, final A value) {
        return orElse(DataFixUtils.consumerToFunction(onError), value);
    }

    /**
     * Provides a default value for decoding in case of error, and transforms the error.
     *
     * @param onError A function that transforms an error if one is returned.
     * @param value   The default value to return if decoding fails.
     * @return A codec that implements the error handling behavior.
     * @implSpec The default implementation calls {@link #mapResult(ResultFunction)} with a {@link ResultFunction}
     *     that maps the results and applies the error function.
     * @see ResultFunction
     */
    default Codec<A> orElse(final UnaryOperator<String> onError, final A value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> apply(final DynamicOps<T> ops,
                                                    final T input,
                                                    final DataResult<Pair<A, T>> a) {
                return DataResult.success(a.mapError(onError)
                    .result()
                    .orElseGet(() -> Pair.of(value, input)));
            }

            @Override
            public <T> DataResult<T> coApply(final DynamicOps<T> ops,
                                             final A input,
                                             final DataResult<T> t) {
                return t.mapError(onError);
            }

            @Override
            public String toString() {
                return "OrElse[" + onError + " " + value + "]";
            }
        });
    }

    /**
     * Provides a default value for decoding in case of error, and runs the given callback on error.
     *
     * @param onError The callback to run if decoding or encoding fails.
     * @param value   A supplier of default values to return if decoding fails.
     * @return A codec that implements the error handling behavior.
     * @implSpec The default implementation calls {@link #orElseGet(UnaryOperator, Supplier)}, passing an error
     *     function that returns its input after calling {@code onError}.
     */
    default Codec<A> orElseGet(final Consumer<String> onError, final Supplier<? extends A> value) {
        return orElseGet(DataFixUtils.consumerToFunction(onError), value);
    }

    /**
     * Provides a default value for decoding in case of error, and transforms the error.
     *
     * @param onError A function that transforms an error if one is returned.
     * @param value   A supplier of default values to return if decoding fails.
     * @return A codec that implements the error handling behavior.
     * @implSpec The default implementation calls {@link #mapResult(ResultFunction)} with a {@link ResultFunction}
     *     that maps the results and applies the error function.
     * @see ResultFunction
     */
    default Codec<A> orElseGet(final UnaryOperator<String> onError,
                               final Supplier<? extends A> value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> apply(final DynamicOps<T> ops,
                                                    final T input,
                                                    final DataResult<Pair<A, T>> a) {
                return DataResult.success(a.mapError(onError)
                    .result()
                    .orElseGet(() -> Pair.of(value.get(), input)));
            }

            @Override
            public <T> DataResult<T> coApply(final DynamicOps<T> ops,
                                             final A input,
                                             final DataResult<T> t) {
                return t.mapError(onError);
            }

            @Override
            public String toString() {
                return "OrElseGet[" + onError + " " + value.get() + "]";
            }
        });
    }

    /**
     * Provides a default value for decoding in case of error.
     *
     * @param value The default value to return if decoding fails.
     * @return A codec that returns the default value if decoding fails.
     * @implSpec The default implementation calls {@link #mapResult(ResultFunction)} with a {@link ResultFunction}
     *     that supplies the default value if this codec cannot decode.
     */
    default Codec<A> orElse(final A value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> apply(final DynamicOps<T> ops,
                                                    final T input,
                                                    final DataResult<Pair<A, T>> a) {
                return DataResult.success(a.result().orElseGet(() -> Pair.of(value, input)));
            }

            @Override
            public <T> DataResult<T> coApply(final DynamicOps<T> ops,
                                             final A input,
                                             final DataResult<T> t) {
                return t;
            }

            @Override
            public String toString() {
                return "OrElse[" + value + "]";
            }
        });
    }

    /**
     * Provides a default value for decoding in case of error.
     *
     * @param value A supplier of default values to return if decoding fails.
     * @return A codec that returns the default value if decoding fails.
     * @implSpec The default implementation calls {@link #mapResult(ResultFunction)} with a {@link ResultFunction}
     *     that supplies the default value if this codec cannot decode.
     */
    default Codec<A> orElseGet(final Supplier<? extends A> value) {
        return mapResult(new ResultFunction<A>() {
            @Override
            public <T> DataResult<Pair<A, T>> apply(final DynamicOps<T> ops,
                                                    final T input,
                                                    final DataResult<Pair<A, T>> a) {
                return DataResult.success(a.result().orElseGet(() -> Pair.of(value.get(), input)));
            }

            @Override
            public <T> DataResult<T> coApply(final DynamicOps<T> ops,
                                             final A input,
                                             final DataResult<T> t) {
                return t;
            }

            @Override
            public String toString() {
                return "OrElseGet[" + value.get() + "]";
            }
        });
    }

    @Override
    default Codec<A> promotePartial(final Consumer<String> onError) {
        return Codec.of(this, Decoder.super.promotePartial(onError));
    }

    /**
     * Returns a {@link Codec} that uses a default type key extracted using this codec to polymorphic-ally dispatch
     * against a complete set of supported subtypes. This method can be used to implement encoding and decoding over
     * algebraic types or sealed class hierarchies which may require different codecs for each subtype.
     *
     * <p>This method is equivalent to {@code dispatch("type", type, codec)}.
     *
     * <p><strong>Warning: This method is unsound. Care must be taken that the codecs returned by
     * {@code codec} actually accept the correct subtype, and not an incidental subtype thereof. Heap pollution
     * may occur when using this method unless the programmer checks that the correct types are inferred or
     * specified.</strong>
     *
     * @param type  A function that extracts the type key from the a polymorphic value.
     * @param codec A function that produces a codec for a subtype corresponding to the provided type key.
     * @param <E>   The polymorphic object type.
     * @return A codec that operates on the polymorphic object type.
     * @implSpec The default implementation calls {@link #dispatch(String, Function, Function)}
     *     with the type key {@code "type"}.
     * @see #dispatchStable(Function, Function)
     */
    default <E> Codec<E> dispatch(final Function<? super E, ? extends A> type,
                                  final Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return dispatch("type", type, codec);
    }

    /**
     * Returns a {@link Codec} that uses a type key extracted using this codec to polymorphic-ally dispatch against a
     * complete set of supported subtypes. This method can be used to implement encoding and decoding over
     * algebraic types or sealed class hierarchies which may require different codecs for each subtype.
     *
     * <p><strong>Warning: This method is unsound. Care must be taken that the codecs returned by
     * {@code codec} actually accept the correct subtype, and not an incidental subtype thereof. Heap pollution
     * may occur when using this method unless the programmer checks that the correct types are inferred or
     * specified.</strong>
     *
     * @param typeKey The field in the record that the serialized type key is stored under.
     * @param type    A partial function that extracts the type key from the a polymorphic value.
     * @param codec   A function that produces a codec for a subtype corresponding to the provided type key.
     * @param <E>     The polymorphic object type.
     * @return A codec that operates on the polymorphic object type.
     * @implSpec The default implementation calls {@link #partialDispatch(String, Function, Function)}, with
     *     the type and codec functions always returning a success.
     * @see #dispatch(Function, Function)
     */
    default <E> Codec<E> dispatch(final String typeKey,
                                  final Function<? super E, ? extends A> type,
                                  final Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return partialDispatch(typeKey,
            type.andThen(DataResult::success),
            codec.andThen(DataResult::success)
        );
    }

    /**
     * Returns a {@link Codec} that uses a default type key extracted using this codec to polymorphic-ally dispatch
     * against a complete set of supported subtypes. This method can be used to implement encoding and decoding over
     * algebraic types or sealed class hierarchies which may require different codecs for each subtype.
     *
     * <p>This method is equivalent to {@code dispatchStable("type", type, codec)}.
     *
     * <p><strong>Warning: This method is unsound. Care must be taken that the codecs returned by
     * {@code codec} actually accept the correct subtype, and not an incidental subtype thereof. Heap pollution
     * may occur when using this method unless the programmer checks that the correct types are inferred or
     * specified.</strong>
     *
     * @param type  A function that extracts the type key from the polymorphic value.
     * @param codec A function that produces a codec for a subtype corresponding to the provided type key.
     * @param <E>   The polymorphic object type.
     * @return A codec that operates on the polymorphic object type.
     * @implSpec The default implementation calls {@link #partialDispatch(String, Function, Function)}
     *     with the type key {@code "type"}.
     * @see #dispatchStable(Function, Function)
     */
    default <E> Codec<E> dispatchStable(final Function<? super E, ? extends A> type,
                                        final Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return partialDispatch("type",
            e -> DataResult.success(type.apply(e), Lifecycle.stable()),
            a -> DataResult.success(codec.apply(a), Lifecycle.stable())
        );
    }

    /**
     * Returns a {@link Codec} that uses a type key extracted using this codec to polymorphic-ally dispatch against a
     * partial set of supported subtypes. This method can be used to implement partial encoding and decoding over
     * algebraic types or sealed class hierarchies which may require different codecs for each subtype.
     *
     * <p><strong>Warning: This method is unsound. Care must be taken that the codecs returned by
     * {@code codec} actually accept the correct subtype, and not an incidental subtype thereof. Heap pollution
     * may occur when using this method unless the programmer checks that the correct types are inferred or
     * specified.</strong>
     *
     * @param typeKey The field in the record that the serialized type key is stored under.
     * @param type    A partial function that extracts the type key from the polymorphic value.
     * @param codec   A function that produces a codec for a subtype corresponding to the provided type key.
     * @param <E>     The polymorphic object type.
     * @return A codec that operates on the polymorphic object type.
     * @implSpec The default implementation returns a codec generated from a {@link KeyDispatchCodec}.
     * @see #dispatch(String, Function, Function)
     */
    default <E> Codec<E> partialDispatch(final String typeKey,
                                         final Function<? super E, ? extends DataResult<? extends A>> type,
                                         final Function<? super A, ? extends DataResult<? extends MapCodec<? extends E>>> codec) {
        return new KeyDispatchCodec<>(typeKey, this, type, codec).codec();
    }

    /**
     * Returns a {@link MapCodec} that uses a default type key extracted using this codec to polymorphic-ally dispatch
     * against a complete set of supported subtypes. This method can be used to implement encoding and decoding over
     * algebraic types or sealed class hierarchies which may require different codecs for each subtype.
     *
     * <p>This method is equivalent to {@code dispatchMap("type", type, codec)}.
     *
     * <p><strong>Warning: This method is unsound. Care must be taken that the codecs returned by
     * {@code codec} actually accept the correct subtype, and not an incidental subtype thereof. Heap pollution
     * may occur when using this method unless the programmer checks that the correct types are inferred or
     * specified.</strong>
     *
     * @param type  A function that extracts the type key from the polymorphic value.
     * @param codec A function that produces a codec for a subtype corresponding to the provided type key.
     * @param <E>   The polymorphic object type.
     * @return A codec that operates on the polymorphic object type.
     * @implSpec The default implementation calls {@link #dispatchMap(String, Function, Function)}
     *     with the type key {@code "type"}.
     * @see #dispatchStable(Function, Function)
     */
    default <E> MapCodec<E> dispatchMap(final Function<? super E, ? extends A> type,
                                        final Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return dispatchMap("type", type, codec);
    }

    /**
     * Returns a {@link MapCodec} that uses a type key extracted using this codec to polymorphic-ally dispatch against a
     * complete set of supported subtypes. This method can be used to implement encoding and decoding over
     * algebraic types or sealed class hierarchies which may require different codecs for each subtype.
     *
     * <p><strong>Warning: This method is unsound. Care must be taken that the codecs returned by
     * {@code codec} actually accept the correct subtype, and not an incidental subtype thereof. Heap pollution
     * may occur when using this method unless the programmer checks that the correct types are inferred or
     * specified.</strong>
     *
     * @param typeKey The field in the record that the serialized type key is stored under.
     * @param type    A function that extracts the type key from the a polymorphic value.
     * @param codec   A function that produces a codec for a subtype corresponding to the provided type key.
     * @param <E>     The polymorphic object type.
     * @return A codec that operates on the polymorphic object type.
     * @implSpec The default implementation returns a new {@link KeyDispatchCodec} with the
     *     type and codec functions always returning a success.
     * @see KeyDispatchCodec
     */
    default <E> MapCodec<E> dispatchMap(final String typeKey,
                                        final Function<? super E, ? extends A> type,
                                        final Function<? super A, ? extends MapCodec<? extends E>> codec) {
        return new KeyDispatchCodec<>(typeKey,
            this,
            type.andThen(DataResult::success),
            codec.andThen(DataResult::success)
        );
    }

    default Codec<A> validate(final Function<A, DataResult<A>> checker) {
        return flatXmap(checker, checker);
    }

    /**
     * A unary operator applied on the result obtained from decoding or encoding from a {@link Codec}. This can be used
     * to directly transform the {@link DataResult} returned from the decoding and encoding methods.
     *
     * @param <A> The type this operator transforms.
     */
    interface ResultFunction<A> {

        /**
         * Applies a transformation to the result of decoding an object {@code a}. This transformation may read
         * more input from the serialized form, or transform successes into errors and visa-versa.
         *
         * @param ops   The {@link DynamicOps} instance defining the serialized form.
         * @param input The original input to {@link Decoder#decode(Dynamic)}.
         * @param a     The result obtained from {@link Decoder#decode(Dynamic)}.
         * @param <T>   The type of the serialized form.
         * @return The transformed {@link DataResult}.
         */
        <T> DataResult<Pair<A, T>> apply(final DynamicOps<T> ops,
                                         final T input,
                                         final DataResult<Pair<A, T>> a);

        /**
         * Applies a transformation to the result of encoding an object. This transformation may serialize additional
         * data from the input object, or transform successes into errors and visa-versa.
         *
         * @param ops   The {@link DynamicOps} instance defining the serialized form.
         * @param input The original input to {@link Encoder#encode(Object, DynamicOps, Object)}.
         * @param t     the result obtained from {@link Encoder#encode(Object, DynamicOps, Object)}.
         * @param <T>   The type of the serialized form.
         * @return The transformed {@link DataResult}.
         */
        <T> DataResult<T> coApply(final DynamicOps<T> ops, final A input, final DataResult<T> t);

    }

    class RecursiveCodec<T> implements Codec<T> {

        private final String name;
        private final Supplier<Codec<T>> wrapped;

        private RecursiveCodec(final String name, final Function<Codec<T>, Codec<T>> wrapped) {
            this.name = name;
            this.wrapped = Suppliers.memoize(() -> wrapped.apply(this));
        }

        @Override
        public <S> DataResult<Pair<T, S>> decode(final DynamicOps<S> ops, final S input) {
            return wrapped.get().decode(ops, input);
        }

        @Override
        public <S> DataResult<S> encode(final T input, final DynamicOps<S> ops, final S prefix) {
            return wrapped.get().encode(input, ops, prefix);
        }

        @Override
        public String toString() {
            return "RecursiveCodec[" + name + ']';
        }

    }

}
