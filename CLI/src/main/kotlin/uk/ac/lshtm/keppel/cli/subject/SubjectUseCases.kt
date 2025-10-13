package uk.ac.lshtm.keppel.cli.subject

import uk.ac.lshtm.keppel.cli.Matcher
import uk.ac.lshtm.keppel.cli.util.parallelFold
import uk.ac.lshtm.keppel.cli.util.uniquePairs

object SubjectUseCases {

    fun findMatches(
        subjects: List<Subject>,
        matcher: Matcher,
        threshold: Double,
        parallelism: Int? = null
    ): List<Match> {
        return subjects
            .map { subject ->
                Pair(subject.id, subject.templates.map { matcher.getTemplate(it.toByteArray()) })
            }
            .uniquePairs()
            .parallelFold(parallelism ?: 2) { pair ->
                val scores = pair.first.second.zip(pair.second.second).map { (one, two) ->
                    one.match(two)
                }

                if (scores.any { it >= threshold }) {
                    setOf(Match(pair.first.first, pair.second.first, scores))
                } else {
                    emptySet()
                }
            }.toList()
    }

    data class Match(val id1: String, val id2: String, val scores: List<Double>)
}