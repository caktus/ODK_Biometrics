package uk.ac.lshtm.keppel.cli.util

import kotlinx.coroutines.*
import java.util.concurrent.Executors

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

fun <T, U> Sequence<T>.parallelFlatMap(
    parallelism: Int? = null,
    windowSize: Int? = null,
    operation: (T) -> Iterable<U>
): Iterable<U> {
    return ParallelFlatMapSequenceIterable(parallelism ?: 2, windowSize ?: 100, this, operation)
}

private class ParallelFlatMapSequenceIterable<T, U>(
    private val windowSize: Int,
    private val parallelism: Int,
    private val sequence: Sequence<T>,
    private val operation: (T) -> Iterable<U>
) : Iterable<U> {

    override fun iterator(): Iterator<U> {
        return ParallelFlatMapSequenceCursor(windowSize, parallelism, sequence, operation)
    }
}

private class ParallelFlatMapSequenceCursor<T, U>(
    windowSize: Int,
    parallelism: Int,
    sequence: Sequence<T>,
    private val operation: (T) -> Iterable<U>
) : Iterator<U> {

    private val dispatcher = Executors.newFixedThreadPool(parallelism).asCoroutineDispatcher()
    private val coroutineScope = CoroutineScope(dispatcher)

    private val chunkIterator = sequence.chunked(windowSize).iterator()
    private var currentWindow: Iterator<Deferred<Iterable<U>>> = emptyList<Deferred<Iterable<U>>>().iterator()
    private var currentList: Iterator<U> = emptyList<U>().iterator()

    override fun next(): U {
        while (!currentList.hasNext()) {
            if (!currentWindow.hasNext()) {
                val futures = chunkIterator.next().map {
                    coroutineScope.async { operation(it) }
                }

                currentWindow = futures.iterator()
            }

            currentList = runBlocking {
                currentWindow.next().await().iterator()
            }
        }

        return currentList.next()
    }

    override fun hasNext(): Boolean {
        return currentList.hasNext() || currentWindow.hasNext() || chunkIterator.hasNext()
    }
}