package io.taesu.jpaauditingenverskporing.user.application

import io.taesu.jpaauditingenverskporing.user.domain.User
import io.taesu.jpaauditingenverskporing.user.domain.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

/**
 * Created by taesu on 2024/02/18.
 *
 * @author Lee Tae Su
 * @version jpa-auditing-envers-kporing
 * @since jpa-auditing-envers-kporing
 */
@SpringBootTest
class UserSaveServiceTest {
    @Autowired
    private lateinit var userRepository: UserRepository

    @BeforeEach
    fun init() {
        userRepository.deleteAll()
    }

    @Test
    fun `Entity의 생성일, 생성자가 설정된다`() {
        // given, when
        val user = userRepository.save(
            User(
                userId = "taesu",
                name = "taesu, lee"
            )
        )

        // then
        userRepository.findByUserId("taesu")!!.run {
            assertThat(this.createdBy).isNotNull()
            assertThat(this.createdAt).isNotNull()
        }
        assertThat(userRepository.findByUserId("taesu")!!.createdBy).isNotNull()
        assertThat(userRepository.findByUserId("taesu")!!.createdAt).isNotNull()
    }

    @Test
    fun `Entity의 수정일, 수정자가 설정된다`() {
        // given
        val user = userRepository.save(
            User(
                userId = "taesu",
                name = "taesu, lee"
            )
        )

        // when
        userRepository.save(user.apply {
            update("changed", "for testing")
        })

        // then
        userRepository.findByUserId("taesu")!!.run {
            assertThat(this.modifiedBy).isNotNull()
            assertThat(this.modifiedAt).isNotNull()
            assertThat(this.reason).isEqualTo("for testing")
        }
    }

    @Test
    fun `Entity의 실제 데이터 변경이 없다면 수정일, 수정자는 변경되지 않는다`() {
        // given
        val user = userRepository.save(
            User(
                userId = "taesu",
                name = "taesu, lee"
            )
        )

        // when
        userRepository.save(user.apply {
            update("taesu, lee", "for testing") // 데이터 변경은 없음. 단순 사유만 변경
        })

        // then
        assertThat(userRepository.findByUserId("taesu")!!.modifiedAt).isEqualTo(user.modifiedAt)
    }
}
