package io.taesu.jpaauditingenverskporing.app.domain

import jakarta.persistence.*
import org.hibernate.envers.RevisionEntity
import org.hibernate.envers.RevisionNumber
import org.hibernate.envers.RevisionTimestamp
import java.io.Serializable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Created by taesu on 2024/02/18.
 *
 * @author Lee Tae Su
 * @version jpa-auditing-envers-kporing
 * @since jpa-auditing-envers-kporing
 */
@Entity
@RevisionEntity
@Table(name = "REVINFO")
class Revision(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rev_seq")
    @SequenceGenerator(name = "rev_seq", sequenceName = "rev_seq", allocationSize = 1)
    @RevisionNumber
    @Column(name = "REV", nullable = false, updatable = false)
    private val key: Long,

    @RevisionTimestamp
    @Column(name = "REVTSTMP", nullable = false, updatable = false)
    private val timestamp: Long,
): Serializable {
    val at: LocalDateTime
        get() = LocalDateTime.from(ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC")))

    companion object {
        const val serialVersionUID = 1L
    }
}

