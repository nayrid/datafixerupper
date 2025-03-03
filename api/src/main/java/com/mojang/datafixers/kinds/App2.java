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

/**
 * A marker interface representing an applied binary type constructor. This interface is necessary because Java does
 * not support higher-kinded types.
 *
 * <p>For a generic (or higher kinded) type {@code F<_, _>}, the use of {@code App2<F.Mu, A, B>}
 * corresponds to the parameterized type {@code F<A, B>}. This allows algorithms to be generified over the
 * type of the constructor {@code F}, which is not otherwise possible in Java.
 *
 * @param <F> The <em>type witness</em> representing the type constructor. This is often a nested {@code Mu} empty class.
 * @param <A> The first type applied to the type constructor.
 * @param <B> The second type applied to the type constructor.
 * @see K2
 * @see App
 */
public interface App2<F extends K2, A, B> {

}
