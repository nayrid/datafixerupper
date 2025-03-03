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
package com.mojang.datafixers.util;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface Function9<T1, T2, T3, T4, T5, T6, T7, T8, T9, R> {

    R apply(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9);

    default Function<T1, Function8<T2, T3, T4, T5, T6, T7, T8, T9, R>> curry() {
        return t1 -> (t2, t3, t4, t5, t6, t7, t8, t9) -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

    default BiFunction<T1, T2, Function7<T3, T4, T5, T6, T7, T8, T9, R>> curry2() {
        return (t1, t2) -> (t3, t4, t5, t6, t7, t8, t9) -> apply(t1,
            t2,
            t3,
            t4,
            t5,
            t6,
            t7,
            t8,
            t9
        );
    }

    default Function3<T1, T2, T3, Function6<T4, T5, T6, T7, T8, T9, R>> curry3() {
        return (t1, t2, t3) -> (t4, t5, t6, t7, t8, t9) -> apply(t1,
            t2,
            t3,
            t4,
            t5,
            t6,
            t7,
            t8,
            t9
        );
    }

    default Function4<T1, T2, T3, T4, Function5<T5, T6, T7, T8, T9, R>> curry4() {
        return (t1, t2, t3, t4) -> (t5, t6, t7, t8, t9) -> apply(t1,
            t2,
            t3,
            t4,
            t5,
            t6,
            t7,
            t8,
            t9
        );
    }

    default Function5<T1, T2, T3, T4, T5, Function4<T6, T7, T8, T9, R>> curry5() {
        return (t1, t2, t3, t4, t5) -> (t6, t7, t8, t9) -> apply(t1,
            t2,
            t3,
            t4,
            t5,
            t6,
            t7,
            t8,
            t9
        );
    }

    default Function6<T1, T2, T3, T4, T5, T6, Function3<T7, T8, T9, R>> curry6() {
        return (t1, t2, t3, t4, t5, t6) -> (t7, t8, t9) -> apply(t1,
            t2,
            t3,
            t4,
            t5,
            t6,
            t7,
            t8,
            t9
        );
    }

    default Function7<T1, T2, T3, T4, T5, T6, T7, BiFunction<T8, T9, R>> curry7() {
        return (t1, t2, t3, t4, t5, t6, t7) -> (t8, t9) -> apply(t1,
            t2,
            t3,
            t4,
            t5,
            t6,
            t7,
            t8,
            t9
        );
    }

    default Function8<T1, T2, T3, T4, T5, T6, T7, T8, Function<T9, R>> curry8() {
        return (t1, t2, t3, t4, t5, t6, t7, t8) -> t9 -> apply(t1, t2, t3, t4, t5, t6, t7, t8, t9);
    }

}
