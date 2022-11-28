package com.global.payment.application.commons

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.toList
import org.assertj.core.api.Assertions
import org.assertj.core.api.ListAssert
import org.assertj.core.api.RecursiveComparisonAssert
import java.time.Duration
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

fun RecursiveComparisonAssert<*>.withDateRangeComparator(
    timeAmount: Duration = Duration.of(1, ChronoUnit.MINUTES)
): RecursiveComparisonAssert<*> = withComparatorForType(
    { actual, expected ->
        if (actual == null || expected == null) {
            return@withComparatorForType if (actual == expected) 0 else -1
        }
        if (actual.isAfter(expected.minus(timeAmount)) && actual.isBefore(expected.plus(timeAmount))) {
            0
        } else -1
    },
    LocalDateTime::class.java
)

fun <T> T.assertThat() = Assertions.assertThat(this)
    .usingRecursiveComparison()
    .withDateRangeComparator()


suspend fun <T> Flow<T>.assertThat(
    listAssert: ListAssert<T>.() -> ListAssert<T>
) = toList().let(Assertions::assertThat).listAssert()
    .usingRecursiveComparison()
    .withDateRangeComparator()
