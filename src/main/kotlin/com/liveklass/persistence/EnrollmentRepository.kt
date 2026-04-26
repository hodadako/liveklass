package com.liveklass.persistence

import com.liveklass.domain.Enrollment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface EnrollmentRepository : JpaRepository<Enrollment, Long> {
    @Query(
        value = """
            select e 
            from Enrollment e
            join fetch e.enrolledClass
            where e.student.id = :memberId
        """,
        countQuery = """
            select count(e) 
            from Enrollment e
            where e.student.id = :memberId
        """
    )
    fun findAllByMember(memberId: Long, pageable: Pageable): Page<Enrollment>

    @Query(
        value = """
            select e
            from Enrollment e
            join fetch e.student
            where e.enrolledClass.id = :classId
        """,
        countQuery = """
            select count(e)
            from Enrollment e
            where e.enrolledClass.id = :classId
        """
    )
    fun findAllByClass(classId: Long, pageable: Pageable): Page<Enrollment>
}
