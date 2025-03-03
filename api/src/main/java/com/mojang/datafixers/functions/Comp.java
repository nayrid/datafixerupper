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
import com.mojang.datafixers.types.Func;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

final class Comp<A, B> extends PointFree<Function<A, B>> {

    final PointFree<? extends Function<?, ?>>[] functions;
    private final Type<Function<A, B>> type;

    @SuppressWarnings("unchecked")
    Comp(final PointFree<? extends Function<?, ?>>... functions) {
        this.functions = functions;
        final PointFree<? extends Function<?, ?>> first = functions[0];
        final PointFree<? extends Function<?, ?>> last = functions[functions.length - 1];
        type = DSL.func(((Func<A, ?>) last.type()).first(), ((Func<?, B>) first.type()).second());
    }

    private Comp(final PointFree<? extends Function<?, ?>>[] functions,
                 final Type<Function<A, B>> type) {
        this.functions = functions;
        this.type = type;
    }

    @SuppressWarnings("unchecked")
    private static <A, B> B applyUnchecked(final Function<A, B> function, final Object input) {
        return function.apply((A) input);
    }

    @Override
    public Type<Function<A, B>> type() {
        return type;
    }

    @Override
    public String toString(final int level) {
        final String content = Arrays.stream(functions)
            .map(function -> function.toString(level + 1))
            .collect(Collectors.joining("\n" + indent(level + 1) + "\u25E6\n" + indent(level + 1)));
        return "(\n" + indent(level + 1) + content + "\n" + indent(level) + ")";
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<? extends PointFree<Function<A, B>>> all(final PointFreeRule rule) {
        final List<PointFree<? extends Function<?, ?>>> newFunctions = new ArrayList<>(functions.length);
        boolean rewritten = false;
        for (final PointFree<? extends Function<?, ?>> function : functions) {
            final PointFree<? extends Function<?, ?>> rewrite = rule.rewriteOrNop(function);
            if (rewrite != function) {
                rewritten = true;
                if (rewrite instanceof Comp<?, ?> comp) {
                    Collections.addAll(newFunctions, comp.functions);
                } else {
                    newFunctions.add(rewrite);
                }
            } else {
                newFunctions.add(function);
            }
        }
        return Optional.of(rewritten ? new Comp<>(newFunctions.toArray(PointFree[]::new),
            type
        ) : this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<? extends PointFree<Function<A, B>>> one(final PointFreeRule rule) {
        for (int i = 0;
             i < functions.length;
             i++) {
            final PointFree<? extends Function<?, ?>> function = functions[i];
            final Optional<? extends PointFree<? extends Function<?, ?>>> rewrite = rule.rewrite(
                function);
            if (rewrite.isPresent()) {
                if (rewrite.get() instanceof Comp<?, ?> comp) {
                    final PointFree<? extends Function<?, ?>>[] newFunctions = new PointFree[functions.length - 1 + comp.functions.length];
                    System.arraycopy(functions, 0, newFunctions, 0, i);
                    System.arraycopy(comp.functions, 0, newFunctions, i, comp.functions.length);
                    System.arraycopy(functions,
                        i + 1,
                        newFunctions,
                        i + comp.functions.length,
                        functions.length - i - 1
                    );
                    return Optional.of(new Comp<>(newFunctions, type));
                } else {
                    final PointFree<? extends Function<?, ?>>[] newFunctions = Arrays.copyOf(
                        functions,
                        functions.length
                    );
                    newFunctions[i] = rewrite.get();
                    return Optional.of(new Comp<>(newFunctions, type));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Comp<?, ?> comp = (Comp<?, ?>) o;
        return Arrays.equals(functions, comp.functions);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(functions);
    }

    @Override
    public Function<DynamicOps<?>, Function<A, B>> eval() {
        return ops -> input -> {
            Object value = input;
            for (int i = functions.length - 1;
                 i >= 0;
                 i--) {
                final PointFree<? extends Function<?, ?>> f = functions[i];
                value = applyUnchecked(f.evalCached().apply(ops), value);
            }
            return (B) value;
        };
    }

}
