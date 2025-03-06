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
package com.nayrid.serialization;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@NullMarked
public class NbtOpsTests {

    private final TestRecord record = new TestRecord(
        (byte) 1,
        (short) 6,
        69,
        495423905248905L,
        3.14f,
        Math.PI,
        true,
        "Hello World!",
        Map.of("Hello", 1),
        List.of(Math.PI, Math.E),
        List.of("Hello", "World")
    );

    @Test
    void test() {
        final CompoundBinaryTag tag = (CompoundBinaryTag) TestRecord.CODEC.encodeStart(NbtOps.INSTANCE, record).getOrThrow();

        final TestRecord parsed = TestRecord.CODEC.parse(NbtOps.INSTANCE, tag).getOrThrow();

        assertEquals(record, parsed);
    }

    record TestRecord(
        byte byteValue,
        short shortValue,
        int intValue,
        long longValue,
        float floatValue,
        double doubleValue,
        boolean booleanValue,
        String stringValue,
        Map<String, Integer> stringIntegerMapValue,
        List<Double> listOfDoublesValue,
        List<String> listOfStringsValue
    ) {

        public static final Codec<TestRecord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE.fieldOf("byte").forGetter(TestRecord::byteValue),
            Codec.SHORT.fieldOf("short").forGetter(TestRecord::shortValue),
            Codec.INT.fieldOf("int").forGetter(TestRecord::intValue),
            Codec.LONG.fieldOf("long").forGetter(TestRecord::longValue),
            Codec.FLOAT.fieldOf("float").forGetter(TestRecord::floatValue),
            Codec.DOUBLE.fieldOf("double").forGetter(TestRecord::doubleValue),
            Codec.BOOL.fieldOf("boolean").forGetter(TestRecord::booleanValue),
            Codec.STRING.fieldOf("string").forGetter(TestRecord::stringValue),
            Codec.unboundedMap(Codec.STRING, Codec.INT).fieldOf("map").forGetter(TestRecord::stringIntegerMapValue),
            Codec.DOUBLE.listOf().fieldOf("double_list").forGetter(TestRecord::listOfDoublesValue),
            Codec.STRING.listOf().fieldOf("string_list").forGetter(TestRecord::listOfStringsValue)
        ).apply(instance, TestRecord::new));

    }

}
