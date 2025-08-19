package com.back.domain.member.member.repository

import com.back.standard.extensions.getOrThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberRepositoryTest {
    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Test
    @DisplayName("findById")
    fun t1() {
        val member = memberRepository.findById(1).orElseThrow()

        assertThat(member.id).isEqualTo(1)
    }

    @Test
    @DisplayName("findQById")
    fun t2() {
        val member = memberRepository.findQById(1).getOrThrow()

        assertThat(member.id).isEqualTo(1)
    }

    @Test
    @DisplayName("findByUsername")
    fun t3() {
        val member = memberRepository.findByUsername("admin").getOrThrow()

        assertThat(member.username).isEqualTo("admin")
    }

    @Test
    @DisplayName("findQByUsername")
    fun t4() {
        val member = memberRepository.findQByUsername("admin").getOrThrow()

        assertThat(member.username).isEqualTo("admin")
    }

    @Test
    @DisplayName("findByIdIn")
    fun t5() {
        val members = memberRepository.findByIdIn(listOf(1, 2, 3))

        assertThat(members).isNotEmpty
        assertThat(members.map { it.id }).containsAnyOf(1, 2, 3)
    }

    @Test
    @DisplayName("findQByIdIn")
    fun t6() {
        val members = memberRepository.findQByIdIn(listOf(1, 2, 3))

        assertThat(members).isNotEmpty
        assertThat(members.map { it.id }).containsAnyOf(1, 2, 3)
    }
    @Test
    @DisplayName("findByUsernameAndNickname")
    fun t7() {
        val member = memberRepository.findByUsernameAndNickname("admin", "관리자").getOrThrow()

        assertThat(member.username).isEqualTo("admin")
        assertThat(member.nickname).isEqualTo("관리자")
    }

    @Test
    @DisplayName("findQByUsernameAndNickname")
    fun t8() {
        val member = memberRepository.findQByUsernameAndNickname("admin", "관리자").getOrThrow()

        assertThat(member.username).isEqualTo("admin")
        assertThat(member.nickname).isEqualTo("관리자")
    }

    @Test
    @DisplayName("findByUsernameOrNickname")
    fun t9() {
        val members = memberRepository.findByUsernameOrNickname("admin", "유저1")

        assertThat(members).isNotEmpty
        assertThat(members.any { it.username == "admin" || it.nickname == "유저1" }).isTrue
    }

    @Test
    @DisplayName("findQByUsernameOrNickname")
    fun t10() {
        val members = memberRepository.findQByUsernameOrNickname("admin", "유저1")

        assertThat(members).isNotEmpty
        assertThat(members.any { it.username == "admin" || it.nickname == "유저1" }).isTrue
    }
    @Test
    @DisplayName("findCByUsernameAndEitherPasswordOrNickname")
    fun t11() {
        val members = memberRepository.findCByUsernameAndEitherPasswordOrNickname("admin", "wrong-password", "관리자")

        assertThat(members).isNotEmpty
        assertThat(members.any { it.username == "admin" && (it.password == "wrong-password" || it.nickname == "관리자") }).isTrue
    }

    @Test
    @DisplayName("findQByUsernameAndPasswordOrNickname")
    fun t12() {
        val members = memberRepository.findQByUsernameAndEitherPasswordOrNickname("admin", "wrong-password", "관리자")

        assertThat(members).isNotEmpty
        assertThat(members.any { it.username == "admin" && (it.password == "wrong-password" || it.nickname == "관리자") }).isTrue
    }
    @Test
    @DisplayName("findByNicknameContaining")
    fun t13() {
        val members = memberRepository.findByNicknameContaining("유저")

        assertThat(members).isNotEmpty
        assertThat(members.all { it.nickname.contains("유저") }).isTrue
    }

    @Test
    @DisplayName("findQByNicknameContaining")
    fun t14() {
        val members = memberRepository.findQByNicknameContaining("유저")

        assertThat(members).isNotEmpty
        assertThat(members.all { it.nickname.contains("유저") }).isTrue
    }

    @Test
    @DisplayName("countByNicknameContaining")
    fun t15() {
        val count = memberRepository.countByNicknameContaining("유저")

        assertThat(count).isEqualTo(3)
    }

    @Test
    @DisplayName("countQByNicknameContaining")
    fun t16() {
        val count = memberRepository.countQByNicknameContaining("유저")

        assertThat(count).isEqualTo(3)
    }

    @Test
    @DisplayName("existsByNicknameContaining")
    fun t17() {
        val exists = memberRepository.existsByNicknameContaining("유저")

        assertThat(exists).isTrue
    }

    @Test
    @DisplayName("existsQByNicknameContaining")
    fun t18() {
        val exists = memberRepository.existsQByNicknameContaining("유저")

        assertThat(exists).isTrue
    }

    @Test
    @DisplayName("findByNicknameContaining with Pageable")
    fun t19() {
        val pageable = PageRequest.of(0, 2)
        val page = memberRepository.findByNicknameContaining("유저", pageable)

        assertThat(page.content).hasSize(2)
        assertThat(page.totalElements).isEqualTo(3)
        assertThat(page.totalPages).isEqualTo(2)
    }

    @Test
    @DisplayName("findQByNicknameContaining with Pageable")
    fun t20() {
        val pageable = PageRequest.of(0, 2)
        val page = memberRepository.findQByNicknameContaining("유저", pageable)

        assertThat(page.content).hasSize(2)
        assertThat(page.totalElements).isEqualTo(3)
        assertThat(page.totalPages).isEqualTo(2)
    }
}