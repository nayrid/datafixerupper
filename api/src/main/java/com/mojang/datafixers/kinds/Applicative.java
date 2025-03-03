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

import com.mojang.datafixers.util.Function10;
import com.mojang.datafixers.util.Function11;
import com.mojang.datafixers.util.Function12;
import com.mojang.datafixers.util.Function13;
import com.mojang.datafixers.util.Function14;
import com.mojang.datafixers.util.Function15;
import com.mojang.datafixers.util.Function16;
import com.mojang.datafixers.util.Function3;
import com.mojang.datafixers.util.Function4;
import com.mojang.datafixers.util.Function5;
import com.mojang.datafixers.util.Function6;
import com.mojang.datafixers.util.Function7;
import com.mojang.datafixers.util.Function8;
import com.mojang.datafixers.util.Function9;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * The applicative type class extends {@linkplain Functor functors} with methods for
 * {@linkplain #lift1(App) applying wrapped transformations} and
 * {@linkplain #point(Object) wrapping values in containers}.
 *
 * <p>In order to be a <em>lawful applicative functor</em>, the implementations of {@link #ap(App, App)} and
 * {@link #point(Object)} must satisfy the following requirements ({@code ==} represents logical equality and not
 * reference equality).
 *
 * <ol>
 *     <li>
 *         {@code ap(point(Function.identity()), fa) = fa} - Applying with the identity function yields the input
 *         (this is related to the first functor law).
 *     </li>
 *     <li>
 *         {@code ap(point(f), point(a)) = point(f(a))} - Applying a wrapped function to a wrapped value is equivalent
 *         to wrapping the result of applying the function to the value.
 *     </li>
 *     <li>
 *         {@code ap(ff, point(a)) = ap(point(f -> f(a)), ff)} - Wrapping a value before applying a function is
 *         equivalent to wrapping the resulting value after applying the function.
 *     </li>
 *     <li>
 *         {@code ap(ff, ap(fg, fa)) = ap(ap(ap(point(f -> f::compose), ff), fg), fa)} - Composing applications is
 *         equivalent to applying compositions (this is related to the second functor law).
 *     </li>
 * </ol>
 *
 * <p>These laws may be made more intuitive my replacing {@code ap(ff, fa)} with the expression {@code ff[fa]},
 * analogous to normal function application {@code f(a)}.
 *
 * <ol>
 *     <li>
 *         {@code point(Function.identity())[fa] = fa}
 *     </li>
 *     <li>
 *         {@code point(f)[point(a)] = point(f(a))}
 *     </li>
 *     <li>
 *         {@code ff[point(a)] = point(f -> f(a))[ff]}
 *     </li>
 *     <li>
 *         {@code ff[fg[fa]] = point(f -> f::compose)[ff][fg][fa]}
 *     </li>
 * </ol>
 *
 * <p>In fact, the implementation of {@link #map(Function, App)} can be written in terms of {@link #ap(App, App)}
 * and {@link #point(Object)} as {@code map(f, fa) = ap(point(f), fa)}. This interface leaves that method abstract
 * because of the differing function types in {@link #map(Function, App)} and {@link #ap(App, App)} that make
 * the default implementation outline above impossible without an unchecked (albeit safe) cast.
 *
 * @param <F>  The container type.
 * @param <Mu> The witness type of this applicative functor.
 * @see Functor
 * @see <a href="https://wiki.haskell.org/Typeclassopedia#Laws_2">Applicative Laws</a>
 */
public interface Applicative<F extends K1, Mu extends Applicative.Mu> extends Functor<F, Mu> {

    /**
     * Unboxes an {@link App} representing an applicative functor into an applicative functor instance.
     *
     * @param proofBox The boxed applicative functor.
     * @param <F>      The container type.
     * @param <Mu>     The witness type of the applicative functor.
     * @return The unboxed applicative functor.
     * @throws ClassCastException If {@code proofBox} is not an applicative functor.
     */
    static <F extends K1, Mu extends Applicative.Mu> Applicative<F, Mu> unbox(final App<Mu, F> proofBox) {
        return (Applicative<F, Mu>) proofBox;
    }

    /**
     * Wraps a value in a container, for example an object in a list.
     *
     * @param a   The input value.
     * @param <A> The type of the value.
     * @return The wrapper containing the value.
     */
    <A> App<F, A> point(final A a);

    /**
     * Lifts a wrapped transformation function into a function between containers.
     *
     * @param function The container containing the transformation function.
     * @param <A>      The input type.
     * @param <R>      The output type.
     * @return The lifted function.
     * @implSpec The expression {@code lift1(point(f)).apply(fa)} should be equivalent to {@code map(f, fa)} for this
     *     to be a lawful applicative functor.
     */
    <A, R> Function<App<F, A>, App<F, R>> lift1(final App<F, Function<A, R>> function);

    /**
     * Lifts a wrapped {@link BiFunction} into a {@link BiFunction} between containers.
     *
     * @param function The container containing the transformation function.
     * @param <A>      The first input type.
     * @param <B>      The second input type.
     * @param <R>      The output type.
     * @return The lifted function.
     */
    default <A, B, R> BiFunction<App<F, A>, App<F, B>, App<F, R>> lift2(final App<F, BiFunction<A, B, R>> function) {
        return (fa, fb) -> ap2(function, fa, fb);
    }

    /**
     * Lifts a wrapped {@link Function3} into a {@link Function3} between containers.
     *
     * @param function The container containing the transformation function.
     * @param <T1>     The first input type.
     * @param <T2>     The second input type.
     * @param <T3>     The third input type.
     * @param <R>      The output type.
     * @return The lifted function.
     */
    default <T1, T2, T3, R> Function3<App<F, T1>, App<F, T2>, App<F, T3>, App<F, R>> lift3(final App<F, Function3<T1, T2, T3, R>> function) {
        return (ft1, ft2, ft3) -> ap3(function, ft1, ft2, ft3);
    }

    /**
     * Lifts a wrapped {@link Function4} into a {@link Function4} between containers.
     *
     * @param function The container containing the transformation function.
     * @param <T1>     The first input type.
     * @param <T2>     The second input type.
     * @param <T3>     The third input type.
     * @param <T4>     The fourth input type.
     * @param <R>      The output type.
     * @return The lifted function.
     */
    default <T1, T2, T3, T4, R> Function4<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, R>> lift4(
        final App<F, Function4<T1, T2, T3, T4, R>> function) {
        return (ft1, ft2, ft3, ft4) -> ap4(function, ft1, ft2, ft3, ft4);
    }

    /**
     * Lifts a wrapped {@link Function5} into a {@link Function5} between containers.
     *
     * @param function The container containing the transformation function.
     * @param <T1>     The first input type.
     * @param <T2>     The second input type.
     * @param <T3>     The third input type.
     * @param <T4>     The fourth input type.
     * @param <T5>     The fifth input type.
     * @param <R>      The output type.
     * @return The lifted function.
     */
    default <T1, T2, T3, T4, T5, R> Function5<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, T5>, App<F, R>> lift5(
        final App<F, Function5<T1, T2, T3, T4, T5, R>> function) {
        return (ft1, ft2, ft3, ft4, ft5) -> ap5(function, ft1, ft2, ft3, ft4, ft5);
    }

    /**
     * Lifts a wrapped {@link Function6} into a {@link Function6} between containers.
     *
     * @param function The container containing the transformation function.
     * @param <T1>     The first input type.
     * @param <T2>     The second input type.
     * @param <T3>     The third input type.
     * @param <T4>     The fourth input type.
     * @param <T5>     The fifth input type.
     * @param <T6>     The sixth input type.
     * @param <R>      The output type.
     * @return The lifted function.
     */
    default <T1, T2, T3, T4, T5, T6, R> Function6<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, T5>, App<F, T6>, App<F, R>> lift6(
        final App<F, Function6<T1, T2, T3, T4, T5, T6, R>> function) {
        return (ft1, ft2, ft3, ft4, ft5, ft6) -> ap6(function, ft1, ft2, ft3, ft4, ft5, ft6);
    }

    /**
     * Lifts a wrapped {@link Function7} into a {@link Function7} between containers.
     *
     * @param function The container containing the transformation function.
     * @param <T1>     The first input type.
     * @param <T2>     The second input type.
     * @param <T3>     The third input type.
     * @param <T4>     The fourth input type.
     * @param <T5>     The fifth input type.
     * @param <T6>     The sixth input type.
     * @param <T7>     The seventh input type.
     * @param <R>      The output type.
     * @return The lifted function.
     */
    default <T1, T2, T3, T4, T5, T6, T7, R> Function7<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, T5>, App<F, T6>, App<F, T7>, App<F, R>> lift7(
        final App<F, Function7<T1, T2, T3, T4, T5, T6, T7, R>> function) {
        return (ft1, ft2, ft3, ft4, ft5, ft6, ft7) -> ap7(function,
            ft1,
            ft2,
            ft3,
            ft4,
            ft5,
            ft6,
            ft7
        );
    }

    /**
     * Lifts a wrapped {@link Function8} into a {@link Function8} between containers.
     *
     * @param function The container containing the transformation function.
     * @param <T1>     The first input type.
     * @param <T2>     The second input type.
     * @param <T3>     The third input type.
     * @param <T4>     The fourth input type.
     * @param <T5>     The fifth input type.
     * @param <T6>     The sixth input type.
     * @param <T7>     The seventh input type.
     * @param <T8>     The eighth input type.
     * @param <R>      The output type.
     * @return The lifted function.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, R> Function8<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, T5>, App<F, T6>, App<F, T7>, App<F, T8>, App<F, R>> lift8(
        final App<F, Function8<T1, T2, T3, T4, T5, T6, T7, T8, R>> function) {
        return (ft1, ft2, ft3, ft4, ft5, ft6, ft7, ft8) -> ap8(function,
            ft1,
            ft2,
            ft3,
            ft4,
            ft5,
            ft6,
            ft7,
            ft8
        );
    }

    /**
     * Lifts a wrapped {@link Function9} into a {@link Function9} between containers.
     *
     * @param function The container containing the transformation function.
     * @param <T1>     The first input type.
     * @param <T2>     The second input type.
     * @param <T3>     The third input type.
     * @param <T4>     The fourth input type.
     * @param <T5>     The fifth input type.
     * @param <T6>     The sixth input type.
     * @param <T7>     The seventh input type.
     * @param <T8>     The eighth input type.
     * @param <T9>     The ninth input type.
     * @param <R>      The output type.
     * @return The lifted function.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> Function9<App<F, T1>, App<F, T2>, App<F, T3>, App<F, T4>, App<F, T5>, App<F, T6>, App<F, T7>, App<F, T8>, App<F, T9>, App<F, R>> lift9(
        final App<F, Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R>> function) {
        return (ft1, ft2, ft3, ft4, ft5, ft6, ft7, ft8, ft9) -> ap9(function,
            ft1,
            ft2,
            ft3,
            ft4,
            ft5,
            ft6,
            ft7,
            ft8,
            ft9
        );
    }

    /**
     * Maps the contents of {@code arg} from {@code A} to {@code R} using a wrapped transformation function.
     *
     * @param func The wrapped transformation function.
     * @param arg  The input container that will be transformed.
     * @param <A>  The input type.
     * @param <R>  The output type.
     * @return The transformed container.
     * @implSpec The default implementation {@linkplain #lift1(App) lifts} the wrapped function
     *     and applies it to the {@code arg}.
     */
    default <A, R> App<F, R> ap(final App<F, Function<A, R>> func, final App<F, A> arg) {
        return lift1(func).apply(arg);
    }

    /**
     * Maps the contents of {@code arg} from {@code A} to {@code R} using the {@code func}.
     * This method is equivalent to {@link #map(Function, App)}.
     *
     * @param func The transformation function.
     * @param arg  The input container that will be transformed.
     * @param <A>  The input type.
     * @param <R>  The output type.
     * @return The transformed container.
     * @implSpec The default implementation delegates to {@link #map(Function, App)}.
     * @see #map(Function, App)
     */
    default <A, R> App<F, R> ap(final Function<A, R> func, final App<F, A> arg) {
        return map(func, arg);
    }

    /**
     * Maps the contents of {@code a} and {@code b} from {@code A} and {@code B} to {@code R}
     * using a wrapped {@link BiFunction}.
     *
     * @param func The wrapped transformation function.
     * @param a    The first input container that will be transformed.
     * @param b    The second input container that will be transformed.
     * @param <A>  The first input type.
     * @param <B>  The second input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <A, B, R> App<F, R> ap2(final App<F, BiFunction<A, B, R>> func,
                                    final App<F, A> a,
                                    final App<F, B> b) {
        final Function<BiFunction<A, B, R>, Function<A, Function<B, R>>> curry = f -> a1 -> b1 -> f.apply(
            a1,
            b1
        );
        return ap(ap(map(curry, func), a), b);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a wrapped {@link Function3}.
     *
     * @param func The wrapped transformation function.
     * @param t1   The first input container that will be transformed.
     * @param t2   The second input container that will be transformed.
     * @param t3   The third input container that will be transformed.
     * @param <T1> The first input type.
     * @param <T2> The second input type.
     * @param <T3> The third input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, R> App<F, R> ap3(final App<F, Function3<T1, T2, T3, R>> func,
                                          final App<F, T1> t1,
                                          final App<F, T2> t2,
                                          final App<F, T3> t3) {
        return ap2(ap(map(Function3::curry, func), t1), t2, t3);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a wrapped {@link Function4}.
     *
     * @param func The wrapped transformation function.
     * @param t1   The first input container that will be transformed.
     * @param t2   The second input container that will be transformed.
     * @param t3   The third input container that will be transformed.
     * @param t4   The fourth input container that will be transformed.
     * @param <T1> The first input type.
     * @param <T2> The second input type.
     * @param <T3> The third input type.
     * @param <T4> The fourth input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, R> App<F, R> ap4(final App<F, Function4<T1, T2, T3, T4, R>> func,
                                              final App<F, T1> t1,
                                              final App<F, T2> t2,
                                              final App<F, T3> t3,
                                              final App<F, T4> t4) {
        return ap2(ap2(map(Function4::curry2, func), t1, t2), t3, t4);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a wrapped {@link Function5}.
     *
     * @param func The wrapped transformation function.
     * @param t1   The first input container that will be transformed.
     * @param t2   The second input container that will be transformed.
     * @param t3   The third input container that will be transformed.
     * @param t4   The fourth input container that will be transformed.
     * @param t5   The fifth input container that will be transformed.
     * @param <T1> The first input type.
     * @param <T2> The second input type.
     * @param <T3> The third input type.
     * @param <T4> The fourth input type.
     * @param <T5> The fifth input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, R> App<F, R> ap5(final App<F, Function5<T1, T2, T3, T4, T5, R>> func,
                                                  final App<F, T1> t1,
                                                  final App<F, T2> t2,
                                                  final App<F, T3> t3,
                                                  final App<F, T4> t4,
                                                  final App<F, T5> t5) {
        return ap3(ap2(map(Function5::curry2, func), t1, t2), t3, t4, t5);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a wrapped {@link Function6}.
     *
     * @param func The wrapped transformation function.
     * @param t1   The first input container that will be transformed.
     * @param t2   The second input container that will be transformed.
     * @param t3   The third input container that will be transformed.
     * @param t4   The fourth input container that will be transformed.
     * @param t5   The fifth input container that will be transformed.
     * @param t6   The sixth input container that will be transformed.
     * @param <T1> The first input type.
     * @param <T2> The second input type.
     * @param <T3> The third input type.
     * @param <T4> The fourth input type.
     * @param <T5> The fifth input type.
     * @param <T6> The sixth input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, R> App<F, R> ap6(final App<F, Function6<T1, T2, T3, T4, T5, T6, R>> func,
                                                      final App<F, T1> t1,
                                                      final App<F, T2> t2,
                                                      final App<F, T3> t3,
                                                      final App<F, T4> t4,
                                                      final App<F, T5> t5,
                                                      final App<F, T6> t6) {
        return ap3(ap3(map(Function6::curry3, func), t1, t2, t3), t4, t5, t6);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a wrapped {@link Function7}.
     *
     * @param func The wrapped transformation function.
     * @param t1   The first input container that will be transformed.
     * @param t2   The second input container that will be transformed.
     * @param t3   The third input container that will be transformed.
     * @param t4   The fourth input container that will be transformed.
     * @param t5   The fifth input container that will be transformed.
     * @param t6   The sixth input container that will be transformed.
     * @param t7   The seventh input container that will be transformed.
     * @param <T1> The first input type.
     * @param <T2> The second input type.
     * @param <T3> The third input type.
     * @param <T4> The fourth input type.
     * @param <T5> The fifth input type.
     * @param <T6> The sixth input type.
     * @param <T7> The seventh input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, T7, R> App<F, R> ap7(final App<F, Function7<T1, T2, T3, T4, T5, T6, T7, R>> func,
                                                          final App<F, T1> t1,
                                                          final App<F, T2> t2,
                                                          final App<F, T3> t3,
                                                          final App<F, T4> t4,
                                                          final App<F, T5> t5,
                                                          final App<F, T6> t6,
                                                          final App<F, T7> t7) {
        return ap4(ap3(map(Function7::curry3, func), t1, t2, t3), t4, t5, t6, t7);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a wrapped {@link Function8}.
     *
     * @param func The wrapped transformation function.
     * @param t1   The first input container that will be transformed.
     * @param t2   The second input container that will be transformed.
     * @param t3   The third input container that will be transformed.
     * @param t4   The fourth input container that will be transformed.
     * @param t5   The fifth input container that will be transformed.
     * @param t6   The sixth input container that will be transformed.
     * @param t7   The seventh input container that will be transformed.
     * @param t8   The eighth input container that will be transformed.
     * @param <T1> The first input type.
     * @param <T2> The second input type.
     * @param <T3> The third input type.
     * @param <T4> The fourth input type.
     * @param <T5> The fifth input type.
     * @param <T6> The sixth input type.
     * @param <T7> The seventh input type.
     * @param <T8> The eighth input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, R> App<F, R> ap8(final App<F, Function8<T1, T2, T3, T4, T5, T6, T7, T8, R>> func,
                                                              final App<F, T1> t1,
                                                              final App<F, T2> t2,
                                                              final App<F, T3> t3,
                                                              final App<F, T4> t4,
                                                              final App<F, T5> t5,
                                                              final App<F, T6> t6,
                                                              final App<F, T7> t7,
                                                              final App<F, T8> t8) {
        return ap4(ap4(map(Function8::curry4, func), t1, t2, t3, t4), t5, t6, t7, t8);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a wrapped {@link Function9}.
     *
     * @param func The wrapped transformation function.
     * @param t1   The first input container that will be transformed.
     * @param t2   The second input container that will be transformed.
     * @param t3   The third input container that will be transformed.
     * @param t4   The fourth input container that will be transformed.
     * @param t5   The fifth input container that will be transformed.
     * @param t6   The sixth input container that will be transformed.
     * @param t7   The seventh input container that will be transformed.
     * @param t8   The eighth input container that will be transformed.
     * @param t9   The ninth input container that will be transformed.
     * @param <T1> The first input type.
     * @param <T2> The second input type.
     * @param <T3> The third input type.
     * @param <T4> The fourth input type.
     * @param <T5> The fifth input type.
     * @param <T6> The sixth input type.
     * @param <T7> The seventh input type.
     * @param <T8> The eighth input type.
     * @param <T9> The ninth input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> App<F, R> ap9(final App<F, Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R>> func,
                                                                  final App<F, T1> t1,
                                                                  final App<F, T2> t2,
                                                                  final App<F, T3> t3,
                                                                  final App<F, T4> t4,
                                                                  final App<F, T5> t5,
                                                                  final App<F, T6> t6,
                                                                  final App<F, T7> t7,
                                                                  final App<F, T8> t8,
                                                                  final App<F, T9> t9) {
        return ap5(ap4(map(Function9::curry4, func), t1, t2, t3, t4), t5, t6, t7, t8, t9);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a wrapped {@link Function10}.
     *
     * @param func  The wrapped transformation function.
     * @param t1    The first input container that will be transformed.
     * @param t2    The second input container that will be transformed.
     * @param t3    The third input container that will be transformed.
     * @param t4    The fourth input container that will be transformed.
     * @param t5    The fifth input container that will be transformed.
     * @param t6    The sixth input container that will be transformed.
     * @param t7    The seventh input container that will be transformed.
     * @param t8    The eighth input container that will be transformed.
     * @param t9    The ninth input container that will be transformed.
     * @param t10   The tenth input container that will be transformed.
     * @param <T1>  The first input type.
     * @param <T2>  The second input type.
     * @param <T3>  The third input type.
     * @param <T4>  The fourth input type.
     * @param <T5>  The fifth input type.
     * @param <T6>  The sixth input type.
     * @param <T7>  The seventh input type.
     * @param <T8>  The eighth input type.
     * @param <T9>  The ninth input type.
     * @param <T10> The tenth input type.
     * @param <R>   The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R> App<F, R> ap10(final App<F, Function10<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, R>> func,
                                                                        final App<F, T1> t1,
                                                                        final App<F, T2> t2,
                                                                        final App<F, T3> t3,
                                                                        final App<F, T4> t4,
                                                                        final App<F, T5> t5,
                                                                        final App<F, T6> t6,
                                                                        final App<F, T7> t7,
                                                                        final App<F, T8> t8,
                                                                        final App<F, T9> t9,
                                                                        final App<F, T10> t10) {
        return ap5(ap5(map(Function10::curry5, func), t1, t2, t3, t4, t5), t6, t7, t8, t9, t10);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a wrapped {@link Function11}.
     *
     * @param func  The wrapped transformation function.
     * @param t1    The first input container that will be transformed.
     * @param t2    The second input container that will be transformed.
     * @param t3    The third input container that will be transformed.
     * @param t4    The fourth input container that will be transformed.
     * @param t5    The fifth input container that will be transformed.
     * @param t6    The sixth input container that will be transformed.
     * @param t7    The seventh input container that will be transformed.
     * @param t8    The eighth input container that will be transformed.
     * @param t9    The ninth input container that will be transformed.
     * @param t10   The tenth input container that will be transformed.
     * @param t11   The eleventh input container that will be transformed.
     * @param <T1>  The first input type.
     * @param <T2>  The second input type.
     * @param <T3>  The third input type.
     * @param <T4>  The fourth input type.
     * @param <T5>  The fifth input type.
     * @param <T6>  The sixth input type.
     * @param <T7>  The seventh input type.
     * @param <T8>  The eighth input type.
     * @param <T9>  The ninth input type.
     * @param <T10> The tenth input type.
     * @param <T11> The eleventh input type.
     * @param <R>   The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R> App<F, R> ap11(final App<F, Function11<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, R>> func,
                                                                             final App<F, T1> t1,
                                                                             final App<F, T2> t2,
                                                                             final App<F, T3> t3,
                                                                             final App<F, T4> t4,
                                                                             final App<F, T5> t5,
                                                                             final App<F, T6> t6,
                                                                             final App<F, T7> t7,
                                                                             final App<F, T8> t8,
                                                                             final App<F, T9> t9,
                                                                             final App<F, T10> t10,
                                                                             final App<F, T11> t11) {
        return ap6(ap5(map(Function11::curry5, func), t1, t2, t3, t4, t5),
            t6,
            t7,
            t8,
            t9,
            t10,
            t11
        );
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a wrapped {@link Function12}.
     *
     * @param func  The wrapped transformation function.
     * @param t1    The first input container that will be transformed.
     * @param t2    The second input container that will be transformed.
     * @param t3    The third input container that will be transformed.
     * @param t4    The fourth input container that will be transformed.
     * @param t5    The fifth input container that will be transformed.
     * @param t6    The sixth input container that will be transformed.
     * @param t7    The seventh input container that will be transformed.
     * @param t8    The eighth input container that will be transformed.
     * @param t9    The ninth input container that will be transformed.
     * @param t10   The tenth input container that will be transformed.
     * @param t11   The eleventh input container that will be transformed.
     * @param t12   The twelfth input container that will be transformed.
     * @param <T1>  The first input type.
     * @param <T2>  The second input type.
     * @param <T3>  The third input type.
     * @param <T4>  The fourth input type.
     * @param <T5>  The fifth input type.
     * @param <T6>  The sixth input type.
     * @param <T7>  The seventh input type.
     * @param <T8>  The eighth input type.
     * @param <T9>  The ninth input type.
     * @param <T10> The tenth input type.
     * @param <T11> The eleventh input type.
     * @param <T12> The twelfth input type.
     * @param <R>   The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R> App<F, R> ap12(final App<F, Function12<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, R>> func,
                                                                                  final App<F, T1> t1,
                                                                                  final App<F, T2> t2,
                                                                                  final App<F, T3> t3,
                                                                                  final App<F, T4> t4,
                                                                                  final App<F, T5> t5,
                                                                                  final App<F, T6> t6,
                                                                                  final App<F, T7> t7,
                                                                                  final App<F, T8> t8,
                                                                                  final App<F, T9> t9,
                                                                                  final App<F, T10> t10,
                                                                                  final App<F, T11> t11,
                                                                                  final App<F, T12> t12) {
        return ap6(ap6(map(Function12::curry6, func), t1, t2, t3, t4, t5, t6),
            t7,
            t8,
            t9,
            t10,
            t11,
            t12
        );
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a wrapped {@link Function13}.
     *
     * @param func  The wrapped transformation function.
     * @param t1    The first input container that will be transformed.
     * @param t2    The second input container that will be transformed.
     * @param t3    The third input container that will be transformed.
     * @param t4    The fourth input container that will be transformed.
     * @param t5    The fifth input container that will be transformed.
     * @param t6    The sixth input container that will be transformed.
     * @param t7    The seventh input container that will be transformed.
     * @param t8    The eighth input container that will be transformed.
     * @param t9    The ninth input container that will be transformed.
     * @param t10   The tenth input container that will be transformed.
     * @param t11   The eleventh input container that will be transformed.
     * @param t12   The twelfth input container that will be transformed.
     * @param t13   The thirteenth input container that will be transformed.
     * @param <T1>  The first input type.
     * @param <T2>  The second input type.
     * @param <T3>  The third input type.
     * @param <T4>  The fourth input type.
     * @param <T5>  The fifth input type.
     * @param <T6>  The sixth input type.
     * @param <T7>  The seventh input type.
     * @param <T8>  The eighth input type.
     * @param <T9>  The ninth input type.
     * @param <T10> The tenth input type.
     * @param <T11> The eleventh input type.
     * @param <T12> The twelfth input type.
     * @param <T13> The thirteenth input type.
     * @param <R>   The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R> App<F, R> ap13(final App<F, Function13<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, R>> func,
                                                                                       final App<F, T1> t1,
                                                                                       final App<F, T2> t2,
                                                                                       final App<F, T3> t3,
                                                                                       final App<F, T4> t4,
                                                                                       final App<F, T5> t5,
                                                                                       final App<F, T6> t6,
                                                                                       final App<F, T7> t7,
                                                                                       final App<F, T8> t8,
                                                                                       final App<F, T9> t9,
                                                                                       final App<F, T10> t10,
                                                                                       final App<F, T11> t11,
                                                                                       final App<F, T12> t12,
                                                                                       final App<F, T13> t13) {
        return ap7(ap6(map(Function13::curry6, func), t1, t2, t3, t4, t5, t6),
            t7,
            t8,
            t9,
            t10,
            t11,
            t12,
            t13
        );
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a wrapped {@link Function14}.
     *
     * @param func  The wrapped transformation function.
     * @param t1    The first input container that will be transformed.
     * @param t2    The second input container that will be transformed.
     * @param t3    The third input container that will be transformed.
     * @param t4    The fourth input container that will be transformed.
     * @param t5    The fifth input container that will be transformed.
     * @param t6    The sixth input container that will be transformed.
     * @param t7    The seventh input container that will be transformed.
     * @param t8    The eighth input container that will be transformed.
     * @param t9    The ninth input container that will be transformed.
     * @param t10   The tenth input container that will be transformed.
     * @param t11   The eleventh input container that will be transformed.
     * @param t12   The twelfth input container that will be transformed.
     * @param t13   The thirteenth input container that will be transformed.
     * @param t14   The fourteenth input container that will be transformed.
     * @param <T1>  The first input type.
     * @param <T2>  The second input type.
     * @param <T3>  The third input type.
     * @param <T4>  The fourth input type.
     * @param <T5>  The fifth input type.
     * @param <T6>  The sixth input type.
     * @param <T7>  The seventh input type.
     * @param <T8>  The eighth input type.
     * @param <T9>  The ninth input type.
     * @param <T10> The tenth input type.
     * @param <T11> The eleventh input type.
     * @param <T12> The twelfth input type.
     * @param <T13> The thirteenth input type.
     * @param <T14> The fourteenth input type.
     * @param <R>   The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R> App<F, R> ap14(final App<F, Function14<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, R>> func,
                                                                                            final App<F, T1> t1,
                                                                                            final App<F, T2> t2,
                                                                                            final App<F, T3> t3,
                                                                                            final App<F, T4> t4,
                                                                                            final App<F, T5> t5,
                                                                                            final App<F, T6> t6,
                                                                                            final App<F, T7> t7,
                                                                                            final App<F, T8> t8,
                                                                                            final App<F, T9> t9,
                                                                                            final App<F, T10> t10,
                                                                                            final App<F, T11> t11,
                                                                                            final App<F, T12> t12,
                                                                                            final App<F, T13> t13,
                                                                                            final App<F, T14> t14) {
        return ap7(ap7(map(Function14::curry7, func), t1, t2, t3, t4, t5, t6, t7),
            t8,
            t9,
            t10,
            t11,
            t12,
            t13,
            t14
        );
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a wrapped {@link Function15}.
     *
     * @param func  The wrapped transformation function.
     * @param t1    The first input container that will be transformed.
     * @param t2    The second input container that will be transformed.
     * @param t3    The third input container that will be transformed.
     * @param t4    The fourth input container that will be transformed.
     * @param t5    The fifth input container that will be transformed.
     * @param t6    The sixth input container that will be transformed.
     * @param t7    The seventh input container that will be transformed.
     * @param t8    The eighth input container that will be transformed.
     * @param t9    The ninth input container that will be transformed.
     * @param t10   The tenth input container that will be transformed.
     * @param t11   The eleventh input container that will be transformed.
     * @param t12   The twelfth input container that will be transformed.
     * @param t13   The thirteenth input container that will be transformed.
     * @param t14   The fourteenth input container that will be transformed.
     * @param t15   The fifteenth input container that will be transformed.
     * @param <T1>  The first input type.
     * @param <T2>  The second input type.
     * @param <T3>  The third input type.
     * @param <T4>  The fourth input type.
     * @param <T5>  The fifth input type.
     * @param <T6>  The sixth input type.
     * @param <T7>  The seventh input type.
     * @param <T8>  The eighth input type.
     * @param <T9>  The ninth input type.
     * @param <T10> The tenth input type.
     * @param <T11> The eleventh input type.
     * @param <T12> The twelfth input type.
     * @param <T13> The thirteenth input type.
     * @param <T14> The fourteenth input type.
     * @param <T15> The fifteenth input type.
     * @param <R>   The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R> App<F, R> ap15(
        final App<F, Function15<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, R>> func,
        final App<F, T1> t1,
        final App<F, T2> t2,
        final App<F, T3> t3,
        final App<F, T4> t4,
        final App<F, T5> t5,
        final App<F, T6> t6,
        final App<F, T7> t7,
        final App<F, T8> t8,
        final App<F, T9> t9,
        final App<F, T10> t10,
        final App<F, T11> t11,
        final App<F, T12> t12,
        final App<F, T13> t13,
        final App<F, T14> t14,
        final App<F, T15> t15) {
        return ap8(ap7(map(Function15::curry7, func), t1, t2, t3, t4, t5, t6, t7),
            t8,
            t9,
            t10,
            t11,
            t12,
            t13,
            t14,
            t15
        );
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a wrapped {@link Function16}.
     *
     * @param func  The wrapped transformation function.
     * @param t1    The first input container that will be transformed.
     * @param t2    The second input container that will be transformed.
     * @param t3    The third input container that will be transformed.
     * @param t4    The fourth input container that will be transformed.
     * @param t5    The fifth input container that will be transformed.
     * @param t6    The sixth input container that will be transformed.
     * @param t7    The seventh input container that will be transformed.
     * @param t8    The eighth input container that will be transformed.
     * @param t9    The ninth input container that will be transformed.
     * @param t10   The tenth input container that will be transformed.
     * @param t11   The eleventh input container that will be transformed.
     * @param t12   The twelfth input container that will be transformed.
     * @param t13   The thirteenth input container that will be transformed.
     * @param t14   The fourteenth input container that will be transformed.
     * @param t15   The fifteenth input container that will be transformed.
     * @param t16   The sixteenth input container that will be transformed.
     * @param <T1>  The first input type.
     * @param <T2>  The second input type.
     * @param <T3>  The third input type.
     * @param <T4>  The fourth input type.
     * @param <T5>  The fifth input type.
     * @param <T6>  The sixth input type.
     * @param <T7>  The seventh input type.
     * @param <T8>  The eighth input type.
     * @param <T9>  The ninth input type.
     * @param <T10> The tenth input type.
     * @param <T11> The eleventh input type.
     * @param <T12> The twelfth input type.
     * @param <T13> The thirteenth input type.
     * @param <T14> The fourteenth input type.
     * @param <T15> The fifteenth input type.
     * @param <T16> The sixteenth input type.
     * @param <R>   The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R> App<F, R> ap16(
        final App<F, Function16<T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16, R>> func,
        final App<F, T1> t1,
        final App<F, T2> t2,
        final App<F, T3> t3,
        final App<F, T4> t4,
        final App<F, T5> t5,
        final App<F, T6> t6,
        final App<F, T7> t7,
        final App<F, T8> t8,
        final App<F, T9> t9,
        final App<F, T10> t10,
        final App<F, T11> t11,
        final App<F, T12> t12,
        final App<F, T13> t13,
        final App<F, T14> t14,
        final App<F, T15> t15,
        final App<F, T16> t16) {
        return ap8(ap8(map(Function16::curry8, func), t1, t2, t3, t4, t5, t6, t7, t8),
            t9,
            t10,
            t11,
            t12,
            t13,
            t14,
            t15,
            t16
        );
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a {@link BiFunction}.
     *
     * @param func The transformation function.
     * @param a    The first input container that will be transformed.
     * @param b    The second input container that will be transformed.
     * @param <A>  The first input type.
     * @param <B>  The second input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <A, B, R> App<F, R> apply2(final BiFunction<A, B, R> func,
                                       final App<F, A> a,
                                       final App<F, B> b) {
        return ap2(point(func), a, b);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a {@link Function3}.
     *
     * @param func The transformation function.
     * @param t1   The first input container that will be transformed.
     * @param t2   The second input container that will be transformed.
     * @param t3   The third input container that will be transformed.
     * @param <T1> The first input type.
     * @param <T2> The second input type.
     * @param <T3> The third input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, R> App<F, R> apply3(final Function3<T1, T2, T3, R> func,
                                             final App<F, T1> t1,
                                             final App<F, T2> t2,
                                             final App<F, T3> t3) {
        return ap3(point(func), t1, t2, t3);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a {@link Function4}.
     *
     * @param func The transformation function.
     * @param t1   The first input container that will be transformed.
     * @param t2   The second input container that will be transformed.
     * @param t3   The third input container that will be transformed.
     * @param t4   The fourth input container that will be transformed.
     * @param <T1> The first input type.
     * @param <T2> The second input type.
     * @param <T3> The third input type.
     * @param <T4> The fourth input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, R> App<F, R> apply4(final Function4<T1, T2, T3, T4, R> func,
                                                 final App<F, T1> t1,
                                                 final App<F, T2> t2,
                                                 final App<F, T3> t3,
                                                 final App<F, T4> t4) {
        return ap4(point(func), t1, t2, t3, t4);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a {@link Function5}.
     *
     * @param func The transformation function.
     * @param t1   The first input container that will be transformed.
     * @param t2   The second input container that will be transformed.
     * @param t3   The third input container that will be transformed.
     * @param t4   The fourth input container that will be transformed.
     * @param t5   The fifth input container that will be transformed.
     * @param <T1> The first input type.
     * @param <T2> The second input type.
     * @param <T3> The third input type.
     * @param <T4> The fourth input type.
     * @param <T5> The fifth input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, R> App<F, R> apply5(final Function5<T1, T2, T3, T4, T5, R> func,
                                                     final App<F, T1> t1,
                                                     final App<F, T2> t2,
                                                     final App<F, T3> t3,
                                                     final App<F, T4> t4,
                                                     final App<F, T5> t5) {
        return ap5(point(func), t1, t2, t3, t4, t5);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a {@link Function6}.
     *
     * @param func The transformation function.
     * @param t1   The first input container that will be transformed.
     * @param t2   The second input container that will be transformed.
     * @param t3   The third input container that will be transformed.
     * @param t4   The fourth input container that will be transformed.
     * @param t5   The fifth input container that will be transformed.
     * @param t6   The sixth input container that will be transformed.
     * @param <T1> The first input type.
     * @param <T2> The second input type.
     * @param <T3> The third input type.
     * @param <T4> The fourth input type.
     * @param <T5> The fifth input type.
     * @param <T6> The sixth input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, R> App<F, R> apply6(final Function6<T1, T2, T3, T4, T5, T6, R> func,
                                                         final App<F, T1> t1,
                                                         final App<F, T2> t2,
                                                         final App<F, T3> t3,
                                                         final App<F, T4> t4,
                                                         final App<F, T5> t5,
                                                         final App<F, T6> t6) {
        return ap6(point(func), t1, t2, t3, t4, t5, t6);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a {@link Function7}.
     *
     * @param func The transformation function.
     * @param t1   The first input container that will be transformed.
     * @param t2   The second input container that will be transformed.
     * @param t3   The third input container that will be transformed.
     * @param t4   The fourth input container that will be transformed.
     * @param t5   The fifth input container that will be transformed.
     * @param t6   The sixth input container that will be transformed.
     * @param t7   The seventh input container that will be transformed.
     * @param <T1> The first input type.
     * @param <T2> The second input type.
     * @param <T3> The third input type.
     * @param <T4> The fourth input type.
     * @param <T5> The fifth input type.
     * @param <T6> The sixth input type.
     * @param <T7> The seventh input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, T7, R> App<F, R> apply7(final Function7<T1, T2, T3, T4, T5, T6, T7, R> func,
                                                             final App<F, T1> t1,
                                                             final App<F, T2> t2,
                                                             final App<F, T3> t3,
                                                             final App<F, T4> t4,
                                                             final App<F, T5> t5,
                                                             final App<F, T6> t6,
                                                             final App<F, T7> t7) {
        return ap7(point(func), t1, t2, t3, t4, t5, t6, t7);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a {@link Function8}.
     *
     * @param func The transformation function.
     * @param t1   The first input container that will be transformed.
     * @param t2   The second input container that will be transformed.
     * @param t3   The third input container that will be transformed.
     * @param t4   The fourth input container that will be transformed.
     * @param t5   The fifth input container that will be transformed.
     * @param t6   The sixth input container that will be transformed.
     * @param t7   The seventh input container that will be transformed.
     * @param t8   The eighth input container that will be transformed.
     * @param <T1> The first input type.
     * @param <T2> The second input type.
     * @param <T3> The third input type.
     * @param <T4> The fourth input type.
     * @param <T5> The fifth input type.
     * @param <T6> The sixth input type.
     * @param <T7> The seventh input type.
     * @param <T8> The eighth input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, R> App<F, R> apply8(final Function8<T1, T2, T3, T4, T5, T6, T7, T8, R> func,
                                                                 final App<F, T1> t1,
                                                                 final App<F, T2> t2,
                                                                 final App<F, T3> t3,
                                                                 final App<F, T4> t4,
                                                                 final App<F, T5> t5,
                                                                 final App<F, T6> t6,
                                                                 final App<F, T7> t7,
                                                                 final App<F, T8> t8) {
        return ap8(point(func), t1, t2, t3, t4, t5, t6, t7, t8);
    }

    /**
     * Maps the contents of the input containers to a container of {@code R} using a {@link Function9}.
     *
     * @param func The transformation function.
     * @param t1   The first input container that will be transformed.
     * @param t2   The second input container that will be transformed.
     * @param t3   The third input container that will be transformed.
     * @param t4   The fourth input container that will be transformed.
     * @param t5   The fifth input container that will be transformed.
     * @param t6   The sixth input container that will be transformed.
     * @param t7   The seventh input container that will be transformed.
     * @param t8   The eighth input container that will be transformed.
     * @param t9   The ninth input container that will be transformed.
     * @param <T1> The first input type.
     * @param <T2> The second input type.
     * @param <T3> The third input type.
     * @param <T4> The fourth input type.
     * @param <T5> The fifth input type.
     * @param <T6> The sixth input type.
     * @param <T7> The seventh input type.
     * @param <T8> The eighth input type.
     * @param <T9> The ninth input type.
     * @param <R>  The output type.
     * @return The transformed container.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> App<F, R> apply9(final Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> func,
                                                                     final App<F, T1> t1,
                                                                     final App<F, T2> t2,
                                                                     final App<F, T3> t3,
                                                                     final App<F, T4> t4,
                                                                     final App<F, T5> t5,
                                                                     final App<F, T6> t6,
                                                                     final App<F, T7> t7,
                                                                     final App<F, T8> t8,
                                                                     final App<F, T9> t9) {
        return ap9(point(func), t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

    /**
     * The witness type of an applicative functor.
     */
    interface Mu extends Functor.Mu {

    }

}
