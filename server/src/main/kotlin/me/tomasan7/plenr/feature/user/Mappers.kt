package me.tomasan7.plenr.feature.user

import org.jetbrains.exposed.sql.ResultRow

fun ResultRow.toUserDto() = UserDto(
    id = this[UserTable.id].value,
    name = this[UserTable.name],
    email = this[UserTable.email],
    phone = this[UserTable.phone],
    isAdmin = this[UserTable.isAdmin],
    isActive = this[UserTable.passwordHash] != null
)