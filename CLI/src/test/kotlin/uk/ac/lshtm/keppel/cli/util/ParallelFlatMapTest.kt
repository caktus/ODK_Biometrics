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
}