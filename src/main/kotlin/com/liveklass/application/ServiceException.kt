package com.liveklass.application

sealed class ServiceException(message: String) : RuntimeException(message)

class EntityNotFoundException(entityId: Long, entityName: String) : ServiceException("$entityName $entityId 을 찾을 수 없습니다.")
