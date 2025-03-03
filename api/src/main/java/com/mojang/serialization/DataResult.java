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
package com.mojang.serialization;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.kinds.K1;
import com.mojang.datafixers.util.Function3;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Represents either a successful operation, or a partial operation with an error message and a partial result (if available).
 * Also stores an additional lifecycle marker (monoidal).
 *
 * @param <R> The type of the wrapped result.
 */
public sealed interface DataResult<R> extends App<DataResult.Mu, R> permits DataResult.Success, DataResult.Error {

    /**
     * Thunk method to cast the applied {@link DataResult} type constructor to the type {@link DataResult}.
     */
    static <R> DataResult<R> unbox(final App<Mu, R> box) {
        return (DataResult<R>) box;
    }

    /**
     * Creates a successful {@link DataResult} with the given result value. The lifecycle of the result
     * is {@linkplain Lifecycle#experimental() experimental}.
     *
     * @param result The result value.
     * @param <R>    The type of the result value.
     * @return A successful {@link DataResult}.
     * @see #success(Object, Lifecycle)
     */
    static <R> DataResult<R> success(final R result) {
        return success(result, Lifecycle.experimental());
    }

    /**
     * Creates an error {@link DataResult} with the given message and partial result value.
     *
     * @param message       The error message.
     * @param partialResult The partial or fallback result value.
     * @param <R>           The type of the result value.
     * @return An error {@link DataResult}.
     * @see #error(Supplier, Object, Lifecycle)
     */
    static <R> DataResult<R> error(final Supplier<String> message, final R partialResult) {
        return error(message, partialResult, Lifecycle.experimental());
    }

    /**
     * Creates an error {@link DataResult} with the given message and no partial result.
     *
     * @param message The error message.
     * @param <R>     The expected type of the result.
     * @return An error {@link DataResult}.
     * @see #error(Supplier, Object)
     */
    static <R> DataResult<R> error(final Supplier<String> message) {
        return error(message, Lifecycle.experimental());
    }

    /**
     * Creates a successful {@link DataResult} with the given result value and lifecycle.
     *
     * @param result       The result value.
     * @param lifecycle    The lifecycle to use.
     * @param <R>          The type of the result value.
     * @return A successful {@link DataResult}.
     * @see #success(Object, Lifecycle)
     */
    static <R> DataResult<R> success(final R result, final Lifecycle lifecycle) {
        return new Success<>(result, lifecycle);
    }

    /**
     * Creates an error {@link DataResult} with the given message, partial result value, and lifecycle.
     *
     * @param message       The error message.
     * @param partialResult The partial or fallback result value.
     * @param lifecycle     The lifecycle to use.
     * @param <R>           The type of the result value.
     * @return An error {@link DataResult}.
     */
    static <R> DataResult<R> error(final Supplier<String> message,
                                   final R partialResult,
                                   final Lifecycle lifecycle) {
        return new Error<>(message, Optional.of(partialResult), lifecycle);
    }

    /**
     * Creates an error {@link DataResult} with the given message and lifecycle.
     *
     * @param message       The error message.
     * @param lifecycle     The lifecycle to use.
     * @param <R>           The type of the result value.
     * @return An error {@link DataResult}.
     */
    static <R> DataResult<R> error(final Supplier<String> message, final Lifecycle lifecycle) {
        return new Error<>(message, Optional.empty(), lifecycle);
    }

    /**
     * Converts a partial function into a function that produces a {@link DataResult}. If the partial function
     * returns {@code null}, then the returned function returns an error {@link DataResult}, otherwise a successful
     * {@link DataResult}.
     *
     * @param partialGet  The partial function.
     * @param errorPrefix The error string to use if {@code partialGet} returns {@code null}.
     * @param <K>         The argument type of the partial function.
     * @param <V>         The result type of the partial function.
     * @return A function that wraps the result of the partial function in a {@link DataResult}.
     * @see Optional
     */
    static <K, V> Function<K, DataResult<V>> partialGet(final Function<K, V> partialGet,
                                                        final Supplier<String> errorPrefix) {
        return name -> Optional.ofNullable(partialGet.apply(name))
            .map(DataResult::success)
            .orElseGet(() -> error(() -> errorPrefix.get() + name));
    }

    /**
     * Returns the <em>applicative type instance</em> for the type {@link DataResult}.
     *
     * @see Instance
     */
    static Instance instance() {
        return Instance.INSTANCE;
    }

    static String appendMessages(final String first, final String second) {
        return first + "; " + second;
    }

    /**
     * Returns the successful result, if one is present.
     *
     * @see #error()
     */
    Optional<R> result();

    /**
     * Returns the error if one is present.
     *
     * @see #result()
     */
    Optional<DataResult.Error<R>> error();

    /**
     * Returns the lifecycle used in this {@link DataResult}.
     */
    Lifecycle lifecycle();

    boolean hasResultOrPartial();

    /**
     * Returns any result, if one is present. If this result is an error, the partial result, if any, is returned.
     *
     * @param onError A callback to run on error. It receives the error message.
     * @return The result or partial result, if present.
     */
    Optional<R> resultOrPartial(Consumer<String> onError);

    Optional<R> resultOrPartial();

    <E extends Throwable> R getOrThrow(Function<String, E> exceptionSupplier) throws E;

    <E extends Throwable> R getPartialOrThrow(Function<String, E> exceptionSupplier) throws E;

    default R getOrThrow() {
        return getOrThrow(IllegalStateException::new);
    }

    default R getPartialOrThrow() {
        return getPartialOrThrow(IllegalStateException::new);
    }

    /**
     * Applies a function to the result or partial result. Successes remain successes and errors remain errors.
     *
     * @param function The conversion function.
     * @param <T>      The new result type.
     * @return The converted result.
     * @apiNote This method implements the <em>functor operator</em> for the type {@link DataResult}.
     * @see #flatMap(Function)
     * @see #ap(DataResult)
     */
    <T> DataResult<T> map(Function<? super R, ? extends T> function);

    <T> T mapOrElse(Function<? super R, ? extends T> successFunction,
                    Function<? super Error<R>, ? extends T> errorFunction);

    DataResult<R> ifSuccess(Consumer<? super R> ifSuccess);

    DataResult<R> ifError(Consumer<? super Error<R>> ifError);

    DataResult<R> promotePartial(Consumer<String> onError);

    /**
     * Applies the function to either full or partial result, in case of partial concatenates
     * errors.
     */
    <R2> DataResult<R2> flatMap(Function<? super R, ? extends DataResult<R2>> function);

    /**
     * Applies a function which is itself wrapped in a {@link DataResult} to the result or partial result.
     *
     * <p>If either {@code this} or {@code functionResult} are errors, then the return value is also an error.
     * Also, if either {@code this} or {@code functionResult} does not contain a result, then the
     * return value also does not contain a result.
     *
     * @param functionResult The function to potentially apply.
     * @param <R2>           The new result type.
     * @return The result of {@code functionResult} applies to {@code this}.
     * @apiNote This method implements the <em>applicative operator</em> in the context of {@link DataResult}.
     * @see #map(Function)
     * @see Applicative
     * @see <a href="https://medium.com/@lettier/your-easy-guide-to-monads-applicatives-functors-862048d61610">Functors, Applicatives, and Monads</a>
     */
    <R2> DataResult<R2> ap(DataResult<Function<R, R2>> functionResult);

    /**
     * Combines this result with another result using the given function.
     *
     * @param function The function to apply to the wrapped values of this and the given result.
     * @param second   The second {@link DataResult} value.
     * @param <R2>     The type of the second result value.
     * @param <S>      The type of the output result value.
     * @return A {@link DataResult} wrapping the application of the {@code function} to the provided results.
     * @apiNote This is an arity-2 specialization for {@link #map(Function)}.
     * @see #map(Function)
     */
    default <R2, S> DataResult<S> apply2(final BiFunction<R, R2, S> function,
                                         final DataResult<R2> second) {
        return unbox(instance().apply2(function, this, second));
    }

    /**
     * Combines this result with another result using the given function, under the "stable" lifecycle.
     *
     * <p>This method is equivalent to {@link #apply2(BiFunction, DataResult)}, except that the stable lifecycle
     * is always used.
     *
     * @param function The function to apply to the wrapped values of this and the given result.
     * @param second   The second {@link DataResult} value.
     * @param <R2>     The type of the second result value.
     * @param <S>      The type of the output result value.
     * @return A {@link DataResult} wrapping the application of the {@code function} to the provided results.
     * @see #apply2(BiFunction, DataResult)
     * @see #map(Function)
     */
    default <R2, S> DataResult<S> apply2stable(final BiFunction<R, R2, S> function,
                                               final DataResult<R2> second) {
        final Applicative<DataResult.Mu, DataResult.Instance.Mu> instance = instance();
        final DataResult<BiFunction<R, R2, S>> f = unbox(instance.point(function)).setLifecycle(
            Lifecycle.stable());
        return unbox(instance.ap2(f, this, second));
    }

    /**
     * Combines this result with two other results using the given function.
     *
     * @param function The function to apply to the wrapped values of this and the given results.
     * @param second   The second {@link DataResult} value.
     * @param third    The third {@link DataResult} value.
     * @param <R2>     The type of the second result value.
     * @param <R3>     The type of the third result value.
     * @param <S>      The type of the output result value.
     * @return A {@link DataResult} wrapping the application of the {@code function} to the provided results.
     * @apiNote This is an arity-3 specialization for {@link #map(Function)}.
     * @see #map(Function)
     */
    default <R2, R3, S> DataResult<S> apply3(final Function3<R, R2, R3, S> function,
                                             final DataResult<R2> second,
                                             final DataResult<R3> third) {
        return unbox(instance().apply3(function, this, second, third));
    }

    /**
     * If this result is an error, replaces any partial result with the supplied partial result.
     *
     * @param partial A {@link Supplier} of partial results.
     * @return A {@link DataResult} equivalent to {@code this} with the partial result set appropriately.
     */
    DataResult<R> setPartial(Supplier<R> partial);

    /**
     * If this result is an error, replaces any partial result with the given partial result.
     *
     * @param partial A partial result.
     * @return A {@link DataResult} equivalent to {@code this} with the partial result set appropriately.
     */
    DataResult<R> setPartial(R partial);

    /**
     * Applies the given function to the error message contained in this result.
     *
     * @param function The function to apply.
     * @return A {@link DataResult} equivalent to {@code this}, but with any error message replaced.
     */
    DataResult<R> mapError(UnaryOperator<String> function);

    /**
     * Returns a {@link DataResult} with the same value as this result, but with the provided lifecycle.
     */
    DataResult<R> setLifecycle(Lifecycle lifecycle);

    /**
     * Returns a {@link DataResult} with the same value as this result, but with the provided lifecycle added to
     * this result's lifecycle.
     *
     * @see Lifecycle#add(Lifecycle)
     */
    default DataResult<R> addLifecycle(final Lifecycle lifecycle) {
        return setLifecycle(lifecycle().add(lifecycle));
    }

    boolean isSuccess();

    default boolean isError() {
        return !isSuccess();
    }

    /**
     * The <em>applicative functor</em> type instance for the type constructor {@link DataResult}.
     *
     * @see <a href="https://medium.com/@lettier/your-easy-guide-to-monads-applicatives-functors-862048d61610">Functors, Applicatives, and Monads</a>
     */
    enum Instance implements Applicative<Mu, Instance.Mu> {
        /**
         * The type instance.
         */
        INSTANCE;

        @Override
        public <T, R> App<DataResult.Mu, R> map(final Function<? super T, ? extends R> func,
                                                final App<DataResult.Mu, T> ts) {
            return unbox(ts).map(func);
        }

        @Override
        public <A> App<DataResult.Mu, A> point(final A a) {
            return success(a);
        }

        @Override
        public <A, R> Function<App<DataResult.Mu, A>, App<DataResult.Mu, R>> lift1(final App<DataResult.Mu, Function<A, R>> function) {
            return fa -> ap(function, fa);
        }

        @Override
        public <A, R> App<DataResult.Mu, R> ap(final App<DataResult.Mu, Function<A, R>> func,
                                               final App<DataResult.Mu, A> arg) {
            return unbox(arg).ap(unbox(func));
        }

        @Override
        public <A, B, R> App<DataResult.Mu, R> ap2(final App<DataResult.Mu, BiFunction<A, B, R>> func,
                                                   final App<DataResult.Mu, A> a,
                                                   final App<DataResult.Mu, B> b) {
            final DataResult<BiFunction<A, B, R>> fr = unbox(func);
            final DataResult<A> ra = unbox(a);
            final DataResult<B> rb = unbox(b);

            // for less recursion
            if (fr.result().isPresent() && ra.result().isPresent() && rb.result().isPresent()) {
                return new Success<>(fr.result().get().apply(ra.result().get(), rb.result().get()),
                    fr.lifecycle().add(ra.lifecycle()).add(rb.lifecycle())
                );
            }

            return Applicative.super.ap2(func, a, b);
        }

        @Override
        public <T1, T2, T3, R> App<DataResult.Mu, R> ap3(final App<DataResult.Mu, Function3<T1, T2, T3, R>> func,
                                                         final App<DataResult.Mu, T1> t1,
                                                         final App<DataResult.Mu, T2> t2,
                                                         final App<DataResult.Mu, T3> t3) {
            final DataResult<Function3<T1, T2, T3, R>> fr = unbox(func);
            final DataResult<T1> dr1 = unbox(t1);
            final DataResult<T2> dr2 = unbox(t2);
            final DataResult<T3> dr3 = unbox(t3);

            // for less recursion
            if (fr.result().isPresent() && dr1.result().isPresent() && dr2.result()
                .isPresent() && dr3.result().isPresent()) {
                return new Success<>(fr.result()
                    .get()
                    .apply(dr1.result().get(), dr2.result().get(), dr3.result().get()),
                    fr.lifecycle().add(dr1.lifecycle()).add(dr2.lifecycle()).add(dr3.lifecycle())
                );
            }

            return Applicative.super.ap3(func, t1, t2, t3);
        }

        /**
         * A marker class representing the meta-type constructor {@link DataResult.Instance}.
         */
        public static final class Mu implements Applicative.Mu {

        }
    }

    /**
     * A marker interface representing the type constructor {@link DataResult}.
     */
    final class Mu implements K1 {

    }

    record Success<R>(R value,
                      Lifecycle lifecycle) implements DataResult<R> {

        @Override
        public Optional<R> result() {
            return Optional.of(value);
        }

        @Override
        public Optional<Error<R>> error() {
            return Optional.empty();
        }

        @Override
        public boolean hasResultOrPartial() {
            return true;
        }

        @Override
        public Optional<R> resultOrPartial(final Consumer<String> onError) {
            return Optional.of(value);
        }

        @Override
        public Optional<R> resultOrPartial() {
            return Optional.of(value);
        }

        @Override
        public <E extends Throwable> R getOrThrow(final Function<String, E> exceptionSupplier) throws E {
            return value;
        }

        @Override
        public <E extends Throwable> R getPartialOrThrow(final Function<String, E> exceptionSupplier) throws E {
            return value;
        }

        @Override
        public <T> DataResult<T> map(final Function<? super R, ? extends T> function) {
            return new Success<>(function.apply(value), lifecycle);
        }

        @Override
        public <T> T mapOrElse(final Function<? super R, ? extends T> successFunction,
                               final Function<? super Error<R>, ? extends T> errorFunction) {
            return successFunction.apply(value);
        }

        @Override
        public DataResult<R> ifSuccess(final Consumer<? super R> ifSuccess) {
            ifSuccess.accept(value);
            return this;
        }

        @Override
        public DataResult<R> ifError(final Consumer<? super Error<R>> ifError) {
            return this;
        }

        /**
         * Promotes an error with a partial result to a success. If this is a success, it is returned
         * unchanged.
         *
         * @param onError A callback to run on error. It is passed the error string.
         * @return A success containing the result or partial result, or an error containing no result.
         */
        @Override
        public DataResult<R> promotePartial(final Consumer<String> onError) {
            return this;
        }

        /**
         * Applies the function to either full or partial result, in case of partial concatenates errors.
         *
         * @param function The partial conversion function.
         * @param <R2>     The new result type.
         * @return The converted result.
         * @apiNote This method implements the <em>monad operator</em> in the context of {@link DataResult}.
         * @see #map(Function)
         */
        @Override
        public <R2> DataResult<R2> flatMap(final Function<? super R, ? extends DataResult<R2>> function) {
            return function.apply(value).addLifecycle(lifecycle);
        }

        /**
         * Applies a function which is itself wrapped in a {@link DataResult} to the result or partial result.
         *
         * <p>If either {@code this} or {@code functionResult} are errors, then the return value is also an error.
         * Also, if either {@code this} or {@code functionResult} does not contain a result, then the
         * return value also does not contain a result.
         *
         * @param functionResult The function to potentially apply.
         * @param <R2>           The new result type.
         * @return The result of {@code functionResult} applies to {@code this}.
         * @apiNote This method implements the <em>applicative operator</em> in the context of {@link DataResult}.
         * @see #map(Function)
         * @see Applicative
         * @see <a href="https://medium.com/@lettier/your-easy-guide-to-monads-applicatives-functors-862048d61610">Functors, Applicatives, and Monads</a>
         */
        @Override
        public <R2> DataResult<R2> ap(final DataResult<Function<R, R2>> functionResult) {
            final Lifecycle combinedLifecycle = lifecycle.add(functionResult.lifecycle());
            if (functionResult instanceof final Success<Function<R, R2>> funcSuccess) {
                return new Success<>(funcSuccess.value.apply(value), combinedLifecycle);
            } else if (functionResult instanceof final Error<Function<R, R2>> funcError) {
                return new Error<>(funcError.messageSupplier,
                    funcError.partialValue.map(f -> f.apply(value)),
                    combinedLifecycle
                );
            } else {
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public DataResult<R> setPartial(final Supplier<R> partial) {
            return this;
        }

        @Override
        public DataResult<R> setPartial(final R partial) {
            return this;
        }

        @Override
        public DataResult<R> mapError(final UnaryOperator<String> function) {
            return this;
        }

        @Override
        public DataResult<R> setLifecycle(final Lifecycle lifecycle) {
            if (this.lifecycle.equals(lifecycle)) {
                return this;
            }
            return new Success<>(value, lifecycle);
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public String toString() {
            return "DataResult.Success[" + value + "]";
        }

    }

    record Error<R>(Supplier<String> messageSupplier,
                    Optional<R> partialValue,
                    Lifecycle lifecycle) implements DataResult<R> {

        public String message() {
            return messageSupplier.get();
        }

        @Override
        public Optional<R> result() {
            return Optional.empty();
        }

        @Override
        public Optional<Error<R>> error() {
            return Optional.of(this);
        }

        @Override
        public boolean hasResultOrPartial() {
            return partialValue.isPresent();
        }

        @Override
        public Optional<R> resultOrPartial(final Consumer<String> onError) {
            onError.accept(messageSupplier.get());
            return partialValue;
        }

        @Override
        public Optional<R> resultOrPartial() {
            return partialValue;
        }

        @Override
        public <E extends Throwable> R getOrThrow(final Function<String, E> exceptionSupplier) throws E {
            throw exceptionSupplier.apply(message());
        }

        @Override
        public <E extends Throwable> R getPartialOrThrow(final Function<String, E> exceptionSupplier) throws E {
            if (partialValue.isPresent()) {
                return partialValue.get();
            }
            throw exceptionSupplier.apply(message());
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> Error<T> map(final Function<? super R, ? extends T> function) {
            if (partialValue.isEmpty()) {
                return (Error<T>) this;
            }
            return new Error<>(messageSupplier, partialValue.map(function), lifecycle);
        }

        @Override
        public <T> T mapOrElse(final Function<? super R, ? extends T> successFunction,
                               final Function<? super Error<R>, ? extends T> errorFunction) {
            return errorFunction.apply(this);
        }

        @Override
        public DataResult<R> ifSuccess(final Consumer<? super R> ifSuccess) {
            return this;
        }

        @Override
        public DataResult<R> ifError(final Consumer<? super Error<R>> ifError) {
            ifError.accept(this);
            return this;
        }

        @Override
        public DataResult<R> promotePartial(final Consumer<String> onError) {
            onError.accept(messageSupplier.get());
            return partialValue.<DataResult<R>>map(value -> new Success<>(value, lifecycle))
                .orElse(this);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <R2> Error<R2> flatMap(final Function<? super R, ? extends DataResult<R2>> function) {
            if (partialValue.isEmpty()) {
                return (Error<R2>) this;
            }
            final DataResult<R2> second = function.apply(partialValue.get());
            final Lifecycle combinedLifecycle = lifecycle.add(second.lifecycle());
            if (second instanceof final Success<R2> secondSuccess) {
                return new Error<>(messageSupplier,
                    Optional.of(secondSuccess.value),
                    combinedLifecycle
                );
            } else if (second instanceof final Error<R2> secondError) {
                return new Error<>(() -> appendMessages(messageSupplier.get(),
                    secondError.messageSupplier.get()
                ), secondError.partialValue, combinedLifecycle);
            } else {
                // TODO: Replace with record pattern matching in Java 21
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public <R2> Error<R2> ap(final DataResult<Function<R, R2>> functionResult) {
            final Lifecycle combinedLifecycle = lifecycle.add(functionResult.lifecycle());
            if (functionResult instanceof final Success<Function<R, R2>> func) {
                return new Error<>(messageSupplier,
                    partialValue.map(func.value),
                    combinedLifecycle
                );
            } else if (functionResult instanceof final Error<Function<R, R2>> funcError) {
                return new Error<>(() -> appendMessages(messageSupplier.get(),
                    funcError.messageSupplier.get()
                ),
                    partialValue.flatMap(a -> funcError.partialValue.map(f -> f.apply(a))),
                    combinedLifecycle
                );
            } else {
                // TODO: Replace with record pattern matching in Java 21
                throw new UnsupportedOperationException();
            }
        }

        @Override
        public Error<R> setPartial(final Supplier<R> partial) {
            return setPartial(partial.get());
        }

        @Override
        public Error<R> setPartial(final R partial) {
            return new Error<>(messageSupplier, Optional.of(partial), lifecycle);
        }

        @Override
        public Error<R> mapError(final UnaryOperator<String> function) {
            return new Error<>(() -> function.apply(messageSupplier.get()),
                partialValue,
                lifecycle
            );
        }

        @Override
        public Error<R> setLifecycle(final Lifecycle lifecycle) {
            if (this.lifecycle.equals(lifecycle)) {
                return this;
            }
            return new Error<>(messageSupplier, partialValue, lifecycle);
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public String toString() {
            return "DataResult.Error['" + message() + "'" + partialValue.map(value -> ": " + value)
                .orElse("") + "]";
        }

    }

}
