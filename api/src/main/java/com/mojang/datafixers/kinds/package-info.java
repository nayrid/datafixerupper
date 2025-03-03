/**
 * Contains common type classes and interfaces that allow the use of higher kinded types.
 *
 * <p>A higher kinded type, or type constructor, is a type that accepts type parameters. For example, the
 * parameterized type {@code List<String>} is an application of the type constructor {@code List<_>} with the
 * type {@link java.lang.String}. In type theory, it is said that the <em>kind</em> of the type {@code List<_>}
 * is {@code * -> *}, so called because it accepts one (non-higher kinded) type argument and produces a type.
 *
 * <p>Most of the type classes defined in this package are for types of the kind {@code * -> *} (the kind {@code *}
 * is the kind of regular types). For types that take more than one type parameter, such as
 * {@link com.mojang.datafixers.util.Either} or {@link java.util.function.Function} (both of kind {@code * -> * -> *}),
 * the first type argument is fixed when defining type classes.
 *
 * <h2>Type Classes</h2>
 *
 * <p>A type class is similar to the concept of a Java interface. Conceptually, a type class defines a collection of
 * functions that must be implemented by any <em>instances</em> of that type classes. However, unlike Java interfaces,
 * instances of type classes are <em>not</em> objects. Rather, a type class instance is a singleton value that is
 * associated with the type that the instance is defined for. As well, the functions defined in type classes are often
 * "static"; that is, they often do not take a parameter of the type they are defined for. For example, the type
 * {@link com.mojang.serialization.DataResult} has a type class instance {@link com.mojang.serialization.DataResult.Instance}
 * which implements both the {@link com.mojang.datafixers.kinds.Functor} and {@link com.mojang.datafixers.kinds.Applicative}
 * type classes.
 *
 * <p>Programmers familiar with the Rust programming language may recognize this concept of type classes, as they are
 * extremely similar to the <a href="https://doc.rust-lang.org/rust-by-example/trait.html">Rust concept of traits</a>.
 * Programmers familiar with Haskell may also recognize the concept of type classes, as Haskell itself has a concept
 * of type classes.
 *
 * <h2>Writing Higher Kinded Types</h2>
 *
 * <p>Writing code that is generic over different applications of higher kinded types poses a difficulty in Java,
 * as the language does not have native support for declaring type variables of higher kinds. In order to address this
 * difficulty, DFU provides two sets of interfaces.
 *
 * <p>The {@link com.mojang.datafixers.kinds.K1} and {@link com.mojang.datafixers.kinds.K2} interfaces represent
 * higher kinded types that take one and two type parameters, respectively. Each higher kinded type declares a
 * nested <em>witness type</em> that inherits from either {@code K1} or {@code K2} depending on the number of
 * type parameters that are required to be bound. These witness types are typically empty classes and are never
 * instantiated. Rather, they are used purely to represent the higher kinded type in places where the unapplied
 * type constructor must be passed. In DFU, these nested witness types are called {@code Mu}, or some variation of.
 * Note that sometimes witness type interfaces in DFU themselves declare type parameters; such a witness represents
 * a partially applied version of the higher kinded type.
 *
 * <p>The {@link com.mojang.datafixers.kinds.App} and {@link com.mojang.datafixers.kinds.App2} interfaces are used
 * to apply one or two types to a type constructor, respectively. The first parameter to both interfaces is a witness
 * type of a type constructor (of one or two parameters, respectively). The remaining parameters correspond to the
 * type parameters declared in that type constructor. In this fashion, different applications of a given higher kinded
 * type may be written using different parameterizations of {@code App}. For example, the type {@code DataResult<String>}
 * may be written as {@code App<DataResult.Mu, String>}, and in general the type {@code F<T>} may be written as
 * {@code App<F.Mu, T>}.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Type_class">Type Class</a>
 */
package com.mojang.datafixers.kinds;
