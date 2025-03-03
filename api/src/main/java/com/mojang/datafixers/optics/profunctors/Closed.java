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
package com.mojang.datafixers.optics.profunctors;

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;

/**
 * A {@link Profunctor} that may be turned into a transformation on functions. This allows one to pass functions
 * through a profunctor and interleave them into the effects governed through this profunctor.
 *
 * @param <P>  The profunctor type.
 * @param <Mu> The witness type for this profunctor.
 */
public interface Closed<P extends K2, Mu extends Closed.Mu> extends Profunctor<P, Mu> {

    /**
     * Casts a boxed {@link Closed} to its unboxed form.
     *
     * @param proofBox The boxed profunctor.
     * @param <P>      The profunctor type.
     * @param <Proof>  The witness type.
     * @return The unboxed profunctor.
     */
    static <P extends K2, Proof extends Closed.Mu> Closed<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (Closed<P, Proof>) proofBox;
    }

    /**
     * Partially composes the given transformation by creating a transformation on functions based on it.
     *
     * @param input The transformation.
     * @param <A>   The input type.
     * @param <B>   The output type.
     * @param <X>   An intermediary input type.
     * @return A transformation on functions from the intermediary input type to the I/O types.
     */
    <A, B, X> App2<P, FunctionType<X, A>, FunctionType<X, B>> closed(final App2<P, A, B> input);

    /**
     * The witness type for a {@link Closed}.
     */
    interface Mu extends Profunctor.Mu {

        /**
         * The run time type token for {@link Closed} type constructors.
         */
        TypeToken<Mu> TYPE_TOKEN = new TypeToken<>() {
        };

    }

}
