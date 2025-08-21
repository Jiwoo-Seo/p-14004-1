# QueryDslUtil 상세 분석

## 1. QueryDslUtil의 목적

### 1.1 문제점
QueryDSL에서 정렬 로직을 구현할 때마다 반복되는 보일러플레이트 코드:

**QueryDslUtil 사용 전 (반복되는 코드)**
```kotlin
// Member 정렬
pageable.sort.forEach { order ->
    when (order.property) {
        "id" -> {
            if (order.isAscending) query.orderBy(member.id.asc())
            else query.orderBy(member.id.desc())
        }
        "username" -> {
            if (order.isAscending) query.orderBy(member.username.asc())
            else query.orderBy(member.username.desc())
        }
        "nickname" -> {
            if (order.isAscending) query.orderBy(member.nickname.asc())
            else query.orderBy(member.nickname.desc())
        }
    }
}

// Post 정렬 (동일한 패턴 반복)
pageable.sort.forEach { order ->
    when (order.property) {
        "id" -> {
            if (order.isAscending) query.orderBy(post.id.asc())
            else query.orderBy(post.id.desc())
        }
        "title" -> {
            if (order.isAscending) query.orderBy(post.title.asc())
            else query.orderBy(post.title.desc())
        }
        // ...
    }
}
```

### 1.2 해결책
QueryDslUtil을 통해 정렬 로직을 공통화하여 중복 제거

## 2. QueryDslUtil 코드 분석

```kotlin
object QueryDslUtil {
    fun <T> applySorting(
        query: JPAQuery<T>,                                          // ① 정렬을 적용할 쿼리
        pageable: Pageable,                                          // ② Spring의 정렬 정보
        pathProvider: (String) -> Path<out Comparable<*>>?           // ③ 필드명 → QueryDSL Path 변환 함수
    ) {
        pageable.sort.forEach { order ->                             // ④ 정렬 조건 순회
            val path = pathProvider(order.property)                  // ⑤ 필드명으로 Path 획득
            if (path != null) {                                      // ⑥ 유효한 Path인지 확인
                val orderSpecifier = OrderSpecifier(                // ⑦ OrderSpecifier 생성
                    if (order.isAscending) Order.ASC else Order.DESC,
                    path as Expression<Comparable<*>>
                )
                query.orderBy(orderSpecifier)                       // ⑧ 쿼리에 정렬 조건 적용
            }
        }
    }
}
```

### 2.1 파라미터 분석

#### ① query: JPAQuery`<T>`
- QueryDSL의 쿼리 객체
- 이 쿼리에 정렬 조건을 추가함

#### ② pageable: Pageable
- Spring Data의 페이징 및 정렬 정보를 담은 객체
- `pageable.sort`로 정렬 조건들에 접근 가능

#### ③ pathProvider: (String) -> Path`<out Comparable<*>>`?
- **핵심 부분**: 문자열 필드명을 QueryDSL Path 객체로 변환하는 람다 함수
- 각 도메인별로 다른 매핑 로직을 제공할 수 있게 함

## 3. pathProvider 함수의 역할

### 3.1 Member 도메인에서의 사용
```kotlin
QueryDslUtil.applySorting(query, pageable) { property ->
    when (property) {
        "id" -> member.id           // String "id" → QMember.id
        "username" -> member.username // String "username" → QMember.username  
        "nickname" -> member.nickname // String "nickname" → QMember.nickname
        else -> null                // 지원하지 않는 필드는 null 반환
    }
}
```

### 3.2 Post 도메인에서의 사용
```kotlin
QueryDslUtil.applySorting(query, pageable) { property ->
    when (property) {
        "id" -> post.id
        "title" -> post.title
        "content" -> post.content
        else -> null
    }
}
```

### 3.3 pathProvider의 장점
1. **타입 안전성**: 컴파일 타임에 잘못된 필드명 체크
2. **유연성**: 도메인마다 다른 필드 매핑 가능
3. **보안**: 지원하지 않는 필드는 null 반환으로 무시

## 4. OrderSpecifier 생성 과정

### 4.1 OrderSpecifier란?
QueryDSL에서 정렬 조건을 나타내는 객체

```kotlin
val orderSpecifier = OrderSpecifier(
    if (order.isAscending) Order.ASC else Order.DESC,  // 정렬 방향
    path as Expression<Comparable<*>>                   // 정렬할 필드
)
```

### 4.2 Order enum
```kotlin
// QueryDSL의 Order enum
enum class Order {
    ASC,    // 오름차순
    DESC    // 내림차순
}
```

### 4.3 Expression`<Comparable<*>>`로 캐스팅하는 이유
- QueryDSL의 Path는 Expression을 상속받음
- OrderSpecifier는 Comparable한 타입만 받을 수 있음
- 타입 시스템을 만족시키기 위한 캐스팅

## 5. 사용 예시 비교

### 5.1 QueryDslUtil 사용 전
```kotlin
override fun findQPagedByKw(...): Page<Member> {
    // ... 검색 조건 설정
    
    // 정렬 처리 (중복 코드)
    pageable.sort.forEach { order ->
        when (order.property) {
            "id" -> {
                if (order.isAscending) query.orderBy(member.id.asc())
                else query.orderBy(member.id.desc())
            }
            "username" -> {
                if (order.isAscending) query.orderBy(member.username.asc())
                else query.orderBy(member.username.desc())
            }
            "nickname" -> {
                if (order.isAscending) query.orderBy(member.nickname.asc())
                else query.orderBy(member.nickname.desc())
            }
        }
    }
    
    // ... 나머지 로직
}
```

### 5.2 QueryDslUtil 사용 후
```kotlin
override fun findQPagedByKw(...): Page<Member> {
    // ... 검색 조건 설정
    
    // 정렬 처리 (간결한 코드)
    QueryDslUtil.applySorting(query, pageable) { property ->
        when (property) {
            "id" -> member.id
            "username" -> member.username
            "nickname" -> member.nickname
            else -> null
        }
    }
    
    // ... 나머지 로직
}
```

## 6. 장점 정리

### 6.1 코드 중복 제거
- 모든 Repository에서 동일한 정렬 로직 사용 가능
- 보일러플레이트 코드 대폭 감소

### 6.2 일관성 보장
- 모든 도메인에서 동일한 정렬 처리 방식
- 버그 발생 가능성 감소

### 6.3 유지보수성 향상
- 정렬 로직 변경 시 한 곳만 수정하면 됨
- 새로운 도메인 추가 시 pathProvider만 구현하면 됨

### 6.4 타입 안전성
- 잘못된 필드명은 null 반환으로 무시
- 컴파일 타임에 오류 발견 가능

## 7. 실제 작동 과정

### 7.1 요청 예시
```
GET /api/v1/adm/members?page=1&pageSize=5&sort=username,asc&sort=id,desc
```

### 7.2 Pageable 객체 생성
```kotlin
// Spring이 자동으로 생성하는 Pageable
pageable.sort = Sort.by(
    Sort.Order.asc("username"),
    Sort.Order.desc("id")
)
```

### 7.3 QueryDslUtil 처리 과정
```kotlin
// 1차 순회: username,asc
order.property = "username"
order.isAscending = true
path = pathProvider("username") // return member.username
orderSpecifier = OrderSpecifier(Order.ASC, member.username)
query.orderBy(orderSpecifier)

// 2차 순회: id,desc  
order.property = "id"
order.isAscending = false
path = pathProvider("id") // return member.id
orderSpecifier = OrderSpecifier(Order.DESC, member.id)
query.orderBy(orderSpecifier)
```

### 7.4 최종 생성된 SQL
```sql
SELECT *
FROM member 
WHERE ...
ORDER BY username ASC, id DESC
LIMIT 5 OFFSET 0
```

QueryDslUtil은 이렇게 복잡한 정렬 로직을 간단하고 재사용 가능한 형태로 추상화한 유틸리티 클래스입니다.
