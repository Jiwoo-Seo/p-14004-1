package com.back.domain.member.member.repository
import com.back.domain.member.member.entity.Member

interface MemberRepositoryCustom {
    fun findQById(id: Int): Member?
}