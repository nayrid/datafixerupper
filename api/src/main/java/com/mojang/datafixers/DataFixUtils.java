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
package com.mojang.datafixers;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class DataFixUtils {

    // Based on: http://graphics.stanford.edu/~seander/bithacks.html#IntegerLogDeBruijn
    private static final int[] MULTIPLY_DE_BRUIJN_BIT_POSITION = {0, 1, 28, 2, 29, 14, 24, 3, 30, 22, 20, 15, 25, 17, 4, 8, 31, 27, 13, 23, 21, 19, 16, 7, 26, 12, 18, 6, 11, 5, 10, 9};

    private DataFixUtils() {
    }

    // Based on: http://graphics.stanford.edu/~seander/bithacks.html#RoundUpPowerOf2
    public static int smallestEncompassingPowerOfTwo(final int input) {
        int result = input - 1;
        result |= result >> 1;
        result |= result >> 2;
        result |= result >> 4;
        result |= result >> 8;
        result |= result >> 16;
        return result + 1;
    }

    // Based on: http://graphics.stanford.edu/~seander/bithacks.html#DetermineIfPowerOf2
    private static boolean isPowerOfTwo(final int input) {
        return input != 0 && (input & (input - 1)) == 0;
    }

    public static int ceillog2(int input) {
        input = isPowerOfTwo(input) ? input : smallestEncompassingPowerOfTwo(input);
        return MULTIPLY_DE_BRUIJN_BIT_POSITION[(int) (input * 0x077CB531L >> 27) & 0x1F];
    }

    public static <T> T make(final Supplier<T> factory) {
        return factory.get();
    }

    public static <T> T make(final T t, final Consumer<T> consumer) {
        consumer.accept(t);
        return t;
    }

    public static <U> U orElse(final Optional<? extends U> optional, final U other) {
        if (optional.isPresent()) {
            return optional.get();
        }
        return other;
    }

    public static <U> U orElseGet(final Optional<? extends U> optional,
                                  final Supplier<? extends U> other) {
        if (optional.isPresent()) {
            return optional.get();
        }
        return other.get();
    }

    public static <U> Optional<U> or(final Optional<? extends U> optional,
                                     final Supplier<? extends Optional<? extends U>> other) {
        if (optional.isPresent()) {
            return optional.map(u -> u);
        }
        return other.get().map(u -> u);
    }

    public static byte[] toArray(final ByteBuffer input) {
        final byte[] bytes;
        if (input.hasArray()) {
            bytes = input.array();
        } else {
            bytes = new byte[input.capacity()];
            input.get(bytes, 0, bytes.length);
        }
        return bytes;
    }

    public static int makeKey(final int version) {
        return makeKey(version, 0);
    }

    public static int makeKey(final int version, final int subVersion) {
        return version * 10 + subVersion;
    }

    public static int getVersion(final int key) {
        return key / 10;
    }

    public static int getSubVersion(final int key) {
        return key % 10;
    }

    public static <T> UnaryOperator<T> consumerToFunction(final Consumer<T> consumer) {
        return s -> {
            consumer.accept(s);
            return s;
        };
    }

}
