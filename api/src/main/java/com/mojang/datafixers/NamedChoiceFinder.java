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
import com.mojang.datafixers.types.templates.Tag;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Either;
import java.util.Objects;

import static com.mojang.datafixers.TypedOptic.tagged;

final class NamedChoiceFinder<FT> implements OpticFinder<FT> {

    private final String name;
    private final Type<FT> type;

    public NamedChoiceFinder(final String name, final Type<FT> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public Type<FT> type() {
        return type;
    }

    @Override
    public <A, FR> Either<TypedOptic<A, ?, FT, FR>, Type.FieldNotFoundException> findType(final Type<A> containerType,
                                                                                          final Type<FR> resultType,
                                                                                          final boolean recurse) {
        return containerType.findTypeCached(type,
            resultType,
            new Matcher<>(name, type, resultType),
            recurse
        );
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof final NamedChoiceFinder<?> that)) {
            return false;
        }
        return Objects.equals(name, that.name) && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        return result;
    }

    private static class Matcher<FT, FR> implements Type.TypeMatcher<FT, FR> {

        private final Type<FR> resultType;
        private final String name;
        private final Type<FT> type;

        public Matcher(final String name, final Type<FT> type, final Type<FR> resultType) {
            this.resultType = resultType;
            this.name = name;
            this.type = type;
        }

        @SuppressWarnings("unchecked")
        @Override
        public <S> Either<TypedOptic<S, ?, FT, FR>, Type.FieldNotFoundException> match(final Type<S> targetType) {
            /*if (targetType instanceof Type.NamedType<?>) {
                final Type.NamedType<?> namedType = (Type.NamedType<?>) targetType;
                if (!Objects.equals(namedType.name, name)) {
                    return Either.right(new Type.FieldNotFoundException(String.format("Not found: \"%s\" (type: %s)", name, targetType)));
                }
                if (!Objects.equals(type, namedType.element)) {
                    return Either.right(new Type.FieldNotFoundException(String.format("Type error for named type \"%s\": expected type: %s, actual type: %s)", name, targetType, namedType.element)));
                }
                return Either.left((Type.TypedOptic<S, ?, FT, FR>) cap(namedType));
            }*/
            if (targetType instanceof final TaggedChoice.TaggedChoiceType<?> choiceType) {
                final Type<?> elementType = choiceType.types().get(name);
                if (elementType != null) {
                    if (!Objects.equals(type, elementType)) {
                        return Either.right(new Type.FieldNotFoundException(String.format(
                            "Type error for choice type \"%s\": expected type: %s, actual type: %s)",
                            name,
                            targetType,
                            elementType
                        )));
                    }
                    return Either.left((TypedOptic<S, ?, FT, FR>) tagged((TaggedChoice.TaggedChoiceType<String>) choiceType,
                        name,
                        type,
                        resultType
                    ));
                }
                return Either.right(new Type.Continue());
            }
            if (targetType instanceof Tag.TagType<?>) {
                return Either.right(new Type.FieldNotFoundException("in tag"));
            }
            return Either.right(new Type.Continue());
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            final Matcher<?, ?> matcher = (Matcher<?, ?>) o;
            return Objects.equals(resultType, matcher.resultType) && Objects.equals(name,
                matcher.name
            ) && Objects.equals(type, matcher.type);
        }

        @Override
        public int hashCode() {
            int result = resultType.hashCode();
            result = 31 * result + name.hashCode();
            result = 31 * result + type.hashCode();
            return result;
        }

    }

}
