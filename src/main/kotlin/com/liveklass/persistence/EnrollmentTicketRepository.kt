package com.liveklass.persistence

import com.liveklass.domain.EnrollmentTicket
import com.liveklass.domain.EnrollmentTicketStatus
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query

interface EnrollmentTicketRepository : JpaRepository<EnrollmentTicket, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select et from EnrollmentTicket et where et.id = :id")
    fun findByIdForUpdate(id: Long): EnrollmentTicket?

    fun findByClassIdAndMemberId(
        classId: Long,
        memberId: Long
    ): EnrollmentTicket?

    fun findAllByClassIdAndMemberId(
        classId: Long,
        memberId: Long
    ): List<EnrollmentTicket>

    fun countByStatus(status: EnrollmentTicketStatus): Long

    @Query(
        """
            select count(et.id)
            from EnrollmentTicket et
            where et.status = :status
            and et.id < :id
        """
    )
    fun findCurrentPosition(id: Long, status: EnrollmentTicketStatus): Long

    @Query(
        """
            select et
            from EnrollmentTicket et
            where et.status = :status
            order by et.id asc
        """
    )
    fun findAllByStatus(
        status: EnrollmentTicketStatus,
        pageable: Pageable
    ): Page<EnrollmentTicket>
}
