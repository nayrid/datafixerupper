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
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.ListBox;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * An abstract base class for objects that encapsulate a value of some serialized type. This allows methods that
 * return generic serialized values to define strongly typed signatures without knowing the actual types
 * of the serialized form.
 *
 * <p>Instances of this class effectively encapsulate a serialized value and the {@link DynamicOps} that
 * can produce and parse it. This allows for more fluid handling of serialized values without needing to
 * separately pass a {@link DynamicOps} object everywhere.
 *
 * @param <T> The type of the serialized form. This is generally unknown to all except the {@link DynamicOps}
 *            instance that defines the serialization behavior.
 * @see Dynamic
 * @see DynamicOps
 */
@SuppressWarnings("unused")
public abstract class DynamicLike<T> {

    /**
     * The {@link DynamicOps} instance used to deserialize values.
     */
    protected final DynamicOps<T> ops;

    /**
     * Constructs a new {@link DynamicLike} with the given serialization operations.
     *
     * @param ops The operations for the serialized type this object wraps.
     */
    public DynamicLike(final DynamicOps<T> ops) {
        this.ops = ops;
    }

    /**
     * Retrieves the {@link DynamicOps} object that defines the serialization behavior of this.
     */
    public DynamicOps<T> getOps() {
        return ops;
    }

    /**
     * Coerces and returns the wrapped value as a number in a {@link DataResult}.
     *
     * @see DynamicOps#getNumberValue(Object)
     */
    public abstract DataResult<Number> asNumber();

    /**
     * Coerces and returns the wrapped value as a string in a {@link DataResult}.
     *
     * @see DynamicOps#getStringValue(Object)
     */
    public abstract DataResult<String> asString();

    /**
     * Coerces and returns the wrapped value as a boolean value in a {@link DataResult}.
     *
     * @see DynamicOps#getBooleanValue(Object)
     */
    public abstract DataResult<Boolean> asBoolean();

    /**
     * Coerces and returns the wrapped value as a stream of values in a {@link DataResult}.
     *
     * @see DynamicOps#getStream(Object)
     */
    public abstract DataResult<Stream<Dynamic<T>>> asStreamOpt();

    /**
     * Coerces and returns the wrapped value as a map of entries in a {@link DataResult}.
     *
     * @see DynamicOps#getMapValues(Object)
     */
    public abstract DataResult<Stream<Pair<Dynamic<T>, Dynamic<T>>>> asMapOpt();

    /**
     * Coerces and returns the wrapped value as a {@link ByteBuffer} in a {@link DataResult}.
     *
     * @see DynamicOps#getByteBuffer(Object)
     */
    public abstract DataResult<ByteBuffer> asByteBufferOpt();

    /**
     * Coerces and returns the wrapped value as a stream of int values in a {@link DataResult}.
     *
     * @see DynamicOps#getIntStream(Object)
     */
    public abstract DataResult<IntStream> asIntStreamOpt();

    /**
     * Coerces and returns the wrapped value as a stream of long values in a {@link DataResult}.
     *
     * @see DynamicOps#getLongStream(Object)
     */
    public abstract DataResult<LongStream> asLongStreamOpt();

    /**
     * If the given key is present in the wrapped value, returns the value associated with it, else returns empty.
     *
     * @param key The key to search for.
     * @return The value associated with the key, wrapped in a {@link OptionalDynamic}.
     */
    public abstract OptionalDynamic<T> get(String key);

    /**
     * If the key represented by the given serialized value is present in the wrapped value, returns the value
     * associated with it, else returns empty.
     *
     * @param key The key to search for.
     * @return The value associated with the key, wrapped in a {@link DataResult}.
     */
    public abstract DataResult<T> getGeneric(T key);

    /**
     * If the given key is present in the wrapped value, returns the value associated with it, else returns empty.
     *
     * @param key The key to search for.
     * @return The value associated with the key, wrapped in a {@link DataResult}.
     * @apiNote This method is exactly equivalent to {@link #getGeneric(Object)}.
     * @see #get(String)
     */
    public abstract DataResult<T> getElement(String key);
    /**
     * If the key represented by the given serialized value is present in the wrapped value, returns the value
     * associated with it, else returns empty.
     *
     * @param key The key to search for.
     * @return The value associated with the key, wrapped in a {@link DataResult}.
     * @apiNote This method is exactly equivalent to {@link #getGeneric(Object)}.
     * @see #getGeneric(Object)
     */

    public abstract DataResult<T> getElementGeneric(T key);

    /**
     * Decodes an object using the given {@link Decoder}, and returns a {@link DataResult} containing a pair
     * of the decoded object and the remaining serialized value.
     *
     * @param decoder The decoder to use.
     * @param <A>     The type of the decoded object.
     * @return A {@link DataResult} containing the decoded object and the remaining serialized value.
     */
    public abstract <A> DataResult<Pair<A, T>> decode(final Decoder<? extends A> decoder);

    /**
     * Uses the given deserialization function to decode a list on elements from the wrapped value.
     *
     * @param deserializer The deserialization function.
     * @param <U>          The type of the deserialized elements.
     * @return A {@link DataResult} containing the list of values.
     */
    public <U> DataResult<List<U>> asListOpt(final Function<Dynamic<T>, U> deserializer) {
        return asStreamOpt().map(stream -> stream.map(deserializer).collect(Collectors.toList()));
    }

    /**
     * Uses the given deserialization functions to decode a map on key-value pairs from the wrapped value.
     *
     * @param keyDeserializer   The key deserialization function.
     * @param valueDeserializer The value deserialization function.
     * @param <K>               The type of the deserialized keys.
     * @param <V>               The type of the deserialized values.
     * @return A {@link DataResult} containing the deserialized map.
     * @implSpec This implementation calls {@link #asMapOpt()}, then transforms the keys and values using the given
     *     functions. In the case of duplicate keys, the key encountered last is preserved.
     */
    public <K, V> DataResult<Map<K, V>> asMapOpt(final Function<Dynamic<T>, K> keyDeserializer,
                                                 final Function<Dynamic<T>, V> valueDeserializer) {
        return asMapOpt().map(map -> {
            final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
            map.forEach(entry -> builder.put(keyDeserializer.apply(entry.first()),
                valueDeserializer.apply(entry.second())
            ));
            return builder.build();
        });
    }

    /**
     * Decodes an object from the wrapped value using the given {@link Decoder} and returns it in a {@link DataResult}.
     *
     * @param decoder The decoder to use.
     * @param <A>     The type of the decoded value.
     * @return A {@link DataResult} containing the decoded value.
     * @implSpec This implementation calls {@link #decode(Decoder)} and returns the first element of the resulting
     *     pair.
     */
    public <A> DataResult<A> read(final Decoder<? extends A> decoder) {
        return decode(decoder).map(Pair::first);
    }

    /**
     * Decodes a list of elements from the wrapped value using the given {@link Decoder}, and returns it in a
     * {@link DataResult}.
     *
     * @param decoder The decoder to use.
     * @param <E>     The element type of the decoded list.
     * @return A {@link DataResult} containing the decoded values.
     * @implSpec This implementation calls {@link #asStreamOpt()}, then decodes each element of the list using
     *     {@link Dynamic#read(Decoder)}.
     */
    public <E> DataResult<List<E>> readList(final Decoder<E> decoder) {
        return asStreamOpt().map(s -> s.map(d -> d.read(decoder))
                .collect(Collectors.<App<DataResult.Mu, E>>toList()))
            .flatMap(l -> DataResult.unbox(ListBox.flip(DataResult.instance(), l)));
    }

    /**
     * Uses the given deserialization function to deserialize a list of elements from the wrapped value. If the
     * deserialization function returns a error {@link DataResult} for any element, this method also returns
     * an error {@link DataResult}.
     *
     * @param decoder The deserialization function.
     * @param <E>     The element type of the deserialized list.
     * @return A {@link DataResult} containing the deserialized list elements.
     */
    public <E> DataResult<List<E>> readList(final Function<? super Dynamic<?>, ? extends DataResult<? extends E>> decoder) {
        return asStreamOpt().map(s -> s.map(decoder)
                .map(r -> r.map(e -> (E) e))
                .collect(Collectors.<App<DataResult.Mu, E>>toList()))
            .flatMap(l -> DataResult.unbox(ListBox.flip(DataResult.instance(), l)));
    }

    /**
     * Decodes a map into a list of pairs using the given key decoder and value decoder. If any key or value cannot
     * be decoded, then this method returns an error result.
     *
     * @param keyDecoder   The {@link Decoder} for the keys.
     * @param valueDecoder The {@link Decoder} for the values.
     * @param <K>          The key type.
     * @param <V>          The value type.
     * @return A {@link DataResult} containing the decoded list of pairs.
     * @implSpec This implementation first calls {@link #asMapOpt()}, then decodes the resulting keys and
     *     values using the given decoders.
     */
    public <K, V> DataResult<List<Pair<K, V>>> readMap(final Decoder<K> keyDecoder,
                                                       final Decoder<V> valueDecoder) {
        return asMapOpt().map(stream -> stream.map(p -> p.first()
                    .read(keyDecoder)
                    .flatMap(f -> p.second().read(valueDecoder).map(s -> Pair.of(f, s))))
                .collect(Collectors.<App<DataResult.Mu, Pair<K, V>>>toList()))
            .flatMap(l -> DataResult.unbox(ListBox.flip(DataResult.instance(), l)));
    }

    /**
     * Decodes a map into a list of pairs using the given key decoder and value decoder. If any key or value cannot
     * be decoded, then this method returns an error result.
     *
     * <p>The value decoder used is determined using the {@code valueDecoder} function, which returns a decoder to
     * use based on the key.
     *
     * @param keyDecoder   The {@link Decoder} for the keys.
     * @param valueDecoder The function used to determine the {@link Decoder} for the values.
     * @param <K>          The key type.
     * @param <V>          The value type.
     * @return A {@link DataResult} containing the decoded list of pairs.
     * @implSpec This implementation first calls {@link #asMapOpt()}, then decodes the resulting keys and
     *     values using the given decoders.
     */
    public <K, V> DataResult<List<Pair<K, V>>> readMap(final Decoder<K> keyDecoder,
                                                       final Function<K, Decoder<V>> valueDecoder) {
        return asMapOpt().map(stream -> stream.map(p -> p.first()
                    .read(keyDecoder)
                    .flatMap(f -> p.second().read(valueDecoder.apply(f)).map(s -> Pair.of(f, s))))
                .collect(Collectors.<App<DataResult.Mu, Pair<K, V>>>toList()))
            .flatMap(l -> DataResult.unbox(ListBox.flip(DataResult.instance(), l)));
    }

    /**
     * Performs a reduction operation on the map entries extracted from the wrapped value.
     *
     * @param empty    The empty or identity value of the result type.
     * @param combiner A function that takes the accumulated result and the serialized key and value, and produces
     *                 a new result value.
     * @param <R>      The type of the result.
     * @return A {@link DataResult} containing the result value.
     * @implSpec This implementation calls {@link #asMapOpt()}, then performs a reduction operation on the elements
     *     of the returned stream.
     * @see Stream#reduce(Object, java.util.function.BinaryOperator)
     */
    public <R> DataResult<R> readMap(final DataResult<R> empty,
                                     final Function3<R, Dynamic<T>, Dynamic<T>, DataResult<R>> combiner) {
        return asMapOpt().flatMap(stream -> {
            final AtomicReference<DataResult<R>> result = new AtomicReference<>(empty);
            stream.forEach(p -> result.setPlain(result.getPlain()
                .flatMap(r -> combiner.apply(r, p.first(), p.second()))));
            return result.getPlain();
        });
    }

    /**
     * Coerces and returns the wrapped value as a number, using the given value as a fallback.
     *
     * @param defaultValue The fallback value.
     * @return The deserialized number.
     * @see #asNumber()
     */
    public Number asNumber(final Number defaultValue) {
        return asNumber().result().orElse(defaultValue);
    }

    /**
     * Coerces and returns the wrapped value as an int, using the given value as a fallback.
     *
     * @param defaultValue The fallback value.
     * @return The deserialized int value.
     */
    public int asInt(final int defaultValue) {
        return asNumber(defaultValue).intValue();
    }

    /**
     * Coerces and returns the wrapped value as a long, using the given value as a fallback.
     *
     * @param defaultValue The fallback value.
     * @return The deserialized long value.
     */
    public long asLong(final long defaultValue) {
        return asNumber(defaultValue).longValue();
    }

    /**
     * Coerces and returns the wrapped value as a float, using the given value as a fallback.
     *
     * @param defaultValue The fallback value.
     * @return The deserialized float value.
     */
    public float asFloat(final float defaultValue) {
        return asNumber(defaultValue).floatValue();
    }

    /**
     * Coerces and returns the wrapped value as a double, using the given value as a fallback.
     *
     * @param defaultValue The fallback value.
     * @return The deserialized double value.
     */
    public double asDouble(final double defaultValue) {
        return asNumber(defaultValue).doubleValue();
    }

    /**
     * Coerces and returns the wrapped value as a byte, using the given value as a fallback.
     *
     * @param defaultValue The fallback value.
     * @return The deserialized byte value.
     */
    public byte asByte(final byte defaultValue) {
        return asNumber(defaultValue).byteValue();
    }

    /**
     * Coerces and returns the wrapped value as a short, using the given value as a fallback.
     *
     * @param defaultValue The fallback value.
     * @return The deserialized short value.
     */
    public short asShort(final short defaultValue) {
        return asNumber(defaultValue).shortValue();
    }

    /**
     * Coerces and returns the wrapped value as a boolean, using the given value as a fallback.
     *
     * @param defaultValue The fallback value.
     * @return The deserialized boolean value.
     */
    public boolean asBoolean(final boolean defaultValue) {
        return asBoolean().result().orElse(defaultValue);
    }

    /**
     * Coerces and returns the wrapped value as a {@link String}, using the given value as a fallback.
     *
     * @param defaultValue The fallback value.
     * @return The deserialized string value.
     * @see #asString()
     */
    public String asString(final String defaultValue) {
        return asString().result().orElse(defaultValue);
    }

    /**
     * Coerces and returns the wrapped value as a {@link Stream}.
     *
     * @return The deserialized stream, or an empty stream if none could be deserialized.
     * @see #asStreamOpt()
     */
    public Stream<Dynamic<T>> asStream() {
        return asStreamOpt().result().orElseGet(Stream::empty);
    }

    /**
     * Coerces and returns the wrapped value as a {@link ByteBuffer}.
     *
     * @return The deserialized buffer, or an empty byte buffer if none could be deserialized.
     * @see #asByteBufferOpt()
     */
    public ByteBuffer asByteBuffer() {
        return asByteBufferOpt().result().orElseGet(() -> ByteBuffer.wrap(new byte[0]));
    }

    /**
     * Coerces and returns the wrapped value as a {@link IntStream}.
     *
     * @return The deserialized stream, or an empty stream if none could be deserialized.
     * @see #asIntStreamOpt()
     */
    public IntStream asIntStream() {
        return asIntStreamOpt().result().orElseGet(IntStream::empty);
    }

    /**
     * Coerces and returns the wrapped value as a {@link LongStream}.
     *
     * @return The deserialized stream, or an empty stream if none could be deserialized.
     * @see #asLongStreamOpt()
     */
    public LongStream asLongStream() {
        return asLongStreamOpt().result().orElseGet(LongStream::empty);
    }

    /**
     * Coerces and returns the wrapped value as a {@link List}.
     *
     * @return The deserialized list, or an empty list if none could be deserialized.
     * @see #asListOpt(Function)
     */
    public <U> List<U> asList(final Function<Dynamic<T>, U> deserializer) {
        return asListOpt(deserializer).result().orElseGet(ImmutableList::of);
    }

    /**
     * Coerces and returns the wrapped value as a {@link Map}.
     *
     * @return The deserialized map, or an empty map if none could be deserialized.
     * @see #asMapOpt(Function, Function)
     */
    public <K, V> Map<K, V> asMap(final Function<Dynamic<T>, K> keyDeserializer,
                                  final Function<Dynamic<T>, V> valueDeserializer) {
        return asMapOpt(keyDeserializer, valueDeserializer).result().orElseGet(ImmutableMap::of);
    }

    /**
     * Retrieves the value associated with the given key in the wrapped value, or returns the default value
     * if none was found.
     *
     * @param key          The key to search for.
     * @param defaultValue The default value.
     * @return The value associated with the given key or the default value.
     * @see #getElement(String)
     */
    public T getElement(final String key, final T defaultValue) {
        return getElement(key).result().orElse(defaultValue);
    }

    /**
     * Retrieves the value associated with the given key in the wrapped value, or returns the default value
     * if none was found.
     *
     * @param key          The representation of the key to search for.
     * @param defaultValue The default value.
     * @return The value associated with the given key or the default value.
     * @see #getElementGeneric(Object)
     */
    public T getElementGeneric(final T key, final T defaultValue) {
        return getElementGeneric(key).result().orElse(defaultValue);
    }

    /**
     * Returns a dynamic value representing the empty list.
     *
     * @see DynamicOps#emptyList()
     */
    public Dynamic<T> emptyList() {
        return new Dynamic<>(ops, ops.emptyList());
    }

    /**
     * Returns a dynamic value representing the empty map.
     *
     * @see DynamicOps#emptyMap()
     */
    public Dynamic<T> emptyMap() {
        return new Dynamic<>(ops, ops.emptyMap());
    }

    /**
     * Returns a dynamic value representing the given number.
     *
     * @see DynamicOps#createNumeric(Number)
     */
    public Dynamic<T> createNumeric(final Number i) {
        return new Dynamic<>(ops, ops.createNumeric(i));
    }

    /**
     * Returns a dynamic value representing the given byte value.
     *
     * @see DynamicOps#createByte(byte)
     */
    public Dynamic<T> createByte(final byte value) {
        return new Dynamic<>(ops, ops.createByte(value));
    }

    /**
     * Returns a dynamic value representing the given short value.
     *
     * @see DynamicOps#createShort(short)
     */
    public Dynamic<T> createShort(final short value) {
        return new Dynamic<>(ops, ops.createShort(value));
    }

    /**
     * Returns a dynamic value representing the given byte value.
     *
     * @see DynamicOps#createByte(byte)
     */
    public Dynamic<T> createInt(final int value) {
        return new Dynamic<>(ops, ops.createInt(value));
    }

    /**
     * Returns a dynamic value representing the given long value.
     *
     * @see DynamicOps#createLong(long)
     */
    public Dynamic<T> createLong(final long value) {
        return new Dynamic<>(ops, ops.createLong(value));
    }

    /**
     * Returns a dynamic value representing the given float value.
     *
     * @see DynamicOps#createFloat(float)
     */
    public Dynamic<T> createFloat(final float value) {
        return new Dynamic<>(ops, ops.createFloat(value));
    }

    /**
     * Returns a dynamic value representing the given double value.
     *
     * @see DynamicOps#createDouble(double)
     */
    public Dynamic<T> createDouble(final double value) {
        return new Dynamic<>(ops, ops.createDouble(value));
    }

    /**
     * Returns a dynamic value representing the given boolean value.
     *
     * @see DynamicOps#createBoolean(boolean)
     */
    public Dynamic<T> createBoolean(final boolean value) {
        return new Dynamic<>(ops, ops.createBoolean(value));
    }

    /**
     * Returns a dynamic value representing the given {@link String} value.
     *
     * @see DynamicOps#createString(String)
     */
    public Dynamic<T> createString(final String value) {
        return new Dynamic<>(ops, ops.createString(value));
    }

    /**
     * Creates a dynamic list from the given stream of dynamic values.
     *
     * @see DynamicOps#createList(Stream)
     */
    public Dynamic<T> createList(final Stream<? extends Dynamic<?>> input) {
        return new Dynamic<>(ops, ops.createList(input.map(element -> element.cast(ops))));
    }

    /**
     * Creates a dynamic map from the given map of dynamic values.
     *
     * @see DynamicOps#createMap(Map)
     */
    public Dynamic<T> createMap(final Map<? extends Dynamic<?>, ? extends Dynamic<?>> map) {
        final ImmutableMap.Builder<T, T> builder = ImmutableMap.builder();
        for (final Map.Entry<? extends Dynamic<?>, ? extends Dynamic<?>> entry : map.entrySet()) {
            builder.put(entry.getKey().cast(ops), entry.getValue().cast(ops));
        }
        return new Dynamic<>(ops, ops.createMap(builder.build()));
    }

    /**
     * Creates a dynamic list from the given byte buffer
     *
     * @see DynamicOps#createByteList(ByteBuffer)
     */
    public Dynamic<?> createByteList(final ByteBuffer input) {
        return new Dynamic<>(ops, ops.createByteList(input));
    }

    /**
     * Creates a dynamic list from the given {@link IntStream}.
     *
     * @see DynamicOps#createIntList(IntStream)
     */
    public Dynamic<?> createIntList(final IntStream input) {
        return new Dynamic<>(ops, ops.createIntList(input));
    }

    /**
     * Creates a dynamic list from the given {@link LongStream}.
     *
     * @see DynamicOps#createLongList(LongStream)
     */
    public Dynamic<?> createLongList(final LongStream input) {
        return new Dynamic<>(ops, ops.createLongList(input));
    }

}
