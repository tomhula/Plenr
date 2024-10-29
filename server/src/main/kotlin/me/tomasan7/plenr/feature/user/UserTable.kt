package me.tomasan7.plenr.feature.user

import org.jetbrains.exposed.dao.id.IntIdTable

object UserTable : IntIdTable("user") {
    val name = varchar("name", 20)
    val email = varchar("email", 30).uniqueIndex()
    val phone = char("phone", 16)
    val passwordHash = binary("password_hash", 32).nullable()
    val isAdmin = bool("is_admin")
}