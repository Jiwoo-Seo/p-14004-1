package com.back.domain.member.member.repository
import com.back.domain.member.member.entity.Member
import com.back.domain.member.member.entity.QMember
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.data.domain.Pageable
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

    override fun findQByUsernameAndEitherPasswordOrNickname(
        username: String,
        password: String?,
        nickname: String?
    ): List<Member> {
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

    override fun findQByNicknameContaining(nickname: String): List<Member> {
        val member = QMember.member

        return queryFactory
            .selectFrom(member)
            .where(member.nickname.contains(nickname))
            .fetch()
    }

    override fun countQByNicknameContaining(nickname: String): Long {
        val member = QMember.member

        return queryFactory
            .select(member.count())
            .from(member)
            .where(member.nickname.contains(nickname))
            .fetchOne() ?: 0L
    }

    override fun existsQByNicknameContaining(nickname: String): Boolean {
        val member = QMember.member

        return queryFactory
            .selectOne()
            .from(member)
            .where(member.nickname.contains(nickname))
            .fetchFirst() != null
    }

    override fun findQByNicknameContaining(nickname: String, pageable: Pageable): Page<Member> {
        val member = QMember.member

        val results = queryFactory
            .selectFrom(member)
            .where(member.nickname.contains(nickname))
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val totalQuery = queryFactory
            .select(member.count())
            .from(member)
            .where(member.nickname.contains(nickname))

        return PageableExecutionUtils.getPage(results, pageable) {
            totalQuery.fetchFirst() ?: 0L
        }
    }

    override fun findQByNicknameContainingOrderByIdDesc(nickname: String): List<Member> {
        val member = QMember.member

        return queryFactory
            .selectFrom(member)
            .where(member.nickname.contains(nickname))
            .orderBy(member.id.desc())
            .fetch()
    }

    override fun findQByUsernameContaining(username: String, pageable: Pageable): Page<Member> {
        val member = QMember.member

        val query = queryFactory
            .selectFrom(member)
            .where(member.username.contains(username))

        // Apply sorting
        pageable.sort.forEach { order ->
            when (order.property) {
                "id" -> query.orderBy(if (order.isAscending) member.id.asc() else member.id.desc())
                "username" -> query.orderBy(if (order.isAscending) member.username.asc() else member.username.desc())
                "nickname" -> query.orderBy(if (order.isAscending) member.nickname.asc() else member.nickname.desc())
            }
        }

        val results = query
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .fetch()

        val totalQuery = queryFactory
            .select(member.count())
            .from(member)
            .where(member.username.contains(username))

        return PageableExecutionUtils.getPage(results, pageable) {
            totalQuery.fetchFirst() ?: 0L
        }
    }
    override fun findQPagedByKw(
        kw: String,
        pageable: Pageable
    ): Page<Member> {
        throw RuntimeException("Not implemented yet")
    }
}