package com.zenmanage.kotlin.flags

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FlagTypeTest {
    @Test
    fun parsesKnownWireValuesCaseInsensitively() {
        assertThat(FlagType.fromWireValue("boolean")).isEqualTo(FlagType.BOOLEAN)
        assertThat(FlagType.fromWireValue("STRING")).isEqualTo(FlagType.STRING)
        assertThat(FlagType.fromWireValue("Number")).isEqualTo(FlagType.NUMBER)
        assertThat(FlagType.fromWireValue("json")).isEqualTo(FlagType.JSON)
        assertThat(FlagType.fromWireValue("JSON")).isEqualTo(FlagType.JSON)
    }

    @Test
    fun unrecognizedWireValueParsesToUnknownInsteadOfThrowing() {
        assertThat(FlagType.fromWireValue("some-future-type")).isEqualTo(FlagType.UNKNOWN)
        assertThat(FlagType.fromWireValue("")).isEqualTo(FlagType.UNKNOWN)
    }
}
