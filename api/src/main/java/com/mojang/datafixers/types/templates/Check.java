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
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.function.IntFunction;
import org.jspecify.annotations.Nullable;

public record Check(String name,
                    int index,
                    TypeTemplate element) implements TypeTemplate {

    @Override
    public int size() {
        return Math.max(index + 1, element.size());
    }

    @Override
    public TypeFamily apply(final TypeFamily family) {
        return new TypeFamily() {
            @Override
            public Type<?> apply(final int index) {
                if (index < 0) {
                    throw new IndexOutOfBoundsException();
                }
                return new CheckType<>(name,
                    index,
                    Check.this.index,
                    element.apply(family).apply(index)
                );
            }

            /*@Override
            public <A, B> Either<Type.FieldOptic<?, ?, A, B>, Type.FieldNotFoundException> findField(final int index, final String name, final Type<A> aType, final Type<B> bType) {
                if (index == Tag.this.index) {
                    return element.apply(family).findField(index, name, aType, bType);
                }
                return Either.right(new Type.FieldNotFoundException("Not a matching index"));
            }*/
        };
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
        if (index == this.index) {
            return element.findFieldOrType(index, name, type, resultType);
        }
        return Either.right(new Type.FieldNotFoundException("Not a matching index"));
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family,
                                                 final IntFunction<RewriteResult<?, ?>> function) {
        return index -> {
            final RewriteResult<?, ?> elementResult = element.hmap(family, function).apply(index);
            return cap(family, index, elementResult);
        };
    }

    private <A> RewriteResult<?, ?> cap(final TypeFamily family,
                                        final int index,
                                        final RewriteResult<A, ?> elementResult) {
        return CheckType.fix((CheckType<A>) apply(family).apply(index), elementResult);
    }

    @Override
    public String toString() {
        return "Tag[" + name + ", " + index + ": " + element + "]";
    }

    public static final class CheckType<A> extends Type<A> {

        private final String name;
        private final int index;
        private final int expectedIndex;
        private final Type<A> delegate;

        public CheckType(final String name,
                         final int index,
                         final int expectedIndex,
                         final Type<A> delegate) {
            this.name = name;
            this.index = index;
            this.expectedIndex = expectedIndex;
            this.delegate = delegate;
        }

        public static <A, B> RewriteResult<A, ?> fix(final CheckType<A> type,
                                                     final RewriteResult<A, B> instance) {
            if (instance.view().isNop()) {
                return RewriteResult.nop(type);
            }
            return opticView(type,
                instance,
                wrapOptic(type,
                    TypedOptic.adapter(instance.view().type(), instance.view().newType())
                )
            );
        }

        private static <A, B, FT, FR> TypedOptic<A, B, FT, FR> wrapOptic(final CheckType<A> type,
                                                                         final TypedOptic<A, B, FT, FR> optic) {
            return optic.castOuter(type,
                new CheckType<>(type.name, type.index, type.expectedIndex, optic.tType())
            );
        }

        @Override
        protected Codec<A> buildCodec() {
            return Codec.of(delegate.codec(), this::read);
        }

        private <T> DataResult<Pair<A, T>> read(final DynamicOps<T> ops, final T input) {
            if (index != expectedIndex) {
                return DataResult.error(() -> "Index mismatch: " + index + " != " + expectedIndex);
            }
            return delegate.codec().decode(ops, input);
        }

        @Override
        public RewriteResult<A, ?> all(final TypeRewriteRule rule,
                                       final boolean recurse,
                                       final boolean checkIndex) {
            if (checkIndex && index != expectedIndex) {
                return RewriteResult.nop(this);
            }
            return fix(this, delegate.rewriteOrNop(rule));
        }

        @Override
        public Optional<RewriteResult<A, ?>> everywhere(final TypeRewriteRule rule,
                                                        final PointFreeRule optimizationRule,
                                                        final boolean recurse,
                                                        final boolean checkIndex) {
            if (checkIndex && index != expectedIndex) {
                return Optional.empty();
            }
            return super.everywhere(rule, optimizationRule, recurse, checkIndex);
        }

        @Override
        public Optional<RewriteResult<A, ?>> one(final TypeRewriteRule rule) {
            return rule.rewrite(delegate).map(view -> fix(this, view));
        }

        @Override
        public Type<?> updateMu(final RecursiveTypeFamily newFamily) {
            return new CheckType<>(name, index, expectedIndex, delegate.updateMu(newFamily));
        }

        @Override
        public TypeTemplate buildTemplate() {
            return DSL.check(name, expectedIndex, delegate.template());
        }

        @Override
        public Optional<TaggedChoice.TaggedChoiceType<?>> findChoiceType(final String name,
                                                                         final int index) {
            if (index == expectedIndex) {
                return delegate.findChoiceType(name, index);
            }
            return Optional.empty();
        }

        @Override
        public Optional<Type<?>> findCheckedType(final int index) {
            if (index == expectedIndex) {
                return Optional.of(delegate);
            }
            return Optional.empty();
        }

        @Override
        public Optional<Type<?>> findFieldTypeOpt(final String name) {
            if (index == expectedIndex) {
                return delegate.findFieldTypeOpt(name);
            }
            return Optional.empty();
        }

        @Override
        public Optional<A> point(final DynamicOps<?> ops) {
            if (index == expectedIndex) {
                return delegate.point(ops);
            }
            return Optional.empty();
        }

        @Override
        public <FT, FR> Either<TypedOptic<A, ?, FT, FR>, FieldNotFoundException> findTypeInChildren(
            final Type<FT> type,
            final Type<FR> resultType,
            final TypeMatcher<FT, FR> matcher,
            final boolean recurse) {
            if (index != expectedIndex) {
                return Either.right(new FieldNotFoundException("Incorrect index in CheckType"));
            }
            return delegate.findType(type, resultType, matcher, recurse)
                .mapLeft(optic -> wrapOptic(this, optic));
        }

        @Override
        public String toString() {
            return "TypeTag[" + index + "~" + expectedIndex + "][" + name + ": " + delegate + "]";
        }

        @Override
        public boolean equals(final Object obj,
                              final boolean ignoreRecursionPoints,
                              final boolean checkIndex) {
            if (!(obj instanceof final CheckType<?> type)) {
                return false;
            }
            if (index == type.index && expectedIndex == type.expectedIndex) {
                if (!checkIndex) {
                    return true;
                }
                return delegate.equals(type.delegate, ignoreRecursionPoints, checkIndex);
            }
            return false;
        }

        @Override
        public int hashCode() {
            int result = index;
            result = 31 * result + expectedIndex;
            result = 31 * result + delegate.hashCode();
            return result;
        }

    }

}
