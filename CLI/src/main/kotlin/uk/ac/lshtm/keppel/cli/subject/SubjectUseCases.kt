package uk.ac.lshtm.keppel.cli.subject

import uk.ac.lshtm.keppel.cli.Template
import uk.ac.lshtm.keppel.cli.TemplateFactory
import uk.ac.lshtm.keppel.cli.util.parallelFlatMap
import uk.ac.lshtm.keppel.cli.util.uniquePairs

object SubjectUseCases {

    fun findMatches(
        subjects: List<Subject>,
        templateFactory: TemplateFactory,
        threshold: Double,
        parallelism: Int? = null
    ): List<Match> {
        return subjects
            .map { subject ->
                SubjectWithTemplates(
                    subject.id,
                    subject.templates.map { templateFactory.getTemplate(it.toByteArray()) }
                )
            }
            .uniquePairs()
            .parallelFlatMap(parallelism ?: 2) { pair ->
                val scores = pair.first.templates.zip(pair.second.templates).map { (one, two) ->
                    one.match(two)
                }

                if (scores.any { it >= threshold }) {
                    setOf(Match(pair.first.id, pair.second.id, scores))
                } else {
                    emptySet()
                }
            }.toList()
    }

    data class Match(val id1: String, val id2: String, val scores: List<Double>)
}

private class SubjectWithTemplates(val id: String, val templates: List<Template>)