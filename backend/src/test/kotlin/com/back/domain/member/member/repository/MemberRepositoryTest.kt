package com.back.domain.member.member.repository

import com.back.standard.extensions.getOrThrow
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
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
}