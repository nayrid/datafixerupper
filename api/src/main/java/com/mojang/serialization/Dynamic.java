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

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.util.Pair;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * Encapsulates the serialized form of a single value. This class allows algorithms to work generically over
 * any serialized type without exposing {@link Object} parameters, and enforces the existence of a {@link DynamicOps}
 * instance for the particular serialized form. {@link Dynamic} also provides an interface similar to
 * {@link DynamicOps} for deserializing the contained value.
 *
 * @param <T> The type of the serialization form.
 * @see DynamicOps
 */
@SuppressWarnings("unused")
public class Dynamic<T> extends DynamicLike<T> {

    private final T value;

    /**
     * Constructs a new {@link Dynamic} containing the empty value of the given serialized form.
     *
     * @param ops The {@link DynamicOps} instance for the serialized form.
     */
    public Dynamic(final DynamicOps<T> ops) {
        this(ops, ops.empty());
    }

    /**
     * Constructs a new {@link Dynamic} containing the given value of the serialized form.
     *
     * @param ops   The {@link DynamicOps} instance for the serialized form.
     * @param value The value this object should contain. If null, this object will contain the empty value.
     */
    public Dynamic(final DynamicOps<T> ops, final @Nullable T value) {
        super(ops);
        this.value = value == null ? ops.empty() : value;
    }

    /**
     * Converts a raw serialized value from one form to another.
     *
     * @param inOps  The {@link DynamicOps} instance for the existing serialized value.
     * @param outOps The {@link DynamicOps} instance for the new serialized form.
     * @param input  The serialized value.
     * @param <S>    The type of the serialized form.
     * @param <T>    The type of the new serialized form.
     * @return A value equivalent to {@code input} of the new serialized form.
     * @apiNote This method should be used for converting raw serialized values. For converting a {@link Dynamic}
     *     value, see {@link #convert(DynamicOps)}.
     * @see #convert(DynamicOps)
     * @see DynamicOps#convertTo(DynamicOps, Object)
     */
    @SuppressWarnings("unchecked")
    public static <S, T> T convert(final DynamicOps<S> inOps,
                                   final DynamicOps<T> outOps,
                                   final S input) {
        if (Objects.equals(inOps, outOps)) {
            return (T) input;
        }

        return inOps.convertTo(outOps, input);
    }

    public static Dynamic<?> copyField(final Dynamic<?> source,
                                       final String sourceFieldName,
                                       final Dynamic<?> target,
                                       final String targetFieldName) {
        return copyAndFixField(source,
            sourceFieldName,
            target,
            targetFieldName,
            UnaryOperator.identity()
        );
    }

    public static <T> Dynamic<?> copyAndFixField(final Dynamic<T> source,
                                                 final String sourceFieldName,
                                                 final Dynamic<?> target,
                                                 final String targetFieldName,
                                                 final UnaryOperator<Dynamic<T>> fixer) {
        final Optional<Dynamic<T>> value = source.get(sourceFieldName).result();
        if (value.isPresent()) {
            return target.set(targetFieldName, fixer.apply(value.get()));
        }
        return target;
    }

    /**
     * Returns the contained serialized value.
     */
    public T getValue() {
        return value;
    }

    /**
     * Transforms the wrapped serialized value using the given mapping function.
     *
     * @param function The function to use to transform the value.
     * @return A {@link Dynamic} containing the new value.
     */
    public Dynamic<T> map(final Function<? super T, ? extends T> function) {
        return new Dynamic<>(ops, function.apply(value));
    }

    /**
     * Safely casts this {@link Dynamic} to a more specific type. This is typically used to
     * downcast a {@code Dynamic<?>} to a concrete serialized type.
     *
     * @param ops The {@link DynamicOps} instance for the serialized form.
     * @param <U> The type of the serialized form.
     * @return This {@link Dynamic}, casted to use the serialized form of the argument.
     * @throws IllegalStateException If the given {@link DynamicOps} does not {@code equal} the actual {@link DynamicOps}
     *                               instance used in this object.
     */
    @SuppressWarnings("unchecked")
    public <U> Dynamic<U> castTyped(final DynamicOps<U> ops) {
        if (!Objects.equals(this.ops, ops)) {
            throw new IllegalStateException("Dynamic type doesn't match");
        }
        return (Dynamic<U>) this;
    }

    /**
     * Safely casts this {@link Dynamic} to a more specific type, and returns the contained value. This method is
     * equivalent to {@code castTyped(ops).getValue()}.
     *
     * @param ops The {@link DynamicOps} instance for the serialized form.
     * @param <U> The type of the serialized form.
     * @return The contained serialized value casted to the type defined by the argument.
     * @throws IllegalStateException If the given {@link DynamicOps} does not {@code equal} the actual {@link DynamicOps}
     *                               instance used in this object.
     * @see #castTyped(DynamicOps)
     */
    public <U> U cast(final DynamicOps<U> ops) {
        return castTyped(ops).getValue();
    }

    /**
     * If this object contains a list, appends the given value to it, otherwise returns an empty {@link OptionalDynamic}.
     *
     * @param value The value to append to the list contained in this object.
     * @return An {@link OptionalDynamic} containing the merged list, or an empty {@link OptionalDynamic} if this value
     *     is not a list.
     * @throws IllegalStateException If {@code value} does not use the same serialization form as this object.
     * @see DynamicOps#mergeToList(Object, Object)
     */
    public OptionalDynamic<T> merge(final Dynamic<?> value) {
        final DataResult<T> merged = ops.mergeToList(this.value, value.cast(ops));
        return new OptionalDynamic<>(ops, merged.map(m -> new Dynamic<>(ops, m)));
    }

    /**
     * If this object contains a map, appends the given key-value pair to it, otherwise returns an empty
     * {@link OptionalDynamic}.
     *
     * @param key   The key to merge into the map.
     * @param value The value to merge into the map.
     * @return An {@link OptionalDynamic} containing the merged map, or an empty {@link OptionalDynamic} if this
     *     value is not a map.
     * @throws IllegalStateException If either {@code key} or {@code value} do not use the same serialization form as this
     *                               object.
     * @see DynamicOps#mergeToMap(Object, Object, Object)
     */
    public OptionalDynamic<T> merge(final Dynamic<?> key, final Dynamic<?> value) {
        final DataResult<T> merged = ops.mergeToMap(this.value, key.cast(ops), value.cast(ops));
        return new OptionalDynamic<>(ops, merged.map(m -> new Dynamic<>(ops, m)));
    }

    /**
     * Partially deserializes the contained value as a map of serialized entries.
     *
     * @return A {@link DataResult} containing the deserialized map, or an error result if the contained serialized
     *     value is not a map.
     * @see DynamicOps#getMapValues(Object)
     */
    public DataResult<Map<Dynamic<T>, Dynamic<T>>> getMapValues() {
        return ops.getMapValues(value).map(map -> {
            final ImmutableMap.Builder<Dynamic<T>, Dynamic<T>> builder = ImmutableMap.builder();
            map.forEach(entry -> builder.put(new Dynamic<>(ops, entry.first()),
                new Dynamic<>(ops, entry.second())
            ));
            return builder.build();
        });
    }

    /**
     * Updates every map entry contained within the serialized value using the given updating function.
     *
     * @param updater A function to transform the existing key-value pair to a new key-value pair.
     * @return A {@link Dynamic} containing the transformed value. If the contained serialized value is not a map,
     *     it is returned unchanged and {@code updater} is not called.
     */
    public Dynamic<T> updateMapValues(final Function<Pair<Dynamic<?>, Dynamic<?>>, Pair<Dynamic<?>, Dynamic<?>>> updater) {
        return DataFixUtils.orElse(getMapValues().map(map -> map.entrySet().stream().map(e -> {
            final Pair<Dynamic<?>, Dynamic<?>> pair = updater.apply(Pair.of(e.getKey(),
                e.getValue()
            ));
            return Pair.of(pair.first().castTyped(ops), pair.second().castTyped(ops));
        }).collect(Pair.toMap())).map(this::createMap).result(), this);
    }

    @Override
    public DataResult<Number> asNumber() {
        return ops.getNumberValue(value);
    }

    @Override
    public DataResult<String> asString() {
        return ops.getStringValue(value);
    }

    @Override
    public DataResult<Boolean> asBoolean() {
        return ops.getBooleanValue(value);
    }

    @Override
    public DataResult<Stream<Dynamic<T>>> asStreamOpt() {
        return ops.getStream(value).map(s -> s.map(e -> new Dynamic<>(ops, e)));
    }

    @Override
    public DataResult<Stream<Pair<Dynamic<T>, Dynamic<T>>>> asMapOpt() {
        return ops.getMapValues(value)
            .map(s -> s.map(p -> Pair.of(new Dynamic<>(ops, p.first()),
                new Dynamic<>(ops, p.second())
            )));
    }

    @Override
    public DataResult<ByteBuffer> asByteBufferOpt() {
        return ops.getByteBuffer(value);
    }

    @Override
    public DataResult<IntStream> asIntStreamOpt() {
        return ops.getIntStream(value);
    }

    @Override
    public DataResult<LongStream> asLongStreamOpt() {
        return ops.getLongStream(value);
    }

    @Override
    public OptionalDynamic<T> get(final String key) {
        return new OptionalDynamic<>(ops, ops.getMap(value).flatMap(m -> {
            final T value = m.get(key);
            if (value == null) {
                return DataResult.error(() -> "key missing: " + key + " in " + this.value);
            }
            return DataResult.success(new Dynamic<>(ops, value));
        }));
    }

    @Override
    public DataResult<T> getGeneric(final T key) {
        return ops.getGeneric(value, key);
    }

    /**
     * Removes the entry associated with the given key in the serialized value.
     *
     * @param key The key to remove from the map.
     * @return A {@link Dynamic} containing the transformed serialized value. If the contained serialized value is
     *     not a map, then it is returned unchanged.
     * @see DynamicOps#remove(Object, String)
     */
    public Dynamic<T> remove(final String key) {
        return map(v -> ops.remove(v, key));
    }

    /**
     * Adds the given entry to the contained serialized value.
     *
     * @param key   The key to add.
     * @param value The value to add, wrapped in a {@link Dynamic}.
     * @return A {@link Dynamic} containing the transformed serialized value. If the contained serialized value is
     *     not a map, then it is returned unchanged.
     * @see DynamicOps#set(Object, String, Object)
     */
    public Dynamic<T> set(final String key, final Dynamic<?> value) {
        return map(v -> ops.set(v, key, value.cast(ops)));
    }

    /**
     * Updates an existing entry in the contained serialized value using the given function. If the given key is
     * not present, the function is not called and no entry is added.
     *
     * @param key      The key of the entry to update.
     * @param function A function to produce a new value from the existing value.
     * @return A {@link Dynamic} containing the transformed serialized value. If the contained serialized value is
     *     not a map, then it is returned unchanged.
     * @see DynamicOps#update(Object, String, Function)
     */
    public Dynamic<T> update(final String key, final Function<Dynamic<?>, Dynamic<?>> function) {
        return map(v -> ops.update(v,
            key,
            value -> function.apply(new Dynamic<>(ops, value)).cast(ops)
        ));
    }

    /**
     * Updates an existing entry in the contained serialized value using the given function. This method is like
     * {@link #update(String, Function)}, expect that it operates on raw serialized values rather than wrapped
     * serialized values.
     *
     * @param key      The serialized form of the key of the entry to update.
     * @param function A function to produce a new value from the existing value.
     * @return A {@link Dynamic} containing the transformed serialized value. If the contained serialized value is
     *     not a map, then it is returned unchanged.
     * @see #update(String, Function)
     * @see DynamicOps#updateGeneric(Object, Object, Function)
     */
    public Dynamic<T> updateGeneric(final T key, final Function<T, T> function) {
        return map(v -> ops.updateGeneric(v, key, function));
    }

    public Dynamic<T> setFieldIfPresent(final String field,
                                        final Optional<? extends Dynamic<?>> value) {
        if (value.isEmpty()) {
            return this;
        }
        return set(field, value.get());
    }

    public Dynamic<T> renameField(final String oldFieldName, final String newFieldName) {
        return renameAndFixField(oldFieldName, newFieldName, UnaryOperator.identity());
    }

    public Dynamic<T> replaceField(final String oldFieldName,
                                   final String newFieldName,
                                   final Optional<? extends Dynamic<?>> newValue) {
        return remove(oldFieldName).setFieldIfPresent(newFieldName, newValue);
    }

    public Dynamic<T> renameAndFixField(final String oldFieldName,
                                        final String newFieldName,
                                        final UnaryOperator<Dynamic<?>> fixer) {
        return remove(oldFieldName).setFieldIfPresent(newFieldName,
            get(oldFieldName).result().map(fixer)
        );
    }

    @Override
    public DataResult<T> getElement(final String key) {
        return getElementGeneric(ops.createString(key));
    }

    @Override
    public DataResult<T> getElementGeneric(final T key) {
        return ops.getGeneric(value, key);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Dynamic<?> dynamic = (Dynamic<?>) o;
        return Objects.equals(ops, dynamic.ops) && Objects.equals(value, dynamic.value);
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + ops.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", ops, value);
    }

    /**
     * Converts this {@link Dynamic} to an equivalent object of another serialized form.
     *
     * @param outOps The {@link DynamicOps} instance representing the new serialized form.
     * @param <R>    The type of the new serialized form.
     * @return A {@link Dynamic} containing an equivalent value of the new serialized form.
     * @see DynamicOps#convertTo(DynamicOps, Object)
     */
    public <R> Dynamic<R> convert(final DynamicOps<R> outOps) {
        return new Dynamic<>(outOps, convert(ops, outOps, value));
    }

    /**
     * Applies the given action to this value and returns a result. The action is passed the value of {@code this}.
     *
     * @param action The action to perform on this dynamic value.
     * @param <V>    The result type.
     * @return The result produced by {@code action}.
     * @apiNote This method is typically used to transform this {@link Dynamic} <em>into</em> another type.
     */
    public <V> V into(final Function<? super Dynamic<T>, ? extends V> action) {
        return action.apply(this);
    }

    @Override
    public <A> DataResult<Pair<A, T>> decode(final Decoder<? extends A> decoder) {
        return decoder.decode(ops, value).map(p -> p.mapFirst(Function.identity()));
    }

}
