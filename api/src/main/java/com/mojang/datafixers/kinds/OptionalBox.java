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
package com.mojang.datafixers.kinds;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class OptionalBox<T> implements App<OptionalBox.Mu, T> {

    private final Optional<T> value;

    private OptionalBox(final Optional<T> value) {
        this.value = value;
    }

    public static <T> Optional<T> unbox(final App<Mu, T> box) {
        return ((OptionalBox<T>) box).value;
    }

    public static <T> OptionalBox<T> create(final Optional<T> value) {
        return new OptionalBox<>(value);
    }

    public enum Instance implements Applicative<Mu, Instance.Mu>, Traversable<Mu, Instance.Mu> {
        INSTANCE;

        @Override
        public <T, R> App<OptionalBox.Mu, R> map(final Function<? super T, ? extends R> func,
                                                 final App<OptionalBox.Mu, T> ts) {
            return create(OptionalBox.unbox(ts).map(func));
        }

        @Override
        public <A> App<OptionalBox.Mu, A> point(final A a) {
            return create(Optional.of(a));
        }

        @Override
        public <A, R> Function<App<OptionalBox.Mu, A>, App<OptionalBox.Mu, R>> lift1(final App<OptionalBox.Mu, Function<A, R>> function) {
            return a -> create(OptionalBox.unbox(function)
                .flatMap(f -> OptionalBox.unbox(a).map(f)));
        }

        @Override
        public <A, B, R> BiFunction<App<OptionalBox.Mu, A>, App<OptionalBox.Mu, B>, App<OptionalBox.Mu, R>> lift2(
            final App<OptionalBox.Mu, BiFunction<A, B, R>> function) {
            return (a, b) -> create(OptionalBox.unbox(function)
                .flatMap(f -> OptionalBox.unbox(a)
                    .flatMap(av -> OptionalBox.unbox(b).map(bv -> f.apply(av, bv)))));
        }

        @Override
        public <F extends K1, A, B> App<F, App<OptionalBox.Mu, B>> traverse(final Applicative<F, ?> applicative,
                                                                            final Function<A, App<F, B>> function,
                                                                            final App<OptionalBox.Mu, A> input) {
            final Optional<App<F, B>> traversed = unbox(input).map(function);
            if (traversed.isPresent()) {
                return applicative.map(b -> OptionalBox.create(Optional.of(b)), traversed.get());
            }
            return applicative.point(OptionalBox.create(Optional.empty()));
        }

        public static final class Mu implements Applicative.Mu, Traversable.Mu {

        }
    }

    public static final class Mu implements K1 {

    }

}
