package uk.ac.lshtm.keppel.cli.util

import java.util.concurrent.Executors
import java.util.concurrent.Future

fun <T> List<T>.uniquePairs(): Sequence<Pair<T, T>> {
    val list = this
    return sequence {
        list.forEachIndexed { index, item ->
            list.subList(index + 1, list.size).forEach {
                yield(Pair(item, it))
            }
        }
    }
}

fun <T, U> Sequence<T>.parallelFlatMap(parallelism: Int = 2, operation: (T) -> List<U>): Iterable<U> {
    return ParallelFlatMapSequenceCursor(parallelism, parallelism, this, operation)
}

private class ParallelFlatMapSequenceCursor<T, U>(
    private val windowSize: Int,
    private val parallelism: Int,
    private val sequence: Sequence<T>,
    private val operation: (T) -> List<U>
) : Iterable<U> {

    override fun iterator(): Iterator<U> {
        return object : Iterator<U> {

            private val workerPool = Executors.newFixedThreadPool(parallelism)
            private val chunkIterator = sequence.chunked(windowSize).iterator()
            private var currentWindow: Iterator<Future<List<U>>> = emptyList<Future<List<U>>>().iterator()
            private var currentList: Iterator<U> = emptyList<U>().iterator()

            override fun next(): U {
                while (!currentList.hasNext()) {
                    if (!currentWindow.hasNext()) {
                        val futures = chunkIterator.next().map {
                            workerPool.submit<List<U>> { operation(it) }
                        }

                        currentWindow = futures.iterator()
                    }

                    currentList = currentWindow.next().get().iterator()
                }

                return currentList.next()
            }

            override fun hasNext(): Boolean {
                return chunkIterator.hasNext()
            }
        }
    }
}