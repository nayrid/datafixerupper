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
package com.mojang.datafixers.functions;

import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DynamicOps;
import java.util.Optional;
import java.util.function.Function;
import org.jspecify.annotations.Nullable;

public abstract class PointFree<T> {

    private volatile boolean initialized;
    private @Nullable Function<DynamicOps<?>, T> value;

    public static String indent(final int level) {
        return " ".repeat(level);
    }

    @SuppressWarnings("ConstantConditions")
    public Function<DynamicOps<?>, T> evalCached() {
        if (!initialized) {
            synchronized (this) {
                if (!initialized) {
                    value = eval();
                    initialized = true;
                }
            }
        }
        return value;
    }

    public abstract Type<T> type();

    public abstract Function<DynamicOps<?>, T> eval();

    Optional<? extends PointFree<T>> all(final PointFreeRule rule) {
        return Optional.of(this);
    }

    Optional<? extends PointFree<T>> one(final PointFreeRule rule) {
        return Optional.empty();
    }

    @Override
    public final String toString() {
        return toString(0);
    }

    public abstract String toString(int level);

}
