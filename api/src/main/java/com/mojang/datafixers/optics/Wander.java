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
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;

/**
 * An interface related to {@link com.mojang.datafixers.kinds.Traversable}, but operating on an optic's object and
 * field types rather than on a functor. This interface is primarily used in specifying the {@link Traversal} optic.
 *
 * @param <S> The input object type.
 * @param <T> The output object type.
 * @param <A> The input field type.
 * @param <B> The output field type.
 * @see Traversal
 * @see com.mojang.datafixers.kinds.Traversable
 */
public interface Wander<S, T, A, B> {

    /**
     * Takes the given {@link Applicative}-effectful function over the field types and produces an effectful function
     * over the object types. The returned function applies the transformation to each field {@code A} in {@code S},
     * and combines the resulting effects into a single effect containing the transformed object {@code T}.
     *
     * @param applicative The {@link Applicative} type class instance defining the behavior of {@code F}.
     * @param input       A function from the input field type to the output field type.
     * @param <F>         The functor produced by the given function.
     * @return A function from the input object type to the output object type.
     */
    <F extends K1> FunctionType<S, App<F, T>> wander(final Applicative<F, ?> applicative,
                                                     final FunctionType<A, App<F, B>> input);

}
