package silentorb.imp.library.standard.math

import silentorb.imp.core.*
import silentorb.imp.execution.CompleteFunction

const val mathPath = "$standardLibraryPath.math"

fun mathUnarySignature(type: TypePair) = CompleteSignature(
    parameters = listOf(
        CompleteParameter("first", type)
    ),
    output = type
)

fun mathTernarySignature(type: TypePair) = CompleteSignature(
    parameters = listOf(
        CompleteParameter("first", type),
        CompleteParameter("second", type)
    ),
    output = type
)

fun mathVariadicSignature(type: TypePair) = CompleteSignature(
    isVariadic = true,
    parameters = listOf(
        CompleteParameter("values", type)
    ),
    output = type
)

typealias UnaryMathOperation<T> = (a: T) -> T
typealias TernaryMathOperation<T> = (a: T, b: T) -> T
typealias VariadicMathOperation<T> = (a: List<T>) -> T

fun <T> unaryImplementation(action: UnaryMathOperation<T>): (Map<Key, Any>) -> T =
    { values ->
      val a = values["first"] as T
      action(a)
    }

fun <T> ternaryImplementation(action: TernaryMathOperation<T>): (Map<Key, Any>) -> T =
    { values ->
      val a = values["first"] as T
      val b = values["second"] as T
      action(a, b)
    }

fun <T> variadicImplementation(action: VariadicMathOperation<T>): (Map<Key, Any>) -> T =
    { values ->
      val a = values["values"] as List<T>
      action(a)
    }

fun <T : Any> operations(
    subtraction: TernaryMathOperation<T>,
    division: TernaryMathOperation<T>,
    modulus: TernaryMathOperation<T>,
    addition: VariadicMathOperation<T>,
    multiplication: VariadicMathOperation<T>,
    negate: UnaryMathOperation<T>,
    typePair: TypePair
): List<CompleteFunction> {
  val unarySignature = mathUnarySignature(typePair)
  val ternarySignature = mathTernarySignature(typePair)
  val variadicSignature = mathVariadicSignature(typePair)

  return listOf(
      "-" to subtraction,
      "/" to division,
      "%" to modulus
  )
      .map { (name, implementation) ->
        CompleteFunction(
            path = PathKey(mathPath, name),
            signature = ternarySignature,
            implementation = ternaryImplementation(implementation)
        )
      } +
      listOf(
          "+" to addition,
          "*" to multiplication
      )
          .map { (name, implementation) ->
            CompleteFunction(
                path = PathKey(mathPath, name),
                signature = variadicSignature,
                implementation = variadicImplementation(implementation)
            )
          } +
      CompleteFunction(
          path = PathKey(mathPath, "-"),
          signature = unarySignature,
          implementation = unaryImplementation(negate)
      )
}

fun mathOperators() =

    listOf<CompleteFunction>() +

        operations<Int>(
            { a, b -> a - b },
            { a, b -> a / b },
            { a, b -> a % b },
            { a -> a.sum() },
            { a -> a.reduce { b, c -> b * c } },
            { -it },
            intType) +

        operations<Float>(
            { a, b -> a - b },
            { a, b -> a / b },
            { a, b -> a % b },
            { a -> a.sum() },
            { a -> a.reduce { b, c -> b * c } },
            { -it },
            floatType) +

        operations<Double>(
            { a, b -> a - b },
            { a, b -> a / b },
            { a, b -> a % b },
            { a -> a.sum() },
            { a -> a.reduce { b, c -> b * c } },
            { -it },
            doubleType)
