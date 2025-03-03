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

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.FamilyOptic;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.optics.Optics;
import com.mojang.datafixers.optics.profunctors.AffineP;
import com.mojang.datafixers.optics.profunctors.Profunctor;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.function.IntFunction;
import org.jspecify.annotations.Nullable;

public record Const(Type<?> type) implements TypeTemplate {

    @Override
    public int size() {
        return 0;
    }

    @Override
    public TypeFamily apply(final TypeFamily family) {
        return new TypeFamily() {
            @Override
            public Type<?> apply(final int index) {
                return type;
            }

            /*@Override
            public <A, B> Either<Type.FieldOptic<?, ?, A, B>, Type.FieldNotFoundException> findField(final int index, final String name, final Type<A> aType, final Type<B> bType) {
                return type.findField(name, aType, bType, false).mapLeft(o -> o);
            }*/
        };
    }

    @Override
    public <A, B> FamilyOptic<A, B> applyO(final FamilyOptic<A, B> input,
                                           final Type<A> aType,
                                           final Type<B> bType) {
        if (Objects.equals(type, aType)) {
            return TypeFamily.familyOptic(i -> new TypedOptic<>(ImmutableSet.of(Profunctor.Mu.TYPE_TOKEN),
                aType,
                bType,
                aType,
                bType,
                Optics.id()
            ));
        }
        final TypedOptic<?, ?, A, B> ignoreOptic = makeIgnoreOptic(type, aType, bType);
        return TypeFamily.familyOptic(i -> ignoreOptic);
    }

    private <T, A, B> TypedOptic<T, T, A, B> makeIgnoreOptic(final Type<T> type,
                                                             final Type<A> aType,
                                                             final Type<B> bType) {
        return new TypedOptic<>(AffineP.Mu.TYPE_TOKEN,
            type,
            type,
            aType,
            bType,
            Optics.affine(Either::left, (b, t) -> t)
        );
    }

    @Override
    public <FT, FR> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(final int index,
                                                                                      final @Nullable String name,
                                                                                      final Type<FT> type,
                                                                                      final Type<FR> resultType) {
        return DSL.fieldFinder(name, type)
            .findType(this.type, resultType, false)
            .mapLeft(field -> new Const(field.tType()));
    }

    @Override
    public IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family,
                                                 final IntFunction<RewriteResult<?, ?>> function) {
        return i -> RewriteResult.nop(type);
    }

    @Override
    public String toString() {
        return "Const[" + type + "]";
    }

    public static final class PrimitiveType<A> extends Type<A> {

        private final Codec<A> codec;

        public PrimitiveType(final Codec<A> codec) {
            this.codec = codec;
        }

        @Override
        public boolean equals(final Object o,
                              final boolean ignoreRecursionPoints,
                              final boolean checkIndex) {
            return this == o;
        }

        @Override
        public TypeTemplate buildTemplate() {
            return DSL.constType(this);
        }

        @Override
        protected Codec<A> buildCodec() {
            return codec;
        }

        @Override
        public String toString() {
            return codec.toString();
        }

    }

}
