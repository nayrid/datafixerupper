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
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.util.Either;
import java.util.function.IntFunction;
import org.jspecify.annotations.Nullable;

public interface TypeTemplate {

    int size();

    TypeFamily apply(final TypeFamily family);

    default Type<?> toSimpleType() {
        return apply(index -> DSL.emptyPartType()).apply(-1);
    }

    /**
     * returned optic will accept {@code template<family<index>>} with the input template, and will
     * return the same with the returned template.
     *
     * <p>{@code (template, optic) = Left(result)}</p>
     *
     * <p>{@code this.apply(family).apply(index) == optic.sType}</p>
     *
     * <p>{@code template.apply(family).apply(index) == optic.tType}</p>
     */
    <A, B> Either<TypeTemplate, Type.FieldNotFoundException> findFieldOrType(final int index,
                                                                             @Nullable String name,
                                                                             Type<A> type,
                                                                             Type<B> resultType);

    /**
     * constraint: family, argFamily and resFamily are matched result.function(i) ::
     * this.apply(function.argFamily()).apply(i) -> this.apply(function.resFamily()).apply(i)
     */
    IntFunction<RewriteResult<?, ?>> hmap(final TypeFamily family,
                                          final IntFunction<RewriteResult<?, ?>> function);

    <A, B> FamilyOptic<A, B> applyO(final FamilyOptic<A, B> input,
                                    final Type<A> aType,
                                    final Type<B> bType);

}
