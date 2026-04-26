package com.liveklass.persistence

import com.liveklass.domain.Class
import com.liveklass.domain.ClassStatus
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface ClassRepository : JpaRepository<Class, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Class c WHERE c.id = :id")
    fun findByIdForUpdate(id: Long): Class?

    fun findAllByClassStatus(classStatus: ClassStatus): List<Class>
}
