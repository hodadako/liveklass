package com.liveklass.application

sealed class ServiceException(message: String) : RuntimeException(message)

class EntityNotFoundException(entityId: Long, entityName: String) :
    ServiceException("$entityName $entityId 을 찾을 수 없습니다.")

class EnrollmentAccessDeniedException(enrollmentId: Long, memberId: Long) :
    ServiceException("수강 신청에 접근할 수 없습니다. enrollmentId=$enrollmentId, memberId=$memberId")

class InvalidTicketException(classId: Long, memberId: Long, ticketId: Long) :
    ServiceException("대기열 티켓의 소유권을 확인할 수 없습니다. classId = $classId, classId = $classId, memberId=$memberId, ticketId = $ticketId")
