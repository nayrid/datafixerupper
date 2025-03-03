/**
 * Contains types used for serializing and deserializing data structures. This includes adapters for generating and
 * parsing serialized forms, such as {@link com.mojang.serialization.DynamicOps} and {@link com.mojang.serialization.Dynamic};
 * and schemas for encoding and decoding data structures, such as {@link com.mojang.serialization.Codec} and
 * {@link com.mojang.serialization.MapCodec}.
 *
 * <h2>Serialization Adapters</h2>
 *
 * <p>Serialization adapters are low level interfaces used to marshall simple data types into a joint serialized
 * form. Programmers should generally not use these adapters to directly serialize values, and should prefer using
 * the {@link com.mojang.serialization.Codec} abstraction unless performance requires using the adapter directly.
 *
 * <p>Instances of {@link com.mojang.serialization.DynamicOps} allow for the extraction and insertion of simple
 * data types to an underlying serialized form such as <a href="https://www.json.org/json-en.html">JSON</a> or
 * <a href="https://minecraft.wiki/NBT">NBT</a>. Low-level serialization and deserialization methods in DFU all take a
 * {@link com.mojang.serialization.DynamicOps} object as an argument, through which serialization and deserialization
 * should be delegated.
 *
 * <p>Instances of {@link com.mojang.serialization.Dynamic} are type-safe wrappers for serialized values which are
 * guaranteed to be associated with a corresponding {@link com.mojang.serialization.DynamicOps} instance. When returning
 * or accepting a serialized value, programmers may find it useful to use instances of {@link com.mojang.serialization.Dynamic}
 * instead of manually keeping track of {@link com.mojang.serialization.DynamicOps} instances. However, usage of these
 * objects is uncommon outside of low level serialization code in practice.
 *
 * <h2>Encoders and Decoders</h2>
 *
 * <p>{@linkplain com.mojang.serialization.Encoder Encoders} and {@linkplain com.mojang.serialization.Decoder decoders}
 * are high level APIs which can be used to construct complex serialization and deserialization routines for structurally
 * hierarchical objects. Programmers should generally prefer using {@link com.mojang.serialization.Codec} to actually
 * build these routines, since typically both encoding and decoding operations are desired.
 *
 * <p>General purpose implementations of {@link com.mojang.serialization.Codec} and its supporting types are provided
 * for programmer usage, although programmers are free to provide additional implementations for most types. Note that
 * additional implementations will not be drop-in replaceable with code that uses the provided implementation.
 *
 * <h2>Building Codecs</h2>
 *
 * <p>Codecs allow the programmer to define a bidirectional transformation for a record-like object that is generic
 * over any hierarchical serialization format. The programmer starts with one of the provided primitive codecs and chains
 * composition methods to build arbitrarily complex structures. Commonly used codec methods include:
 *
 * <ul>
 *     <li>
 *         {@link com.mojang.serialization.Codec#listOf()} builds a codec for a list from a codec for its elements.
 *     </li>
 *     <li>
 *         {@link com.mojang.serialization.Codec#unboundedMap(com.mojang.serialization.Codec, com.mojang.serialization.Codec)}
 *         builds a codec for an arbitrary-keyed map based on codecs for the key and value types.
 *     </li>
 *     <li>
 *         {@link com.mojang.serialization.Codec#fieldOf(java.lang.String)} builds a codec for a key in a record from
 *         a codec for the value of that field.
 *     </li>
 *     <li>
 *         {@link com.mojang.serialization.Codec#optionalFieldOf(java.lang.String)} builds a codec for an optional
 *         serialized value based on a codec for the present value.
 *     </li>
 *     <li>
 *         {@link com.mojang.serialization.Codec#xmap(java.util.function.Function, java.util.function.Function)} and
 *         related methods use a bidirectional transformation to produce a codec for another type.
 *     </li>
 *     <li>
 *         {@link com.mojang.serialization.Codec#dispatch(java.util.function.Function, java.util.function.Function)} and
 *         related methods allow for dynamically dispatching a polymorphic object/serialized form based on a type key.
 *     </li>
 * </ul>
 *
 * <p>To build a codec for a record-like object, the {@link com.mojang.serialization.codecs.RecordCodecBuilder} class
 * is used to collect codecs for each record element. The general form for constructing a record codec is predictable
 * and is typically as follows.
 *
 * <pre><code>
 * RecordCodecBuilder.create(inst -> inst.group(
 *     field1Codec.fieldOf("Field1").forGetter(r -> r.field1),
 *     field2Codec.fieldOf("Field2").forGetter(r -> r.field2),
 *     ...
 *     fieldNCodec.fieldOf("FieldN").forGetter(r -> r.field3)
 * ).apply(inst, SomeRecord::new))</code></pre>
 *
 * <p>Codecs are immutable and should be stored in a static location once constructed.
 */
package com.mojang.serialization;
