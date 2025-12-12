package uk.ac.lshtm.keppel.cli.util

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.lessThan
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test
import kotlin.math.sqrt
import kotlin.random.Random
import kotlin.time.measureTime

class ParallelFlatMapTest {

    @Test
    fun `creates a set from the sets returned from the operation`() {
        val sequence = sequenceOf(1, 2, 3, 4)
        val evens = sequence.parallelFlatMap {
            if (it % 2 == 0) {
                listOf(it)
            } else {
                emptyList()
            }
        }

        assertThat(evens.toList(), equalTo(listOf(2, 4)))
    }

    @Test
    fun `does not leave items behind once there are no more windows`() {
        val sequence = sequenceOf(1, 2, 3, 4)
        sequence.parallelFlatMap { listOf(it) }
        assertThat(sequence.parallelFlatMap(windowSize = 4) { listOf(it) }.count(), equalTo(4))
    }

    @Test
    fun `does not leave items behind from an operation`() {
        val sequence = sequenceOf(1, 2, 3, 4)
        sequence.parallelFlatMap { listOf(it) }
        assertThat(sequence.parallelFlatMap(windowSize = 4) { listOf(it, it) }.count(), equalTo(8))
    }

    @Test
    fun `beats sequential flatMap for 1000+ 1 millisecond operations with default parallelism and window size`() {
        val list = generateSequence { Random.nextInt() }.take(1000).toList()
        val operation: (Int) -> Iterable<Int> = {
            Thread.sleep(1)
            listOf(it)
        }

        val parallelTime = measureTime {
            list.asSequence().parallelFlatMap(operation = operation).forEach { }
        }

        val sequentialTime = measureTime {
            list.asSequence().flatMap(operation).forEach { }
        }

        assertThat(parallelTime.inWholeMilliseconds, lessThan(sequentialTime.inWholeMilliseconds))
    }
}