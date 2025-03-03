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

import com.mojang.datafixers.Products;

/**
 * A <em>type class</em> for a unary type constructor.
 *
 * <p>A type class may be thought of as an interface for types. All types that implement this type class
 * must define the operations specified within that type class. This interface, being the base type for
 * type classes, specifies no required operations.
 *
 * @param <F>  The witness type for the type constructor this type class is defined for.
 * @param <Mu> The witness type for this type class.
 * @apiNote This interface is called {@link Kind1} because it operates on types of the kind {@link K1}.
 * @see Kind2
 * @see K1
 * @see <a href="https://en.wikipedia.org/wiki/Type_class">Type class</a>
 */
public interface Kind1<F extends K1, Mu extends Kind1.Mu> extends App<Mu, F> {

    /**
     * Thunk method that casts an applied {@link Kind1.Mu} to a {@link Kind1}.
     *
     * @param proofBox The boxed {@link Kind1}.
     * @param <F>      The container type.
     * @param <Proof>  The witness type.
     * @return The cast {@link Kind1}.
     */
    static <F extends K1, Proof extends Kind1.Mu> Kind1<F, Proof> unbox(final App<Proof, F> proofBox) {
        return (Kind1<F, Proof>) proofBox;
    }

    /**
     * Aggregates a value into a {@linkplain Products product}.
     *
     * @param t1   The value.
     * @param <T1> The type of the value.
     * @return An aggregate of the values.
     */
    default <T1> Products.P1<F, T1> group(final App<F, T1> t1) {
        return new Products.P1<>(t1);
    }

    /**
     * Aggregates values into a {@linkplain Products product}.
     *
     * @param t1   The first value.
     * @param t2   The second value.
     * @param <T1> The type of the first value.
     * @param <T2> The type of the second value.
     * @return An aggregate of the values.
     */
    default <T1, T2> Products.P2<F, T1, T2> group(final App<F, T1> t1, final App<F, T2> t2) {
        return new Products.P2<>(t1, t2);
    }

    /**
     * Aggregates the values into a {@linkplain Products product}.
     *
     * @param t1   The first value.
     * @param t2   The second value.
     * @param t3   The third value.
     * @param <T1> The type of the first value.
     * @param <T2> The type of the second value.
     * @param <T3> The type of the third value.
     * @return An aggregate of the values.
     */
    default <T1, T2, T3> Products.P3<F, T1, T2, T3> group(final App<F, T1> t1,
                                                          final App<F, T2> t2,
                                                          final App<F, T3> t3) {
        return new Products.P3<>(t1, t2, t3);
    }

    /**
     * Aggregates the values into a {@linkplain Products product}.
     *
     * @param t1   The first value.
     * @param t2   The second value.
     * @param t3   The third value.
     * @param t4   The forth value.
     * @param <T1> The type of the first value.
     * @param <T2> The type of the second value.
     * @param <T3> The type of the third value.
     * @param <T4> The type of the forth value.
     * @return An aggregate of the values.
     */
    default <T1, T2, T3, T4> Products.P4<F, T1, T2, T3, T4> group(final App<F, T1> t1,
                                                                  final App<F, T2> t2,
                                                                  final App<F, T3> t3,
                                                                  final App<F, T4> t4) {
        return new Products.P4<>(t1, t2, t3, t4);
    }

    /**
     * Aggregates the values into a {@linkplain Products product}.
     *
     * @param t1   The first value.
     * @param t2   The second value.
     * @param t3   The third value.
     * @param t4   The forth value.
     * @param t5   The fifth value.
     * @param <T1> The type of the first value.
     * @param <T2> The type of the second value.
     * @param <T3> The type of the third value.
     * @param <T4> The type of the forth value.
     * @param <T5> The type of the fifth value.
     * @return An aggregate of the values.
     */
    default <T1, T2, T3, T4, T5> Products.P5<F, T1, T2, T3, T4, T5> group(final App<F, T1> t1,
                                                                          final App<F, T2> t2,
                                                                          final App<F, T3> t3,
                                                                          final App<F, T4> t4,
                                                                          final App<F, T5> t5) {
        return new Products.P5<>(t1, t2, t3, t4, t5);
    }

    /**
     * Aggregates the values into a {@linkplain Products product}.
     *
     * @param t1   The first value.
     * @param t2   The second value.
     * @param t3   The third value.
     * @param t4   The forth value.
     * @param t5   The fifth value.
     * @param t6   The sixth value.
     * @param <T1> The type of the first value.
     * @param <T2> The type of the second value.
     * @param <T3> The type of the third value.
     * @param <T4> The type of the forth value.
     * @param <T5> The type of the fifth value.
     * @param <T6> The type of the sixth value.
     * @return An aggregate of the values.
     */
    default <T1, T2, T3, T4, T5, T6> Products.P6<F, T1, T2, T3, T4, T5, T6> group(final App<F, T1> t1,
                                                                                  final App<F, T2> t2,
                                                                                  final App<F, T3> t3,
                                                                                  final App<F, T4> t4,
                                                                                  final App<F, T5> t5,
                                                                                  final App<F, T6> t6) {
        return new Products.P6<>(t1, t2, t3, t4, t5, t6);
    }

    /**
     * Aggregates the values into a {@linkplain Products product}.
     *
     * @param t1   The first value.
     * @param t2   The second value.
     * @param t3   The third value.
     * @param t4   The forth value.
     * @param t5   The fifth value.
     * @param t6   The sixth value.
     * @param t7   The seventh value.
     * @param <T1> The type of the first value.
     * @param <T2> The type of the second value.
     * @param <T3> The type of the third value.
     * @param <T4> The type of the forth value.
     * @param <T5> The type of the fifth value.
     * @param <T6> The type of the sixth value.
     * @param <T7> The type of the seventh value.
     * @return An aggregate of the values.
     */
    default <T1, T2, T3, T4, T5, T6, T7> Products.P7<F, T1, T2, T3, T4, T5, T6, T7> group(final App<F, T1> t1,
                                                                                          final App<F, T2> t2,
                                                                                          final App<F, T3> t3,
                                                                                          final App<F, T4> t4,
                                                                                          final App<F, T5> t5,
                                                                                          final App<F, T6> t6,
                                                                                          final App<F, T7> t7) {
        return new Products.P7<>(t1, t2, t3, t4, t5, t6, t7);
    }

    /**
     * Aggregates the values into a {@linkplain Products product}.
     *
     * @param t1   The first value.
     * @param t2   The second value.
     * @param t3   The third value.
     * @param t4   The forth value.
     * @param t5   The fifth value.
     * @param t6   The sixth value.
     * @param t7   The seventh value.
     * @param t8   The eighth value.
     * @param <T1> The type of the first value.
     * @param <T2> The type of the second value.
     * @param <T3> The type of the third value.
     * @param <T4> The type of the forth value.
     * @param <T5> The type of the fifth value.
     * @param <T6> The type of the sixth value.
     * @param <T7> The type of the seventh value.
     * @param <T8> The type of the eighth value.
     * @return An aggregate of the values.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8> Products.P8<F, T1, T2, T3, T4, T5, T6, T7, T8> group(
        final App<F, T1> t1,
        final App<F, T2> t2,
        final App<F, T3> t3,
        final App<F, T4> t4,
        final App<F, T5> t5,
        final App<F, T6> t6,
        final App<F, T7> t7,
        final App<F, T8> t8) {
        return new Products.P8<>(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    /**
     * Aggregates the values into a {@linkplain Products product}.
     *
     * @param t1   The first value.
     * @param t2   The second value.
     * @param t3   The third value.
     * @param t4   The forth value.
     * @param t5   The fifth value.
     * @param t6   The sixth value.
     * @param t7   The seventh value.
     * @param t8   The eighth value.
     * @param t9   The ninth value.
     * @param <T1> The type of the first value.
     * @param <T2> The type of the second value.
     * @param <T3> The type of the third value.
     * @param <T4> The type of the forth value.
     * @param <T5> The type of the fifth value.
     * @param <T6> The type of the sixth value.
     * @param <T7> The type of the seventh value.
     * @param <T8> The type of the eighth value.
     * @param <T9> The type of the ninth value.
     * @return An aggregate of the values.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9> Products.P9<F, T1, T2, T3, T4, T5, T6, T7, T8, T9> group(
        final App<F, T1> t1,
        final App<F, T2> t2,
        final App<F, T3> t3,
        final App<F, T4> t4,
        final App<F, T5> t5,
        final App<F, T6> t6,
        final App<F, T7> t7,
        final App<F, T8> t8,
        final App<F, T9> t9) {
        return new Products.P9<>(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

    /**
     * Aggregates the values into a {@linkplain Products product}.
     *
     * @param t1    The first value.
     * @param t2    The second value.
     * @param t3    The third value.
     * @param t4    The forth value.
     * @param t5    The fifth value.
     * @param t6    The sixth value.
     * @param t7    The seventh value.
     * @param t8    The eighth value.
     * @param t9    The ninth value.
     * @param t10   The tenth value.
     * @param <T1>  The type of the first value.
     * @param <T2>  The type of the second value.
     * @param <T3>  The type of the third value.
     * @param <T4>  The type of the forth value.
     * @param <T5>  The type of the fifth value.
     * @param <T6>  The type of the sixth value.
     * @param <T7>  The type of the seventh value.
     * @param <T8>  The type of the eighth value.
     * @param <T9>  The type of the ninth value.
     * @param <T10> The type of the tenth value.
     * @return An aggregate of the values.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> Products.P10<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10> group(
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
        return new Products.P10<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10);
    }

    /**
     * Aggregates the values into a {@linkplain Products product}.
     *
     * @param t1    The first value.
     * @param t2    The second value.
     * @param t3    The third value.
     * @param t4    The forth value.
     * @param t5    The fifth value.
     * @param t6    The sixth value.
     * @param t7    The seventh value.
     * @param t8    The eighth value.
     * @param t9    The ninth value.
     * @param t10   The tenth value.
     * @param t11   The eleventh value.
     * @param <T1>  The type of the first value.
     * @param <T2>  The type of the second value.
     * @param <T3>  The type of the third value.
     * @param <T4>  The type of the forth value.
     * @param <T5>  The type of the fifth value.
     * @param <T6>  The type of the sixth value.
     * @param <T7>  The type of the seventh value.
     * @param <T8>  The type of the eighth value.
     * @param <T9>  The type of the ninth value.
     * @param <T10> The type of the tenth value.
     * @param <T11> The type of the eleventh value.
     * @return An aggregate of the values.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> Products.P11<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11> group(
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
        return new Products.P11<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11);
    }

    /**
     * Aggregates the values into a {@linkplain Products product}.
     *
     * @param t1    The first value.
     * @param t2    The second value.
     * @param t3    The third value.
     * @param t4    The forth value.
     * @param t5    The fifth value.
     * @param t6    The sixth value.
     * @param t7    The seventh value.
     * @param t8    The eighth value.
     * @param t9    The ninth value.
     * @param t10   The tenth value.
     * @param t11   The eleventh value.
     * @param t12   The twelfth value.
     * @param <T1>  The type of the first value.
     * @param <T2>  The type of the second value.
     * @param <T3>  The type of the third value.
     * @param <T4>  The type of the forth value.
     * @param <T5>  The type of the fifth value.
     * @param <T6>  The type of the sixth value.
     * @param <T7>  The type of the seventh value.
     * @param <T8>  The type of the eighth value.
     * @param <T9>  The type of the ninth value.
     * @param <T10> The type of the tenth value.
     * @param <T11> The type of the eleventh value.
     * @param <T12> The type of the twelfth value.
     * @return An aggregate of the values.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> Products.P12<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12> group(
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
        return new Products.P12<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12);
    }

    /**
     * Aggregates the values into a {@linkplain Products product}.
     *
     * @param t1    The first value.
     * @param t2    The second value.
     * @param t3    The third value.
     * @param t4    The forth value.
     * @param t5    The fifth value.
     * @param t6    The sixth value.
     * @param t7    The seventh value.
     * @param t8    The eighth value.
     * @param t9    The ninth value.
     * @param t10   The tenth value.
     * @param t11   The eleventh value.
     * @param t12   The twelfth value.
     * @param t13   The thirteenth value.
     * @param <T1>  The type of the first value.
     * @param <T2>  The type of the second value.
     * @param <T3>  The type of the third value.
     * @param <T4>  The type of the forth value.
     * @param <T5>  The type of the fifth value.
     * @param <T6>  The type of the sixth value.
     * @param <T7>  The type of the seventh value.
     * @param <T8>  The type of the eighth value.
     * @param <T9>  The type of the ninth value.
     * @param <T10> The type of the tenth value.
     * @param <T11> The type of the eleventh value.
     * @param <T12> The type of the twelfth value.
     * @param <T13> The type of the thirteenth value.
     * @return An aggregate of the values.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> Products.P13<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13> group(
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
        return new Products.P13<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13);
    }

    /**
     * Aggregates the values into a {@linkplain Products product}.
     *
     * @param t1    The first value.
     * @param t2    The second value.
     * @param t3    The third value.
     * @param t4    The forth value.
     * @param t5    The fifth value.
     * @param t6    The sixth value.
     * @param t7    The seventh value.
     * @param t8    The eighth value.
     * @param t9    The ninth value.
     * @param t10   The tenth value.
     * @param t11   The eleventh value.
     * @param t12   The twelfth value.
     * @param t13   The thirteenth value.
     * @param t14   The fourteenth value.
     * @param <T1>  The type of the first value.
     * @param <T2>  The type of the second value.
     * @param <T3>  The type of the third value.
     * @param <T4>  The type of the forth value.
     * @param <T5>  The type of the fifth value.
     * @param <T6>  The type of the sixth value.
     * @param <T7>  The type of the seventh value.
     * @param <T8>  The type of the eighth value.
     * @param <T9>  The type of the ninth value.
     * @param <T10> The type of the tenth value.
     * @param <T11> The type of the eleventh value.
     * @param <T12> The type of the twelfth value.
     * @param <T13> The type of the thirteenth value.
     * @param <T14> The type of the fourteenth value.
     * @return An aggregate of the values.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> Products.P14<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14> group(
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
        return new Products.P14<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14);
    }

    /**
     * Aggregates the values into a {@linkplain Products product}.
     *
     * @param t1    The first value.
     * @param t2    The second value.
     * @param t3    The third value.
     * @param t4    The forth value.
     * @param t5    The fifth value.
     * @param t6    The sixth value.
     * @param t7    The seventh value.
     * @param t8    The eighth value.
     * @param t9    The ninth value.
     * @param t10   The tenth value.
     * @param t11   The eleventh value.
     * @param t12   The twelfth value.
     * @param t13   The thirteenth value.
     * @param t14   The fourteenth value.
     * @param t15   The fifteenth value.
     * @param <T1>  The type of the first value.
     * @param <T2>  The type of the second value.
     * @param <T3>  The type of the third value.
     * @param <T4>  The type of the forth value.
     * @param <T5>  The type of the fifth value.
     * @param <T6>  The type of the sixth value.
     * @param <T7>  The type of the seventh value.
     * @param <T8>  The type of the eighth value.
     * @param <T9>  The type of the ninth value.
     * @param <T10> The type of the tenth value.
     * @param <T11> The type of the eleventh value.
     * @param <T12> The type of the twelfth value.
     * @param <T13> The type of the thirteenth value.
     * @param <T14> The type of the fourteenth value.
     * @param <T15> The type of the fifteenth value.
     * @return An aggregate of the values.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> Products.P15<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15> group(
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
        return new Products.P15<>(t1, t2, t3, t4, t5, t6, t7, t8, t9, t10, t11, t12, t13, t14, t15);
    }

    /**
     * Aggregates the values into a {@linkplain Products product}.
     *
     * @param t1    The first value.
     * @param t2    The second value.
     * @param t3    The third value.
     * @param t4    The forth value.
     * @param t5    The fifth value.
     * @param t6    The sixth value.
     * @param t7    The seventh value.
     * @param t8    The eighth value.
     * @param t9    The ninth value.
     * @param t10   The tenth value.
     * @param t11   The eleventh value.
     * @param t12   The twelfth value.
     * @param t13   The thirteenth value.
     * @param t14   The fourteenth value.
     * @param t15   The fifteenth value.
     * @param t16   The sixteenth value.
     * @param <T1>  The type of the first value.
     * @param <T2>  The type of the second value.
     * @param <T3>  The type of the third value.
     * @param <T4>  The type of the forth value.
     * @param <T5>  The type of the fifth value.
     * @param <T6>  The type of the sixth value.
     * @param <T7>  The type of the seventh value.
     * @param <T8>  The type of the eighth value.
     * @param <T9>  The type of the ninth value.
     * @param <T10> The type of the tenth value.
     * @param <T11> The type of the eleventh value.
     * @param <T12> The type of the twelfth value.
     * @param <T13> The type of the thirteenth value.
     * @param <T14> The type of the fourteenth value.
     * @param <T15> The type of the fifteenth value.
     * @param <T16> The type of the sixteenth value.
     * @return An aggregate of the values.
     */
    default <T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> Products.P16<F, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15, T16> group(
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
        return new Products.P16<>(t1,
            t2,
            t3,
            t4,
            t5,
            t6,
            t7,
            t8,
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
     * The witness type of a {@link Kind1}.
     */
    interface Mu extends K1 {

    }

}
