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
package com.mojang.datafixers.functions;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.Algebra;
import com.mojang.datafixers.types.templates.RecursivePoint;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;

public abstract class Functions {

    @SuppressWarnings("unchecked")
    public static <A, B, C> PointFree<Function<A, C>> comp(final PointFree<Function<B, C>> f1,
                                                           final PointFree<Function<A, B>> f2) {
        if (Functions.isId(f1)) {
            return (PointFree<Function<A, C>>) (PointFree<?>) f2;
        }
        if (Functions.isId(f2)) {
            return (PointFree<Function<A, C>>) (PointFree<?>) f1;
        }
        if (f1 instanceof Comp<B, C> comp1 && f2 instanceof Comp<A, B> comp2) {
            final PointFree<? extends Function<?, ?>>[] functions = new PointFree[comp1.functions.length + comp2.functions.length];
            System.arraycopy(comp1.functions, 0, functions, 0, comp1.functions.length);
            System.arraycopy(comp2.functions,
                0,
                functions,
                comp1.functions.length,
                comp2.functions.length
            );
            return new Comp<>(functions);
        } else if (f1 instanceof Comp<B, C> comp1) {
            final PointFree<? extends Function<?, ?>>[] functions = new PointFree[comp1.functions.length + 1];
            System.arraycopy(comp1.functions, 0, functions, 0, comp1.functions.length);
            functions[functions.length - 1] = f2;
            return new Comp<>(functions);
        } else if (f2 instanceof Comp<A, B> comp2) {
            final PointFree<? extends Function<?, ?>>[] functions = new PointFree[1 + comp2.functions.length];
            functions[0] = f1;
            System.arraycopy(comp2.functions, 0, functions, 1, comp2.functions.length);
            return new Comp<>(functions);
        }
        return new Comp<>(f1, f2);
    }

    public static <A, B> PointFree<Function<A, B>> fun(final String name,
                                                       final Function<DynamicOps<?>, Function<A, B>> fun,
                                                       final Type<A> input,
                                                       final Type<B> output) {
        return new FunctionWrapper<>(name, fun, input, output);
    }

    public static <A, B> PointFree<B> app(final PointFree<Function<A, B>> fun,
                                          final PointFree<A> arg) {
        return new Apply<>(fun, arg);
    }

    public static <S, T, A, B> PointFree<Function<Function<A, B>, Function<S, T>>> profunctorTransformer(
        final TypedOptic<S, T, A, B> lens) {
        return new ProfunctorTransformer<>(lens);
    }

    public static <A> Bang<A> bang(final Type<A> type) {
        return new Bang<>(type);
    }

    public static <A> PointFree<Function<A, A>> in(final RecursivePoint.RecursivePointType<A> type) {
        return new In<>(type);
    }

    public static <A> PointFree<Function<A, A>> out(final RecursivePoint.RecursivePointType<A> type) {
        return new Out<>(type);
    }

    public static <A, B> PointFree<Function<A, B>> fold(final RecursivePoint.RecursivePointType<A> aType,
                                                        final RecursivePoint.RecursivePointType<B> bType,
                                                        final Algebra algebra,
                                                        final int index) {
        return new Fold<>(aType, bType, algebra, index);
    }

    public static <A> PointFree<Function<A, A>> id(final Type<A> type) {
        return new Id<>(DSL.func(type, type));
    }

    public static boolean isId(final PointFree<?> function) {
        return function instanceof Id<?>;
    }

}
