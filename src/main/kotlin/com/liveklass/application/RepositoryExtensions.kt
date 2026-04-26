package com.liveklass.application

import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.findByIdOrNull

inline fun <reified T : Any> CrudRepository<T, Long>.findByIdOrThrow(id: Long): T =
    findByIdOrNull(id)
        ?: throw EntityNotFoundException(id, T::class.simpleName ?: "Entity")
