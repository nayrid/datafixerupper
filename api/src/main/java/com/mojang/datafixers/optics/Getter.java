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
package com.mojang.datafixers.optics;

import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.GetterP;
import java.util.function.Function;
import java.util.function.Supplier;

interface Getter<S, T, A, B> extends App2<Getter.Mu<A, B>, S, T>, Optic<GetterP.Mu, S, T, A, B> {

    static <S, T, A, B> Getter<S, T, A, B> unbox(final App2<Mu<A, B>, S, T> box) {
        return (Getter<S, T, A, B>) box;
    }

    A get(S s);

    @Override
    default <P extends K2> FunctionType<App2<P, A, B>, App2<P, S, T>> eval(final App<? extends GetterP.Mu, P> proof) {
        final GetterP<P, ?> ops = GetterP.unbox(proof);
        return input -> ops.lmap(ops.secondPhantom(input), this::get);
    }

    final class Mu<A, B> implements K2 {

    }

    final class Instance<A2, B2> implements GetterP<Mu<A2, B2>, GetterP.Mu> {

        @Override
        public <A, B, C, D> FunctionType<App2<Getter.Mu<A2, B2>, A, B>, App2<Getter.Mu<A2, B2>, C, D>> dimap(
            final Function<C, A> g,
            final Function<B, D> h) {
            return input -> Optics.getter(g.andThen(Getter.unbox(input)::get));
        }

        @Override
        public <A, B, C, D> FunctionType<Supplier<App2<Getter.Mu<A2, B2>, A, B>>, App2<Getter.Mu<A2, B2>, C, D>> cimap(
            final Function<C, A> g,
            final Function<D, B> h) {
            return input -> Optics.getter(g.andThen(Getter.unbox(input.get())::get));
        }

    }

}
