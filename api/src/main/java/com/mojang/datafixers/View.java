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
package com.mojang.datafixers;

import com.mojang.datafixers.functions.Functions;
import com.mojang.datafixers.functions.PointFree;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.types.Func;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.function.Function;

public record View<A, B>(PointFree<Function<A, B>> function) implements App2<View.Mu, A, B> {

    static <A, B> View<A, B> unbox(final App2<Mu, A, B> box) {
        return (View<A, B>) box;
    }

    public static <A> View<A, A> nopView(final Type<A> type) {
        return new View<>(Functions.id(type));
    }

    public static <A, B> View<A, B> create(final PointFree<Function<A, B>> function) {
        return new View<>(function);
    }

    public static <A, B> View<A, B> create(final String name,
                                           final Type<A> type,
                                           final Type<B> newType,
                                           final Function<DynamicOps<?>, Function<A, B>> function) {
        return new View<>(Functions.fun(name, function, type, newType));
    }

    public Type<A> type() {
        return ((Func<A, B>) funcType()).first();
    }

    public Type<B> newType() {
        return ((Func<A, B>) funcType()).second();
    }

    public Type<Function<A, B>> funcType() {
        return function.type();
    }

    @Override
    public String toString() {
        return "View[" + function + "," + newType() + "]";
    }

    public Optional<? extends View<A, B>> rewrite(final PointFreeRule rule) {
        return rule.rewrite(function()).map(View::new);
    }

    public View<A, B> rewriteOrNop(final PointFreeRule rule) {
        return DataFixUtils.orElse(rewrite(rule), this);
    }

    public <C> View<A, C> flatMap(final Function<Type<B>, View<B, C>> function) {
        final View<B, C> instance = function.apply(newType());
        return new View<>(Functions.comp(instance.function(), function()));
    }

    @SuppressWarnings("unchecked")
    public <C> View<C, B> compose(final View<C, A> that) {
        if (isNop()) {
            return new View<>(((View<C, B>) that).function());
        }
        if (that.isNop()) {
            return new View<>(((View<C, B>) this).function());
        }
        return new View<>(Functions.comp(function(), that.function()));
    }

    public boolean isNop() {
        return Functions.isId(function());
    }

    static final class Mu implements K2 {

    }

}
