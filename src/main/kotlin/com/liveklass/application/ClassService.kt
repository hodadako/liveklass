package com.liveklass.application

import com.liveklass.domain.Class
import com.liveklass.domain.ClassStatus
import com.liveklass.dto.CreateClassRequest
import com.liveklass.dto.FindClassListResponse
import com.liveklass.dto.FindClassResponse
import com.liveklass.persistence.ClassRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

interface ClassUseCase {
    fun createClass(request: CreateClassRequest): Long
    fun openClass(classId: Long)
    fun closeClass(classId: Long)
    fun findClassDetail(classId: Long): FindClassResponse
    fun findClasses(status: ClassStatus?): FindClassListResponse
}

@Service
class ClassService(
    private val classRepository: ClassRepository
) : ClassUseCase {
    @Transactional
    override fun createClass(request: CreateClassRequest): Long =
        classRepository.save(
            Class.create(
                request.title,
                request.description,
                request.price,
                request.capacity,
                request.startDate,
                request.endDate
            )
        ).id

    @Transactional
    override fun openClass(classId: Long) {
        val foundClass = classRepository.findByIdOrThrow(classId)
        foundClass.open()
    }

    @Transactional
    override fun closeClass(classId: Long) {
        val foundClass = classRepository.findByIdOrThrow(classId)
        foundClass.close()
    }

    @Transactional(readOnly = true)
    override fun findClassDetail(classId: Long): FindClassResponse = FindClassResponse.from(classRepository.findByIdOrThrow(classId))

    @Transactional(readOnly = true)
    override fun findClasses(
        status: ClassStatus?
    ): FindClassListResponse = FindClassListResponse.from(classRepository.findAllByClassStatus(status))
}
