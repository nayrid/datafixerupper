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
import com.mojang.datafixers.kinds.CocartesianLike;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.util.Either;

/**
 * A profunctor that supports converting a transformation acting on some type to and from a transformation acting
 * on {@link Either eithers} holding that type. This allows one to add a "pass-through" value to the type transformed
 * via the profunctor that is left untransformed.
 *
 * @param <P>  The transformation type.
 * @param <Mu> The witness type for this profunctor.
 */
public interface Cocartesian<P extends K2, Mu extends Cocartesian.Mu> extends Profunctor<P, Mu> {

    /**
     * Thunk method that casts an applied {@link Cocartesian.Mu} to a {@link Cocartesian}.
     *
     * @param proofBox The boxed {@link Cocartesian}.
     * @param <P>      The transformation type.
     * @param <Proof>  The witness type for the profunctor.
     * @return The unboxed {@link Cocartesian}.
     */
    static <P extends K2, Proof extends Cocartesian.Mu> Cocartesian<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (Cocartesian<P, Proof>) proofBox;
    }

    /**
     * Converts the given transformation into one that transforms the left type of an {@link Either}. The right
     * type of the {@link Either} is left unchanged by the returned transformation.
     *
     * @param input The transformation.
     * @param <A>   The input type.
     * @param <B>   The output type.
     * @param <C>   A "pass-through" type that is not transformed.
     * @return A transformation on the left type of an {@link Either}.
     */
    <A, B, C> App2<P, Either<A, C>, Either<B, C>> left(final App2<P, A, B> input);

    /**
     * Converts the given transformation into one that transforms the right type of an {@link Either}. The left
     * type of the {@link Either} is left unchanged by the returned transformation.
     *
     * @param input The transformation.
     * @param <A>   The input type.
     * @param <B>   The output type.
     * @param <C>   A "pass-through" type that is not transformed.
     * @return A transformation on the right type of an {@link Either}.
     * @implSpec The default implementation calls {@link #left(App2)} and swaps the order of the types in the
     *     {@link Either}.
     */
    default <A, B, C> App2<P, Either<C, A>, Either<C, B>> right(final App2<P, A, B> input) {
        return dimap(left(input), Either::swap, Either::swap);
    }

    /**
     * Converts this profunctor into a {@link FunctorProfunctor} that distributes {@link CocartesianLike} functors.
     */
    default FunctorProfunctor<CocartesianLike.Mu, P, FunctorProfunctor.Mu<CocartesianLike.Mu>> toFP() {
        return new FunctorProfunctor<CocartesianLike.Mu, P, FunctorProfunctor.Mu<CocartesianLike.Mu>>() {
            @Override
            public <A, B, F extends K1> App2<P, App<F, A>, App<F, B>> distribute(final App<? extends CocartesianLike.Mu, F> proof,
                                                                                 final App2<P, A, B> input) {
                return cap(CocartesianLike.unbox(proof), input);
            }

            private <A, B, F extends K1, C> App2<P, App<F, A>, App<F, B>> cap(final CocartesianLike<F, C, ?> cLike,
                                                                              final App2<P, A, B> input) {
                return dimap(left(input), e -> Either.unbox(cLike.to(e)), cLike::from);
            }
        };
    }

    /**
     * The witness type for {@link Cocartesian}.
     */
    interface Mu extends Profunctor.Mu {

        /**
         * The value representing the witness type {@link Cocartesian.Mu}.
         */
        TypeToken<Mu> TYPE_TOKEN = new TypeToken<>() {
        };

    }

}
