package com.back.domain.member.member.repository
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.entity.QMember
import com.back.standard.extensions.getOrThrow
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository

@Repository
class MemberRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : MemberRepositoryCustom {

    override fun findQById(id: Int): Member? {
        val member = QMember.member

        return queryFactory
            .selectFrom(member)
            .where(member.id.eq(id))
            .fetchOne()
    }
    override fun findQByUsername(username: String): Member? {
        val member = QMember.member

        return queryFactory
            .select(member)
            .from(member)
            .where(member.username.eq(username))
            .fetchOne()
    }

    override fun findQByIdIn(ids: List<Int>): List<Member> {
        val member = QMember.member

        return queryFactory
            .selectFrom(member)
            .where(member.id.`in`(ids))
            .fetch()
    }


    override fun findQByUsernameAndNickname(username: String, nickname: String): Member? {
        val member = QMember.member

        return queryFactory
            .selectFrom(member)
            .where(
                member.username.eq(username)
                    .and(member.nickname.eq(nickname))
            )
            .fetchOne()
    }

    override fun findQByUsernameOrNickname(username: String, nickname: String): List<Member> {
        val member = QMember.member

        return queryFactory
            .selectFrom(member)
            .where(
                member.username.eq(username)
                    .or(member.nickname.eq(nickname))
            )
            .fetch()
    }
    override fun findQByUsernameAndEitherPasswordOrNickname(username: String, password: String?, nickname: String?): List<Member> {
        val member = QMember.member

        return queryFactory
            .selectFrom(member)
            .where(
                member.username.eq(username)
                    .and(
                        member.password.eq(password)
                            .or(member.nickname.eq(nickname))
                    )
            )
            .fetch()
    }
}