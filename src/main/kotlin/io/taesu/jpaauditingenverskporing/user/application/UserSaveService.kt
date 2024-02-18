package io.taesu.jpaauditingenverskporing.user.application

import io.taesu.jpaauditingenverskporing.user.domain.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Created by taesu on 2024/02/18.
 *
 * @author Lee Tae Su
 * @version jpa-auditing-envers-kporing
 * @since jpa-auditing-envers-kporing
 */
@Service
@Transactional
class UserSaveService(private val userRepository: UserRepository) {

}
