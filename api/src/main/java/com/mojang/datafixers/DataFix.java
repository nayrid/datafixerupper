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

import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.BitSet;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DataFix {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataFix.class);

    private final Schema outputSchema;
    private final boolean changesType;
    private @Nullable TypeRewriteRule rule;

    public DataFix(final Schema outputSchema, final boolean changesType) {
        this.outputSchema = outputSchema;
        this.changesType = changesType;
    }

    private static <A, B> RewriteResult<A, B> unchecked(final String name,
                                                        final Type<A> type,
                                                        final Type<B> newType,
                                                        final Function<DynamicOps<?>, Function<A, B>> function,
                                                        final BitSet bitSet) {
        return RewriteResult.create(View.create(name,
            type,
            newType,
            new NamedFunctionWrapper<>(name, function)
        ), bitSet);
    }

    @SuppressWarnings("unchecked")
    public static <A, B> RewriteResult<A, B> checked(final String name,
                                                     final Type<A> type,
                                                     final Type<B> newType,
                                                     final Function<Typed<?>, Typed<?>> function,
                                                     final BitSet bitSet) {
        return RewriteResult.create(View.create(name,
            type,
            newType,
            new NamedFunctionWrapper<>(name, ops -> a -> {
                final Typed<?> result = function.apply(new Typed<>(type, ops, a));
                if (!newType.equals(result.type, true, false)) {
                    throw new IllegalStateException(String.format(
                        "Dynamic type check failed: %s not equal to %s",
                        newType,
                        result.type
                    ));
                }
                return (B) result.value;
            })
        ), bitSet);
    }

    protected <A> TypeRewriteRule fixTypeEverywhere(final String name,
                                                    final Type<A> type,
                                                    final Function<DynamicOps<?>, Function<A, A>> function) {
        return fixTypeEverywhere(name, type, type, function, new BitSet());
    }

    @SuppressWarnings("unchecked")
    protected <A, B> TypeRewriteRule convertUnchecked(final String name,
                                                      final Type<A> type,
                                                      final Type<B> newType) {
        return fixTypeEverywhere(name,
            type,
            newType,
            ops -> (Function<A, B>) Function.identity(),
            new BitSet()
        );
    }

    protected TypeRewriteRule writeAndRead(final String name,
                                           final Type<?> type,
                                           final Type<?> newType) {
        return writeFixAndRead(name, type, newType, Function.identity());
    }

    @SuppressWarnings("unchecked")
    protected <A, B> TypeRewriteRule writeFixAndRead(final String name,
                                                     final Type<A> type,
                                                     final Type<B> newType,
                                                     final Function<Dynamic<?>, Dynamic<?>> fix) {
        final AtomicReference<Type<A>> patchedType = new AtomicReference<>();
        final RewriteResult<A, B> view = unchecked(name, type, newType, ops -> input -> {
            final Optional<? extends Dynamic<?>> written = patchedType.getPlain()
                .writeDynamic(ops, input)
                .resultOrPartial(LOGGER::error);
            if (written.isEmpty()) {
                throw new RuntimeException("Could not write the object in " + name);
            }
            final Optional<? extends Pair<Typed<B>, ?>> read = newType.readTyped(fix.apply(written.get()))
                .resultOrPartial(LOGGER::error);
            if (read.isEmpty()) {
                throw new RuntimeException("Could not read the new object in " + name);
            }
            return read.get().first().getValue();
        }, new BitSet());
        final TypeRewriteRule rule = fixTypeEverywhere(type, view);
        // Replace the input type within itself recursively, as this is what is actually passed to the fixer
        patchedType.setPlain((Type<A>) type.all(rule, true, false).view().newType());
        return rule;
    }

    protected <A, B> TypeRewriteRule fixTypeEverywhere(final String name,
                                                       final Type<A> type,
                                                       final Type<B> newType,
                                                       final Function<DynamicOps<?>, Function<A, B>> function) {
        return fixTypeEverywhere(name, type, newType, function, new BitSet());
    }

    protected <A, B> TypeRewriteRule fixTypeEverywhere(final String name,
                                                       final Type<A> type,
                                                       final Type<B> newType,
                                                       final Function<DynamicOps<?>, Function<A, B>> function,
                                                       final BitSet bitSet) {
        return fixTypeEverywhere(type, unchecked(name, type, newType, function, bitSet));
    }

    protected <A> TypeRewriteRule fixTypeEverywhereTyped(final String name,
                                                         final Type<A> type,
                                                         final Function<Typed<?>, Typed<?>> function) {
        return fixTypeEverywhereTyped(name, type, function, new BitSet());
    }

    protected <A> TypeRewriteRule fixTypeEverywhereTyped(final String name,
                                                         final Type<A> type,
                                                         final Function<Typed<?>, Typed<?>> function,
                                                         final BitSet bitSet) {
        return fixTypeEverywhereTyped(name, type, type, function, bitSet);
    }

    protected <A, B> TypeRewriteRule fixTypeEverywhereTyped(final String name,
                                                            final Type<A> type,
                                                            final Type<B> newType,
                                                            final Function<Typed<?>, Typed<?>> function) {
        return fixTypeEverywhereTyped(name, type, newType, function, new BitSet());
    }

    protected <A, B> TypeRewriteRule fixTypeEverywhereTyped(final String name,
                                                            final Type<A> type,
                                                            final Type<B> newType,
                                                            final Function<Typed<?>, Typed<?>> function,
                                                            final BitSet bitSet) {
        return fixTypeEverywhere(type, checked(name, type, newType, function, bitSet));
    }

    protected <A, B> TypeRewriteRule fixTypeEverywhere(final Type<A> type,
                                                       final RewriteResult<A, B> view) {
        return TypeRewriteRule.checkOnce(TypeRewriteRule.everywhere(TypeRewriteRule.ifSame(type,
            view
        ), DataFixerUpper.OPTIMIZATION_RULE, true, true), this::onFail);
    }

    protected void onFail(final Type<?> type) {
        LOGGER.info("Not matched: " + this + " " + type);
    }

    public final int getVersionKey() {
        return getOutputSchema().getVersionKey();
    }

    public TypeRewriteRule getRule() {
        if (rule == null) {
            rule = makeRule();
        }
        return rule;
    }

    protected abstract TypeRewriteRule makeRule();

    protected Schema getInputSchema() {
        if (changesType) {
            return outputSchema.getParent();
        }
        return getOutputSchema();
    }

    protected Schema getOutputSchema() {
        return outputSchema;
    }

    private record NamedFunctionWrapper<A, B>(String name,
                                              Function<DynamicOps<?>, Function<A, B>> delegate) implements Function<DynamicOps<?>, Function<A, B>> {

        @Override
        public Function<A, B> apply(final DynamicOps<?> ops) {
            return delegate.apply(ops);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final NamedFunctionWrapper<?, ?> that = (NamedFunctionWrapper<?, ?>) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

    }

}
