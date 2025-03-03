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

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A container wrapping a single value.
 *
 * <p>The main feature of this class is its {@linkplain IdF.Instance implementation} of
 * {@linkplain Applicative applicative functor} operations. It's useful in cases where a functor is required
 * but there is no existing functor that would be used.
 *
 * @param <A> The type of the contained value.
 * @apiNote This class represents the <em>identity functor</em>, hence the name {@code IdF}.
 * @see <a href="https://en.wikipedia.org/wiki/Monad_(functional_programming)#Identity_monad">Identity monads,
 * a similar concept for the monad type class</a>
 */
public final class IdF<A> implements App<IdF.Mu, A> {

    /**
     * The value contained in this {@code IdF}.
     */
    private final A value;

    IdF(final A value) {
        this.value = value;
    }

    /**
     * Gets the value stored in an {@link App} representing an {@code IdF} instance.
     *
     * @param box The boxed {@code IdF} instance.
     * @param <A> The type of the contained value.
     * @throws ClassCastException If the {@code box} is not an instance of {@code IdF}.
     */
    public static <A> A get(final App<Mu, A> box) {
        return ((IdF<A>) box).value;
    }

    /**
     * Creates an {@code IdF} container for a value.
     *
     * @param a   The value that will be stored.
     * @param <A> The type of the contained value.
     * @return The created container.
     */
    public static <A> IdF<A> create(final A a) {
        return new IdF<>(a);
    }

    /**
     * Gets the value of this container.
     *
     * @return The value.
     */
    public A value() {
        return value;
    }

    /**
     * An instance of {@link Functor} and {@link Applicative} for {@link IdF}.
     */
    public enum Instance implements Functor<Mu, Instance.Mu>, Applicative<Mu, Instance.Mu> {
        /**
         * The singleton instance of this type.
         */
        INSTANCE;

        @Override
        public <T, R> App<IdF.Mu, R> map(final Function<? super T, ? extends R> func,
                                         final App<IdF.Mu, T> ts) {
            final IdF<T> idF = (IdF<T>) ts;
            return new IdF<>(func.apply(idF.value));
        }

        @Override
        public <A> App<IdF.Mu, A> point(final A a) {
            return create(a);
        }

        @Override
        public <A, R> Function<App<IdF.Mu, A>, App<IdF.Mu, R>> lift1(final App<IdF.Mu, Function<A, R>> function) {
            return a -> create(get(function).apply(get(a)));
        }

        @Override
        public <A, B, R> BiFunction<App<IdF.Mu, A>, App<IdF.Mu, B>, App<IdF.Mu, R>> lift2(final App<IdF.Mu, BiFunction<A, B, R>> function) {
            return (a, b) -> create(get(function).apply(get(a), get(b)));
        }

        /**
         * The witness type of {@code IdF.Instance}.
         */
        public static final class Mu implements Functor.Mu, Applicative.Mu {

        }
    }

    /**
     * The witness type of {@link IdF}.
     */
    public static final class Mu implements K1 {

    }

}
