package com.liveklass.dto

import com.liveklass.domain.Class

data class FindClassListResponse(
    val responses: List<FindClassResponse>
) {
    companion object {
        fun from(classes: List<Class>): FindClassListResponse {
            return FindClassListResponse(classes.map(FindClassResponse::from))
        }
    }
}
