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

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import java.util.Objects;

/**
 * Unchecked cast if name matches
 */
public final class InjTagged<K, A, B> implements Prism<Pair<K, ?>, Pair<K, ?>, A, B> {

    private final K key;

    public InjTagged(final K key) {
        this.key = key;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Either<Pair<K, ?>, A> match(final Pair<K, ?> pair) {
        return Objects.equals(key,
            pair.first()
        ) ? Either.right((A) pair.second()) : Either.left(pair);
    }

    @Override
    public Pair<K, ?> build(final B b) {
        return Pair.of(key, b);
    }

    @Override
    public String toString() {
        return "inj[" + key + "]";
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof InjTagged<?, ?, ?> && Objects.equals(((InjTagged<?, ?, ?>) obj).key,
            key
        );
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }

}
