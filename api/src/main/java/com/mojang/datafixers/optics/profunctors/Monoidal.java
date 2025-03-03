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
package com.mojang.datafixers.optics.profunctors;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.util.Pair;
import java.util.function.Supplier;

public interface Monoidal<P extends K2, Mu extends Monoidal.Mu> extends Profunctor<P, Mu> {

    static <P extends K2, Proof extends Monoidal.Mu> Monoidal<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (Monoidal<P, Proof>) proofBox;
    }

    <A, B, C, D> App2<P, Pair<A, C>, Pair<B, D>> par(final App2<P, A, B> first,
                                                     final Supplier<App2<P, C, D>> second);

    App2<P, Void, Void> empty();

    interface Mu extends Profunctor.Mu {

    }

    /*default <R extends K1, I, A extends KK1, B extends KK1, C extends KK1, D extends KK1> App2<P, App<HApp<TypeFamilyContext.PairMu<A, C>, R>, I>, App<HApp<TypeFamilyContext.PairMu<B, D>, R>, I>> parF(final App2<P, App<HApp<A, R>, I>, App<HApp<B, R>, I>> first, final App2<P, App<HApp<C, R>, I>, App<HApp<D, R>, I>> second) {
        return dimap(
            par(first, second),
            TypeFamilyContext::unLift,
            TypeFamilyContext::lift
        );
    }

    default <R extends K1, I> App2<P, App<HApp<TypeFamilyContext.ConstMu<Void>, R>, I>, App<HApp<TypeFamilyContext.ConstMu<Void>, R>, I>> emptyF() {
        return dimap(empty(), TypeFamilyContext::getConst, TypeFamilyContext::constant);
    }*/
}
