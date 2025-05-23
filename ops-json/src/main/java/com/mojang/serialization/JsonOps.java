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

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Pair;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.Nullable;

/**
 * A {@link DynamicOps} adapter for JSON. Instances of this class serialize data to and from
 * JSON.
 */
public class JsonOps implements DynamicOps<JsonElement> {

    /**
     * The singleton {@link JsonOps} instance for uncompressed JSON serialization.
     *
     * @see #compressMaps()
     */
    public static final JsonOps INSTANCE = new JsonOps(false);

    /**
     * The singleton {@link JsonOps} instance for compressed JSON serialization.
     *
     * @see #compressMaps()
     */
    public static final JsonOps COMPRESSED = new JsonOps(true);

    private final boolean compressed;

    /**
     * Constructs a {@link JsonOps} with the given settings.
     *
     * @param compressed Whether this {@link JsonOps} should use compressed serialization.
     */
    protected JsonOps(final boolean compressed) {
        this.compressed = compressed;
    }

    @Override
    public JsonElement empty() {
        return JsonNull.INSTANCE;
    }

    @Override
    public <U> U convertTo(final DynamicOps<U> outOps, final JsonElement input) {
        if (input instanceof JsonObject) {
            return convertMap(outOps, input);
        }
        if (input instanceof JsonArray) {
            return convertList(outOps, input);
        }
        if (input instanceof JsonNull) {
            return outOps.empty();
        }
        final JsonPrimitive primitive = input.getAsJsonPrimitive();
        if (primitive.isString()) {
            return outOps.createString(primitive.getAsString());
        }
        if (primitive.isBoolean()) {
            return outOps.createBoolean(primitive.getAsBoolean());
        }
        final BigDecimal value = primitive.getAsBigDecimal();
        try {
            final long l = value.longValueExact();
            if ((byte) l == l) {
                return outOps.createByte((byte) l);
            }
            if ((short) l == l) {
                return outOps.createShort((short) l);
            }
            if ((int) l == l) {
                return outOps.createInt((int) l);
            }
            return outOps.createLong(l);
        } catch (final ArithmeticException e) {
            final double d = value.doubleValue();
            if ((float) d == d) {
                return outOps.createFloat((float) d);
            }
            return outOps.createDouble(d);
        }
    }

    @Override
    public DataResult<Number> getNumberValue(final JsonElement input) {
        if (input instanceof JsonPrimitive) {
            if (input.getAsJsonPrimitive().isNumber()) {
                return DataResult.success(input.getAsNumber());
            }
            if (compressed && input.getAsJsonPrimitive().isString()) {
                try {
                    return DataResult.success(Integer.parseInt(input.getAsString()));
                } catch (final NumberFormatException e) {
                    return DataResult.error(() -> "Not a number: " + e + " " + input);
                }
            }
        }
        return DataResult.error(() -> "Not a number: " + input);
    }

    @Override
    public JsonElement createNumeric(final Number i) {
        return new JsonPrimitive(i);
    }

    @Override
    public DataResult<Boolean> getBooleanValue(final JsonElement input) {
        if (input instanceof JsonPrimitive && input.getAsJsonPrimitive().isBoolean()) {
            return DataResult.success(input.getAsBoolean());
        }
        return DataResult.error(() -> "Not a boolean: " + input);
    }

    @Override
    public JsonElement createBoolean(final boolean value) {
        return new JsonPrimitive(value);
    }

    @Override
    public DataResult<String> getStringValue(final JsonElement input) {
        if (input instanceof JsonPrimitive) {
            if (input.getAsJsonPrimitive().isString() || input.getAsJsonPrimitive().isNumber() && compressed) {
                return DataResult.success(input.getAsString());
            }
        }
        return DataResult.error(() -> "Not a string: " + input);
    }

    @Override
    public JsonElement createString(final String value) {
        return new JsonPrimitive(value);
    }

    @Override
    public DataResult<JsonElement> mergeToList(final JsonElement list, final JsonElement value) {
        if (!(list instanceof JsonArray) && list != empty()) {
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
        }

        final JsonArray result = new JsonArray();
        if (list != empty()) {
            result.addAll(list.getAsJsonArray());
        }
        result.add(value);
        return DataResult.success(result);
    }

    @Override
    public DataResult<JsonElement> mergeToList(final JsonElement list, final List<JsonElement> values) {
        if (!(list instanceof JsonArray) && list != empty()) {
            return DataResult.error(() -> "mergeToList called with not a list: " + list, list);
        }

        final JsonArray result = new JsonArray();
        if (list != empty()) {
            result.addAll(list.getAsJsonArray());
        }
        values.forEach(result::add);
        return DataResult.success(result);
    }

    @Override
    public DataResult<JsonElement> mergeToMap(final JsonElement map, final JsonElement key, final JsonElement value) {
        if (!(map instanceof JsonObject) && map != empty()) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }
        if (!(key instanceof JsonPrimitive) || !key.getAsJsonPrimitive().isString() && !compressed) {
            return DataResult.error(() -> "key is not a string: " + key, map);
        }

        final JsonObject output = new JsonObject();
        if (map != empty()) {
            map.getAsJsonObject().entrySet().forEach(entry -> output.add(entry.getKey(), entry.getValue()));
        }
        output.add(key.getAsString(), value);

        return DataResult.success(output);
    }

    @Override
    public DataResult<JsonElement> mergeToMap(final JsonElement map, final MapLike<JsonElement> values) {
        if (!(map instanceof JsonObject) && map != empty()) {
            return DataResult.error(() -> "mergeToMap called with not a map: " + map, map);
        }

        final JsonObject output = new JsonObject();
        if (map != empty()) {
            map.getAsJsonObject().entrySet().forEach(entry -> output.add(entry.getKey(), entry.getValue()));
        }

        final List<JsonElement> missed = Lists.newArrayList();

        values.entries().forEach(entry -> {
            final JsonElement key = entry.first();
            if (!(key instanceof JsonPrimitive) || !key.getAsJsonPrimitive().isString() && !compressed) {
                missed.add(key);
                return;
            }
            output.add(key.getAsString(), entry.second());
        });

        if (!missed.isEmpty()) {
            return DataResult.error(() -> "some keys are not strings: " + missed, output);
        }

        return DataResult.success(output);
    }

    @Override
    public DataResult<Stream<Pair<JsonElement, JsonElement>>> getMapValues(final JsonElement input) {
        if (!(input instanceof JsonObject)) {
            return DataResult.error(() -> "Not a JSON object: " + input);
        }
        return DataResult.success(input.getAsJsonObject().entrySet().stream().map(entry -> Pair.of(new JsonPrimitive(entry.getKey()), entry.getValue() instanceof JsonNull ? null : entry.getValue())));
    }

    @Override
    public DataResult<Consumer<BiConsumer<JsonElement, JsonElement>>> getMapEntries(final JsonElement input) {
        if (!(input instanceof JsonObject)) {
            return DataResult.error(() -> "Not a JSON object: " + input);
        }
        return DataResult.success(c -> {
            for (final Map.Entry<String, JsonElement> entry : input.getAsJsonObject().entrySet()) {
                c.accept(createString(entry.getKey()), entry.getValue() instanceof JsonNull ? null : entry.getValue());
            }
        });
    }

    @Override
    public DataResult<MapLike<JsonElement>> getMap(final JsonElement input) {
        if (!(input instanceof JsonObject)) {
            return DataResult.error(() -> "Not a JSON object: " + input);
        }
        final JsonObject object = input.getAsJsonObject();
        return DataResult.success(new MapLike<JsonElement>() {
            @Override
            public @Nullable JsonElement get(final JsonElement key) {
                final JsonElement element = object.get(key.getAsString());
                if (element instanceof JsonNull) {
                    return null;
                }
                return element;
            }

            @Override
            public @Nullable JsonElement get(final String key) {
                final JsonElement element = object.get(key);
                if (element instanceof JsonNull) {
                    return null;
                }
                return element;
            }

            @Override
            public Stream<Pair<JsonElement, JsonElement>> entries() {
                return object.entrySet().stream().map(e -> Pair.of(new JsonPrimitive(e.getKey()), e.getValue()));
            }

            @Override
            public String toString() {
                return "MapLike[" + object + "]";
            }
        });
    }

    @Override
    public JsonElement createMap(final Stream<Pair<JsonElement, JsonElement>> map) {
        final JsonObject result = new JsonObject();
        map.forEach(p -> result.add(p.first().getAsString(), p.second()));
        return result;
    }

    @Override
    public DataResult<Stream<JsonElement>> getStream(final JsonElement input) {
        if (input instanceof JsonArray) {
            return DataResult.success(StreamSupport.stream(input.getAsJsonArray().spliterator(), false).map(e -> e instanceof JsonNull ? null : e));
        }
        return DataResult.error(() -> "Not a json array: " + input);
    }

    @Override
    public DataResult<Consumer<Consumer<JsonElement>>> getList(final JsonElement input) {
        if (input instanceof JsonArray) {
            return DataResult.success(c -> {
                for (final JsonElement element : input.getAsJsonArray()) {
                    c.accept(element instanceof JsonNull ? null : element);
                }
            });
        }
        return DataResult.error(() -> "Not a json array: " + input);
    }

    @Override
    public JsonElement createList(final Stream<JsonElement> input) {
        final JsonArray result = new JsonArray();
        input.forEach(result::add);
        return result;
    }

    @Override
    public JsonElement remove(final JsonElement input, final String key) {
        if (input instanceof JsonObject) {
            final JsonObject result = new JsonObject();
            input.getAsJsonObject().entrySet().stream().filter(entry -> !Objects.equals(entry.getKey(), key)).forEach(entry -> result.add(entry.getKey(), entry.getValue()));
            return result;
        }
        return input;
    }

    @Override
    public String toString() {
        return "JSON";
    }

    @Override
    public ListBuilder<JsonElement> listBuilder() {
        return new ArrayBuilder();
    }

    private static final class ArrayBuilder implements ListBuilder<JsonElement> {
        private DataResult<JsonArray> builder = DataResult.success(new JsonArray(), Lifecycle.stable());

        @Override
        public DynamicOps<JsonElement> ops() {
            return INSTANCE;
        }

        @Override
        public ListBuilder<JsonElement> add(final JsonElement value) {
            builder = builder.map(b -> {
                b.add(value);
                return b;
            });
            return this;
        }

        @Override
        public ListBuilder<JsonElement> add(final DataResult<JsonElement> value) {
            builder = builder.apply2stable((b, element) -> {
                b.add(element);
                return b;
            }, value);
            return this;
        }

        @Override
        public ListBuilder<JsonElement> withErrorsFrom(final DataResult<?> result) {
            builder = builder.flatMap(r -> result.map(v -> r));
            return this;
        }

        @Override
        public ListBuilder<JsonElement> mapError(final UnaryOperator<String> onError) {
            builder = builder.mapError(onError);
            return this;
        }

        @Override
        public DataResult<JsonElement> build(final JsonElement prefix) {
            final DataResult<JsonElement> result = builder.flatMap(b -> {
                if (!(prefix instanceof JsonArray) && prefix != ops().empty()) {
                    return DataResult.error(() -> "Cannot append a list to not a list: " + prefix, prefix);
                }

                final JsonArray array = new JsonArray();
                if (prefix != ops().empty()) {
                    array.addAll(prefix.getAsJsonArray());
                }
                array.addAll(b);
                return DataResult.success(array, Lifecycle.stable());
            });

            builder = DataResult.success(new JsonArray(), Lifecycle.stable());
            return result;
        }
    }

    @Override
    public boolean compressMaps() {
        return compressed;
    }

    @Override
    public RecordBuilder<JsonElement> mapBuilder() {
        return new JsonRecordBuilder();
    }

    private class JsonRecordBuilder extends RecordBuilder.AbstractStringBuilder<JsonElement, JsonObject> {
        protected JsonRecordBuilder() {
            super(JsonOps.this);
        }

        @Override
        protected JsonObject initBuilder() {
            return new JsonObject();
        }

        @Override
        protected JsonObject append(final String key, final JsonElement value, final JsonObject builder) {
            builder.add(key, value);
            return builder;
        }

        @Override
        protected DataResult<JsonElement> build(final JsonObject builder, final JsonElement prefix) {
            if (prefix == null || prefix instanceof JsonNull) {
                return DataResult.success(builder);
            }
            if (prefix instanceof JsonObject) {
                final JsonObject result = new JsonObject();
                for (final Map.Entry<String, JsonElement> entry : prefix.getAsJsonObject().entrySet()) {
                    result.add(entry.getKey(), entry.getValue());
                }
                for (final Map.Entry<String, JsonElement> entry : builder.entrySet()) {
                    result.add(entry.getKey(), entry.getValue());
                }
                return DataResult.success(result);
            }
            return DataResult.error(() -> "mergeToMap called with not a map: " + prefix, prefix);
        }
    }

}
