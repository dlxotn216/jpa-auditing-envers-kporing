package io.taesu.jpaauditingenverskporing.app.config

import io.taesu.jpaauditingenverskporing.app.domain.AuditableEntity
import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManagerFactory
import org.hibernate.boot.Metadata
import org.hibernate.engine.spi.SessionFactoryImplementor
import org.hibernate.envers.boot.internal.EnversIntegrator
import org.hibernate.envers.boot.internal.EnversService
import org.hibernate.envers.event.spi.*
import org.hibernate.event.internal.DefaultFlushEntityEventListener
import org.hibernate.event.service.spi.EventListenerRegistry
import org.hibernate.event.spi.EventType
import org.hibernate.event.spi.FlushEntityEvent
import org.hibernate.event.spi.PostInsertEvent
import org.hibernate.event.spi.PostUpdateEvent
import org.hibernate.internal.SessionFactoryImpl
import org.hibernate.service.spi.SessionFactoryServiceRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.stereotype.Component
import java.util.*

/**
 * Created by taesu on 2024/02/18.
 *
 * @author Lee Tae Su
 * @version jpa-auditing-envers-kporing
 * @since jpa-auditing-envers-kporing
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
class PersistenceConfig {
    @Bean
    fun auditorAware() = UserContextHolderAuditorAware()
}

class UserContextHolderAuditorAware: AuditorAware<Long> {
    override fun getCurrentAuditor(): Optional<Long> {
        return Optional.ofNullable(123L)
    }
}

@Component
class CustomFlushEntityEventConfig(
    private val entityManagerFactory: EntityManagerFactory,
    private val customFlushEntityEventListener: CustomFlushEntityEventListener,
) {
    @PostConstruct
    protected fun init() {
        val sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl::class.java)
        val registry = sessionFactory.serviceRegistry.getService(
            EventListenerRegistry::class.java
        ) ?: return
        registry.getEventListenerGroup(EventType.FLUSH_ENTITY).clearListeners()
        registry.getEventListenerGroup(EventType.FLUSH_ENTITY).appendListener(customFlushEntityEventListener)
    }
}

@Component
class CustomFlushEntityEventListener: DefaultFlushEntityEventListener() {
    override fun dirtyCheck(event: FlushEntityEvent) {
        super.dirtyCheck(event)
        removeIgnoredDirtyCheckProperties(event)
    }

    private fun removeIgnoredDirtyCheckProperties(event: FlushEntityEvent) {
        val propertyNames = event.entityEntry.persister.propertyNames
        val dirtyPropertyIndexes = event.dirtyProperties ?: return

        val entity = event.entity
        if (entity is AuditableEntity) {
            if (entity.forceUpdate) {
                return
            }
        }

        val resolved = resolveDirtyPropertyIndexes(dirtyPropertyIndexes, event.entityEntry.entityName, propertyNames)
        event.dirtyProperties = if (resolved.isNotEmpty()) resolved else null
    }

    private fun resolveDirtyPropertyIndexes(
        dirtyPropertyIndex: IntArray,
        entityName: String,
        propertyNames: Array<String>,
    ): IntArray {
        val newDirtyProperties = arrayListOf<Int>()
        val dirtyPropertyNames = mutableSetOf<String>()
        for (dirtyProperty in dirtyPropertyIndex) {
            dirtyPropertyNames += propertyNames[dirtyProperty]
            newDirtyProperties += dirtyProperty
        }

        if (dirtyPropertyNames.filterNot { it in IGNORE_DIRTY_CHECK_PROPERTY_NAMES }.isEmpty()) {
            // ignore 대상 외에 변경된 프로퍼티가 없다면 업데이트 하지 않음
            newDirtyProperties.clear()
        }
        return newDirtyProperties.toIntArray()
    }

    companion object {
        private val IGNORE_DIRTY_CHECK_PROPERTY_NAMES: Set<String> = setOf(
            "reason",
            "modifiedBy",
            "modifiedAt"
        )
    }
}


class CustomEnversIntegrator: EnversIntegrator() {
    override fun integrate(
        metadata: Metadata,
        sessionFactory: SessionFactoryImplementor,
        serviceRegistry: SessionFactoryServiceRegistry,
    ) {
        val enversService = serviceRegistry.getService(EnversService::class.java) ?: return
        val listenerRegistry = serviceRegistry.getService(EventListenerRegistry::class.java) ?: return
        listenerRegistry.addDuplicationStrategy(EnversListenerDuplicationStrategy.INSTANCE)

        if (enversService.entitiesConfigurations.hasAuditedEntities()) {
            listenerRegistry.appendListeners(
                EventType.POST_DELETE,
                EnversPostDeleteEventListenerImpl(enversService)
            )
            listenerRegistry.appendListeners(
                EventType.POST_INSERT, CustomEnversPostInsertEventListener(enversService)
            )
            listenerRegistry.appendListeners(
                EventType.PRE_UPDATE,
                EnversPreUpdateEventListenerImpl(enversService)
            )
            listenerRegistry.appendListeners(
                EventType.POST_UPDATE, CustomEnversPostUpdateEventListener(enversService)
            )
            listenerRegistry.appendListeners(
                EventType.POST_COLLECTION_RECREATE,
                EnversPostCollectionRecreateEventListenerImpl(enversService)
            )
            listenerRegistry.appendListeners(
                EventType.PRE_COLLECTION_REMOVE,
                EnversPreCollectionRemoveEventListenerImpl(enversService)
            )
            listenerRegistry.appendListeners(
                EventType.PRE_COLLECTION_UPDATE,
                EnversPreCollectionUpdateEventListenerImpl(enversService)
            )
        }
    }
}

class CustomEnversPostInsertEventListener(
    enversService: EnversService,
): EnversPostInsertEventListenerImpl(enversService) {
    override fun onPostInsert(event: PostInsertEvent) {
        val entity = event.entity
        if (entity is AuditableEntity && entity.skipRevision) {
            return
        }
        super.onPostInsert(event)
    }
}

class CustomEnversPostUpdateEventListener(
    enversService: EnversService,
): EnversPostUpdateEventListenerImpl(enversService) {
    override fun onPostUpdate(event: PostUpdateEvent) {
        val entity = event.entity
        if (entity is AuditableEntity && entity.skipRevision) {
            return
        }
        super.onPostUpdate(event)
    }
}
