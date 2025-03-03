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

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Type;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface TypeRewriteRule {

    static TypeRewriteRule nop() {
        return Nop.INSTANCE;
    }

    static TypeRewriteRule seq(final List<TypeRewriteRule> rules) {
        return new Seq(rules);
    }

    static TypeRewriteRule seq(final TypeRewriteRule first, final TypeRewriteRule second) {
        if (Objects.equals(first, nop())) {
            return second;
        }
        if (Objects.equals(second, nop())) {
            return first;
        }
        return seq(ImmutableList.of(first, second));
    }

    static TypeRewriteRule seq(final TypeRewriteRule firstRule, final TypeRewriteRule... rules) {
        if (rules.length == 0) {
            return firstRule;
        }
        int lastRule = rules.length - 1;
        TypeRewriteRule tail = rules[lastRule];
        while (lastRule > 0) {
            lastRule--;
            tail = seq(rules[lastRule], tail);
        }
        return seq(firstRule, tail);
    }

    static TypeRewriteRule orElse(final TypeRewriteRule first, final TypeRewriteRule second) {
        return orElse(first, () -> second);
    }

    static TypeRewriteRule orElse(final TypeRewriteRule first,
                                  final Supplier<TypeRewriteRule> second) {
        return new OrElse(first, second);
    }

    static TypeRewriteRule all(final TypeRewriteRule rule,
                               final boolean recurse,
                               final boolean checkIndex) {
        return new All(rule, recurse, checkIndex);
    }

    static TypeRewriteRule one(final TypeRewriteRule rule) {
        return new One(rule);
    }

    static TypeRewriteRule once(final TypeRewriteRule rule) {
        return orElse(rule, () -> one(once(rule)));
    }

    static TypeRewriteRule checkOnce(final TypeRewriteRule rule, final Consumer<Type<?>> onFail) {
        // TODO: toggle somehow
        //        return new CheckOnce(rule, onFail);
        return rule;
    }

    static TypeRewriteRule everywhere(final TypeRewriteRule rule,
                                      final PointFreeRule optimizationRule,
                                      final boolean recurse,
                                      final boolean checkIndex) {
        return new Everywhere(rule, optimizationRule, recurse, checkIndex);
    }

    static <B> TypeRewriteRule ifSame(final Type<B> targetType, final RewriteResult<B, ?> value) {
        return new IfSame<>(targetType, value);
    }

    <A> Optional<RewriteResult<A, ?>> rewrite(final Type<A> type);

    enum Nop implements TypeRewriteRule, Supplier<TypeRewriteRule> {
        INSTANCE;

        @Override
        public <A> Optional<RewriteResult<A, ?>> rewrite(final Type<A> type) {
            return Optional.of(RewriteResult.nop(type));
        }

        @Override
        public TypeRewriteRule get() {
            return this;
        }
    }

    final class Seq implements TypeRewriteRule {

        private final List<TypeRewriteRule> rules;
        private final int hashCode;

        public Seq(final List<TypeRewriteRule> rules) {
            this.rules = ImmutableList.copyOf(rules);
            hashCode = this.rules.hashCode();
        }

        @Override
        public <A> Optional<RewriteResult<A, ?>> rewrite(final Type<A> type) {
            RewriteResult<A, ?> result = RewriteResult.nop(type);
            for (final TypeRewriteRule rule : rules) {
                final Optional<RewriteResult<A, ?>> newResult = cap1(rule, result);
                if (!newResult.isPresent()) {
                    return Optional.empty();
                }
                result = newResult.get();
            }
            return Optional.of(result);
        }

        private <A, B> Optional<RewriteResult<A, ?>> cap1(final TypeRewriteRule rule,
                                                          final RewriteResult<A, B> f) {
            return rule.rewrite(f.view().newType()).map(s -> s.compose(f));
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof final Seq that)) {
                return false;
            }
            return Objects.equals(rules, that.rules);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

    }

    final class OrElse implements TypeRewriteRule {

        private final TypeRewriteRule first;
        private final Supplier<TypeRewriteRule> second;
        private final int hashCode;

        public OrElse(final TypeRewriteRule first, final Supplier<TypeRewriteRule> second) {
            this.first = first;
            this.second = second;
            hashCode = Objects.hash(first, second);
        }

        @Override
        public <A> Optional<RewriteResult<A, ?>> rewrite(final Type<A> type) {
            final Optional<RewriteResult<A, ?>> view = first.rewrite(type);
            if (view.isPresent()) {
                return view;
            }
            return second.get().rewrite(type);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof final OrElse that)) {
                return false;
            }
            return Objects.equals(first, that.first) && Objects.equals(second, that.second);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

    }

    class All implements TypeRewriteRule {

        private final TypeRewriteRule rule;
        private final boolean recurse;
        private final boolean checkIndex;
        private final int hashCode;

        public All(final TypeRewriteRule rule, final boolean recurse, final boolean checkIndex) {
            this.rule = rule;
            this.recurse = recurse;
            this.checkIndex = checkIndex;
            hashCode = Objects.hash(rule, recurse, checkIndex);
        }

        @Override
        public <A> Optional<RewriteResult<A, ?>> rewrite(final Type<A> type) {
            return Optional.of(type.all(rule, recurse, checkIndex));
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof final All that)) {
                return false;
            }
            return Objects.equals(rule,
                that.rule
            ) && recurse == that.recurse && checkIndex == that.checkIndex;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

    }

    record One(TypeRewriteRule rule) implements TypeRewriteRule {

        @Override
        public <A> Optional<RewriteResult<A, ?>> rewrite(final Type<A> type) {
            return type.one(rule);
        }

    }

    record CheckOnce(TypeRewriteRule rule,
                     Consumer<Type<?>> onFail) implements TypeRewriteRule {

        @Override
        public <A> Optional<RewriteResult<A, ?>> rewrite(final Type<A> type) {
            final Optional<RewriteResult<A, ?>> result = rule.rewrite(type);
            if (!result.isPresent() || result.get().view().isNop()) {
                onFail.accept(type);
            }
            return result;
        }

    }

    class Everywhere implements TypeRewriteRule {

        protected final TypeRewriteRule rule;
        protected final PointFreeRule optimizationRule;
        protected final boolean recurse;
        private final boolean checkIndex;
        private final int hashCode;

        public Everywhere(final TypeRewriteRule rule,
                          final PointFreeRule optimizationRule,
                          final boolean recurse,
                          final boolean checkIndex) {
            this.rule = rule;
            this.optimizationRule = optimizationRule;
            this.recurse = recurse;
            this.checkIndex = checkIndex;
            hashCode = Objects.hash(rule, optimizationRule, recurse, checkIndex);
        }

        @Override
        public <A> Optional<RewriteResult<A, ?>> rewrite(final Type<A> type) {
            return type.everywhere(rule, optimizationRule, recurse, checkIndex);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof final Everywhere that)) {
                return false;
            }
            return Objects.equals(rule, that.rule) && Objects.equals(optimizationRule,
                that.optimizationRule
            ) && recurse == that.recurse && checkIndex == that.checkIndex;
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

    }

    class IfSame<B> implements TypeRewriteRule {

        private final Type<B> targetType;
        private final RewriteResult<B, ?> value;
        private final int hashCode;

        public IfSame(final Type<B> targetType, final RewriteResult<B, ?> value) {
            this.targetType = targetType;
            this.value = value;
            hashCode = Objects.hash(targetType, value);
        }

        @Override
        public <A> Optional<RewriteResult<A, ?>> rewrite(final Type<A> type) {
            return type.ifSame(targetType, value);
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof final IfSame<?> that)) {
                return false;
            }
            return Objects.equals(targetType, that.targetType) && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

    }

}
