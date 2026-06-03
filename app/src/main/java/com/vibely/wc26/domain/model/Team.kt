package com.vibely.wc26.domain.model

data class Team(
    val code: String,   // "MEX", "CAN", ...
    val name: String,   // "Mexico", "Canada", ...
    val group: String,  // "A" .. "L"
    val seed: Int,      // 1..4 (FIFA seed within group)
)
