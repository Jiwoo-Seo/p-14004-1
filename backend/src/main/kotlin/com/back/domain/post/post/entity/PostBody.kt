package com.back.domain.post.post.entity

import com.back.global.jpa.entity.BaseTime
import jakarta.persistence.Entity

@Entity
class PostBody(
    var content: String
) : BaseTime() {

}