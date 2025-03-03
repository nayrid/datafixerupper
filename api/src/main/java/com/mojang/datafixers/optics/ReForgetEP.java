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

interface ReForgetEP<R, A, B> extends App2<ReForgetEP.Mu<R>, A, B> {

    static <R, A, B> ReForgetEP<R, A, B> unbox(final App2<Mu<R>, A, B> box) {
        return (ReForgetEP<R, A, B>) box;
    }

    B run(final Either<A, Pair<A, R>> e);

    final class Mu<R> implements K2 {

    }

    final class Instance<R> implements AffineP<Mu<R>, Instance.Mu<R>>, App<Instance.Mu<R>, Mu<R>> {

        @Override
        public <A, B, C, D> FunctionType<App2<ReForgetEP.Mu<R>, A, B>, App2<ReForgetEP.Mu<R>, C, D>> dimap(
            final Function<C, A> g,
            final Function<B, D> h) {
            return input -> Optics.reForgetEP("dimap", e -> {
                final Either<A, Pair<A, R>> either = e.mapBoth(g,
                    p -> Pair.of(g.apply(p.first()), p.second())
                );
                final B b = ReForgetEP.unbox(input).run(either);
                final D d = h.apply(b);
                return d;
            });
        }

        @Override
        public <A, B, C> App2<ReForgetEP.Mu<R>, Either<A, C>, Either<B, C>> left(final App2<ReForgetEP.Mu<R>, A, B> input) {
            final ReForgetEP<R, A, B> reForgetEP = ReForgetEP.unbox(input);
            return Optics.reForgetEP("left",
                e -> e.map(e2 -> e2.mapLeft(a -> reForgetEP.run(Either.left(a))),
                    (Pair<Either<A, C>, R> p) -> p.first()
                        .mapLeft(a -> reForgetEP.run(Either.right(Pair.of(a, p.second()))))
                )
            );
        }

        @Override
        public <A, B, C> App2<ReForgetEP.Mu<R>, Either<C, A>, Either<C, B>> right(final App2<ReForgetEP.Mu<R>, A, B> input) {
            final ReForgetEP<R, A, B> reForgetEP = ReForgetEP.unbox(input);
            return Optics.reForgetEP("right",
                e -> e.map(e2 -> e2.mapRight(a -> reForgetEP.run(Either.left(a))),
                    (Pair<Either<C, A>, R> p) -> p.first()
                        .mapRight(a -> reForgetEP.run(Either.right(Pair.of(a, p.second()))))
                )
            );
        }

        @Override
        public <A, B, C> App2<ReForgetEP.Mu<R>, Pair<A, C>, Pair<B, C>> first(final App2<ReForgetEP.Mu<R>, A, B> input) {
            final ReForgetEP<R, A, B> reForgetEP = ReForgetEP.unbox(input);
            return Optics.reForgetEP("first",
                e -> e.map(p -> Pair.of(reForgetEP.run(Either.left(p.first())), p.second()),
                    (Pair<Pair<A, C>, R> p) -> Pair.of(reForgetEP.run(Either.right(Pair.of(p.first()
                        .first(), p.second()))), p.first().second())
                )
            );
        }

        @Override
        public <A, B, C> App2<ReForgetEP.Mu<R>, Pair<C, A>, Pair<C, B>> second(final App2<ReForgetEP.Mu<R>, A, B> input) {
            final ReForgetEP<R, A, B> reForgetEP = ReForgetEP.unbox(input);
            return Optics.reForgetEP("second",
                e -> e.map(p -> Pair.of(p.first(), reForgetEP.run(Either.left(p.second()))),
                    (Pair<Pair<C, A>, R> p) -> Pair.of(p.first().first(),
                        reForgetEP.run(Either.right(Pair.of(p.first().second(),
                            p.second()
                        )))
                    )
                )
            );
        }

        static final class Mu<R> implements AffineP.Mu {

        }

    }

}
