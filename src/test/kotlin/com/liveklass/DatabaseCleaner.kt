package com.liveklass

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component

@Component
class DatabaseCleaner(
    private val jdbcTemplate: JdbcTemplate
) {
    fun clean() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0")

        jdbcTemplate.execute("TRUNCATE TABLE enrollment_ticket")
        jdbcTemplate.execute("TRUNCATE TABLE enrollment")
        jdbcTemplate.execute("TRUNCATE TABLE class")
        jdbcTemplate.execute("TRUNCATE TABLE member")

        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1")
    }
}
