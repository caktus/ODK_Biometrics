package uk.ac.lshtm.keppel.cli.support

import org.apache.commons.codec.binary.Hex
import uk.ac.lshtm.keppel.cli.Matcher
import uk.ac.lshtm.keppel.cli.Template

class FakeMatcher : Matcher {

    private val scores = mutableListOf<Triple<String, String, Double>>()
    override fun getTemplate(bytes: ByteArray): Template {
        return FakeTemplate(bytes, scores)
    }

    fun addScore(one: String, two: String, score: Double) {
        scores.add(Triple(one, two, score))
    }
}

private class FakeTemplate(private val bytes: ByteArray, private val scores: List<Triple<String, String, Double>>) :
    Template {
    override fun match(other: Template): Double {
        val stringOne = String(Hex.decodeHex(String(bytes)))
        val stringTwo = String(Hex.decodeHex(String((other as FakeTemplate).bytes)))

        val score = scores.find {
            (it.first == stringOne && it.second == stringTwo) || (it.first == stringTwo && it.second == stringOne)
        }

        if (score != null) {
            return score.third
        } else {
            throw IllegalStateException("FakeMatcher: No score for $stringOne and $stringTwo!")
        }
    }
}