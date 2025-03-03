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

import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import org.jspecify.annotations.Nullable;

public interface OpticFinder<FT> {

    Type<FT> type();

    <A, FR> Either<TypedOptic<A, ?, FT, FR>, Type.FieldNotFoundException> findType(final Type<A> containerType,
                                                                                   final Type<FR> resultType,
                                                                                   final boolean recurse);

    default <A> Either<TypedOptic<A, ?, FT, FT>, Type.FieldNotFoundException> findType(final Type<A> containerType,
                                                                                       final boolean recurse) {
        return findType(containerType, type(), recurse);
    }

    default <GT> OpticFinder<FT> inField(final @Nullable String name, final Type<GT> type) {
        final OpticFinder<FT> outer = this;
        return new OpticFinder<FT>() {
            @Override
            public Type<FT> type() {
                return outer.type();
            }

            @Override
            public <A, FR> Either<TypedOptic<A, ?, FT, FR>, Type.FieldNotFoundException> findType(
                final Type<A> containerType,
                final Type<FR> resultType,
                final boolean recurse) {
                final Either<TypedOptic<GT, ?, FT, FR>, Type.FieldNotFoundException> secondOptic = outer.findType(
                    type,
                    resultType,
                    recurse
                );
                return secondOptic.map(l -> cap(containerType, l, recurse), Either::right);
            }

            private <A, FR, GR> Either<TypedOptic<A, ?, FT, FR>, Type.FieldNotFoundException> cap(
                final Type<A> containterType,
                final TypedOptic<GT, GR, FT, FR> l1,
                final boolean recurse) {
                final Either<TypedOptic<A, ?, GT, GR>, Type.FieldNotFoundException> first = DSL.fieldFinder(
                    name,
                    type
                ).findType(containterType, l1.tType(), recurse);
                return first.mapLeft(l -> l.compose(l1));
            }
        };
    }

}
