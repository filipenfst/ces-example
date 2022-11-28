package com.global.payment.commons.utils.date

import java.time.LocalDate

operator fun LocalDate.rangeTo(other: LocalDate) = DateRange(this, other)

infix fun LocalDate.until(other: LocalDate) = this..other.minusDays(1)
