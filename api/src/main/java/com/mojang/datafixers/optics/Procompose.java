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
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.optics.profunctors.Profunctor;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A composition of {@linkplain Profunctor profunctors}. Where a profunctor represents a transformation from one
 * type to another, a {@link Procompose} represents the composition of these transformations. This is analogous
 * to function composition, but for a general profunctor.
 *
 * <p>A {@code Procompose<F, G, A, B, C>} is analogous to a transformation {@code F} from {@code A} to {@code C}
 * followed by a transformation {@code G} from {@code C} to {@code B}.
 *
 * @param first  The first profunctor.
 * @param second The second profunctor.
 * @param <F> The witness type of the first transformation.
 * @param <G> The witness type of the second transformation.
 * @param <A> The input type.
 * @param <B> The output type.
 * @param <C> The intermediate type.
 * @see Profunctor
 * @see Function#compose(Function)
 */
public record Procompose<F extends K2, G extends K2, A, B, C>(Supplier<App2<F, A, C>> first,
                                                              App2<G, C, B> second) implements App2<Procompose.Mu<F, G>, A, B> {

    /**
     * Casts an applied {@link Procompose.Mu} to a {@link Procompose}.
     *
     * @param box The boxed {@link Procompose}.
     * @param <F> The type of the profunctor applied first.
     * @param <G> The type of the profunctor applied second.
     * @param <A> The input type.
     * @param <B> The output type.
     * @return The unboxed {@link Procompose}.
     */
    public static <F extends K2, G extends K2, A, B> Procompose<F, G, A, B, ?> unbox(final App2<Mu<F, G>, A, B> box) {
        return (Procompose<F, G, A, B, ?>) box;
    }

    /**
     * The witness type of {@link Procompose}.
     *
     * @param <F> The type of the profunctor applied first.
     * @param <G> The type of the profunctor applied second.
     */
    public static final class Mu<F extends K2, G extends K2> implements K2 {

    }

    /**
     * The {@link Profunctor} type class instance for {@link Procompose}.
     *
     * @param <F> The type of the profunctor applied first.
     * @param <G> The type of the profunctor applied second.
     */
    static final class ProfunctorInstance<F extends K2, G extends K2> implements Profunctor<Mu<F, G>, Profunctor.Mu> {

        private final Profunctor<F, Mu> p1;
        private final Profunctor<G, Mu> p2;

        ProfunctorInstance(final Profunctor<F, Mu> p1, final Profunctor<G, Mu> p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        @Override
        public <A, B, C, D> FunctionType<App2<Procompose.Mu<F, G>, A, B>, App2<Procompose.Mu<F, G>, C, D>> dimap(
            final Function<C, A> g,
            final Function<B, D> h) {
            return cmp -> cap(Procompose.unbox(cmp), g, h);
        }

        private <A, B, C, D, E> App2<Procompose.Mu<F, G>, C, D> cap(final Procompose<F, G, A, B, E> cmp,
                                                                    final Function<C, A> g,
                                                                    final Function<B, D> h) {
            return new Procompose<>(() -> p1.dimap(g, Function.<E>identity())
                .apply(cmp.first.get()), p2.dimap(Function.<E>identity(), h).apply(cmp.second));
        }

    }

}
