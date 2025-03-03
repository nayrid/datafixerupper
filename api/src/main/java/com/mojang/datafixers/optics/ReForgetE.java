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
import com.mojang.datafixers.optics.profunctors.Cocartesian;
import com.mojang.datafixers.util.Either;
import java.util.function.Function;

interface ReForgetE<R, A, B> extends App2<ReForgetE.Mu<R>, A, B> {

    static <R, A, B> ReForgetE<R, A, B> unbox(final App2<Mu<R>, A, B> box) {
        return (ReForgetE<R, A, B>) box;
    }

    B run(final Either<A, R> r);

    final class Mu<R> implements K2 {

    }

    final class Instance<R> implements Cocartesian<Mu<R>, Instance.Mu<R>>, App<Instance.Mu<R>, Mu<R>> {

        @Override
        public <A, B, C, D> FunctionType<App2<ReForgetE.Mu<R>, A, B>, App2<ReForgetE.Mu<R>, C, D>> dimap(
            final Function<C, A> g,
            final Function<B, D> h) {
            return input -> Optics.reForgetE("dimap", e -> {
                final Either<A, R> either = e.mapLeft(g);
                final B b = ReForgetE.unbox(input).run(either);
                final D d = h.apply(b);
                return d;
            });
        }

        @Override
        public <A, B, C> App2<ReForgetE.Mu<R>, Either<A, C>, Either<B, C>> left(final App2<ReForgetE.Mu<R>, A, B> input) {
            final ReForgetE<R, A, B> reForgetE = ReForgetE.unbox(input);
            return Optics.reForgetE("left",
                e -> e.map(e2 -> e2.map(a -> Either.left(reForgetE.run(Either.left(a))),
                    Either::right
                ), r -> Either.left(reForgetE.run(Either.right(r))))
            );
        }

        @Override
        public <A, B, C> App2<ReForgetE.Mu<R>, Either<C, A>, Either<C, B>> right(final App2<ReForgetE.Mu<R>, A, B> input) {
            final ReForgetE<R, A, B> reForgetE = ReForgetE.unbox(input);
            return Optics.reForgetE("right",
                e -> e.map(e2 -> e2.map(Either::left,
                    a -> Either.right(reForgetE.run(Either.left(a)))
                ), r -> Either.right(reForgetE.run(Either.right(r))))
            );
        }

        static final class Mu<R> implements Cocartesian.Mu {

        }

    }

}
