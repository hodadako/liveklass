package com.liveklass.presentation

import com.liveklass.application.ServiceException
import com.liveklass.domain.DomainException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ExceptionHandler {
    @ExceptionHandler(DomainException::class)
    fun handleDomainException(e: DomainException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(e.toHttpStatus()).body(ErrorResponse.from(e.message ?: "알 수 없는 Domain Exception 입니다."))

    @ExceptionHandler(ServiceException::class)
    fun handleServiceException(e: ServiceException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(e.toHttpStatus()).body(ErrorResponse.from(e.message ?: "알 수 없는 Service Exception 입니다."))

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.from(e.message ?: "잘못된 요청입니다."))

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse.from(e.message ?: "알 수 없는 서버 예외입니다."))
}
