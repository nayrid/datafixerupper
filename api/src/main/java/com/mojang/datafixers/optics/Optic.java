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

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * An optic is a generic tool used to inspect and modify part of a structure, called a focus. The strength of optics
 * lies in the fact that they are composable: they can be combined in order to "focus" on a specific sub-component
 * of a structure, with complete disregard to the remainder of the structure.
 *
 * <p>An optic can be thought to take some transformation between field types and embed it into a transformation between
 * object types. The object type is also known as a <em>source</em>, and the field type is also known as a <em>focus</em>.
 * The terms "object" and "field" are used throughout this documentation in order to draw a parallel to the
 * object-oriented paradigm. Note, however, that an optic need not act on a literal field of a conventional object,
 * and that optics need not act on a single field, depending on the profunctor this optic takes its behavior from.
 *
 * @param <Proof> The type class this optic evaluates under.
 * @param <S>     The input object type.
 * @param <T>     The output object type.
 * @param <A>     The input field type.
 * @param <B>     The output field type.
 * @see com.mojang.datafixers.optics.profunctors.Profunctor
 * @see <a href="https://medium.com/@gcanti/introduction-to-optics-lenses-and-prisms-3230e73bfcfe">An Introduction to Optics: Lenses and Prisms</a>
 */
public interface Optic<Proof extends K1, S, T, A, B> {

    /**
     * Produces a function that embeds a transformation between field types into a transformation between the
     * object types.
     *
     * @param proof The type class associated with this optic.
     * @param <P>   The type associated with the profunctor.
     * @return A function that embeds a transformation from the input field to the output field into a
     *     transformation from the input object to the output object.
     */
    <P extends K2> Function<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends Proof, P> proof);

    /**
     * Refines the type class accepted by this optic to the given type, checking against the given bounds.
     *
     * @param proofBounds The lower bounds of {@code Proof2}.
     * @param proof       The type tag for the refined profunctor type.
     * @param <Proof2>    The refined type class.
     * @return This optic, refined to accept the given type class, or an absent optional if the bounds could
     *     not be satisfied.
     */
    @SuppressWarnings("unchecked")
    default <Proof2 extends K1> Optional<Optic<? super Proof2, S, T, A, B>> upCast(final Set<TypeToken<? extends K1>> proofBounds,
                                                                                   final TypeToken<Proof2> proof) {
        if (proofBounds.stream().allMatch(bound -> bound.isSupertypeOf(proof))) {
            return Optional.of((Optic<? super Proof2, S, T, A, B>) this);
        }
        return Optional.empty();
    }

    /**
     * An optic that represents the composition of two compatible optics. Two optics are compatible if
     *
     * <ol>
     *     <li>The type classes the two optics use have a common subtype.</li>
     *     <li>The fields types of the first optic correspond to the object types of the second optic.</li>
     * </ol>
     *
     * <p>The composition acts on the objects of the first optic and the fields of the second optic.
     *
     * @param optics the optics
     * @param <Proof> The type class associated with this optic, which must be a subtype of the outer and inner optics' type.
     * @param <S>     The input object type.
     * @param <T>     The output object type.
     * @param <A>     The input field type.
     * @param <B>     The output field type.
     */
    record CompositionOptic<Proof extends K1, S, T, A, B>(List<? extends Optic<? super Proof, ?, ?, ?, ?>> optics) implements Optic<Proof, S, T, A, B> {

        @SuppressWarnings("unchecked")
        private static <P extends K2, T extends App2<P, ?, ?>> App2<P, ?, ?> applyUnchecked(final Function<T, ? extends App2<P, ?, ?>> function,
                                                                                            final App2<P, ?, ?> input) {
            return function.apply((T) input);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <P extends K2> Function<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends Proof, P> proof) {
            final List<Function<? extends App2<P, ?, ?>, ? extends App2<P, ?, ?>>> functions = new ArrayList<>(
                optics.size());
            for (int i = optics.size() - 1;
                 i >= 0;
                 i--) {
                functions.add(optics.get(i).eval(proof));
            }
            return input -> {
                App2<P, ?, ?> result = input;
                for (final Function<? extends App2<P, ?, ?>, ? extends App2<P, ?, ?>> function : functions) {
                    result = applyUnchecked(function, result);
                }
                return (App2<P, S, T>) result;
            };
        }

        @Override
        public String toString() {
            return "(" + optics.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" \u25E6 ")) + ")";
        }

    }

}
