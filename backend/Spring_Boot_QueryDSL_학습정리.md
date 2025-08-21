# Spring Boot + QueryDSL 페이징 및 검색 기능 학습 정리

## 1. 페이징 API 설계 및 구현

### 1.1 기본 페이징 파라미터 추가
- **page**: 페이지 번호 (기본값: 1)
- **pageSize**: 페이지 크기 (기본값: 5, 최대: 30)
- 유효성 검증으로 안전한 페이징 구현

```kotlin
@GetMapping
fun getItems(
    @RequestParam(defaultValue = "1") page: Int,
    @RequestParam(defaultValue = "5") pageSize: Int,
    // ...
): PageDto<MemberWithUsernameDto> {
    val page: Int = if (page >= 1) page else 1
    val pageSize: Int = if (pageSize in 1..30) pageSize else 5
    // ...
}
```

### 1.2 API 응답 구조 개선
**기존**: 단순 배열 형태
```json
[
  { "id": 8, "name": "희성", ... },
  { "id": 7, "name": "몰입코딩", ... }
]
```

**개선**: 페이징 정보 포함
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 1,
    "pageSize": 5,
    "totalElements": 8,
    "totalPages": 2,
    "sorted": true
  }
}
```

### 1.3 PageDto 도입으로 불필요한 필드 제거
Spring의 `Page` 객체를 직접 반환하면 불필요한 내부 필드들이 노출되는 문제를 해결
- `PageDto`와 `PageableDto`로 깔끔한 API 응답 구조 제공
- 클라이언트가 필요한 정보만 선별적으로 전달

## 2. 동적 검색 기능 구현

### 2.1 Query Method의 한계점
복합 검색 조건에서 Query Method는 비효율적:
- `findByUsernameContaining()`
- `findByNicknameContaining()`
- `findByUsernameContainingOrNicknameContaining()`
- 조건이 늘어날수록 메소드 수가 기하급수적으로 증가

### 2.2 QueryDSL을 이용한 동적 검색
**핵심 구현**: `findQPagedByKw` 메소드
```kotlin
override fun findQPagedByKw(
    kwType: MemberSearchKeywordType1,
    kw: String,
    pageable: Pageable
): Page<Member> {
    val member = QMember.member
    val builder = BooleanBuilder()

    if (kw.isNotBlank()) {
        when (kwType) {
            MemberSearchKeywordType1.USERNAME -> builder.and(member.username.contains(kw))
            MemberSearchKeywordType1.NICKNAME -> builder.and(member.nickname.contains(kw))
            else -> builder.and(member.username.contains(kw).or(member.nickname.contains(kw)))
        }
    }
    // ... 페이징 및 정렬 처리
}
```

### 2.3 검색 타입 enum 활용
```kotlin
enum class MemberSearchKeywordType1 {
    USERNAME,   // 사용자명으로만 검색
    NICKNAME,   // 닉네임으로만 검색
    ALL         // 전체 검색 (사용자명 OR 닉네임)
}
```

## 3. 정렬 기능 구현

### 3.1 정렬 조건 enum 설계
```kotlin
enum class MemberSearchSortType1 {
    ID,           // ID 내림차순
    ID_ASC,       // ID 오름차순
    USERNAME,     // 사용자명 내림차순
    USERNAME_ASC, // 사용자명 오름차순
    NICKNAME,     // 닉네임 내림차순
    NICKNAME_ASC  // 닉네임 오름차순
}
```

### 3.2 QueryDslUtil을 통한 정렬 로직 공통화
```kotlin
object QueryDslUtil {
    fun <T> applySorting(
        query: JPAQuery<T>,
        pageable: Pageable,
        pathProvider: (String) -> Path<out Comparable<*>>?
    ) {
        pageable.sort.forEach { order ->
            val path = pathProvider(order.property)
            if (path != null) {
                val orderSpecifier = OrderSpecifier(
                    if (order.isAscending) Order.ASC else Order.DESC,
                    path as Expression<Comparable<*>>
                )
                query.orderBy(orderSpecifier)
            }
        }
    }
}
```

## 4. Post 도메인으로 확장

### 4.1 Post 검색 타입
```kotlin
enum class PostSearchKeywordType1 {
    TITLE,     // 제목으로만 검색
    CONTENT,   // 내용으로만 검색
    ALL        // 전체 검색 (제목 OR 내용)
}
```

### 4.2 작성자명으로 글 검색 (미구현)
학습 자료에서는 `PostSearchKeywordType1.AUTHOR_NICKNAME`을 언급했지만, 현재 코드에서는 아직 구현되지 않은 상태입니다.

## 5. MySQL vs JPQL 주요 차이점

### 5.1 테이블 vs 엔티티
- **MySQL**: 실제 테이블명 사용 (`member`, `post`)
- **JPQL**: 엔티티 클래스명 사용 (`Member`, `Post`)

### 5.2 조인 문법
- **MySQL**: 명시적 JOIN 키워드 필요
- **JPQL**: 객체 관계를 통한 자동 조인

### 5.3 서브쿼리 지원
- **MySQL**: SELECT, FROM, WHERE 절에서 자유롭게 사용
- **JPQL**: FROM 절 서브쿼리 미지원, WHERE/HAVING 절에서만 가능

### 5.4 페이징 처리
- **MySQL**: `LIMIT`와 `OFFSET` 사용
- **JPQL**: JPA의 `setFirstResult()`, `setMaxResults()` 메소드 활용

## 6. 핵심 설계 원칙

### 6.1 타입 안전성
- enum class를 활용한 검색 타입과 정렬 조건 정의
- 컴파일 타임에 오타나 잘못된 값 방지

### 6.2 재사용성
- `QueryDslUtil`로 정렬 로직 공통화
- `PageDto`로 일관된 페이징 응답 구조

### 6.3 확장성
- `BooleanBuilder`를 활용한 동적 쿼리 구성
- 새로운 검색 조건 추가 시 enum과 when문만 수정하면 됨

### 6.4 API 일관성
- 모든 페이징 API에서 동일한 파라미터 구조 사용
- 표준화된 응답 형식으로 프론트엔드 개발 편의성 향상
