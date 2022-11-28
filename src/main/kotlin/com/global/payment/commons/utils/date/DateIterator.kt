package com.global.payment.commons.utils.date

import java.time.LocalDate

class DateIterator(
    startDate: LocalDate,
    private val endDateInclusive: LocalDate,
    private val stepDays: Long
) : Iterator<LocalDate> {
    private var currentDate = startDate

    override fun hasNext() = currentDate <= endDateInclusive

    override fun next(): LocalDate {
        val next = currentDate
        if (next > endDateInclusive) throw NoSuchElementException("There is no a next element")

        currentDate = currentDate.plusDays(stepDays)

        return next
    }
}
