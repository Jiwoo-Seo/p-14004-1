package com.back.domain.member.member.repository
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.entity.QMember
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class MemberRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : MemberRepositoryCustom {

    override fun findQById(id: Int): Member? {
        val member = QMember.member

        return queryFactory
            .select(member)
            .from(member)
            .where(member.id.eq(id))
            .fetchOne()
    }
}