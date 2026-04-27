package com.liveklass.presentation

import com.liveklass.application.ClassUseCase
import com.liveklass.domain.ClassStatus
import com.liveklass.dto.CreateClassRequest
import com.liveklass.dto.FindClassListResponse
import com.liveklass.dto.FindClassResponse
import com.liveklass.dto.UpdateClassStatusRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
@RequestMapping("/api/v1/classes")
class ClassController(
    private val classUseCase: ClassUseCase
) {
    @GetMapping
    fun findAll(@RequestParam(required = false) status: ClassStatus?): ResponseEntity<FindClassListResponse> =
        ResponseEntity.ok(classUseCase.findClasses(status))

    @GetMapping("/{classId}")
    fun findDetail(@PathVariable classId: Long): ResponseEntity<FindClassResponse> =
        ResponseEntity.ok(classUseCase.findClassDetail(classId))

    @PostMapping
    fun create(@RequestBody request: CreateClassRequest): ResponseEntity<Unit> {
        val createdId = classUseCase.createClass(request)
        val location =
            ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdId)
                .toUri()
        return ResponseEntity.created(location).build()
    }

    @PatchMapping("/{classId}/status")
    fun updateStatus(
        @PathVariable classId: Long,
        @RequestBody request: UpdateClassStatusRequest
    ): ResponseEntity<Unit> {
        when (request.classStatus) {
            ClassStatus.OPEN -> classUseCase.openClass(classId)
            ClassStatus.CLOSED -> classUseCase.closeClass(classId)
            ClassStatus.DRAFT -> throw IllegalArgumentException("DRAFT 상태로 변경하는 API는 지원하지 않습니다.")
        }

        return ResponseEntity.noContent().build()
    }
}
