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

import com.mojang.datafixers.util.Pair;

public final class Proj1<F, G, F2> implements Lens<Pair<F, G>, Pair<F2, G>, F, F2> {

    public static final Proj1<?, ?, ?> INSTANCE = new Proj1<>();

    private Proj1() {
    }

    @Override
    public F view(final Pair<F, G> pair) {
        return pair.first();
    }

    @Override
    public Pair<F2, G> update(final F2 newValue, final Pair<F, G> pair) {
        return Pair.of(newValue, pair.second());
    }

    @Override
    public String toString() {
        return "\u03C01";
    }

}
