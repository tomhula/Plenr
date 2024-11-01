package me.tomasan7.plenr

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import me.tomasan7.plenr.config.Config
import me.tomasan7.plenr.config.ConfigManager
import me.tomasan7.plenr.config.EnvVarConfigManager
import me.tomasan7.plenr.config.JsonFileConfigManager
import me.tomasan7.plenr.module.configureAuthentication
import me.tomasan7.plenr.module.configureContentNegotiation
import me.tomasan7.plenr.module.configureExceptionHandling
import me.tomasan7.plenr.module.configureRouting
import me.tomasan7.plenr.feature.user.DatabaseUserService
import me.tomasan7.plenr.feature.user.UserService
import me.tomasan7.plenr.mail.MailService
import me.tomasan7.plenr.mail.SmtpMailService
import me.tomasan7.plenr.security.*
import org.jetbrains.exposed.sql.Database
import java.nio.file.Path

class Plenr : ConfigManager
{
    private lateinit var embeddedServer: EmbeddedServer<*, *>
    private lateinit var configManager: ConfigManager
    private lateinit var config: Config
    private lateinit var database: Database
    private lateinit var passwordValidator: PasswordValidator
    private lateinit var passwordHasher: PasswordHasher
    private lateinit var tokenGenerator: TokenGenerator

    lateinit var userService: UserService
    lateinit var mailService: MailService

    fun init(configFilePath: Path)
    {
        runBlocking {
            /* Config has to be initialized first */
            initConfigManager(configFilePath)
            reloadConfig()

            initPasswordManagement()
            initApplicationEngine()
            initDb()
            initServices()
        }
    }

    private suspend fun initConfigManager(configFilePath: Path)
    {
        configManager = EnvVarConfigManager(JsonFileConfigManager(configFilePath))
        configManager.initConfig()
    }

    private fun initPasswordManagement()
    {
        passwordValidator = PasswordValidator(
            lowerCaseLetter = config.passwordRequirements.requireLowercaseLetter,
            upperCaseLetter = config.passwordRequirements.requireUppercaseLetter,
            number = config.passwordRequirements.requireNumber,
            specialSymbol = config.passwordRequirements.requireSpecialSymbol,
            minLength = config.passwordRequirements.minLength
        )
        passwordHasher = Sha256PasswordHasher()
        tokenGenerator = SecureRandomTokenGenerator()
    }

    private fun initApplicationEngine()
    {
        embeddedServer = embeddedServer(
            factory = Netty,
            host = config.server.host,
            port = config.server.port,
            module = { module() }
        )
    }

    private fun initDb()
    {
        val dbConfig = config.database
        database = Database.connect(
            url = dbConfig.url,
            driver = dbConfig.driver,
            user = dbConfig.user,
            password = dbConfig.password
        )
    }

    private suspend fun initServices()
    {
        mailService = SmtpMailService(
            smtpHost = config.smtp.host,
            smtpPort = config.smtp.port,
            smtpUsername = config.smtp.username,
            smtpPassword = config.smtp.password
        )
        userService = DatabaseUserService(
            database,
            passwordValidator,
            passwordHasher,
            tokenGenerator,
            config.server.url,
            mailService
        ).also { it.createIfNotExists() }
    }

    fun startBlocking()
    {
        embeddedServer.start(wait = true)
    }

    private fun Application.module()
    {
        configureContentNegotiation()
        configureExceptionHandling()
        configureAuthentication()
        configureRouting(this@Plenr)
    }

    override suspend fun getConfig() = config

    override suspend fun setConfig(config: Config)
    {
        this.config = config
        configManager.setConfig(config)
    }

    suspend fun reloadConfig()
    {
        config = configManager.getConfig()
    }
}