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
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.Profunctor;
import java.util.function.Function;

/**
 * An adapter is an optic that defines an invertible transformation between the input and output types. It provides
 * functionality to extract a value {@code A} from the input object {@code S} and to create an output object {@code T}
 * from an output field {@code B}.
 *
 * <p>The canonical example of an adapter is boxing and unboxing.
 *
 * <p>In order to be a <em>lawful adapter</em>, the implementations of {@link #from(Object)} and {@link #to(Object)}
 * must satisfy certain requirements. Assume that the object types {@code S} and {@code T} are implicitly convertible
 * between each other and that the field types {@code A} and {@code B} are similarly convertible. Then the following
 * rules must hold ({@code ==} here represents logical equality and not reference equality).
 *
 * <ol>
 *     <li>
 *         {@code from(to(b)) == b} - Round-tripping a field yields the input.
 *     </li>
 *     <li>
 *         {@code to(from(s)) == s} - Round-tripping an object yields the input.
 *     </li>
 * </ol>
 *
 * <p>Adapter optics that are not <em>lawful</em> are said to be either <em>neutral</em> or <em>chaotic</em>, depending
 * on the degree to which the adapter laws are broken.
 *
 * @param <S> The input object type.
 * @param <T> The output object type.
 * @param <A> The input field type.
 * @param <B> The output field type.
 * @apiNote An adapter is a special case of a {@link Prism}, and is sometimes called an {@code Iso}.
 */
public interface Adapter<S, T, A, B> extends App2<Adapter.Mu<A, B>, S, T>, Optic<Profunctor.Mu, S, T, A, B> {

    /**
     * Thunk method that casts an applied {@link Adapter.Mu} to a {@link Adapter}.
     *
     * @param box The boxed adapter.
     * @param <S> The input object type.
     * @param <T> The output object type.
     * @param <A> The input field type.
     * @param <B> The output field type.
     * @return The unboxed adapter.
     */
    static <S, T, A, B> Adapter<S, T, A, B> unbox(final App2<Mu<A, B>, S, T> box) {
        return (Adapter<S, T, A, B>) box;
    }

    /**
     * Unboxes a value from the input object.
     *
     * @param s A value of the input object type.
     * @return The unboxed value of the input field type.
     * @implSpec The implementation must, in conjunction with {@link #to(Object)}, satisfy the adapter laws
     *     in order for this adapter to be a <em>lawful adapter</em>.
     */
    A from(final S s);

    /**
     * Boxes a value into an output object.
     *
     * @param b A value of the output field type.
     * @return The boxed value of the output object type.
     * @implSpec The implementation must, in conjunction with {@link #from(Object)}, satisfy the adapter laws
     *     in order for this adapter to be a <em>lawful adapter</em>.
     */
    T to(final B b);

    /**
     * Evaluates this adapter to produce a function that, when given a transformation between field types, produces
     * a transformation between object types. The transformation {@linkplain #from(Object) unboxes} a value from the
     * input object and {@linkplain #to(Object) boxes} the transformed value into an output object.
     *
     * @param proofBox The {@link Profunctor} type class instance for the transformation type.
     * @param <P>      The type of transformation.
     * @return A function that takes a transformation between field types and produces a transformation between
     *     object types.
     * @see Adapter.Instance
     */
    @Override
    default <P extends K2> FunctionType<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends Profunctor.Mu, P> proofBox) {
        final Profunctor<P, ? extends Profunctor.Mu> proof = Profunctor.unbox(proofBox);
        return a -> proof.dimap(a, this::from, this::to);
    }

    /**
     * The witness type for {@link Adapter}.
     *
     * @param <A> The input field type.
     * @param <B> The output field type.
     */
    final class Mu<A, B> implements K2 {

    }

    /**
     * The {@link Profunctor} type class instance for {@link Adapter}.
     *
     * @param <A2> The input field type.
     * @param <B2> The output field type.
     */
    final class Instance<A2, B2> implements Profunctor<Mu<A2, B2>, Profunctor.Mu> {

        @Override
        public <A, B, C, D> FunctionType<App2<Adapter.Mu<A2, B2>, A, B>, App2<Adapter.Mu<A2, B2>, C, D>> dimap(
            final Function<C, A> g,
            final Function<B, D> h) {
            return a -> Optics.adapter(c -> Adapter.unbox(a).from(g.apply(c)),
                b2 -> h.apply(Adapter.unbox(a).to(b2))
            );
        }

    }

}
