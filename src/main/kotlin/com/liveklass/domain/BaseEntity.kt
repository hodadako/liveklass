package com.liveklass.domain

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.proxy.HibernateProxy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.lang.Class
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0L
        protected set

    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdDate: LocalDateTime? = null
        protected set

    @LastModifiedDate
    var updatedDate: LocalDateTime? = null
        protected set

    private fun resolvePersistenceClass(obj: Any): Class<*> =
        when (obj) {
            is HibernateProxy -> obj.hibernateLazyInitializer.persistentClass
            else -> obj.javaClass
        }

    final override fun hashCode(): Int = resolvePersistenceClass(this).hashCode()

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseEntity) return false
        val otherEffectiveClass = resolvePersistenceClass(other)
        val thisEffectiveClass = resolvePersistenceClass(this)
        if (thisEffectiveClass != otherEffectiveClass) return false

        return id != 0L && id == other.id
    }

    override fun toString(): String = "BaseEntity(id=$id)"
}
