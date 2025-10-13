package uk.ac.lshtm.keppel.cli

import com.machinezoo.sourceafis.FingerprintCompatibility.importTemplate
import com.machinezoo.sourceafis.FingerprintMatcher
import org.apache.commons.codec.binary.Hex

class SourceAFISMatcher : Matcher {

    override fun match(one: ByteArray, two: ByteArray): Double {
        val oneTemplate = importTemplate(Hex.decodeHex(String(one)))
        val twoTemplate = importTemplate(Hex.decodeHex(String(two)))

        return FingerprintMatcher(oneTemplate).match(twoTemplate)
    }
}