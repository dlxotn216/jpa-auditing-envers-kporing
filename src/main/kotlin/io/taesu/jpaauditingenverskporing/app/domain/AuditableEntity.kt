package io.taesu.jpaauditingenverskporing.app.domain

import jakarta.persistence.Column
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

/**
 * Created by taesu on 2024/02/18.
 *
 * @author Lee Tae Su
 * @version jpa-auditing-envers-kporing
 * @since jpa-auditing-envers-kporing
 */
@MappedSuperclass
class AuditableEntity(
    @CreatedBy
    @Column(name = "CREATED_BY", nullable = false)
    var createdBy: Long? = null,
    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false)
    var createdAt: LocalDateTime? = null,
    @LastModifiedBy
    @Column(name = "MODIFIED_BY", nullable = false)
    var modifiedBy: Long? = null,
    @LastModifiedDate
    @Column(name = "MODIFIED_AT", nullable = false)
    var modifiedAt: LocalDateTime? = null,
) {
    @Transient
    var forceUpdate = false
}
