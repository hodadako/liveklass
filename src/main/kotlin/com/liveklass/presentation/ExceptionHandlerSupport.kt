package com.liveklass.presentation

import com.liveklass.application.EnrollmentAccessDeniedException
import com.liveklass.application.EntityNotFoundException
import com.liveklass.application.InvalidTicketException
import com.liveklass.application.ServiceException
import com.liveklass.domain.ClassCapacityExceededException
import com.liveklass.domain.ClassEnrollmentCancelException
import com.liveklass.domain.ClassEnrollmentException
import com.liveklass.domain.ClassStatusException
import com.liveklass.domain.DomainException
import com.liveklass.domain.EnrollmentCancelException
import com.liveklass.domain.EnrollmentStatusException
import com.liveklass.domain.EnrollmentTicketExpiredException
import com.liveklass.domain.EnrollmentTicketStatusException
import org.springframework.http.HttpStatus

data class ErrorResponse(
    val message: String
) {
    companion object {
        fun from(message: String) = ErrorResponse(message)
    }
}

fun DomainException.toHttpStatus(): HttpStatus =
    when (this) {
        is ClassStatusException -> HttpStatus.CONFLICT
        is ClassEnrollmentException -> HttpStatus.CONFLICT
        is ClassCapacityExceededException -> HttpStatus.CONFLICT
        is ClassEnrollmentCancelException -> HttpStatus.CONFLICT
        is EnrollmentStatusException -> HttpStatus.CONFLICT
        is EnrollmentCancelException -> HttpStatus.CONFLICT
        is EnrollmentTicketStatusException -> HttpStatus.CONFLICT
        is EnrollmentTicketExpiredException -> HttpStatus.CONFLICT
        is DomainException -> HttpStatus.BAD_REQUEST
    }

fun ServiceException.toHttpStatus(): HttpStatus =
    when (this) {
        is EntityNotFoundException -> HttpStatus.NOT_FOUND
        is EnrollmentAccessDeniedException -> HttpStatus.FORBIDDEN
        is InvalidTicketException -> HttpStatus.FORBIDDEN
        is ServiceException -> HttpStatus.BAD_REQUEST
    }
