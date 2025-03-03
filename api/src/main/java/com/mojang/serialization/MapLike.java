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
import java.util.Map;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

/**
 * An unmodifiable store for serialized key-value pairs. This interface can be used when access to and iteration over
 * serialized key-value pairs is necessary, but other aspects of Java maps, such as mutability, are unnecessary.
 *
 * @param <T> The type of the serialized form.
 */
public interface MapLike<T> {

    /**
     * Creates a {@link MapLike} containing the entries of the given {@link Map}.
     *
     * <p>The map is not defensively copied, so modifications to the map are reflected in the returned {@link MapLike}.
     *
     * @param map The map to wrap.
     * @param ops A {@link DynamicOps} instance defining the serialized form.
     * @param <T> The type of the serialized form.
     * @return A {@link MapLike} containing the entries of the given map.
     */
    static <T> MapLike<T> forMap(final Map<T, T> map, final DynamicOps<T> ops) {
        return new MapLike<T>() {
            @Override
            public @Nullable T get(final T key) {
                return map.get(key);
            }

            @Override
            public @Nullable T get(final String key) {
                return get(ops.createString(key));
            }

            @Override
            public Stream<Pair<T, T>> entries() {
                return map.entrySet().stream().map(e -> Pair.of(e.getKey(), e.getValue()));
            }

            @Override
            public String toString() {
                return "MapLike[" + map + "]";
            }
        };
    }

    /**
     * Retrieves the value corresponding to the given key, or {@code null} if there is no mapping for the given key.
     * Note that, unlike {@link Map#get(Object)}, a return value of {@code null} should only occur if this map
     * truly does not contain a mapping.
     *
     * @param key The serialized key.
     * @return The value associated with the key, or {@code null} if the entry does not exist.
     */
    @Nullable T get(final T key);

    /**
     * Retrieves the value corresponding to the given key, or {@code null} if there is no mapping for the given key.
     * Note that, unlike {@link Map#get(Object)}, a return value of {@code null} should only occur if this map
     * truly does not contain a mapping.
     *
     * <p>The argument should be the {@link String} representation of a serialized key.
     *
     * @param key The string key.
     * @return The value associated with the key, or {@code null} if the entry does not exist.
     */
    @Nullable T get(final String key);

    /**
     * Returns a fresh {@link Stream} of all the entries in this map.
     */
    Stream<Pair<T, T>> entries();

}
