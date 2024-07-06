
/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("NOTHING_TO_INLINE")

package androidx.graphics.shapes

import androidx.collection.FloatFloatPair
import kotlin.math.sqrt

internal typealias Point = FloatFloatPair

internal val Point.x
    get() = first

internal val Point.y
    get() = second

internal fun Point.copy(x: Float = first, y: Float = second) = Point(x, y)

/**
 * The magnitude of the Point, which is the distance of this point from (0, 0).
 *
 * If you need this value to compare it to another [Point]'s distance, consider using
 * [getDistanceSquared] instead, since it is cheaper to compute.
 */
internal fun Point.getDistance() = sqrt(x * x + y * y)

/**
 * The square of the magnitude (which is the distance of this point from (0, 0)) of the Point.
 *
 * This is cheaper than computing the [getDistance] itself.
 */
internal fun Point.getDistanceSquared() = x * x + y * y

internal fun Point.dotProduct(other: Point) = x * other.x + y * other.y

internal fun Point.dotProduct(otherX: Float, otherY: Float) = x * otherX + y * otherY

/**
 * Compute the Z coordinate of the cross product of two vectors, to check if the second vector is
 * going clockwise ( > 0 ) or counterclockwise (< 0) compared with the first one. It could also be
 * 0, if the vectors are co-linear.
 */
internal fun Point.clockwise(other: Point) = x * other.y - y * other.x > 0

/** Returns unit vector representing the direction to this point from (0, 0) */
internal fun Point.getDirection() = run {
    val d = this.getDistance()
    require(d > 0f) { "Can't get the direction of a 0-length vector" }
    this / d
}

/**
 * Unary negation operator.
 *
 * Returns a Point with the coordinates negated.
 *
 * If the [Point] represents an arrow on a plane, this operator returns the same arrow but pointing
 * in the reverse direction.
 */
internal operator fun Point.unaryMinus(): Point = Point(-x, -y)

/**
 * Binary subtraction operator.
 *
 * Returns a Point whose [x] value is the left-hand-side operand's [x] minus the right-hand-side
 * operand's [x] and whose [y] value is the left-hand-side operand's [y] minus the right-hand-side
 * operand's [y].
 */
internal operator fun Point.minus(other: Point): Point = Point(x - other.x, y - other.y)

/**
 * Binary addition operator.
 *
 * Returns a Point whose [x] value is the sum of the [x] values of the two operands, and whose [y]
 * value is the sum of the [y] values of the two operands.
 */
internal operator fun Point.plus(other: Point): Point = Point(x + other.x, y + other.y)

/**
 * Multiplication operator.
 *
 * Returns a Point whose coordinates are the coordinates of the left-hand-side operand (a Point)
 * multiplied by the scalar right-hand-side operand (a Float).
 */
internal operator fun Point.times(operand: Float): Point = Point(x * operand, y * operand)

/**
 * Division operator.
 *
 * Returns a Point whose coordinates are the coordinates of the left-hand-side operand (a Point)
 * divided by the scalar right-hand-side operand (a Float).
 */
internal operator fun Point.div(operand: Float): Point = Point(x / operand, y / operand)

/**
 * Modulo (remainder) operator.
 *
 * Returns a Point whose coordinates are the remainder of dividing the coordinates of the
 * left-hand-side operand (a Point) by the scalar right-hand-side operand (a Float).
 */
internal operator fun Point.rem(operand: Float) = Point(x % operand, y % operand)

/**
 * Linearly interpolate between two Points.
 *
 * The [fraction] argument represents position on the timeline, with 0.0 meaning that the
 * interpolation has not started, returning [start] (or something equivalent to [start]), 1.0
 * meaning that the interpolation has finished, returning [stop] (or something equivalent to
 * [stop]), and values in between meaning that the interpolation is at the relevant point on the
 * timeline between [start] and [stop]. The interpolation can be extrapolated beyond 0.0 and 1.0, so
 * negative values and values greater than 1.0 are valid (and can easily be generated by curves).
 *
 * Values for [fraction] are usually obtained from an [Animation<Float>], such as an
 * `AnimationController`.
 */
internal fun interpolate(start: Point, stop: Point, fraction: Float): Point {
    return Point(interpolate(start.x, stop.x, fraction), interpolate(start.y, stop.y, fraction))
}

internal fun Point.transformed(f: PointTransformer): Point {
    val result = f.transform(x, y)
    return Point(result.first, result.second)
}