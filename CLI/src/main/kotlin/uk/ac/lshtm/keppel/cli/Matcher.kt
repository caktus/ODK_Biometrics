package uk.ac.lshtm.keppel.cli

interface Matcher {

    fun getTemplate(bytes: ByteArray): Template
}

interface Template {
    fun match(other: Template): Double
}