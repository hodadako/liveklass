package com.liveklass

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(MySQLContainerExtension::class, DatabaseCleaner::class)
abstract class IntegrationTestSupport {
    @Autowired
    private lateinit var databaseCleaner: DatabaseCleaner

    @BeforeEach
    fun cleanUpTables() {
        databaseCleaner.clean()
    }
}
