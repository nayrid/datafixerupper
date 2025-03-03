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

/**
 * A marker defining the stability of a given result. {@linkplain #experimental() Experimental} results are may changed
 * incompatibly in the future and {@linkplain #deprecated(int) deprecated} results may be removed in the future.
 *
 * @see Codec#withLifecycle(Lifecycle)
 * @see DataResult#setLifecycle(Lifecycle)
 */
public class Lifecycle {

    private static final Lifecycle STABLE = new Lifecycle() {
        @Override
        public String toString() {
            return "Stable";
        }
    };
    private static final Lifecycle EXPERIMENTAL = new Lifecycle() {
        @Override
        public String toString() {
            return "Experimental";
        }
    };

    private Lifecycle() {
    }

    /**
     * Returns the experimental lifecycle.
     */
    public static Lifecycle experimental() {
        return EXPERIMENTAL;
    }

    /**
     * Returns the stable lifecycle.
     */
    public static Lifecycle stable() {
        return STABLE;
    }

    /**
     * Returns a deprecated lifecycle for the given version.
     *
     * @param since The version the lifecycle is for.
     * @see Deprecated
     */
    public static Lifecycle deprecated(final int since) {
        return new Deprecated(since);
    }

    /**
     * Combines this lifecycle with another lifecycle. The lifecycle with the highest potential for breakage
     * (either {@code this} or {@code other}) is returned.
     *
     * <p>For example, the following lifecycles are ordered according to breakage potential.
     * <ol>
     *     <li>{@code Experimental}</li>
     *     <li>{@code Deprecated(since = 1)}</li>
     *     <li>{@code Deprecated(since = 2)}</li>
     *     <li>{@code Stable}</li>
     * </ol>
     *
     * @param other The lifecycle to compare this lifecycle against.
     * @return The lifecycle with the highest "breakage potential".
     */
    public Lifecycle add(final Lifecycle other) {
        if (this == EXPERIMENTAL || other == EXPERIMENTAL) {
            return EXPERIMENTAL;
        }
        if (this instanceof Deprecated) {
            if (other instanceof Deprecated && ((Deprecated) other).since < ((Deprecated) this).since) {
                return other;
            }
            return this;
        }
        if (other instanceof Deprecated) {
            return other;
        }
        return STABLE;
    }

    /**
     * A class for {@linkplain Lifecycle#deprecated(int) deprecated} lifecycles.
     *
     * @see Lifecycle#deprecated(int)
     */
    public static final class Deprecated extends Lifecycle {

        private final int since;

        /**
         * Constructs a deprecated lifecycle with the given starting version.
         *
         * @param since The version that the result was deprecated in.
         */
        public Deprecated(final int since) {
            this.since = since;
        }

        /**
         * Returns the version since the result was deprecated.
         */
        public int since() {
            return since;
        }

    }

}
