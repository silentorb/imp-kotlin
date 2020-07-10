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

typealias UnaryMathOperation<T> = (a: T) -> T
typealias TernaryMathOperation<T> = (a: T, b: T) -> T

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

fun <T : Any> operations(
    addition: TernaryMathOperation<T>,
    subtraction: TernaryMathOperation<T>,
    multiplication: TernaryMathOperation<T>,
    division: TernaryMathOperation<T>,
    modulus: TernaryMathOperation<T>,
    negate: UnaryMathOperation<T>,
    typePair: TypePair
): List<CompleteFunction> {
  val unarySignature = mathUnarySignature(typePair)
  val ternarySignature = mathTernarySignature(typePair)

  return listOf(
      "+" to addition,
      "-" to subtraction,
      "*" to multiplication,
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
      CompleteFunction(
          path = PathKey(mathPath, "-"),
          signature = unarySignature,
          implementation = unaryImplementation(negate)
      )
}

fun mathOperators() =

    listOf<CompleteFunction>() +

        operations<Int>(
            { a, b -> a + b },
            { a, b -> a - b },
            { a, b -> a * b },
            { a, b -> a / b },
            { a, b -> a % b },
            { -it },
            intType) +

        operations<Float>(
            { a, b -> a + b },
            { a, b -> a - b },
            { a, b -> a * b },
            { a, b -> a / b },
            { a, b -> a % b },
            { -it },
            floatType) +

        operations<Double>(
            { a, b -> a + b },
            { a, b -> a - b },
            { a, b -> a * b },
            { a, b -> a / b },
            { a, b -> a % b },
            { -it },
            doubleType)
