import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilsTest {
    @Test
    fun testSignum() {
        assertEquals(1, signum(566L))
        assertEquals(-1, signum(-566L))
        assertEquals(0, signum(0L))
        assertEquals(0, signum(-0L))
    }

    @Test
    fun testShiftList() {
        assertEquals(listOf<Int>(), shiftedList(listOf<Int>()))
        assertEquals(listOf(1), shiftedList(listOf(1)))
        assertEquals(listOf(2, 1), shiftedList(listOf(1, 2)))
        assertEquals(listOf(2, 3, 1), shiftedList(listOf(1, 2, 3)))
        assertEquals(listOf(2, 3, 4, 5, 6, 1), shiftedList(listOf(1, 2, 3, 4, 5, 6)))
    }
}

class QuartersTests {
    @Test
    fun testQuarters() {
        assertEquals(Quarter.FIRST, quarterOf(1, 1))
        assertEquals(Quarter.SECOND, quarterOf(-1, 1))
        assertEquals(Quarter.THIRD, quarterOf(-1, -1))
        assertEquals(Quarter.FOURTH, quarterOf(1, -1))


        assertEquals(Quarter.FIRST, quarterOf(1, 0))
        assertEquals(Quarter.SECOND, quarterOf(0, 1))
        assertEquals(Quarter.THIRD, quarterOf(-1, 0))
        assertEquals(Quarter.FOURTH, quarterOf(0, -1))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testZeroPointQuarter() {
        quarterOf(0, 0)
    }

    @Test
    fun testOrder() {
        assertTrue(Quarter.FIRST < Quarter.SECOND)
        assertTrue(Quarter.SECOND < Quarter.THIRD)
        assertTrue(Quarter.THIRD < Quarter.FOURTH)
    }
}

class RationalTests {
    @Test
    fun testSignum() {
        assertEquals(1, Rational(1, 2).signum)
        assertEquals(1, Rational(-1, -2).signum)
        assertEquals(-1, Rational(1, -2).signum)
        assertEquals(-1, Rational(-1, 2).signum)
        assertEquals(0, Rational(0, 2).signum)
        assertEquals(0, Rational(0, -2).signum)
        // infinity is signed
        assertEquals(1, Rational(2, 0).signum)
        assertEquals(-1, Rational(-2, 0).signum)
    }

    @Test
    fun testOrder() {
        assertTrue(Rational(1, 1) == Rational(2, 2))
        assertTrue(Rational(-1, -1) == Rational(-2, -2))
        assertTrue(Rational(-1, 1) == Rational(2, -2))
        assertTrue(Rational(1, 0) == Rational(2, 0))
        assertTrue(Rational(-1, 0) == Rational(-2, 0))
        assertTrue(Rational(1, 0) != Rational(-1, 0))
        assertTrue(Rational(1, 2) < Rational(1, 1))
        assertTrue(Rational(1, 2) < Rational(2, 2))
        assertTrue(Rational(1, 2) > Rational(2, -2))
        assertTrue(Rational(-1, 0) < Rational(1, 2))
        assertTrue(Rational(1, 0) > Rational(1, 2))
        assertTrue(Rational(1, 0) > Rational(-1, 0))
    }
}

class Vector2DTests {
    @Test
    fun testDotProduct() {
        assertEquals(4, Vector2D(1, 1) dot Vector2D(2, 2))
        assertEquals(2, Vector2D(-1, -1) dot Vector2D(-1, -1))
        assertEquals(-4, Vector2D(-1, -1) dot Vector2D(2, 2))
        assertEquals(0, Vector2D(-1, -1) dot Vector2D(-1, 1))
    }

    @Test
    fun testCrossProduct() {
        assertEquals(0, Vector2D(1, 1) cross Vector2D(2, 2))
        assertEquals(4, Vector2D(3, 1) cross Vector2D(2, 2))
        assertEquals(0, Vector2D(1, 1) cross Vector2D(-2, -2))
        assertEquals(4, Vector2D(1, 1) cross Vector2D(-2, 2))
        assertEquals(-4, Vector2D(-2, 2) cross Vector2D(1, 1))
    }
}

class AngleTests {

    private fun signum(value: Int) = when {
        value > 0 -> 1
        value < 0 -> -1
        else -> 0
    }

    @Test
    fun testOrder() {
        val correctOrder = listOf(
                Angle(1, 0),
                Angle(2, 1),
                Angle(1, 1),
                Angle(1, 2),
                Angle(0, 1),
                Angle(-1, 2),
                Angle(-1, 1),
                Angle(-2, 1),
                Angle(-1, 0),
                Angle(-2, -1),
                Angle(-1, -1),
                Angle(-1, -2),
                Angle(0, -1),
                Angle(1, -2),
                Angle(1, -1),
                Angle(2, -1)
        )
        for (i in correctOrder.indices) {
            for (j in correctOrder.indices) {
                assertEquals(signum(i.compareTo(j)), signum(correctOrder[i].compareTo(correctOrder[j])))
            }
        }
    }

    @Test
    fun testAngleBetweenVectors() {
        assertEquals(Angle(0, 1), Angle.between(Vector2D(1, 1), Vector2D(-1, 1)))
        assertEquals(Angle(-1, 0), Angle.between(Vector2D(1, 0), Vector2D(-1, 0)))
        assertEquals(Angle(-1, 0), Angle.between(Vector2D(0, 1), Vector2D(0, -1)))
        assertEquals(Angle(-1, 0), Angle.between(Vector2D(0, -1), Vector2D(0, 1)))
        assertEquals(Angle(1, 0), Angle.between(Vector2D(0, -1), Vector2D(0, -1)))
        assertEquals(Angle(1, 0), Angle.between(Vector2D(14, -91), Vector2D(14, -91)))
    }
}


class SolutionTests {
    @Test
    fun test1() {
        val vectors = listOf(
                Vector2D(-1, 0),
                Vector2D(0, -1),
                Vector2D(1, 0),
                Vector2D(1, 1)
        )
        val correctAnswers = listOf(Pair(3, 4), Pair(4, 3))
        assertTrue(correctAnswers.contains(solve(vectors)))
    }

    @Test
    fun test2() {
        val vectors = listOf(
                Vector2D(-1, 0),
                Vector2D(0, -1),
                Vector2D(1, 0),
                Vector2D(1, 1),
                Vector2D(-4, -5),
                Vector2D(-4, -6)
        )
        val correctAnswers = listOf(Pair(6, 5), Pair(5, 6))
        assertTrue(correctAnswers.contains(solve(vectors)))
    }
}

