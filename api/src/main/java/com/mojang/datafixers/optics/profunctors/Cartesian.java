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
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.CartesianLike;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.util.Pair;

/**
 * A profunctor that supports converting a transformation acting on some type to and from a transformation acting
 * on pairs holding that type. This allows one to add a "pass-through" value to the type transformed via the
 * profunctor that is left untransformed.
 *
 * @param <P>  The transformation type.
 * @param <Mu> The witness type for this profunctor.
 */
public interface Cartesian<P extends K2, Mu extends Cartesian.Mu> extends Profunctor<P, Mu> {

    /**
     * Thunk method that casts an applied {@link Cartesian.Mu} to a {@link Cartesian}.
     *
     * @param proofBox The boxed {@link Cartesian}.
     * @param <P>      The transformation type.
     * @param <Proof>  The witness type for the profunctor.
     * @return The unboxed {@link Cartesian}.
     */
    static <P extends K2, Proof extends Cartesian.Mu> Cartesian<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (Cartesian<P, Proof>) proofBox;
    }

    /**
     * Converts the given transformation into one that transforms the first type of a {@link Pair}. The second type
     * is not transformed - any values of that type are passed through the returned transformation unchanged.
     *
     * @param input The transformation.
     * @param <A>   The input type.
     * @param <B>   The output type.
     * @param <C>   A "pass-through" type that is not transformed.
     * @return A transformation on pairs, where the first type is transformed and the second is not.
     */
    <A, B, C> App2<P, Pair<A, C>, Pair<B, C>> first(final App2<P, A, B> input);

    /**
     * Converts the given transformation into one that transforms the second type of a {@link Pair}. The first type
     * is not transformed - any values of that type are passed through the returned transformation unchanged.
     *
     * @param input The transformation.
     * @param <A>   The input type.
     * @param <B>   The output type.
     * @param <C>   A "pass-through" type that is not transformed.
     * @return A transformation on pairs, where the second type is transformed and the first is not.
     * @implSpec The default implementation calls {@link #first(App2)} and swaps the order of the types in the
     *     {@link Pair}.
     */
    default <A, B, C> App2<P, Pair<C, A>, Pair<C, B>> second(final App2<P, A, B> input) {
        return dimap(first(input), Pair::swap, Pair::swap);
    }

    /**
     * Converts this profunctor into a {@link FunctorProfunctor} that distributes {@link CartesianLike} functors.
     */
    default FunctorProfunctor<CartesianLike.Mu, P, FunctorProfunctor.Mu<CartesianLike.Mu>> toFP2() {
        return new FunctorProfunctor<CartesianLike.Mu, P, FunctorProfunctor.Mu<CartesianLike.Mu>>() {
            @Override
            public <A, B, F extends K1> App2<P, App<F, A>, App<F, B>> distribute(final App<? extends CartesianLike.Mu, F> proof,
                                                                                 final App2<P, A, B> input) {
                return cap(CartesianLike.unbox(proof), input);
            }

            private <A, B, F extends K1, C> App2<P, App<F, A>, App<F, B>> cap(final CartesianLike<F, C, ?> cLike,
                                                                              final App2<P, A, B> input) {
                return dimap(first(input), p -> Pair.unbox(cLike.to(p)), cLike::from);
            }
        };
    }

    /**
     * The witness type for {@link Cartesian}.
     */
    interface Mu extends Profunctor.Mu {

        /**
         * The value representing the witness type {@link Cartesian.Mu}.
         */
        TypeToken<Mu> TYPE_TOKEN = new TypeToken<>() {
        };

    }

}
