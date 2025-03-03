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
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.TraversalP;
import java.util.function.Function;

/**
 * A traversal is an optic that implements access to and effectful modification of any number of fields of an object.
 * All other optics can be implemented as specializations of {@code Traversal}.
 *
 * <p>The operation performed by traversals is analogous to {@link com.mojang.datafixers.kinds.Traversable},
 * which defines a structure-preserving transformation in the context of an effectful transformation.
 *
 * @param <S> The input object type.
 * @param <T> The output object type.
 * @param <A> The input field type.
 * @param <B> The output field type.
 * @see com.mojang.datafixers.kinds.Traversable
 */
public interface Traversal<S, T, A, B> extends Wander<S, T, A, B>, App2<Traversal.Mu<A, B>, S, T>, Optic<TraversalP.Mu, S, T, A, B> {

    /**
     * Thunk method that casts an applied {@link Traversal.Mu} to a {@link Traversal}.
     *
     * @param box The boxed {@link Traversal}.
     * @param <S> The input object type.
     * @param <T> The output object type.
     * @param <A> The input field type.
     * @param <B> The output field type.
     * @return The unboxed {@link Traversal}.
     */
    static <S, T, A, B> Traversal<S, T, A, B> unbox(final App2<Mu<A, B>, S, T> box) {
        return (Traversal<S, T, A, B>) box;
    }

    /**
     * Produces a function that takes a transformation between field types and produces a transformation between
     * object types. The returned function uses this traversal to traverse the object types and transform each
     * applicable field.
     *
     * @param proof The type class associated with this optic.
     * @param <P>   The transformation type.
     * @return A function from field transformations to object transformations.
     * @see Wander#wander(Applicative, FunctionType)
     */
    @Override
    default <P extends K2> FunctionType<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends TraversalP.Mu, P> proof) {
        final TraversalP<P, ? extends TraversalP.Mu> proof1 = TraversalP.unbox(proof);
        return input -> proof1.wander(this, input);
    }

    /**
     * The witness type for {@link Traversal}.
     *
     * @param <A> The input field type.
     * @param <B> The input object type.
     */
    final class Mu<A, B> implements K2 {

    }

    /**
     * The {@link TraversalP} type class instance for {@link Traversal}.
     *
     * @param <A2> The input field type.
     * @param <B2> The output field type.
     */
    final class Instance<A2, B2> implements TraversalP<Mu<A2, B2>, TraversalP.Mu> {

        @Override
        public <A, B, C, D> FunctionType<App2<Traversal.Mu<A2, B2>, A, B>, App2<Traversal.Mu<A2, B2>, C, D>> dimap(
            final Function<C, A> g,
            final Function<B, D> h) {
            return tr -> new Traversal<C, D, A2, B2>() {
                @Override
                public <F extends K1> FunctionType<C, App<F, D>> wander(final Applicative<F, ?> applicative,
                                                                        final FunctionType<A2, App<F, B2>> input) {
                    return c -> applicative.map(h,
                        Traversal.unbox(tr).wander(applicative, input).apply(g.apply(c))
                    );
                }
            };
        }

        @Override
        public <S, T, A, B> App2<Traversal.Mu<A2, B2>, S, T> wander(final Wander<S, T, A, B> wander,
                                                                    final App2<Traversal.Mu<A2, B2>, A, B> input) {
            return new Traversal<S, T, A2, B2>() {
                @Override
                public <F extends K1> FunctionType<S, App<F, T>> wander(final Applicative<F, ?> applicative,
                                                                        final FunctionType<A2, App<F, B2>> function) {
                    return wander.wander(applicative, unbox(input).wander(applicative, function));
                }
            };
        }

    }

}
