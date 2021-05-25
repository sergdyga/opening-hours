package org.exercise.openinghours.utils

import org.springframework.test.web.servlet.ResultActions
import org.springframework.test.web.servlet.ResultMatcher
import org.springframework.test.web.servlet.result.MockMvcResultHandlers
import java.io.StringWriter

/**
 * Prints request details for easier debugging if assertion fails.
 */
fun ResultActions.andExpectWithDebug(matcher: ResultMatcher?): ResultActions {
    return try {
        this.andExpect(matcher!!)
    } catch (e: Error) {
        val out = StringWriter()
        this.andDo(MockMvcResultHandlers.print(out))
        throw AssertionError("${e.message}$out", e.cause)
    }
}