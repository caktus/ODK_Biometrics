package uk.ac.lshtm.keppel.cli.util

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

fun <T, U> Sequence<T>.parallelFlatMap(threads: Int = 2, operation: (T) -> List<U>): List<U> {
    val workerPool = Executors.newFixedThreadPool(threads)
    val futures = this.map { item ->
        workerPool.submit<List<U>> {
            operation(item)
        }
    }

    val set = futures.fold(emptyList<U>()) { accum, future ->
        accum + future.get()
    }

    workerPool.shutdown()
    workerPool.awaitTermination(30L, java.util.concurrent.TimeUnit.SECONDS)
    return set
}