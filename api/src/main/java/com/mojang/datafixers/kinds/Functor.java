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

import java.util.function.Function;

/**
 * The functor type class defines one method, {@link #map(Function, App)}, which transforms the contents of a container
 * to another type.
 *
 * <p>In order to be a <em>lawful functor</em>, the implementation of {@link #map(Function, App)} must satisfy
 * the following requirements ({@code ==} represents logical equality and not reference equality).
 *
 * <ol>
 *     <li>
 *         {@code map(identity(), ft) = ft} - Mapping with the identity function yields the input.
 *     </li>
 *     <li>
 *         {@code map(f.compose(g), ft) = map(f, map(g, ft))} - Mapping can be distributed over function composition.
 *     </li>
 * </ol>
 *
 * <p>Functors which do not satisfy these laws are said to be either <em>neutral</em> or <em>chaotic</em>, depending on
 * the degree to which the laws are violated.
 *
 * @param <F>  The container type.
 * @param <Mu> The witness type of this functor.
 * @see <a href="https://en.wikipedia.org/wiki/Functor_(functional_programming)">The functor type class</a>
 */
public interface Functor<F extends K1, Mu extends Functor.Mu> extends Kind1<F, Mu> {

    /**
     * Unboxes an {@link App} representing a functor into a functor.
     *
     * @param proofBox The boxed functor.
     * @param <F>      The container type.
     * @param <Mu>     The witness type of the functor.
     * @return The unboxed functor.
     * @throws ClassCastException If {@code proofBox} is not a functor.
     */
    static <F extends K1, Mu extends Functor.Mu> Functor<F, Mu> unbox(final App<Mu, F> proofBox) {
        return (Functor<F, Mu>) proofBox;
    }

    /**
     * Maps the contents of {@code ts} from {@code T} to {@code R} using the {@code func}.
     *
     * @param func The transformation function.
     * @param ts   The input container that will be transformed.
     * @param <T>  The input type.
     * @param <R>  The output type.
     * @return The transformed container.
     * @implSpec This method must obey the functor laws in order for this functor to be lawful.
     */
    <T, R> App<F, R> map(final Function<? super T, ? extends R> func, final App<F, T> ts);

    /**
     * The witness type of a functor.
     */
    interface Mu extends Kind1.Mu {

    }

}
