import java.lang.Math.abs

/**
 * Represents quarters of plane R^2
 *
 * @property tgFinite true if y/x if finite for every point (x, y) from this quarter
 * @constructor Creates an empty group.
 */
enum class Quarter(val tgFinite: Boolean) {
    FIRST(true),
    SECOND(false),
    THIRD(true),
    FOURTH(false);
}

/**
 * Represents figures from Q + {+Inf, -Inf}
 *
 * @property numerator is numerator of represented figure. May be negative.
 * @property denominator is denominator of represented figure. May be negative.
 * @property signum is signum of represented value (-1, 0 or 1)
 * @constructor Creates Representation of numerator/denominator
 */
data class Rational(
        private val numerator: Long,
        private val denominator: Long
) : Comparable<Rational> {

    val signum = when {
        denominator != 0L -> signum(numerator) * signum(denominator)
        else -> signum(numerator)
    }

    override fun compareTo(other: Rational): Int {
        if (signum != other.signum) {
            return signum - other.signum
        }
        val difference = abs(numerator * other.denominator) - abs(other.numerator * denominator)
        return signum * signum(difference)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is Rational) false else compareTo(other) == 0
    }

    override fun hashCode(): Int {
        throw UnsupportedOperationException("It's not easy to define hash code for this class")
    }
}

/**
 * Represents figures from Q + {+Inf, -Inf}
 *
 * @property x is x cartesian coordinate
 * @property y is y cartesian coordinate
 * @property angle is angle between the vector and the X-axis when traversing counter-clockwise
 * @constructor Creates radius vector to point (x, y)
 */
data class Vector2D(private val x: Long, private val y: Long) {
    val angle = Angle(x, y)

    /**
     * Calculate cross product of vectors
     * @return z-coordinate of cross product of vectors
     */
    infix fun cross(other: Vector2D): Long {
        return x * other.y - y * other.x
    }

    /**
     * Calculate dot product of vectors
     * @return dot product
     */
    infix fun dot(other: Vector2D): Long {
        return x * other.x + y * other.y
    }
}

/**
 * Represents angle from [0, 2 * Pi)
 *
 * @property tg is tangent of angle
 * @property ctg is cotangent of angle
 * @constructor Creates angle between the radius vector to point (x, y) and the X-axis when
 *      traversing counter-clockwise
 */
class Angle(x: Long, y: Long) :
        Comparable<Angle> {
    private val tg = Rational(y, x)
    private val ctg = Rational(x, y)
    private val quarter = quarterOf(x, y)

    override fun compareTo(other: Angle): Int = when {
        quarter != other.quarter -> quarter.compareTo(other.quarter)
        quarter.tgFinite -> tg.compareTo(other.tg)
        else -> -ctg.compareTo(other.ctg) // cotangent is a decreasing function
    }

    override fun equals(other: Any?): Boolean {
        return if (other === null || other !is Angle) false else compareTo(other) == 0
    }

    override fun hashCode(): Int {
        throw UnsupportedOperationException("It's not easy to define hash code for this class")
    }

    companion object {
        /**
         * Calculate angle between vectors when traversing counter-clockwise
         * @return angle between vectors
         */
        fun between(v1: Vector2D, v2: Vector2D): Angle {
            val dotProduct = v1 dot v2 // = |V1|*|V2|*cos(a)
            val crossProduct = v1 cross v2// = |V1|*|V2|*sin(a)
            return Angle(dotProduct, crossProduct)
        }
    }

    override fun toString(): String {
        return "Angle(tg=$tg,ctg=$ctg)"
    }
}

/**
 * @return mathematical signum function of value
 */
fun signum(value: Long): Int {
    return when {
        value > 0 -> 1
        value < 0 -> -1
        else -> 0
    }
}

/**
 * @return quarter which contains point (x, y)
 */
fun quarterOf(x: Long, y: Long): Quarter {
    return when {
        x > 0 && y >= 0 -> Quarter.FIRST
        x <= 0 && y > 0 -> Quarter.SECOND
        x < 0 && y <= 0 -> Quarter.THIRD
        x >= 0 && y < 0 -> Quarter.FOURTH
        else -> throw IllegalArgumentException("Quarter isn't defined for point (0, 0)")
    }
}

/**
 * Shift list to left: x:tail -> tail:x
 * @return shifted list
 */
fun <E> shiftedList(list: List<E>): List<E> =
        if (list.size > 1) list.drop(1) + list.first() else list

/**
 * Finds pair of vectors which have minimal unsigned angle between them
 * @return pair of indexes of vectors in given list
 */
fun solve(vectors: List<Vector2D>): Pair<Int, Int>? {
    val sortedList = vectors.sortedBy { it.angle }
    val result = sortedList.zip(shiftedList(sortedList))
            .minBy { (x, y) -> Angle.between(x, y) }
    return when (result) {
        null -> null
        else -> Pair(vectors.indexOf(result.first) + 1, vectors.indexOf(result.second) + 1)
    }
}

fun main(args: Array<String>) {
    val n = readLine()!!.toInt()
    val vectors = ArrayList<Vector2D>()
    for (i in 0 until n) {
        val (x, y) = readLine()!!.split(' ').map(String::toLong)
        vectors.add(Vector2D(x, y))
    }
    val result = solve(vectors) ?: throw IllegalArgumentException("Wrong input: n < 2")
    print("${result.first} ${result.second}")
}