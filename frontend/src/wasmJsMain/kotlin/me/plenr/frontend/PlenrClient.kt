package me.plenr.frontend

import dev.kilua.externals.JSON
import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.http.*
import io.ktor.util.*
import kotlinx.browser.localStorage
import kotlinx.browser.window
import kotlinx.rpc.krpc.ktor.client.installRPC
import kotlinx.rpc.krpc.ktor.client.rpc
import kotlinx.rpc.krpc.ktor.client.rpcConfig
import kotlinx.rpc.krpc.serialization.json.json
import kotlinx.rpc.withService
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.tomasan7.plenr.feature.user.UserDto
import me.tomasan7.plenr.feature.user.UserService

private const val AUTH_TOKEN_STORAGE_KEY = "authToken"
private const val USER_STORAGE_KEY = "user"

class PlenrClient
{
    private val httpClient = HttpClient(Js) {
        /*defaultRequest {
            url(window.origin + "/api/")
        }*/
        /*install(ContentNegotiation) {
            json()
        }*/
        installRPC()
    }
    private val json = Json
    private lateinit var userService: UserService
    private var authToken: String? = null
    var user: UserDto? = null

    val isLoggedIn: Boolean
        get() = authToken != null && user != null

    suspend fun init()
    {
        userService = httpClient.rpc {
            url {
                host = window.location.hostname
                port = getCurrentPort()
                encodedPath = "/api"
            }

            rpcConfig {
                serialization {
                    json(json)
                }
            }
        }.withService<UserService>()

        authToken = localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)
        user = localStorage.getItem(USER_STORAGE_KEY)?.let { json.decodeFromString(it) }
    }

    suspend fun adminExists() = userService.adminExists()

    suspend fun createUser(user: UserDto) = userService.createUser(user, authToken)

    suspend fun setPassword(tokenB64: String, password: String)
    {
        val token = tokenB64.decodeBase64Bytes()
        userService.setPassword(token, password)
    }

    suspend fun login(username: String, password: String): Boolean
    {
        val authResponse = userService.login(username, password) ?: return false

        authToken = authResponse.authToken
        user = authResponse.user
        localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, authToken!!)
        localStorage.setItem(USER_STORAGE_KEY, json.encodeToString(user))

        return true
    }
}