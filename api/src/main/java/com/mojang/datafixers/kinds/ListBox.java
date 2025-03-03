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

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A container wrapping an ordered sequence of values.
 *
 * @param <T> The type of values contained in the list.
 * @apiNote This class represents the <em>list functor</em>.
 * @see ListBox.Instance
 */
public final class ListBox<T> implements App<ListBox.Mu, T> {

    private final List<T> value;

    private ListBox(final List<T> value) {
        this.value = value;
    }

    /**
     * Thunk method that casts the given applied {@link ListBox.Mu} to a {@link ListBox}.
     *
     * @param box The boxed {@link ListBox}.
     * @param <T> The type of values contained in the list.
     * @return The unboxed list.
     */
    public static <T> List<T> unbox(final App<Mu, T> box) {
        return ((ListBox<T>) box).value;
    }

    /**
     * Creates a {@link ListBox} containing the same elements as the given {@link List}.
     *
     * @param value The list of values.
     * @param <T>   The type of values.
     * @return A {@link ListBox} containing the same values as {@code value}.
     */
    public static <T> ListBox<T> create(final List<T> value) {
        return new ListBox<>(value);
    }

    /**
     * Applies an operation {@code function} to each element of {@code input}, then returns a container holding a
     * list of the outputs.
     *
     * @param applicative The {@link Applicative} instance that defines the behavior of {@code F}.
     * @param function    The operation to apply to each element.
     * @param input       The input elements.
     * @param <F>         The output container type.
     * @param <A>         The type of the input elements.
     * @param <B>         The type of the output elements.
     * @return A container of the output values.
     * @apiNote This method implements the <em>traversable operator</em> for the type {@link ListBox}.
     * @see Instance#traverse(Applicative, Function, App)
     */
    public static <F extends K1, A, B> App<F, List<B>> traverse(final Applicative<F, ?> applicative,
                                                                final Function<A, App<F, B>> function,
                                                                final List<A> input) {
        return applicative.map(ListBox::unbox,
            Instance.INSTANCE.traverse(applicative, function, create(input))
        );
    }

    /**
     * Transforms a list of some container to a container of lists.
     *
     * @param applicative The {@link Applicative} instance that defines the behavior of {@code F}.
     * @param input       The list of container.
     * @param <F>         The container type.
     * @param <A>         The inner element type.
     * @return A container of list.
     * @see Traversable#flip(Applicative, App)
     */
    public static <F extends K1, A> App<F, List<A>> flip(final Applicative<F, ?> applicative,
                                                         final List<App<F, A>> input) {
        return applicative.map(ListBox::unbox, Instance.INSTANCE.flip(applicative, create(input)));
    }

    /**
     * The {@link Traversable} type class instance for {@link ListBox}.
     */
    public enum Instance implements Traversable<Mu, Instance.Mu> {
        INSTANCE;

        @Override
        public <T, R> App<ListBox.Mu, R> map(final Function<? super T, ? extends R> func,
                                             final App<ListBox.Mu, T> ts) {
            return create(ListBox.unbox(ts).stream().map(func).collect(Collectors.toList()));
        }

        @Override
        public <F extends K1, A, B> App<F, App<ListBox.Mu, B>> traverse(final Applicative<F, ?> applicative,
                                                                        final Function<A, App<F, B>> function,
                                                                        final App<ListBox.Mu, A> input) {
            final List<? extends A> list = unbox(input);

            App<F, ImmutableList.Builder<B>> result = applicative.point(ImmutableList.builder());

            for (final A a : list) {
                final App<F, B> fb = function.apply(a);
                result = applicative.ap2(applicative.point(ImmutableList.Builder::add), result, fb);
            }

            return applicative.map(b -> create(b.build()), result);
        }

        /**
         * The witness type of {@link Instance}.
         */
        public static final class Mu implements Traversable.Mu {

        }
    }

    /**
     * The witness type of {@link ListBox}.
     */
    public static final class Mu implements K1 {

    }

}
