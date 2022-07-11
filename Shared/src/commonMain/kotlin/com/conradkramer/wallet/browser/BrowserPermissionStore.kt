package com.conradkramer.wallet.browser

import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver

internal class BrowserPermissionStore(private val driver: SqlDriver) {
    enum class State {
        ALLOWED,
        DENIED,
        UNSPECIFIED;
    }

    fun allow(domain: String, permission: String) {
        driver.change(domain, permission, true)
    }

    fun deny(domain: String, permission: String) {
        driver.change(domain, permission, false)
    }

    fun state(domain: String, permission: String): State {
        val allowed = driver.permissions(domain, true)
        val denied = driver.permissions(domain, false)
        return when (allowed.contains(permission) to denied.contains(permission)) {
            (true to false) -> State.ALLOWED
            (false to true) -> State.DENIED
            (false to false) -> State.UNSPECIFIED
            else -> throw Exception("Invalid permissions state for $permission in domain $domain")
        }
    }
}

private fun SqlDriver.change(domain: String, permission: String, allow: Boolean) {
    val add = if (allow) "allow" else "deny"
    val remove = if (allow) "deny" else "allow"
    val sql = """
    INSERT INTO browser_permission(domain, $add)
    VALUES (?1, json_array(?2))
    ON CONFLICT(domain) DO UPDATE
    SET $add = (
        SELECT json_group_array(value) from (
            SELECT DISTINCT value
            FROM json_each(json_insert($add, '${'$'}[#]', ?2))
        )
    ),
    $remove = (
        SELECT json_group_array(value) FROM (
            SELECT DISTINCT value
            FROM json_each($remove)
            WHERE value != ?2
        )
    )
    """.trimIndent()

    execute(null, sql, 2) {
        bindString(1, domain)
        bindString(2, permission)
    }
}

private fun <R> SqlCursor.rows(mapper: (SqlCursor) -> R): List<R> {
    val list = mutableListOf<R>()
    while (next()) {
        list.add(mapper(this))
    }
    return list
}

private fun SqlDriver.permissions(domain: String, allowed: Boolean): Set<String> {
    val column = if (allowed) "allow" else "deny"
    val sql = "SELECT DISTINCT value FROM json_each((SELECT $column FROM browser_permission WHERE domain = ?))"
    val mapper = { cursor: SqlCursor -> cursor.rows { it.getString(0) }.filterNotNull().toSet() }
    return executeQuery(null, sql, mapper, 1) {
        bindString(1, domain)
    }.value
}
