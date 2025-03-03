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

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Functor;
import com.mojang.datafixers.kinds.K1;
import java.util.function.Function;

interface PStore<I, J, X> extends App<PStore.Mu<I, J>, X> {

    static <I, J, X> PStore<I, J, X> unbox(final App<Mu<I, J>, X> box) {
        return (PStore<I, J, X>) box;
    }

    X peek(final J j);

    I pos();

    final class Mu<I, J> implements K1 {

    }

    final class Instance<I, J> implements Functor<Mu<I, J>, Instance.Mu<I, J>> {

        @Override
        public <T, R> App<PStore.Mu<I, J>, R> map(final Function<? super T, ? extends R> func,
                                                  final App<PStore.Mu<I, J>, T> ts) {
            final PStore<I, J, T> input = PStore.unbox(ts);
            return Optics.pStore(func.compose(input::peek)::apply, input::pos);
        }

        public static final class Mu<I, J> implements Functor.Mu {

        }

    }

}
