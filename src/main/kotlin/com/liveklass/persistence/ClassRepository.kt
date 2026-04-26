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

    @Query(
        """
            select c 
            from Class c
            where (:status is null or c.classStatus = :status)
        """
    )
    fun findAllByClassStatus(status: ClassStatus?): List<Class>
}
