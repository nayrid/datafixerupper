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

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import net.kyori.adventure.nbt.BinaryTag;
import net.kyori.adventure.nbt.BinaryTagType;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.ByteArrayBinaryTag;
import net.kyori.adventure.nbt.ByteBinaryTag;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.DoubleBinaryTag;
import net.kyori.adventure.nbt.EndBinaryTag;
import net.kyori.adventure.nbt.FloatBinaryTag;
import net.kyori.adventure.nbt.IntArrayBinaryTag;
import net.kyori.adventure.nbt.IntBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.kyori.adventure.nbt.LongArrayBinaryTag;
import net.kyori.adventure.nbt.LongBinaryTag;
import net.kyori.adventure.nbt.NumberBinaryTag;
import net.kyori.adventure.nbt.ShortBinaryTag;
import net.kyori.adventure.nbt.StringBinaryTag;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * {@link BinaryTag NBT} {@link DynamicOps}.
 */
@NullMarked
public final class NbtOps implements DynamicOps<BinaryTag> {

    public static final NbtOps INSTANCE = new NbtOps();
    private static final String WRAPPER_MARKER = "";

    private NbtOps() {
    }

    @Override
    public BinaryTag empty() {
        return EndBinaryTag.endBinaryTag();
    }

    @Override
    public <U> U convertTo(final DynamicOps<U> outOps, final BinaryTag input) {
        return switch (input.type().id()) {
            case 0 -> outOps.empty();
            case 1 -> outOps.createByte(((ByteBinaryTag) input).value());
            case 2 -> outOps.createShort(((ShortBinaryTag) input).value());
            case 3 -> outOps.createInt(((IntBinaryTag) input).value());
            case 4 -> outOps.createLong(((LongBinaryTag) input).value());
            case 5 -> outOps.createFloat(((FloatBinaryTag) input).value());
            case 6 -> outOps.createDouble(((DoubleBinaryTag) input).value());
            case 7 -> outOps.createByteList(ByteBuffer.wrap(((ByteArrayBinaryTag) input).value()));
            case 8 -> outOps.createString(((StringBinaryTag) input).value());
            case 9 -> convertList(outOps, input);
            case 10 -> convertMap(outOps, input);
            case 11 -> outOps.createIntList(Arrays.stream(((IntArrayBinaryTag) input).value()));
            case 12 -> outOps.createLongList(Arrays.stream(((LongArrayBinaryTag) input).value()));
            default -> throw new IllegalStateException("Unknown tag type: " + input.type());
        };
    }

    @Override
    public DataResult<Number> getNumberValue(final BinaryTag input) {
        if (input instanceof NumberBinaryTag numberBinaryTag) {
            final Number number;
            switch (input.type().id()) {
                case 1 -> number = numberBinaryTag.byteValue();
                case 2 -> number = numberBinaryTag.shortValue();
                case 3 -> number = numberBinaryTag.intValue();
                case 4 -> number = numberBinaryTag.longValue();
                case 5 -> number = numberBinaryTag.floatValue();
                case 6 -> number = numberBinaryTag.doubleValue();
                default -> throw new IllegalStateException("Unknown numeric tag type: " + input.type());
            }
            return DataResult.success(number);
        } else {
            return DataResult.error(() -> "Not a number");
        }
    }

    @Override
    public BinaryTag createNumeric(final Number i) {
        return DoubleBinaryTag.doubleBinaryTag(i.doubleValue());
    }

    @Override
    public BinaryTag createByte(final byte value) {
        return ByteBinaryTag.byteBinaryTag(value);
    }

    @Override
    public BinaryTag createShort(final short value) {
        return ShortBinaryTag.shortBinaryTag(value);
    }

    @Override
    public BinaryTag createInt(final int value) {
        return IntBinaryTag.intBinaryTag(value);
    }

    @Override
    public BinaryTag createLong(final long value) {
        return LongBinaryTag.longBinaryTag(value);
    }

    @Override
    public BinaryTag createFloat(final float value) {
        return FloatBinaryTag.floatBinaryTag(value);
    }

    @Override
    public BinaryTag createDouble(final double value) {
        return DoubleBinaryTag.doubleBinaryTag(value);
    }

    @Override
    public BinaryTag createBoolean(final boolean value) {
        return value ? ByteBinaryTag.ONE : ByteBinaryTag.ZERO;
    }

    @Override
    public DataResult<String> getStringValue(final BinaryTag input) {
        return input instanceof StringBinaryTag stringBinaryTag ? DataResult.success(stringBinaryTag.value()) : DataResult.error(() -> "Not a string");
    }

    @Override
    public BinaryTag createString(final String value) {
        return StringBinaryTag.stringBinaryTag(value);
    }

    @Override
    public DataResult<BinaryTag> mergeToList(final BinaryTag list, final BinaryTag value) {
        return createCollector(list)
            .map(listCollector -> DataResult.success(listCollector.accept(value).result()))
            .orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + (list), list));
    }

    @Override
    public DataResult<BinaryTag> mergeToList(final BinaryTag list, final List<BinaryTag> values) {
        return createCollector(list)
            .map(listCollector -> DataResult.success(listCollector.acceptAll(values).result()))
            .orElseGet(() -> DataResult.error(() -> "mergeToList called with not a list: " + (list), list));
    }

    @Override
    public DataResult<BinaryTag> mergeToMap(final BinaryTag map,
                                            final BinaryTag key,
                                            final BinaryTag value) {
        if (!(map instanceof CompoundBinaryTag) && !(map instanceof EndBinaryTag)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + (map), map);
        } else if (!(key instanceof StringBinaryTag)) {
            return DataResult.error(() -> "key is not a string: " + (key), map);
        } else {
            final CompoundBinaryTag compoundBinaryTag = map instanceof CompoundBinaryTag compoundBinaryTag1 ? CompoundBinaryTag.builder().put(compoundBinaryTag1).build() : CompoundBinaryTag.builder().build();
            compoundBinaryTag.put(((StringBinaryTag) key).value(), value);
            return DataResult.success(compoundBinaryTag);
        }
    }

    @Override
    public DataResult<BinaryTag> mergeToMap(final BinaryTag map, final MapLike<BinaryTag> values) {
        if (!(map instanceof CompoundBinaryTag) && !(map instanceof EndBinaryTag)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + (map), map);
        } else {
            final CompoundBinaryTag compoundBinaryTag = map instanceof CompoundBinaryTag compoundBinaryTag1 ? CompoundBinaryTag.builder().put(compoundBinaryTag1).build() : CompoundBinaryTag.builder().build();
            final List<BinaryTag> list = new ArrayList<>();
            values.entries().forEach(entry -> {
                final BinaryTag tag = entry.getFirst();
                if (!(tag instanceof StringBinaryTag)) {
                    list.add(tag);
                } else {
                    compoundBinaryTag.put(((StringBinaryTag) tag).value(), entry.getSecond());
                }
            });
            return !list.isEmpty() ? DataResult.error(() -> "some keys are not strings: " + list, compoundBinaryTag) : DataResult.success(compoundBinaryTag);
        }
    }

    @Override
    public DataResult<BinaryTag> mergeToMap(final BinaryTag map,
                                            final Map<BinaryTag, BinaryTag> values) {
        if (!(map instanceof CompoundBinaryTag) && !(map instanceof EndBinaryTag)) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + (map), map);
        } else {
            final CompoundBinaryTag compoundBinaryTag = map instanceof CompoundBinaryTag compoundBinaryTag1 ? CompoundBinaryTag.builder().put(compoundBinaryTag1).build() : CompoundBinaryTag.builder().build();
            final List<BinaryTag> list = new ArrayList<>();

            for (final Map.Entry<BinaryTag, BinaryTag> entry : values.entrySet()) {
                final BinaryTag tag = entry.getKey();
                if (!(tag instanceof StringBinaryTag)) {
                    list.add(tag);
                } else {
                    compoundBinaryTag.put(((StringBinaryTag) tag).value(), entry.getValue());
                }
            }

            return !list.isEmpty() ? DataResult.error(() -> "some keys are not strings: " + list, compoundBinaryTag) : DataResult.success(compoundBinaryTag);
        }
    }

    @Override
    public DataResult<Stream<Pair<BinaryTag, BinaryTag>>> getMapValues(final BinaryTag input) {
        return input instanceof CompoundBinaryTag compound
            ? DataResult.success(
            compound.keySet().stream()
                .map(key -> Pair.of(createString(key), compound.get(key))))
            : DataResult.error(() -> "Not a map: " + (input));
    }

    @Override
    public DataResult<Consumer<BiConsumer<BinaryTag, BinaryTag>>> getMapEntries(final BinaryTag input) {
        return input instanceof CompoundBinaryTag compound ? DataResult.success(consumer -> {
            for (final String key : compound.keySet()) {
                consumer.accept(createString(key), Objects.requireNonNull(compound.get(key), "value of key %s in compound".formatted(key)));
            }
        }) : DataResult.error(() -> "Not a map: " + (input));
    }

    @Override
    public DataResult<MapLike<BinaryTag>> getMap(final BinaryTag input) {
        return input instanceof CompoundBinaryTag compound ? DataResult.success(new MapLike<>() {
            @Override
            public @Nullable BinaryTag get(final BinaryTag key) {
                return compound.get(asString(key));
            }

            @Override
            public @Nullable BinaryTag get(final String key) {
                return compound.get(key);
            }

            @Override
            public Stream<Pair<BinaryTag, BinaryTag>> entries() {
                return compound.keySet().stream().map(key -> Pair.of(createString(key), compound.get(key)));
            }

            @Override
            public String toString() {
                return "MapLike[" + (compound) + "]";
            }

        }) : DataResult.error(() -> "Not a map: " + (input));
    }

    @Override
    public BinaryTag createMap(final Stream<Pair<BinaryTag, BinaryTag>> map) {
        final CompoundBinaryTag compound = CompoundBinaryTag.builder().build();
        map.forEach(pair -> compound.put(asString(pair.getFirst()), pair.getSecond()));
        return compound;
    }

    private static BinaryTag tryUnwrap(final CompoundBinaryTag compound) {
        if (compound.size() == 1) {
            final BinaryTag tag = compound.get(WRAPPER_MARKER);
            if (tag != null) {
                return tag;
            }
        }

        return compound;
    }

    @Override
    public DataResult<Stream<BinaryTag>> getStream(final BinaryTag input) {
        return switch (input) {
            case ListBinaryTag listTag ->
                listTag.elementType().id() == BinaryTagTypes.COMPOUND.id() ? DataResult.success(listTag.stream()
                    .map(tag -> tryUnwrap((CompoundBinaryTag) tag))) : DataResult.success(listTag.stream());
            case IntArrayBinaryTag intArrayTag -> DataResult.success(intArrayTag.stream()
                .mapToObj(IntArrayBinaryTag::intArrayBinaryTag));
            case LongArrayBinaryTag longArrayTag -> DataResult.success(longArrayTag.stream()
                .mapToObj(LongArrayBinaryTag::longArrayBinaryTag));
            default -> DataResult.error(() -> "Not a list: " + (input));
        };
    }

    @Override
    public DataResult<Consumer<Consumer<BinaryTag>>> getList(final BinaryTag input) {
        return switch (input) {
            case ListBinaryTag listTag ->
                listTag.elementType().id() == BinaryTagTypes.COMPOUND.id() ? DataResult.success(consumer -> {
                    for (final BinaryTag tag : listTag) {
                        consumer.accept(tag);
                    }
                }) : DataResult.success(listTag::forEach);
            case IntArrayBinaryTag intArrayTag -> DataResult.success(sink -> sink.accept(intArrayTag));
            case LongArrayBinaryTag longArrayTag -> DataResult.success(sink -> sink.accept(longArrayTag));
            default -> DataResult.error(() -> "Not a list: " + (input));
        };
    }

    @Override
    public DataResult<ByteBuffer> getByteBuffer(final BinaryTag input) {
        return input instanceof ByteArrayBinaryTag byteArrayTag
            ? DataResult.success(ByteBuffer.wrap(byteArrayTag.value()))
            : DynamicOps.super.getByteBuffer(input);
    }

    @Override
    public BinaryTag createByteList(final ByteBuffer input) {
        final ByteBuffer byteBuffer = input.duplicate().clear();
        final byte[] byteArray = new byte[input.capacity()];
        byteBuffer.get(0, byteArray, 0, byteArray.length);
        return ByteArrayBinaryTag.byteArrayBinaryTag(byteArray);
    }

    @Override
    public DataResult<IntStream> getIntStream(final BinaryTag input) {
        return input instanceof IntArrayBinaryTag intArrayTag
            ? DataResult.success(Arrays.stream(intArrayTag.value()))
            : DynamicOps.super.getIntStream(input);
    }

    @Override
    public BinaryTag createIntList(final IntStream input) {
        return IntArrayBinaryTag.intArrayBinaryTag(input.toArray());
    }

    @Override
    public DataResult<LongStream> getLongStream(final BinaryTag input) {
        return input instanceof LongArrayBinaryTag longArrayTag
            ? DataResult.success(Arrays.stream(longArrayTag.value()))
            : DynamicOps.super.getLongStream(input);
    }

    @Override
    public BinaryTag createLongList(final LongStream input) {
        return LongArrayBinaryTag.longArrayBinaryTag(input.toArray());
    }

    @Override
    public BinaryTag createList(final Stream<BinaryTag> input) {
        return InitialListCollector.INSTANCE.acceptAll(input).result();
    }

    @Override
    public BinaryTag remove(final BinaryTag input, final String key) {
        if (input instanceof CompoundBinaryTag compound) {
            final CompoundBinaryTag compound1 = CompoundBinaryTag.builder().put(compound).build();
            compound1.remove(key);
            return compound1;
        } else {
            return input;
        }
    }

    @Override
    public String toString() {
        return "NBT";
    }

    @Override
    public RecordBuilder<BinaryTag> mapBuilder() {
        return new NbtRecordBuilder();
    }

    private static Optional<ListCollector> createCollector(final BinaryTag tag) {
        if (tag instanceof EndBinaryTag) {
            return Optional.of(InitialListCollector.INSTANCE);
        } else {
            return switch (tag) {
                case ListBinaryTag listTag -> {
                    if (listTag.isEmpty()) {
                        yield Optional.of(InitialListCollector.INSTANCE);
                    } else {
                        yield switch (listTag.elementType().id()) {
                            case 0 -> Optional.of(InitialListCollector.INSTANCE);
                            case 10 -> Optional.of(new HeterogenousListCollector(listTag));
                            default -> Optional.of(new HomogenousListCollector(listTag));
                        };
                    }
                }
                case ByteArrayBinaryTag byteArrayTag ->
                    Optional.of(new ByteListCollector(byteArrayTag.value()));
                case IntArrayBinaryTag intArrayTag ->
                    Optional.of(new IntListCollector(intArrayTag.value()));
                case LongArrayBinaryTag longArrayTag ->
                    Optional.of(new LongListCollector(longArrayTag.value()));
                default -> Optional.empty();
            };
        }
    }

    private static String asString(final BinaryTag tag) {
        if (tag instanceof StringBinaryTag stringBinaryTag) {
            return stringBinaryTag.value();
        } else {
            throw new IllegalStateException("Not a string: " + (tag));
        }
    }

    @SuppressWarnings("unused")
    interface ListCollector {
        NbtOps.ListCollector accept(BinaryTag tag);

        default NbtOps.ListCollector acceptAll(Iterable<BinaryTag> tagIterable) {
            NbtOps.ListCollector listCollector = this;

            for (final BinaryTag tag : tagIterable) {
                listCollector = listCollector.accept(tag);
            }

            return listCollector;
        }

        default NbtOps.ListCollector acceptAll(Stream<BinaryTag> tagStream) {
            return this.acceptAll(tagStream::iterator);
        }

        BinaryTag result();
    }

    static final class InitialListCollector implements ListCollector {

        public static final InitialListCollector INSTANCE = new InitialListCollector();

        private InitialListCollector() {
        }

        @Override
        public ListCollector accept(final BinaryTag tag) {
            return switch (tag) {
                case CompoundBinaryTag compound -> new HeterogenousListCollector().accept(compound);
                case ByteBinaryTag byteTag -> new ByteListCollector(byteTag.value());
                case IntBinaryTag intTag -> new IntListCollector(intTag.value());
                default ->
                    tag instanceof LongBinaryTag longTag ? new LongListCollector(
                        longTag.value()) : new HomogenousListCollector(tag);
            };
        }

        @Override
        public BinaryTag result() {
            return ListBinaryTag.builder().build();
        }

    }

    @SuppressWarnings("unused")
    static class HeterogenousListCollector implements ListCollector {

        private final ListBinaryTag result = ListBinaryTag.builder().build();

        HeterogenousListCollector() {
        }

        HeterogenousListCollector(final ListBinaryTag listTag) {
            for (final BinaryTag binaryTag : listTag) {
                result.add(binaryTag);
            }
        }

        HeterogenousListCollector(final IntArrayList intArrayList) {
            intArrayList.forEach(integer -> result.add(wrapElement(IntArrayBinaryTag.intArrayBinaryTag(integer))));
        }

        HeterogenousListCollector(final ByteArrayList byteArrayList) {
            byteArrayList.forEach(aByte -> result.add(wrapElement(ByteBinaryTag.byteBinaryTag(aByte))));
        }

        HeterogenousListCollector(final LongArrayList longArrayList) {
            longArrayList.forEach(aLong -> result.add(wrapElement(LongArrayBinaryTag.longArrayBinaryTag(aLong))));
        }

        private static boolean isWrapper(final CompoundBinaryTag compoundBinaryTag) {
            return compoundBinaryTag.size() == 1 && compoundBinaryTag.get(WRAPPER_MARKER) != null;
        }

        private static BinaryTag wrapIfNeeded(final BinaryTag tag) {
            if (tag instanceof CompoundBinaryTag compound && !isWrapper(compound)) {
                return compound;
            }

            return wrapElement(tag);
        }

        @SuppressWarnings("unused")
        private static CompoundBinaryTag wrapElement(final BinaryTag tag) {
            final CompoundBinaryTag compound = CompoundBinaryTag.builder().build();
            compound.put(WRAPPER_MARKER, tag);
            return compound;
        }

        @Override
        public ListCollector accept(final BinaryTag tag) {
            result.add(wrapIfNeeded(tag));
            return this;
        }

        @Override
        public BinaryTag result() {
            return result;
        }

    }

    @SuppressWarnings("unused")
    static class HomogenousListCollector implements ListCollector {

        private final ListBinaryTag.Builder<BinaryTag> result;
        private final BinaryTagType<?> elementType;

        HomogenousListCollector(final BinaryTag tag) {
            elementType = tag.type();
            result = ListBinaryTag.builder().add(tag);
        }

        HomogenousListCollector(final ListBinaryTag listTag) {
            elementType = listTag.elementType();

            final ListBinaryTag.Builder<BinaryTag> builder = ListBinaryTag.builder();

            for (final BinaryTag binaryTag : listTag) {
                builder.add(binaryTag);
            }

            result = builder;
        }

        @Override
        public ListCollector accept(final BinaryTag tag) {
            if (tag.type().id() != elementType.id()) {
                return new HeterogenousListCollector().acceptAll(result.build()).accept(tag);
            } else {
                result.add(tag);
                return this;
            }
        }

        @Override
        public BinaryTag result() {
            return result.build();
        }

    }

    @SuppressWarnings("unused")
    static class ByteListCollector implements ListCollector {

        private final ByteArrayList values = new ByteArrayList();

        ByteListCollector(final byte aByte) {
            values.add(aByte);
        }

        ByteListCollector(final byte[] byteArray) {
            values.addElements(0, byteArray);
        }

        @Override
        public ListCollector accept(final BinaryTag tag) {
            if (tag instanceof ByteBinaryTag byteTag) {
                values.add(byteTag.value());
                return this;
            } else {
                return new HeterogenousListCollector(values).accept(tag);
            }
        }

        @Override
        public BinaryTag result() {
            return ByteArrayBinaryTag.byteArrayBinaryTag(values.toByteArray());
        }

    }

    @SuppressWarnings("unused")
    static class IntListCollector implements ListCollector {

        private final IntArrayList values = new IntArrayList();

        IntListCollector(final int integer) {
            values.add(integer);
        }

        IntListCollector(final int[] intArray) {
            values.addElements(0, intArray);
        }

        @Override
        public ListCollector accept(final BinaryTag tag) {
            if (tag instanceof IntBinaryTag intTag) {
                values.add(intTag.value());
                return this;
            } else {
                return new HeterogenousListCollector(values).accept(tag);
            }
        }

        @Override
        public BinaryTag result() {
            return IntArrayBinaryTag.intArrayBinaryTag(values.toIntArray());
        }

    }

    @SuppressWarnings("unused")
    static class LongListCollector implements ListCollector {

        private final LongArrayList values = new LongArrayList();

        LongListCollector(final long aLong) {
            values.add(aLong);
        }

        LongListCollector(final long[] longArray) {
            values.addElements(0, longArray);
        }

        @Override
        public ListCollector accept(final BinaryTag tag) {
            if (tag instanceof LongBinaryTag longTag) {
                values.add(longTag.value());
                return this;
            } else {
                return new HeterogenousListCollector(values).accept(tag);
            }
        }

        @Override
        public BinaryTag result() {
            return LongArrayBinaryTag.longArrayBinaryTag(values.toLongArray());
        }

    }

    @SuppressWarnings("unused")
    class NbtRecordBuilder extends RecordBuilder.AbstractStringBuilder<BinaryTag, CompoundBinaryTag.Builder> {

        protected NbtRecordBuilder() {
            super(NbtOps.this);
        }

        @Override
        protected CompoundBinaryTag.Builder initBuilder() {
            return CompoundBinaryTag.builder();
        }

        @Override
        protected CompoundBinaryTag.Builder append(final String key,
                                                   final BinaryTag value,
                                                   final CompoundBinaryTag.Builder builder) {
            builder.put(key, value);
            return builder;
        }

        @Override
        protected DataResult<BinaryTag> build(final CompoundBinaryTag.Builder builder,
                                              final @Nullable BinaryTag prefix) {
            if (prefix == null || prefix.type().id() == BinaryTagTypes.END.id()) {
                return DataResult.success(builder.build());
            } else if (!(prefix instanceof CompoundBinaryTag compound)) {
                return DataResult.error(() -> "mergeToMap called with not a map: " + (prefix), prefix);
            } else {
                return DataResult.success(
                    CompoundBinaryTag.builder()
                        .put(compound)
                        .put(builder.build())
                        .build()
                );
            }
        }

    }

}
