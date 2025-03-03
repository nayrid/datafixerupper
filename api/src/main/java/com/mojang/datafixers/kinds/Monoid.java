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
package com.mojang.datafixers.kinds;

import com.google.common.collect.ImmutableList;
import java.util.List;

/**
 * The monoid type class defines the {@link #add(Object, Object)} method for combining two objects.
 *
 * @param <T> The object type that this monoid instance handles.
 * @see <a href="https://en.wikipedia.org/wiki/Monoid">The monoid algebraic structure / type class</a>
 */
public interface Monoid<T> {

    /**
     * Creates a monoid instance for {@link List}.
     *
     * @param <T> The element type of the list.
     * @return The list monoid.
     */
    static <T> Monoid<List<T>> listMonoid() {
        // TODO: immutable list with structural sharing
        return new Monoid<>() {
            @Override
            public List<T> point() {
                return ImmutableList.of();
            }

            @Override
            public List<T> add(final List<T> first, final List<T> second) {
                final ImmutableList.Builder<T> builder = ImmutableList.builder();
                builder.addAll(first);
                builder.addAll(second);
                return builder.build();
            }
        };
    }

    /**
     * Returns the identity element of this monoid.
     *
     * @return The identity element.
     * @see #add(Object, Object)
     */
    T point();

    /**
     * Combines {@code first} and {@code second} together.
     *
     * <p>When the identity element is combined with any other object,
     * the result is the other input parameter.
     *
     * @param first  The first input.
     * @param second The second input.
     * @return The combined result.
     */
    T add(final T first, final T second);

}
