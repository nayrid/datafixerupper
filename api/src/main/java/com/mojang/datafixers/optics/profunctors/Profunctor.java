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

import com.google.common.reflect.TypeToken;
import com.mojang.datafixers.FunctionType;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.App2;
import com.mojang.datafixers.kinds.K2;
import com.mojang.datafixers.kinds.Kind2;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The profunctor type class defines a method, {@link #dimap(Function, Function)}, which transforms a mapping
 * between types to a mapping between wrapped types.
 *
 * <p>This type class requires that the container {@code P} is contravariant in its first parameter.
 *
 * @param <P>  The container type.
 * @param <Mu> The witness type for this profunctor.
 * @see <a href="https://typeclasses.com/profunctors">Understanding profunctors</a>
 */
public interface Profunctor<P extends K2, Mu extends Profunctor.Mu> extends Kind2<P, Mu> {

    /**
     * Thunk method that casts an applied {@link Profunctor.Mu} to a {@link Profunctor}.
     *
     * @param proofBox The boxed profunctor.
     * @param <P>      The container type.
     * @param <Proof>  The witness type.
     * @return The unboxed profunctor.
     */
    static <P extends K2, Proof extends Profunctor.Mu> Profunctor<P, Proof> unbox(final App<Proof, P> proofBox) {
        return (Profunctor<P, Proof>) proofBox;
    }

    /**
     * Takes a function from the output type {@code C} to the input type {@code A} and a function from the input
     * type {@code B} to the output type {@code D}, and returns a function from a container of {@code A, B} to
     * a container of {@code C, D}.
     *
     * <p>A straightforward example of an implementation of {@code dimap} is the method {@link Function#andThen(Function)}.
     * For functions, the expression
     *
     * <pre><code>
     * dimap(g, h).apply(f)
     * </code></pre>
     *
     * <p>is equivalent to
     *
     * <pre><code>
     * g.andThen(f).andThen(h)
     * </code></pre>
     *
     * @param g   A function from output to input.
     * @param h   A function from input to output.
     * @param <A> The first input type.
     * @param <B> The second input type.
     * @param <C> The first output type.
     * @param <D> The second output type.
     * @return A function from an input container to an output container.
     * @see #dimap(App2, Function, Function)
     */
    <A, B, C, D> FunctionType<App2<P, A, B>, App2<P, C, D>> dimap(final Function<C, A> g,
                                                                  final Function<B, D> h);

    /**
     * Takes an input container, a left mapping from output to input, and a right mapping from input to output,
     * and returns an output container.
     *
     * @param arg The input container.
     * @param g   A function from output to input.
     * @param h   A function from input to output.
     * @param <A> The first input type.
     * @param <B> The second input type.
     * @param <C> The first output type.
     * @param <D> The second output type.
     * @return An output container.
     * @implSpec This method is equivalent to {@code dimap(g, h).apply(arg)}.
     * @see #dimap(Function, Function)
     */
    default <A, B, C, D> App2<P, C, D> dimap(final App2<P, A, B> arg,
                                             final Function<C, A> g,
                                             final Function<B, D> h) {
        return dimap(g, h).apply(arg);
    }

    //<A, B, C, D> FunctionType<App2<P, A, B>, App2<P, C, D>> dimap(final Function<C, A> g, final Function<B, D> h);

    /**
     * Takes an input container, a mapping from output to input, and a mapping from input to output, and returns
     * an output container.
     *
     * @param arg The input container.
     * @param g   A function from output to input.
     * @param h   A function from input to output.
     * @param <A> The first input type.
     * @param <B> The second input type.
     * @param <C> The first output type.
     * @param <D> The second output type.
     * @return An output container.
     * @implSpec This method is equivalent to {@code dimap(arg.get(), g, h)}.
     * @see #dimap(App2, Function, Function)
     * @see #dimap(Function, Function)
     */
    default <A, B, C, D> App2<P, C, D> dimap(final Supplier<App2<P, A, B>> arg,
                                             final Function<C, A> g,
                                             final Function<B, D> h) {
        return dimap(g, h).apply(arg.get());
    }

    /**
     * Maps the first, or left hand, parameter of the given input.
     *
     * @param input The input container.
     * @param g     The mapping function.
     * @param <A>   The first input type.
     * @param <B>   The second input and output type.
     * @param <C>   The first output type.
     * @return A container taking the mapped input and yielding the same output.
     * @see #dimap(App2, Function, Function)
     */
    default <A, B, C> App2<P, C, B> lmap(final App2<P, A, B> input, final Function<C, A> g) {
        return dimap(input, g, Function.identity());
    }

    /**
     * Maps the second, or left hand, parameter of the given input.
     *
     * @param input The input container.
     * @param h     The mapping function.
     * @param <A>   The first input and output type.
     * @param <B>   The second input type.
     * @param <D>   The second output type.
     * @return A container taking the same input and yielding the mapped output.
     * @see #dimap(App2, Function, Function)
     */
    default <A, B, D> App2<P, A, D> rmap(final App2<P, A, B> input, final Function<B, D> h) {
        return dimap(input, Function.identity(), h);
    }

    /**
     * The witness type of a {@link Profunctor}.
     *
     * @see #TYPE_TOKEN
     */
    interface Mu extends Kind2.Mu {

        /**
         * The value representing the witness type {@link Mu}.
         */
        TypeToken<Mu> TYPE_TOKEN = new TypeToken<>() {
        };

    }

}
