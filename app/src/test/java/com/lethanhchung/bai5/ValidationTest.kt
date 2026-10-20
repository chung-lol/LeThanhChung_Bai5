package com.lethanhchung.bai5

import com.lethanhchung.bai5.data.model.Course
import com.lethanhchung.bai5.utils.Validation
import org.junit.Assert.*
import org.junit.Test

class ValidationTest {
    private val valid = Course(title = "Kotlin", description = "Cơ bản", categoryId = "1",
        instructor = "Chung", price = 0.0, duration = 30, thumbnail = "https://example.org/a.jpg",
        trailerUrl = "https://example.org/a.mp4", createdAt = "2026-10-20")
    @Test fun acceptsFreeCourse() { assertNull(Validation.validateCourse(valid, setOf("1"))) }
    @Test fun rejectsInvalidPrices() {
        listOf(-1.0, Double.NaN, Double.POSITIVE_INFINITY).forEach {
            assertNotNull(Validation.validateCourse(valid.copy(price = it), setOf("1")))
        }
    }
    @Test fun rejectsMissingCategory() { assertNotNull(Validation.validateCourse(valid, emptySet())) }
    @Test fun rejectsMalformedUrls() {
        listOf("httpwrong", "https://", "file:///tmp/a", "http://example.org/a").forEach {
            assertNotNull(Validation.validateCourse(valid.copy(thumbnail = it), setOf("1")))
        }
    }
    @Test fun rejectsInvalidDateDurationAndBlankTitle() {
        listOf(valid.copy(createdAt = "2026-02-30"), valid.copy(duration = 0), valid.copy(title = " "))
            .forEach { assertNotNull(Validation.validateCourse(it, setOf("1"))) }
    }
}
