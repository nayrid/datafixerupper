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

import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.RecursivePoint;
import java.util.BitSet;
import java.util.Objects;

public record RewriteResult<A, B>(View<A, B> view,
                                  BitSet recData) {

    public static <A, B> RewriteResult<A, B> create(final View<A, B> view, final BitSet recData) {
        return new RewriteResult<>(view, recData);
    }

    public static <A> RewriteResult<A, A> nop(final Type<A> type) {
        return new RewriteResult<>(View.nopView(type), new BitSet());
    }

    public <C> RewriteResult<C, B> compose(final RewriteResult<C, A> that) {
        final BitSet newData;
        if (view.type() instanceof RecursivePoint.RecursivePointType<?> && that.view.type() instanceof RecursivePoint.RecursivePointType<?>) {
            // same family, merge results - not exactly accurate, but should be good enough
            newData = (BitSet) recData.clone();
            newData.or(that.recData);
        } else {
            newData = recData;
        }
        return create(view.compose(that.view), newData);
    }

    @Override
    public String toString() {
        return "RR[" + view + "]";
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final RewriteResult<?, ?> that = (RewriteResult<?, ?>) o;
        return Objects.equals(view, that.view);
    }

    @Override
    public int hashCode() {
        return view.hashCode();
    }

}
