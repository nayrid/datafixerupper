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
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.util.Pair;
import java.util.function.Function;

/**
 * A lens is an optic the provides access and modification to a single field. It provides functionality
 * to extract a single value of the input field type {@code A} from the input <em>product type</em> {@code S} and
 * to combine the remaining input {@code S} and the transformed field type {@code B} into the output product
 * type {@code T}.
 *
 * <p>The canonical example for using a lens is to extract and update a field of a {@code struct} type, using the
 * C language meaning of {@code struct}.
 *
 * <p>In order to be a <em>lawful lens</em>, the implementations of {@link #view(Object)} and {@link #update(Object, Object)}
 * must satisfy certain requirements. Assume that the object types {@code S} and {@code T} are implicitly convertible
 * between each other and that the field types {@code A} and {@code B} are similarly convertible. Then the following
 * rules must hold ({@code ==} here represents logical equality and not reference equality).
 *
 * <ol>
 *     <li>
 *         {@code update(b2, update(b1, s)) == update(b2, s)} - Updating twice is equivalent to updating once.
 *     </li>
 *     <li>
 *         {@code view(update(b, s)) == b} - Viewing after an update yields the value used to update.
 *     </li>
 *     <li>
 *         {@code update(view(s), s) == s} - Updating with a viewed value yields the original object.
 *     </li>
 * </ol>
 *
 * <p>Lenses that are not <em>lawful</em> are said to be either <em>neutral</em> or <em>chaotic</em>, depending on the
 * degree to which the lens laws are broken.
 *
 * @param <S> The input object type.
 * @param <T> The output object type.
 * @param <A> The input field type.
 * @param <B> The output field type.
 * @see <a href="https://en.wikipedia.org/wiki/Record_(computer_science)">Record Types</a>
 */
public interface Lens<S, T, A, B> extends App2<Lens.Mu<A, B>, S, T>, Optic<Cartesian.Mu, S, T, A, B> {

    /**
     * Thunk method that casts an applied {@link Lens.Mu} to a {@link Lens}.
     *
     * @param box The boxed lens.
     * @param <S> The input object type.
     * @param <T> The output object type.
     * @param <A> The input field type.
     * @param <B> The output field type.
     * @return The unboxed lens.
     */
    static <S, T, A, B> Lens<S, T, A, B> unbox(final App2<Mu<A, B>, S, T> box) {
        return (Lens<S, T, A, B>) box;
    }

    /**
     * Thunk method that casts an applied {@link Lens.Mu2} to a {@link Lens}.
     *
     * @param box The boxed lens.
     * @param <S> The input object type.
     * @param <T> The output object type.
     * @param <A> The input field type.
     * @param <B> The output field type.
     * @return The unboxed lens.
     */
    static <S, T, A, B> Lens<S, T, A, B> unbox2(final App2<Mu2<S, T>, B, A> box) {
        return ((Box<S, T, A, B>) box).lens;
    }

    /**
     * Boxes the given lens into an applied {@link Lens.Mu2}.
     *
     * @param lens The lens.
     * @param <S>  The input object type.
     * @param <T>  The output object type.
     * @param <A>  The input field type.
     * @param <B>  The output field type.
     * @return The boxed lens.
     * @apiNote This method is necessary because {@link Lens} cannot extend two different instantiations of
     *     {@link App2}.
     */
    static <S, T, A, B> App2<Mu2<S, T>, B, A> box(final Lens<S, T, A, B> lens) {
        return new Box<>(lens);
    }

    /**
     * Extracts a value of the input field type from the input object type.
     *
     * <p>This method is analogous to a "getter" in traditional object-oriented programming.
     *
     * @param s A value of the input object type.
     * @return The extracted value of the input field type.
     * @implSpec The implementation must, in conjunction with {@link #update(Object, Object)}, satisfy the lens laws
     *     in order for this lens to be a <em>lawful lens</em>.
     */
    A view(final S s);

    /**
     * Combines a value of the output field type with a value of the input object type to produce a combined value
     * of the output object type.
     *
     * <p>This method is analogous to a "setter" in traditional object-oriented programming.
     *
     * @param b A value of the output field type with which to update the output object.
     * @param s A value of the input object type to be converted to a value of the output object type.
     * @return A value of the output object type, with the output field updated with the given value.
     * @implSpec The implementation must, in conjunction with {@link #view(Object)}, satisfy the lens laws in order
     *     for this lens to be a <em>lawful lens</em>.
     */
    T update(final B b, final S s);

    /**
     * Evaluates this lens to produce a function that, when given a transformation between field types, produces
     * a transformation between object types. The transformation {@linkplain #view(Object) extracts} a value from
     * the input object, uses the given transformation to produce a new value of the output field type, and finally
     * uses that value to {@linkplain #update(Object, Object) update} the corresponding field in the output object.
     *
     * @param proofBox The {@link Cartesian} type class instance for the transformation type.
     * @param <P>      The type of transformation.
     * @return A function that takes a transformation between field types and produces a transformation between
     *     object types.
     * @see Lens.Instance
     */
    @Override
    default <P extends K2> FunctionType<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends Cartesian.Mu, P> proofBox) {
        final Cartesian<P, ? extends Cartesian.Mu> proof = Cartesian.unbox(proofBox);
        return a -> proof.dimap(proof.<A, B, S>first(a),
            s -> Pair.<A, S>of(view(s), s),
            pair -> update(pair.first(), pair.second())
        );
    }

    /**
     * The witness type for {@link Lens} with the field types applied.
     *
     * @param <A> The input field type.
     * @param <B> The output field type.
     */
    final class Mu<A, B> implements K2 {

    }

    /**
     * The witness type for {@link Lens} with the object types applied.
     *
     * @param <S> The input object type.
     * @param <T> The output object type.
     */
    final class Mu2<S, T> implements K2 {

    }

    /**
     * A container for a {@link Lens} that implements {@link App2} suitable for instantiation with {@link Mu2}.
     * Programmers will typically not explicitly use this type.
     *
     * @param <S> The input object type.
     * @param <T> The output object type.
     * @param <A> The input field type.
     * @param <B> The output field type.
     */
    final class Box<S, T, A, B> implements App2<Mu2<S, T>, B, A> {

        private final Lens<S, T, A, B> lens;

        /**
         * Constructs a new box wrapping the given lens.
         *
         * @param lens The lens.
         */
        public Box(final Lens<S, T, A, B> lens) {
            this.lens = lens;
        }

    }

    /**
     * The {@link Cartesian} type class instance for {@link Lens}. This type class corresponds to the partially applied
     * {@link Lens.Mu}.
     *
     * @param <A2> The input field type.
     * @param <B2> The output field type.
     */
    final class Instance<A2, B2> implements Cartesian<Mu<A2, B2>, Cartesian.Mu> {

        @Override
        public <A, B, C, D> FunctionType<App2<Lens.Mu<A2, B2>, A, B>, App2<Lens.Mu<A2, B2>, C, D>> dimap(
            final Function<C, A> g,
            final Function<B, D> h) {
            return l -> Optics.lens(c -> Lens.unbox(l).view(g.apply(c)),
                (b2, c) -> h.apply(Lens.unbox(l).update(b2, g.apply(c)))
            );
        }

        @Override
        public <A, B, C> App2<Lens.Mu<A2, B2>, Pair<A, C>, Pair<B, C>> first(final App2<Lens.Mu<A2, B2>, A, B> input) {
            return Optics.lens(pair -> Lens.unbox(input).view(pair.first()),
                (b2, pair) -> Pair.of(Lens.unbox(input).update(b2, pair.first()),
                    pair.second()
                )
            );
        }

        @Override
        public <A, B, C> App2<Lens.Mu<A2, B2>, Pair<C, A>, Pair<C, B>> second(final App2<Lens.Mu<A2, B2>, A, B> input) {
            return Optics.lens(pair -> Lens.unbox(input).view(pair.second()),
                (b2, pair) -> Pair.of(pair.first(),
                    Lens.unbox(input).update(b2, pair.second())
                )
            );
        }

    }

}
