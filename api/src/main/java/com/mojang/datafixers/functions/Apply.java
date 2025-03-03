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

import com.mojang.datafixers.types.Func;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

final class Apply<A, B> extends PointFree<B> {

    final PointFree<Function<A, B>> func;
    final PointFree<A> arg;
    final Type<B> type;

    Apply(final PointFree<Function<A, B>> func, final PointFree<A> arg) {
        this(func, arg, ((Func<A, B>) func.type()).second());
    }

    Apply(final PointFree<Function<A, B>> func, final PointFree<A> arg, final Type<B> type) {
        this.func = func;
        this.arg = arg;
        this.type = type;
    }

    @Override
    public Function<DynamicOps<?>, B> eval() {
        return ops -> func.evalCached().apply(ops).apply(arg.evalCached().apply(ops));
    }

    @Override
    public Type<B> type() {
        return type;
    }

    @Override
    public String toString(final int level) {
        return "(ap " + func.toString(level + 1) + "\n" + indent(level + 1) + arg.toString(level + 1) + "\n" + indent(
            level) + ")";
    }

    @Override
    public Optional<? extends PointFree<B>> all(final PointFreeRule rule) {
        final PointFree<Function<A, B>> f = rule.rewriteOrNop(func);
        final PointFree<A> a = rule.rewriteOrNop(arg);
        if (f == func && a == arg) {
            return Optional.of(this);
        }
        return Optional.of(new Apply<>(f, a, type));
    }

    @Override
    public Optional<? extends PointFree<B>> one(final PointFreeRule rule) {
        return rule.rewrite(func)
            .map(f -> new Apply<>(f, arg, type))
            .or(() -> rule.rewrite(arg).map(a -> new Apply<>(func, a, type)));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final Apply<?, ?> apply)) {
            return false;
        }
        return Objects.equals(func, apply.func) && Objects.equals(arg, apply.arg);
    }

    @Override
    public int hashCode() {
        int result = func.hashCode();
        result = 31 * result + arg.hashCode();
        return result;
    }

}
