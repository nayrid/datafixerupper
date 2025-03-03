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

import com.mojang.datafixers.util.Pair;
import java.util.function.Function;

/**
 * A {@link Traversable} functor that operates on a product type, such as a tuple. This type class defines two
 * methods, {@link #from(App)} and {@link #to(App)}, that convert the type {@code T} to and from a {@link Pair}.
 *
 * @param <T>  The partially applied product type.
 * @param <C>  The other type of interest in the product type.
 * @param <Mu> The witness type of this functor.
 * @see com.mojang.datafixers.optics.profunctors.Cartesian
 */
public interface CartesianLike<T extends K1, C, Mu extends CartesianLike.Mu> extends Functor<T, Mu>, Traversable<T, Mu> {

    /**
     * Thunk method that casts an applied {@link CartesianLike.Mu} to a {@link CartesianLike}.
     *
     * @param proofBox The boxed value.
     * @param <F>      The container type.
     * @param <C>      A type contained in {@link F}.
     * @param <Mu>     The witness type of this functor.
     * @return The unboxed value.
     */
    static <F extends K1, C, Mu extends CartesianLike.Mu> CartesianLike<F, C, Mu> unbox(final App<Mu, F> proofBox) {
        return (CartesianLike<F, C, Mu>) proofBox;
    }

    /**
     * Transforms a container of types {@code A} and {@code C} into a {@link Pair} of {@code (A, C)}.
     *
     * @param input The input container.
     * @param <A>   The type of the value stored in the container.
     * @return A pair containing the values stored in the container.
     */
    <A> App<Pair.Mu<C>, A> to(final App<T, A> input);

    /**
     * Transforms a pair of {@code (A, C)} into a container of {@code A} and {@code C}.
     *
     * @param input The input pair.
     * @param <A>   The type of value stored in the pair.
     * @return A container of the values in the pair.
     */
    <A> App<T, A> from(final App<Pair.Mu<C>, A> input);

    @Override
    default <F extends K1, A, B> App<F, App<T, B>> traverse(final Applicative<F, ?> applicative,
                                                            final Function<A, App<F, B>> function,
                                                            final App<T, A> input) {
        return applicative.map(this::from,
            new Pair.Instance<C>().traverse(applicative, function, to(input))
        );
    }

    /**
     * The witness type of {@link CartesianLike}.
     */
    interface Mu extends Functor.Mu, Traversable.Mu {

    }

}
