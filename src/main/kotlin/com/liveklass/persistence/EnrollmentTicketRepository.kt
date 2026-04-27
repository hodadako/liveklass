package com.liveklass.persistence

import com.liveklass.domain.EnrollmentTicket
import org.springframework.data.jpa.repository.JpaRepository

interface EnrollmentTicketRepository : JpaRepository<EnrollmentTicket, Long>
