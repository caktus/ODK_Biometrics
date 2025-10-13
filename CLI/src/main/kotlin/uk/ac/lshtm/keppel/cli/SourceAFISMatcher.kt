package uk.ac.lshtm.keppel.cli

import com.machinezoo.sourceafis.FingerprintCompatibility.importTemplate
import com.machinezoo.sourceafis.FingerprintMatcher
import com.machinezoo.sourceafis.FingerprintTemplate
import org.apache.commons.codec.binary.Hex

class SourceAFISMatcher : Matcher {
    override fun getTemplate(bytes: ByteArray): Template {
        return SourceAFISTemplate(bytes)
    }
}

private class SourceAFISTemplate(private val bytes: ByteArray) : Template {

    val template: FingerprintTemplate by lazy { importTemplate(Hex.decodeHex(String(bytes))) }
    private val matcher by lazy { FingerprintMatcher(template) }

    override fun match(other: Template): Double {
        return matcher.match((other as SourceAFISTemplate).template)
    }
}