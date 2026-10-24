package com.brigadeiro.app.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TextNormalizerTest {

    @Test
    fun lowercasesStripsAccentsAndPunctuation() {
        assertThat(TextNormalizer.normalize("Queijo Mussarela!")).isEqualTo("queijo mussarela")
        assertThat(TextNormalizer.normalize("Pão")).isEqualTo("pao")
    }

    @Test
    fun singularizesCommonPlurals() {
        assertThat(TextNormalizer.normalize("tomates")).isEqualTo("tomate")
        assertThat(TextNormalizer.normalize("cebolas")).isEqualTo("cebola")
        assertThat(TextNormalizer.normalize("ovos")).isEqualTo("ovo")
    }

    @Test
    fun keepsInvariantWords() {
        assertThat(TextNormalizer.normalize("arroz")).isEqualTo("arroz")
        assertThat(TextNormalizer.normalize("feijão")).isEqualTo("feijao")
    }
}
