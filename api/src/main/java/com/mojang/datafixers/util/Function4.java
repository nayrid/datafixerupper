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

@FunctionalInterface
public interface Function4<T1, T2, T3, T4, R> {

    R apply(T1 t1, T2 t2, T3 t3, T4 t4);

    default Function<T1, Function3<T2, T3, T4, R>> curry() {
        return t1 -> (t2, t3, t4) -> apply(t1, t2, t3, t4);
    }

    default BiFunction<T1, T2, BiFunction<T3, T4, R>> curry2() {
        return (t1, t2) -> (t3, t4) -> apply(t1, t2, t3, t4);
    }

    default Function3<T1, T2, T3, Function<T4, R>> curry3() {
        return (t1, t2, t3) -> t4 -> apply(t1, t2, t3, t4);
    }

}
