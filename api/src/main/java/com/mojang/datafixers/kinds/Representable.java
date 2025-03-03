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

import com.mojang.datafixers.FunctionType;

/**
 * A {@link Functor} that can convert values to and from a function.
 *
 * @param <T>  The container type.
 * @param <C>  The input type of the functions.
 * @param <Mu> The witness type of this functor.
 */
public interface Representable<T extends K1, C, Mu extends Representable.Mu> extends Functor<T, Mu> {

    /**
     * Thunk method that casts an applied {@link Representable.Mu} to a {@link Representable}.
     *
     * @param proofBox The boxed {@link Representable}.
     * @param <F>      The container type.
     * @param <C>      The function input type.
     * @param <Mu>     The witness type of the given functor.
     * @return The unboxed {@link Representable}.
     */
    static <F extends K1, C, Mu extends Representable.Mu> Representable<F, C, Mu> unbox(final App<Mu, F> proofBox) {
        return (Representable<F, C, Mu>) proofBox;
    }

    /**
     * Converts the given container to a function.
     *
     * @param input The container.
     * @param <A>   The type of contained value.
     * @return A function from the input type to the contained value type.
     */
    <A> App<FunctionType.ReaderMu<C>, A> to(final App<T, A> input);

    /**
     * Converts a function to a value of the container type.
     *
     * @param input A function from the input type to the contained value type.
     * @param <A>   The type of contained value.
     * @return A container.
     */
    <A> App<T, A> from(final App<FunctionType.ReaderMu<C>, A> input);

    /**
     * The witness type of a {@link Representable}.
     */
    interface Mu extends Functor.Mu {

    }

}
