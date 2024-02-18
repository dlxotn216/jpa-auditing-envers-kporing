package io.taesu.jpaauditingenverskporing.user.domain

import io.taesu.jpaauditingenverskporing.app.domain.AuditableEntity
import jakarta.persistence.*
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.annotations.NaturalId
import org.hibernate.envers.Audited
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.data.jpa.repository.JpaRepository

/**
 * Created by taesu on 2024/02/18.
 *
 * @author Lee Tae Su
 * @version jpa-auditing-envers-kporing
 * @since jpa-auditing-envers-kporing
 */
@Audited
@EntityListeners(value = [AuditingEntityListener::class])
@Table(name = "users")
@DynamicUpdate
@Entity(name = "User")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1)
    val userKey: Long = 0L,

    @NaturalId
    @Column(name = "user_id", length = 256, unique = true, nullable = false, updatable = false)
    val userId: String,

    @Column(name = "name", length = 256, nullable = false)
    var name: String,

    @Column(name = "reason", length = 4096, nullable = true)
    var reason: String? = null,
): AuditableEntity() {

    fun update(name: String, reason: String) {
        this.name = name
        this.reason = reason
    }

    override fun equals(other: Any?): Boolean =
        this === other || (
            other is User
                && other.userKey == this.userKey
            )

    override fun hashCode(): Int {
        return userKey.hashCode()
    }
}

interface UserRepository: JpaRepository<User, Long> {
    fun findByUserId(userId: String): User?
}
