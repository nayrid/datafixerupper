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

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.Functor;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.kinds.K2;
import java.util.function.Function;

public record ProfunctorFunctorWrapper<P extends K2, F extends K1, G extends K1, A, B>(App2<P, App<F, A>, App<G, B>> value) implements App2<ProfunctorFunctorWrapper.Mu<P, F, G>, A, B> {

    public static <P extends K2, F extends K1, G extends K1, A, B> ProfunctorFunctorWrapper<P, F, G, A, B> unbox(
        final App2<Mu<P, F, G>, A, B> box) {
        return (ProfunctorFunctorWrapper<P, F, G, A, B>) box;
    }

    public static final class Mu<P extends K2, F extends K1, G extends K1> implements K2 {

    }

    public static final class Instance<P extends K2, F extends K1, G extends K1> implements Profunctor<Mu<P, F, G>, Instance.Mu>, App<Instance.Mu, Mu<P, F, G>> {

        private final Profunctor<P, ? extends Profunctor.Mu> profunctor;
        private final Functor<F, ?> fFunctor;
        private final Functor<G, ?> gFunctor;

        public Instance(final App<? extends Profunctor.Mu, P> proof,
                        final Functor<F, ?> fFunctor,
                        final Functor<G, ?> gFunctor) {
            profunctor = Profunctor.unbox(proof);
            this.fFunctor = fFunctor;
            this.gFunctor = gFunctor;
        }

        @Override
        public <A, B, C, D> FunctionType<App2<ProfunctorFunctorWrapper.Mu<P, F, G>, A, B>, App2<ProfunctorFunctorWrapper.Mu<P, F, G>, C, D>> dimap(
            final Function<C, A> g,
            final Function<B, D> h) {
            return input -> {
                final App2<P, App<F, A>, App<G, B>> value = ProfunctorFunctorWrapper.unbox(input)
                    .value();
                final App2<P, App<F, C>, App<G, D>> newValue = profunctor.dimap(value,
                    c -> fFunctor.map(g, c),
                    b -> gFunctor.map(h, b)
                );
                return new ProfunctorFunctorWrapper<>(newValue);
            };
        }

        public static final class Mu implements Profunctor.Mu {

        }

    }

}
