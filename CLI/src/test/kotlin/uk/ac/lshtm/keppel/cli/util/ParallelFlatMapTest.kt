package uk.ac.lshtm.keppel.cli.util

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.junit.Test

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
}