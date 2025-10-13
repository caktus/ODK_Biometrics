package uk.ac.lshtm.keppel.cli

interface TemplateFactory {

    fun getTemplate(bytes: ByteArray): Template
}

interface Template {
    fun match(other: Template): Double
}