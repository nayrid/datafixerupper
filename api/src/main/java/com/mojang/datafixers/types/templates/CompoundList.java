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

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.profunctors.Cartesian;
import com.mojang.datafixers.optics.profunctors.TraversalP;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import org.jspecify.annotations.Nullable;

public record CompoundList(TypeTemplate key,
                           TypeTemplate element) implements TypeTemplate {

    @Override
    public int size() {
        return Math.max(key.size(), element.size());
    }

    @Override
    public TypeFamily apply(final TypeFamily family) {
        return index -> DSL.compoundList(key.apply(family).apply(index),
            element.apply(family).apply(index)
        );
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(final FamilyOptic<A, B> input,
                                           final Type<A> aType,
                                           final Type<B> bType) {
        return TypeFamily.familyOptic(i -> cap(element.applyO(input, aType, bType).apply(i)));
    }

    private <S, T, A, B> TypedOptic<?, ?, A, B> cap(final TypedOptic<S, T, A, B> concreteOptic) {
        final Type<Pair<String, S>> sTypeEntry = DSL.and(DSL.string(), concreteOptic.sType());
        final Type<Pair<String, T>> tTypeEntry = DSL.and(DSL.string(), concreteOptic.tType());
        return new TypedOptic<>(TraversalP.Mu.TYPE_TOKEN,
            DSL.compoundList(concreteOptic.sType()),
            DSL.compoundList(concreteOptic.tType()),
            sTypeEntry,
            tTypeEntry,
            Optics.listTraversal()
        ).compose(new TypedOptic<>(Cartesian.Mu.TYPE_TOKEN,
            sTypeEntry,
            tTypeEntry,
            concreteOptic.sType(),
            concreteOptic.tType(),
            Optics.proj2()
        )).compose(concreteOptic);
    }

    @Override
    public <FT, FR> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(final int index,
                                                                                      final @Nullable String name,
                                                                                      final Type<FT> type,
                                                                                      final Type<FR> resultType) {
        return element.findFieldOrType(index, name, type, resultType)
            .mapLeft(element1 -> new CompoundList(key, element1));
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family,
                                                 final IntFunction<RewriteResult<?, ?>> function) {
        return i -> {
            final RewriteResult<?, ?> f1 = key.hmap(family, function).apply(i);
            final RewriteResult<?, ?> f2 = element.hmap(family, function).apply(i);
            return cap(apply(family).apply(i), f1, f2);
        };
    }

    private <L, R> RewriteResult<?, ?> cap(final Type<?> type,
                                           final RewriteResult<L, ?> f1,
                                           final RewriteResult<R, ?> f2) {
        return ((CompoundListType<L, R>) type).mergeViews(f1, f2);
    }

    @Override
    public String toString() {
        return "CompoundList[" + element + "]";
    }

    public static final class CompoundListType<K, V> extends Type<List<Pair<K, V>>> {

        private final Type<K> key;
        private final Type<V> element;

        public CompoundListType(final Type<K> key, final Type<V> element) {
            this.key = key;
            this.element = element;
        }

        private static <K, V, K2> RewriteResult<List<Pair<K, V>>, List<Pair<K2, V>>> fixKeys(final Type<List<Pair<K, V>>> type,
                                                                                             final Type<K> first,
                                                                                             final Type<V> second,
                                                                                             final RewriteResult<K, K2> view) {
            return opticView(type,
                view,
                TypedOptic.compoundListKeys(first, view.view().newType(), second)
            );
        }

        private static <K, V, V2> RewriteResult<List<Pair<K, V>>, List<Pair<K, V2>>> fixValues(final Type<List<Pair<K, V>>> type,
                                                                                               final Type<K> first,
                                                                                               final Type<V> second,
                                                                                               final RewriteResult<V, V2> view) {
            return opticView(type,
                view,
                TypedOptic.compoundListElements(first, second, view.view().newType())
            );
        }

        @Override
        public RewriteResult<List<Pair<K, V>>, ?> all(final TypeRewriteRule rule,
                                                      final boolean recurse,
                                                      final boolean checkIndex) {
            return mergeViews(key.rewriteOrNop(rule), element.rewriteOrNop(rule));
        }

        public <K2, V2> RewriteResult<List<Pair<K, V>>, ?> mergeViews(final RewriteResult<K, K2> leftView,
                                                                      final RewriteResult<V, V2> rightView) {
            final RewriteResult<List<Pair<K, V>>, List<Pair<K2, V>>> v1 = fixKeys(this,
                key,
                element,
                leftView
            );
            final RewriteResult<List<Pair<K2, V>>, List<Pair<K2, V2>>> v2 = fixValues(v1.view()
                .newType(), leftView.view().newType(), element, rightView);
            return v2.compose(v1);
        }

        @Override
        public Optional<RewriteResult<List<Pair<K, V>>, ?>> one(final TypeRewriteRule rule) {
            return DataFixUtils.or(rule.rewrite(key).map(v -> fixKeys(this, key, element, v)),
                () -> rule.rewrite(element).map(v -> fixValues(this, key, element, v))
            );
        }

        @Override
        public Type<?> updateMu(final RecursiveTypeFamily newFamily) {
            return DSL.compoundList(key.updateMu(newFamily), element.updateMu(newFamily));
        }

        @Override
        public TypeTemplate buildTemplate() {
            return new CompoundList(key.template(), element.template());
        }

        @Override
        public Optional<List<Pair<K, V>>> point(final DynamicOps<?> ops) {
            return Optional.of(ImmutableList.of());
        }

        @Override
        public <FT, FR> Either<TypedOptic<List<Pair<K, V>>, ?, FT, FR>, FieldNotFoundException> findTypeInChildren(
            final Type<FT> type,
            final Type<FR> resultType,
            final TypeMatcher<FT, FR> matcher,
            final boolean recurse) {
            final Either<TypedOptic<K, ?, FT, FR>, FieldNotFoundException> firstFieldLens = key.findType(
                type,
                resultType,
                matcher,
                recurse
            );
            return firstFieldLens.map(this::capLeft, r -> {
                final Either<TypedOptic<V, ?, FT, FR>, FieldNotFoundException> secondFieldLens = element.findType(
                    type,
                    resultType,
                    matcher,
                    recurse
                );
                return secondFieldLens.mapLeft(this::capRight);
            });
        }

        private <FT, K2, FR> Either<TypedOptic<List<Pair<K, V>>, ?, FT, FR>, FieldNotFoundException> capLeft(
            final TypedOptic<K, K2, FT, FR> optic) {
            return Either.left(TypedOptic.compoundListKeys(optic.sType(), optic.tType(), element)
                .compose(optic));
        }

        private <FT, V2, FR> TypedOptic<List<Pair<K, V>>, ?, FT, FR> capRight(final TypedOptic<V, V2, FT, FR> optic) {
            return TypedOptic.compoundListElements(key, optic.sType(), optic.tType())
                .compose(optic);
        }

        @Override
        protected Codec<List<Pair<K, V>>> buildCodec() {
            return Codec.compoundList(key.codec(), element.codec());
        }

        @Override
        public String toString() {
            return "CompoundList[" + key + " -> " + element + "]";
        }

        @Override
        public boolean equals(final Object obj,
                              final boolean ignoreRecursionPoints,
                              final boolean checkIndex) {
            if (!(obj instanceof final CompoundListType<?, ?> that)) {
                return false;
            }
            return key.equals(that.key,
                ignoreRecursionPoints,
                checkIndex
            ) && element.equals(that.element, ignoreRecursionPoints, checkIndex);
        }

        @Override
        public int hashCode() {
            int result = key.hashCode();
            result = 31 * result + element.hashCode();
            return result;
        }

        public Type<K> getKey() {
            return key;
        }

        public Type<V> getElement() {
            return element;
        }

    }

}
