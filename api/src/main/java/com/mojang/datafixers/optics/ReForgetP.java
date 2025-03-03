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
import com.mojang.datafixers.optics.profunctors.AffineP;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.function.Function;

interface ReForgetP<R, A, B> extends App2<ReForgetP.Mu<R>, A, B> {

    static <R, A, B> ReForgetP<R, A, B> unbox(final App2<Mu<R>, A, B> box) {
        return (ReForgetP<R, A, B>) box;
    }

    B run(final A a, final R r);

    final class Mu<R> implements K2 {

    }

    final class Instance<R> implements AffineP<Mu<R>, Instance.Mu<R>>, App<Instance.Mu<R>, Mu<R>> {

        @Override
        public <A, B, C, D> FunctionType<App2<ReForgetP.Mu<R>, A, B>, App2<ReForgetP.Mu<R>, C, D>> dimap(
            final Function<C, A> g,
            final Function<B, D> h) {
            return input -> Optics.reForgetP("dimap", (c, r) -> {
                final A a = g.apply(c);
                final B b = ReForgetP.unbox(input).run(a, r);
                final D d = h.apply(b);
                return d;
            });
        }

        @Override
        public <A, B, C> App2<ReForgetP.Mu<R>, Either<A, C>, Either<B, C>> left(final App2<ReForgetP.Mu<R>, A, B> input) {
            return Optics.reForgetP("left",
                (e, r) -> e.mapLeft(a -> ReForgetP.unbox(input).run(a, r))
            );
        }

        @Override
        public <A, B, C> App2<ReForgetP.Mu<R>, Either<C, A>, Either<C, B>> right(final App2<ReForgetP.Mu<R>, A, B> input) {
            return Optics.reForgetP("right",
                (e, r) -> e.mapRight(a -> ReForgetP.unbox(input).run(a, r))
            );
        }

        @Override
        public <A, B, C> App2<ReForgetP.Mu<R>, Pair<A, C>, Pair<B, C>> first(final App2<ReForgetP.Mu<R>, A, B> input) {
            return Optics.reForgetP("first",
                (p, r) -> Pair.of(ReForgetP.unbox(input).run(p.first(), r), p.second())
            );
        }

        @Override
        public <A, B, C> App2<ReForgetP.Mu<R>, Pair<C, A>, Pair<C, B>> second(final App2<ReForgetP.Mu<R>, A, B> input) {
            return Optics.reForgetP("second",
                (p, r) -> Pair.of(p.first(), ReForgetP.unbox(input).run(p.second(), r))
            );
        }

        static final class Mu<R> implements AffineP.Mu {

        }

    }

}
