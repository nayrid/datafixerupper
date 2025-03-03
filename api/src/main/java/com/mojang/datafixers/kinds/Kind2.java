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

/**
 * A <em>type class</em> for a binary type constructor.
 *
 * <p>A type class may be thought of as an interface for types. All types that implement this type class
 * must define the operations specified within that type class. This interface, being the base type for
 * type classes, specifies no required operations.
 *
 * @param <F>  The witness type for the type constructor this type class is defined for.
 * @param <Mu> The witness type for this type class.
 * @apiNote This interface is called {@link Kind2} because it operates on types of the kind {@link K2}.
 * @see Kind1
 * @see K2
 * @see <a href="https://en.wikipedia.org/wiki/Type_class">Type class</a>
 */
public interface Kind2<F extends K2, Mu extends Kind2.Mu> extends App<Mu, F> {

    /**
     * Thunk method that casts an applied {@link Kind2.Mu} to a {@link Kind2}.
     *
     * @param proofBox The boxed {@link Kind2}.
     * @param <F>      The container type.
     * @param <Proof>  The witness type.
     * @return The cast {@link Kind2}.
     */
    static <F extends K2, Proof extends Kind2.Mu> Kind2<F, Proof> unbox(final App<Proof, F> proofBox) {
        return (Kind2<F, Proof>) proofBox;
    }

    /**
     * The witness type of a {@link Kind2}.
     */
    interface Mu extends K1 {

    }

}
