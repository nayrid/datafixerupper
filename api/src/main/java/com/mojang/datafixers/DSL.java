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

import com.google.common.collect.Maps;
import com.google.common.collect.ObjectArrays;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Func;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.constant.EmptyPart;
import com.mojang.datafixers.types.constant.EmptyPartPassthrough;
import com.mojang.datafixers.types.templates.Check;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.datafixers.types.templates.Const;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.List;
import com.mojang.datafixers.types.templates.Named;
import com.mojang.datafixers.types.templates.Product;
import com.mojang.datafixers.types.templates.RecursivePoint;
import com.mojang.datafixers.types.templates.Sum;
import com.mojang.datafixers.types.templates.Tag;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface DSL {

    static Type<Boolean> bool() {
        return Instances.BOOL_TYPE;
    }

    // Type/Template Factories

    static Type<Integer> intType() {
        return Instances.INT_TYPE;
    }

    static Type<Long> longType() {
        return Instances.LONG_TYPE;
    }

    static Type<Byte> byteType() {
        return Instances.BYTE_TYPE;
    }

    static Type<Short> shortType() {
        return Instances.SHORT_TYPE;
    }

    static Type<Float> floatType() {
        return Instances.FLOAT_TYPE;
    }

    static Type<Double> doubleType() {
        return Instances.DOUBLE_TYPE;
    }

    static Type<String> string() {
        return Instances.STRING_TYPE;
    }

    static TypeTemplate emptyPart() {
        return constType(Instances.EMPTY_PART);
    }

    static Type<Unit> emptyPartType() {
        return Instances.EMPTY_PART;
    }

    static TypeTemplate remainder() {
        return constType(Instances.EMPTY_PASSTHROUGH);
    }

    static Type<Dynamic<?>> remainderType() {
        return Instances.EMPTY_PASSTHROUGH;
    }

    static TypeTemplate check(final String name, final int index, final TypeTemplate element) {
        return new Check(name, index, element);
    }

    static TypeTemplate compoundList(final TypeTemplate element) {
        return compoundList(constType(string()), element);
    }

    static <V> CompoundList.CompoundListType<String, V> compoundList(final Type<V> value) {
        return compoundList(string(), value);
    }

    static TypeTemplate compoundList(final TypeTemplate key, final TypeTemplate element) {
        return and(new CompoundList(key, element), remainder());
    }

    static <K, V> CompoundList.CompoundListType<K, V> compoundList(final Type<K> key,
                                                                   final Type<V> value) {
        return new CompoundList.CompoundListType<>(key, value);
    }

    static TypeTemplate constType(final Type<?> type) {
        return new Const(type);
    }

    static TypeTemplate hook(final TypeTemplate template,
                             final Hook.HookFunction preRead,
                             final Hook.HookFunction postWrite) {
        return new Hook(template, preRead, postWrite);
    }

    static <A> Type<A> hook(final Type<A> type,
                            final Hook.HookFunction preRead,
                            final Hook.HookFunction postWrite) {
        return new Hook.HookType<>(type, preRead, postWrite);
    }

    static TypeTemplate list(final TypeTemplate element) {
        return new List(element);
    }

    static <A> List.ListType<A> list(final Type<A> first) {
        return new List.ListType<>(first);
    }

    static TypeTemplate named(final String name, final TypeTemplate element) {
        return new Named(name, element);
    }

    static <A> Type<Pair<String, A>> named(final String name, final Type<A> element) {
        return new Named.NamedType<>(name, element);
    }

    static TypeTemplate and(final TypeTemplate first, final TypeTemplate second) {
        return new Product(first, second);
    }

    static TypeTemplate and(final TypeTemplate first, final TypeTemplate... rest) {
        if (rest.length == 0) {
            return first;
        }
        TypeTemplate result = rest[rest.length - 1];
        for (int i = rest.length - 2;
             i >= 0;
             i--) {
            result = and(rest[i], result);
        }
        return and(first, result);
    }

    static TypeTemplate and(final java.util.List<TypeTemplate> types) {
        return switch (types.size()) {
            case 0 -> throw new IllegalArgumentException("Must have at least one type");
            case 1 -> types.get(0);
            default ->
                and(types.get(0), types.subList(1, types.size()).toArray(TypeTemplate[]::new));
        };
    }

    static TypeTemplate allWithRemainder(final TypeTemplate first, final TypeTemplate... rest) {
        return and(first, ObjectArrays.concat(rest, remainder()));
    }

    static <F, G> Type<Pair<F, G>> and(final Type<F> first, final Type<G> second) {
        return new Product.ProductType<>(first, second);
    }

    static <F, G, H> Type<Pair<F, Pair<G, H>>> and(final Type<F> first,
                                                   final Type<G> second,
                                                   final Type<H> third) {
        return and(first, and(second, third));
    }

    static <F, G, H, I> Type<Pair<F, Pair<G, Pair<H, I>>>> and(final Type<F> first,
                                                               final Type<G> second,
                                                               final Type<H> third,
                                                               final Type<I> forth) {
        return and(first, and(second, and(third, forth)));
    }

    static TypeTemplate id(final int index) {
        return new RecursivePoint(index);
    }

    static TypeTemplate or(final TypeTemplate left, final TypeTemplate right) {
        return new Sum(left, right);
    }

    static <F, G> Type<Either<F, G>> or(final Type<F> first, final Type<G> second) {
        return new Sum.SumType<>(first, second);
    }

    static TypeTemplate field(final String name, final TypeTemplate element) {
        return new Tag(name, element);
    }

    static <A> Tag.TagType<A> field(final String name, final Type<A> element) {
        return new Tag.TagType<>(name, element);
    }

    static <K> TaggedChoice<K> taggedChoice(final String name,
                                            final Type<K> keyType,
                                            final Map<K, TypeTemplate> templates) {
        return new TaggedChoice<>(name, keyType, new Object2ObjectOpenHashMap<>(templates));
    }

    static <K> TaggedChoice<K> taggedChoiceLazy(final String name,
                                                final Type<K> keyType,
                                                final Map<K, Supplier<TypeTemplate>> templates) {
        return taggedChoice(name,
            keyType,
            templates.entrySet()
                .stream()
                .map(e -> Pair.of(e.getKey(), e.getValue().get()))
                .collect(Pair.toMap())
        );
    }

    @SuppressWarnings("unchecked")
    static <K> Type<Pair<K, ?>> taggedChoiceType(final String name,
                                                 final Type<K> keyType,
                                                 final Map<K, ? extends Type<?>> types) {
        return (Type<Pair<K, ?>>) Instances.TAGGED_CHOICE_TYPE_CACHE.computeIfAbsent(new Instances.TaggedChoiceCacheKey<>(
            name,
            keyType,
            types
        ), Instances.TaggedChoiceCacheKey::build);
    }

    static <A, B> Type<Function<A, B>> func(final Type<A> input, final Type<B> output) {
        return new Func<>(input, output);
    }

    static <A> Type<Either<A, Unit>> optional(final Type<A> type) {
        return or(type, emptyPartType());
    }

    // Helpers

    static TypeTemplate optional(final TypeTemplate value) {
        return or(value, emptyPart());
    }

    static TypeTemplate fields(final String name1, final TypeTemplate element1) {
        return allWithRemainder(field(name1, element1));
    }

    static TypeTemplate fields(final String name1,
                               final TypeTemplate element1,
                               final String name2,
                               final TypeTemplate element2) {
        return allWithRemainder(field(name1, element1), field(name2, element2));
    }

    static TypeTemplate fields(final String name1,
                               final TypeTemplate element1,
                               final String name2,
                               final TypeTemplate element2,
                               final String name3,
                               final TypeTemplate element3) {
        return allWithRemainder(field(name1, element1),
            field(name2, element2),
            field(name3, element3)
        );
    }

    static TypeTemplate fields(final String name,
                               final TypeTemplate element,
                               final TypeTemplate rest) {
        return and(field(name, element), rest);
    }

    static TypeTemplate fields(final String name1,
                               final TypeTemplate element1,
                               final String name2,
                               final TypeTemplate element2,
                               final TypeTemplate rest) {
        return and(field(name1, element1), field(name2, element2), rest);
    }

    static TypeTemplate fields(final String name1,
                               final TypeTemplate element1,
                               final String name2,
                               final TypeTemplate element2,
                               final String name3,
                               final TypeTemplate element3,
                               final TypeTemplate rest) {
        return and(field(name1, element1), field(name2, element2), field(name3, element3), rest);
    }

    static TypeTemplate optionalFields(final String name, final TypeTemplate element) {
        return allWithRemainder(optional(field(name, element)));
    }

    static TypeTemplate optionalFields(final String name1,
                                       final TypeTemplate element1,
                                       final String name2,
                                       final TypeTemplate element2) {
        return allWithRemainder(optional(field(name1, element1)), optional(field(name2, element2)));
    }

    static TypeTemplate optionalFields(final String name1,
                                       final TypeTemplate element1,
                                       final String name2,
                                       final TypeTemplate element2,
                                       final String name3,
                                       final TypeTemplate element3) {
        return allWithRemainder(optional(field(name1, element1)),
            optional(field(name2, element2)),
            optional(field(name3, element3))
        );
    }

    static TypeTemplate optionalFields(final String name1,
                                       final TypeTemplate element1,
                                       final String name2,
                                       final TypeTemplate element2,
                                       final String name3,
                                       final TypeTemplate element3,
                                       final String name4,
                                       final TypeTemplate element4) {
        return allWithRemainder(optional(field(name1, element1)),
            optional(field(name2, element2)),
            optional(field(name3, element3)),
            optional(field(name4, element4))
        );
    }

    static TypeTemplate optionalFields(final String name1,
                                       final TypeTemplate element1,
                                       final String name2,
                                       final TypeTemplate element2,
                                       final String name3,
                                       final TypeTemplate element3,
                                       final String name4,
                                       final TypeTemplate element4,
                                       final String name5,
                                       final TypeTemplate element5) {
        return allWithRemainder(optional(field(name1, element1)),
            optional(field(name2, element2)),
            optional(field(name3, element3)),
            optional(field(name4, element4)),
            optional(field(name5, element5))
        );
    }

    static TypeTemplate optionalFields(final String name,
                                       final TypeTemplate element,
                                       final TypeTemplate rest) {
        return and(optional(field(name, element)), rest);
    }

    static TypeTemplate optionalFields(final String name1,
                                       final TypeTemplate element1,
                                       final String name2,
                                       final TypeTemplate element2,
                                       final TypeTemplate rest) {
        return and(optional(field(name1, element1)), optional(field(name2, element2)), rest);
    }

    static TypeTemplate optionalFields(final String name1,
                                       final TypeTemplate element1,
                                       final String name2,
                                       final TypeTemplate element2,
                                       final String name3,
                                       final TypeTemplate element3,
                                       final TypeTemplate rest) {
        return and(optional(field(name1, element1)),
            optional(field(name2, element2)),
            optional(field(name3, element3)),
            rest
        );
    }

    static TypeTemplate optionalFields(final String name1,
                                       final TypeTemplate element1,
                                       final String name2,
                                       final TypeTemplate element2,
                                       final String name3,
                                       final TypeTemplate element3,
                                       final String name4,
                                       final TypeTemplate element4,
                                       final TypeTemplate rest) {
        return and(optional(field(name1, element1)),
            optional(field(name2, element2)),
            optional(field(name3, element3)),
            optional(field(name4, element4)),
            rest
        );
    }

    static TypeTemplate optionalFields(final String name1,
                                       final TypeTemplate element1,
                                       final String name2,
                                       final TypeTemplate element2,
                                       final String name3,
                                       final TypeTemplate element3,
                                       final String name4,
                                       final TypeTemplate element4,
                                       final String name5,
                                       final TypeTemplate element5,
                                       final TypeTemplate rest) {
        return and(optional(field(name1, element1)),
            optional(field(name2, element2)),
            optional(field(name3, element3)),
            optional(field(name4, element4)),
            optional(field(name5, element5)),
            rest
        );
    }

    @SafeVarargs
    static TypeTemplate optionalFields(final Pair<String, TypeTemplate>... fields) {
        return and(Stream.concat(Arrays.stream(fields)
                .map(entry -> optional(field(entry.first(), entry.second()))),
            Stream.of(remainder())
        ).toList());
    }

    static TypeTemplate optionalFieldsLazy(final Map<String, Supplier<TypeTemplate>> fields) {
        return and(Stream.concat(fields.entrySet()
                .stream()
                .map(entry -> optional(field(entry.getKey(), entry.getValue().get()))),
            Stream.of(remainder())
        ).toList());
    }

    static OpticFinder<Dynamic<?>> remainderFinder() {
        return Instances.REMAINDER_FINDER;
    }

    // Type matchers

    static <FT> OpticFinder<FT> typeFinder(final Type<FT> type) {
        return new FieldFinder<>(null, type);
    }

    static <FT> OpticFinder<FT> fieldFinder(final String name, final Type<FT> type) {
        return new FieldFinder<>(name, type);
    }

    static <FT> OpticFinder<FT> namedChoice(final String name, final Type<FT> type) {
        return new NamedChoiceFinder<>(name, type);
    }

    static Unit unit() {
        return Unit.INSTANCE;
    }

    interface TypeReference {

        String typeName();

        default TypeTemplate in(final Schema schema) {
            return schema.id(typeName());
        }

    }

    final class Instances {

        private static final Type<Boolean> BOOL_TYPE = new Const.PrimitiveType<>(Codec.BOOL);
        private static final Type<Integer> INT_TYPE = new Const.PrimitiveType<>(Codec.INT);
        private static final Type<Long> LONG_TYPE = new Const.PrimitiveType<>(Codec.LONG);
        private static final Type<Byte> BYTE_TYPE = new Const.PrimitiveType<>(Codec.BYTE);
        private static final Type<Short> SHORT_TYPE = new Const.PrimitiveType<>(Codec.SHORT);
        private static final Type<Float> FLOAT_TYPE = new Const.PrimitiveType<>(Codec.FLOAT);
        private static final Type<Double> DOUBLE_TYPE = new Const.PrimitiveType<>(Codec.DOUBLE);
        private static final Type<String> STRING_TYPE = new Const.PrimitiveType<>(Codec.STRING);
        private static final Type<Unit> EMPTY_PART = new EmptyPart();
        private static final Type<Dynamic<?>> EMPTY_PASSTHROUGH = new EmptyPartPassthrough();

        private static final OpticFinder<Dynamic<?>> REMAINDER_FINDER = remainderType().finder();

        private static final Map<TaggedChoiceCacheKey<?>, Type<? extends Pair<?, ?>>> TAGGED_CHOICE_TYPE_CACHE = Maps.newConcurrentMap();

        public record TaggedChoiceCacheKey<K>(String name,
                                              Type<K> keyType,
                                              Map<K, ? extends Type<?>> types) {

            public TaggedChoice.TaggedChoiceType<K> build() {
                return new TaggedChoice.TaggedChoiceType<>(name,
                    keyType,
                    new Object2ObjectOpenHashMap<>(types)
                );
            }

        }

    }

}
