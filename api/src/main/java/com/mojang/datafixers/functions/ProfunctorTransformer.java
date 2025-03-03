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
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.TypedOptic;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;
import java.util.Objects;
import java.util.function.Function;

final class ProfunctorTransformer<S, T, A, B> extends PointFree<Function<Function<A, B>, Function<S, T>>> {

    final TypedOptic<S, T, A, B> optic;

    public ProfunctorTransformer(final TypedOptic<S, T, A, B> optic) {
        this.optic = optic;
    }

    public <S2, T2> ProfunctorTransformer<S2, T2, A, B> castOuterUnchecked(final Type<S2> sType,
                                                                           final Type<T2> tType) {
        return new ProfunctorTransformer<>(optic.castOuterUnchecked(sType, tType));
    }

    @Override
    public Type<Function<Function<A, B>, Function<S, T>>> type() {
        return DSL.func(DSL.func(optic.aType(), optic.bType()),
            DSL.func(optic.sType(), optic.tType())
        );
    }

    @Override
    public String toString(final int level) {
        return "Optic[" + optic + "]";
    }

    @Override
    public Function<DynamicOps<?>, Function<Function<A, B>, Function<S, T>>> eval() {
        final Function<App2<FunctionType.Mu, A, B>, App2<FunctionType.Mu, S, T>> func = optic.upCast(
            FunctionType.Instance.Mu.TYPE_TOKEN).orElseThrow().eval(FunctionType.Instance.INSTANCE);
        final Function<Function<A, B>, Function<S, T>> unwrappedFunction = input -> FunctionType.unbox(
            func.apply(FunctionType.create(input)));
        return ops -> unwrappedFunction;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ProfunctorTransformer<?, ?, ?, ?> that = (ProfunctorTransformer<?, ?, ?, ?>) o;
        return Objects.equals(optic, that.optic);
    }

    @Override
    public int hashCode() {
        return optic.hashCode();
    }

}
