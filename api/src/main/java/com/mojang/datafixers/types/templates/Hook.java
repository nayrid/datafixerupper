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
package com.mojang.datafixers.types.templates;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Objects;
import java.util.Optional;
import java.util.function.IntFunction;
import org.jspecify.annotations.Nullable;

public record Hook(TypeTemplate element,
                   HookFunction preRead,
                   HookFunction postWrite) implements TypeTemplate {

    @Override
    public int size() {
        return element.size();
    }

    @Override
    public TypeFamily apply(final TypeFamily family) {
        return index -> DSL.hook(element.apply(family).apply(index), preRead, postWrite);
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(final FamilyOptic<A, B> input,
                                           final Type<A> aType,
                                           final Type<B> bType) {
        return TypeFamily.familyOptic(i -> element.applyO(input, aType, bType).apply(i));
    }

    @Override
    public <FT, FR> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(final int index,
                                                                                      final @Nullable String name,
                                                                                      final Type<FT> type,
                                                                                      final Type<FR> resultType) {
        return element.findFieldOrType(index, name, type, resultType);
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family,
                                                 final IntFunction<RewriteResult<?, ?>> function) {
        return index -> {
            final RewriteResult<?, ?> elementResult = element.hmap(family, function).apply(index);
            return cap(family, index, elementResult);
        };
    }

    private <A> RewriteResult<A, ?> cap(final TypeFamily family,
                                        final int index,
                                        final RewriteResult<A, ?> elementResult) {
        return HookType.fix((HookType<A>) apply(family).apply(index), elementResult);
    }

    @Override
    public String toString() {
        return "Hook[" + element + ", " + preRead + ", " + postWrite + "]";
    }

    public interface HookFunction {

        HookFunction IDENTITY = new HookFunction() {
            @Override
            public <T> T apply(final DynamicOps<T> ops, final T value) {
                return value;
            }
        };

        <T> T apply(final DynamicOps<T> ops, final T value);

    }

    public static final class HookType<A> extends Type<A> {

        private final Type<A> delegate;
        private final HookFunction preRead;
        private final HookFunction postWrite;

        public HookType(final Type<A> delegate,
                        final HookFunction preRead,
                        final HookFunction postWrite) {
            this.delegate = delegate;
            this.preRead = preRead;
            this.postWrite = postWrite;
        }

        public static <A, B> RewriteResult<A, ?> fix(final HookType<A> type,
                                                     final RewriteResult<A, B> instance) {
            if (instance.view().isNop()) {
                return RewriteResult.nop(type);
            }
            return opticView(type,
                instance,
                wrapOptic(TypedOptic.adapter(instance.view().type(), instance.view().newType()),
                    type.preRead,
                    type.postWrite
                )
            );
        }

        private static <A, B, FT, FR> TypedOptic<A, B, FT, FR> wrapOptic(final TypedOptic<A, B, FT, FR> optic,
                                                                         final HookFunction preRead,
                                                                         final HookFunction postWrite) {
            return optic.castOuter(DSL.hook(optic.sType(), preRead, postWrite),
                DSL.hook(optic.tType(), preRead, postWrite)
            );
        }

        @Override
        protected Codec<A> buildCodec() {
            return new Codec<A>() {
                @Override
                public <T> DataResult<Pair<A, T>> decode(final DynamicOps<T> ops, final T input) {
                    return delegate.codec()
                        .decode(ops, preRead.apply(ops, input))
                        .setLifecycle(Lifecycle.experimental());
                }

                @Override
                public <T> DataResult<T> encode(final A input,
                                                final DynamicOps<T> ops,
                                                final T prefix) {
                    return delegate.codec()
                        .encode(input, ops, prefix)
                        .map(v -> postWrite.apply(ops, v))
                        .setLifecycle(Lifecycle.experimental());
                }
            };
        }

        @Override
        public RewriteResult<A, ?> all(final TypeRewriteRule rule,
                                       final boolean recurse,
                                       final boolean checkIndex) {
            return fix(this, delegate.rewriteOrNop(rule));
        }

        @Override
        public Optional<RewriteResult<A, ?>> one(final TypeRewriteRule rule) {
            return rule.rewrite(delegate).map(view -> fix(this, view));
        }

        @Override
        public Type<?> updateMu(final RecursiveTypeFamily newFamily) {
            return new HookType<>(delegate.updateMu(newFamily), preRead, postWrite);
        }

        @Override
        public TypeTemplate buildTemplate() {
            return DSL.hook(delegate.template(), preRead, postWrite);
        }

        @Override
        public Optional<TaggedChoice.TaggedChoiceType<?>> findChoiceType(final String name,
                                                                         final int index) {
            return delegate.findChoiceType(name, index);
        }

        @Override
        public Optional<Type<?>> findCheckedType(final int index) {
            return delegate.findCheckedType(index);
        }

        @Override
        public Optional<Type<?>> findFieldTypeOpt(final String name) {
            return delegate.findFieldTypeOpt(name);
        }

        @Override
        public Optional<A> point(final DynamicOps<?> ops) {
            return delegate.point(ops);
        }

        @Override
        public <FT, FR> Either<TypedOptic<A, ?, FT, FR>, FieldNotFoundException> findTypeInChildren(
            final Type<FT> type,
            final Type<FR> resultType,
            final TypeMatcher<FT, FR> matcher,
            final boolean recurse) {
            return delegate.findType(type, resultType, matcher, recurse)
                .mapLeft(optic -> wrapOptic(optic, preRead, postWrite));
        }

        @Override
        public String toString() {
            return "HookType[" + delegate + ", " + preRead + ", " + postWrite + "]";
        }

        @Override
        public boolean equals(final Object obj,
                              final boolean ignoreRecursionPoints,
                              final boolean checkIndex) {
            if (!(obj instanceof final HookType<?> type)) {
                return false;
            }
            return delegate.equals(type.delegate,
                ignoreRecursionPoints,
                checkIndex
            ) && Objects.equals(preRead, type.preRead) && Objects.equals(postWrite, type.postWrite);
        }

        @Override
        public int hashCode() {
            int result = delegate.hashCode();
            result = 31 * result + preRead.hashCode();
            result = 31 * result + postWrite.hashCode();
            return result;
        }

    }

}
