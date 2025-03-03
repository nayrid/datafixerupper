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

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * A definer or acceptor of serialized keys.
 *
 * <p>Types implementing this interface define a set of valid keys. Typically, these keys are serialized to or
 * extracted from some serialized object.
 *
 * @see MapCodec
 */
public interface Keyable {

    /**
     * Returns a {@link Keyable} that defines the keys supplied by the argument.
     *
     * <p>The supplier must return a fresh stream on each invocation. As well, care should be taken that the
     * source backing the returned stream is not modified or otherwise invalidated, because the returned
     * {@link Keyable} does not store a local copy of the keys. <strong>The caller is responsible for making
     * a defensive copy of the backing source, if one is required.</strong>
     *
     * @param keys A supplier of key streams. A fresh stream should be returned on each invocation.
     * @return A {@link Keyable} for the given keys.
     */
    static Keyable forStrings(final Supplier<Stream<String>> keys) {
        return new Keyable() {
            @Override
            public <T> Stream<T> keys(final DynamicOps<T> ops) {
                return keys.get().map(ops::createString);
            }
        };
    }

    /**
     * Returns the set of keys this object defines or accepts, serialized to the provided form.
     *
     * @param ops The {@link DynamicOps} instance defining the serialized form.
     * @param <T> The type of the serialized form.
     * @return The set of keys this object defines.
     * @implSpec The returned stream should be finite and provide distinct elements. The stream, furthermore, should
     *     not already be consumed (that is, a new stream should be created each time this method is called).
     */
    <T> Stream<T> keys(DynamicOps<T> ops);

}
