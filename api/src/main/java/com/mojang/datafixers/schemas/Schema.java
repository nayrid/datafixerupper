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
package com.mojang.datafixers.schemas;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.families.RecursiveTypeFamily;
import com.mojang.datafixers.types.families.TypeFamily;
import com.mojang.datafixers.types.templates.RecursivePoint;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.types.templates.TypeTemplate;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

public class Schema {

    protected final Object2IntMap<String> RECURSIVE_TYPES = new Object2IntOpenHashMap<>();
    protected final Schema parent;
    private final Map<String, Supplier<TypeTemplate>> TYPE_TEMPLATES = Maps.newHashMap();
    private final Map<String, Type<?>> TYPES;
    private final int versionKey;
    private final String name;

    public Schema(final int versionKey, final Schema parent) {
        this.versionKey = versionKey;
        final int subVersion = DataFixUtils.getSubVersion(versionKey);
        name = "V" + DataFixUtils.getVersion(versionKey) + (subVersion == 0 ? "" : "." + subVersion);
        this.parent = parent;
        registerTypes(this, registerEntities(this), registerBlockEntities(this));
        TYPES = buildTypes();
    }

    protected Map<String, Type<?>> buildTypes() {
        final Map<String, Type<?>> types = Maps.newHashMap();

        final List<TypeTemplate> templates = Lists.newArrayList();

        for (final Object2IntMap.Entry<String> entry : RECURSIVE_TYPES.object2IntEntrySet()) {
            templates.add(DSL.check(entry.getKey(),
                entry.getIntValue(),
                getTemplate(entry.getKey())
            ));
        }

        final TypeTemplate choice = templates.stream().reduce(DSL::or).get();
        final TypeFamily family = new RecursiveTypeFamily(name, choice);

        for (final String name : TYPE_TEMPLATES.keySet()) {
            final Type<?> type;
            final int recurseId = RECURSIVE_TYPES.getOrDefault(name, -1);
            if (recurseId != -1) {
                type = family.apply(recurseId);
            } else {
                type = getTemplate(name).apply(family).apply(-1);
            }
            types.put(name, type);
        }
        return types;
    }

    public Set<String> types() {
        return TYPES.keySet();
    }

    public Type<?> getTypeRaw(final DSL.TypeReference type) {
        final String name = type.typeName();
        return TYPES.computeIfAbsent(name, key -> {
            throw new IllegalArgumentException("Unknown type: " + name);
        });
    }

    public Type<?> getType(final DSL.TypeReference type) {
        final String name = type.typeName();
        final Type<?> type1 = TYPES.computeIfAbsent(name, key -> {
            throw new IllegalArgumentException("Unknown type: " + name);
        });
        if (type1 instanceof RecursivePoint.RecursivePointType<?>) {
            return type1.findCheckedType(-1)
                .orElseThrow(() -> new IllegalStateException(
                    "Could not find choice type in the recursive type"));
        }
        return type1;
    }

    public TypeTemplate resolveTemplate(final String name) {
        return TYPE_TEMPLATES.getOrDefault(name, () -> {
            throw new IllegalArgumentException("Unknown type: " + name);
        }).get();
    }

    public TypeTemplate id(final String name) {
        final int id = RECURSIVE_TYPES.getOrDefault(name, -1);
        if (id != -1) {
            return DSL.id(id);
        }
        return getTemplate(name);
    }

    protected TypeTemplate getTemplate(final String name) {
        return DSL.named(name, resolveTemplate(name));
    }

    public Type<?> getChoiceType(final DSL.TypeReference type, final String choiceName) {
        final TaggedChoice.TaggedChoiceType<?> choiceType = findChoiceType(type);
        if (!choiceType.types().containsKey(choiceName)) {
            throw new IllegalArgumentException("Data fixer not registered for: " + choiceName + " in " + type.typeName());
        }
        return choiceType.types().get(choiceName);
    }

    public TaggedChoice.TaggedChoiceType<?> findChoiceType(final DSL.TypeReference type) {
        return getType(type).findChoiceType("id", -1)
            .orElseThrow(() -> new IllegalArgumentException("Not a choice type"));
    }

    public void registerTypes(final Schema schema,
                              final Map<String, Supplier<TypeTemplate>> entityTypes,
                              final Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        parent.registerTypes(schema, entityTypes, blockEntityTypes);
    }

    public Map<String, Supplier<TypeTemplate>> registerEntities(final Schema schema) {
        return parent.registerEntities(schema);
    }

    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(final Schema schema) {
        return parent.registerBlockEntities(schema);
    }

    public void registerSimple(final Map<String, Supplier<TypeTemplate>> map, final String name) {
        register(map, name, DSL::remainder);
    }

    public void register(final Map<String, Supplier<TypeTemplate>> map,
                         final String name,
                         final Function<String, TypeTemplate> template) {
        register(map, name, () -> template.apply(name));
    }

    public void register(final Map<String, Supplier<TypeTemplate>> map,
                         final String name,
                         final Supplier<TypeTemplate> template) {
        map.put(name, template);
    }

    public void registerType(final boolean recursive,
                             final DSL.TypeReference type,
                             final Supplier<TypeTemplate> template) {
        TYPE_TEMPLATES.put(type.typeName(), template);
        // TODO: calculate recursiveness instead of hardcoding
        if (recursive && !RECURSIVE_TYPES.containsKey(type.typeName())) {
            RECURSIVE_TYPES.put(type.typeName(), RECURSIVE_TYPES.size());
        }
    }

    public int getVersionKey() {
        return versionKey;
    }

    public Schema getParent() {
        return parent;
    }

}
