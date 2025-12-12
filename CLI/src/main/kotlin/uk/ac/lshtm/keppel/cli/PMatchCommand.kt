package uk.ac.lshtm.keppel.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.double
import com.github.ajalt.clikt.parameters.types.int
import uk.ac.lshtm.keppel.cli.subject.SubjectParser
import uk.ac.lshtm.keppel.cli.subject.SubjectUseCases
import java.io.File

class PMatchCommand(
    private val templateFactory: TemplateFactory,
    private val defaultThreshold: Double
) :
    CliktCommand(
        name = "pmatch",
        help = "Find matches between subjects from an input CSV. Threshold used for matching is $defaultThreshold."
    ) {

    private val inputCsvPath by option("-i", help = Strings.INPUT_CSV_HELP).required()
    private val outputCsvPath by option("-o", help = Strings.OUTPUT_CSV_HELP).required()
    private val parallelism by option("-p", help = Strings.PARALLELISM_HELP).int()
    private val threshold by option("-t", help = Strings.THRESHOLD_HELP).double()
    private val windowSize by option("-w").int()

    override fun run() {
        val subjects = try {
            SubjectParser.parseCsv(inputCsvPath)
        } catch (e: SubjectParser.BadHeaderException) {
            throw IllegalArgumentException(Strings.ERROR_PMATCH_NO_HEADER_ROW)
        }

        val matches = SubjectUseCases.findMatches(subjects, templateFactory, threshold ?: defaultThreshold, parallelism, windowSize)

        val outputCsv = File(outputCsvPath)
        outputCsv.printWriter().use { writer ->
            matches.forEachIndexed { index, match ->
                if (index == 0) {
                    val header = "id_1, id_2, " + (1..match.scores.size).joinToString(", ") { "score_$it" }
                    writer.println(header)
                }

                val row = "${match.id1}, ${match.id2}, " + match.scores.joinToString(", ")
                writer.println(row)
            }
        }
    }
}