package me.plenr.frontend.page.passwordsetup

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.text.text
import dev.kilua.html.AlignItems
import dev.kilua.html.button
import dev.kilua.html.h1t
import dev.kilua.html.label
import io.ktor.http.*
import kotlinx.coroutines.launch
import me.plenr.frontend.PlenrClient
import me.plenr.frontend.component.column
import web.window

@Composable
fun IComponent.passwordSetupPage(plenrClient: PlenrClient, token: String)
{
    val coroutineScope = rememberCoroutineScope()
    val router = Router.current

    val tokenUrlDecoded = token.decodeURLPart()

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }


    column(
        alignItems = AlignItems.Center
    ) {
        h1t("Password Setup")

        column {
            label {
                htmlFor("password")
                +"Password"
            }
            text(password, id = "password") {
                onInput {
                    password = this.value ?: ""
                }
                type(InputType.Password)
            }
        }

        column {
            label {
                htmlFor("confirm-password")
                +"Confirm Password"
            }
            text(confirmPassword, id = "confirm-password") {
                onInput {
                    confirmPassword = this.value ?: ""
                }
                type(InputType.Password)
            }
        }

        button {
            onClick {
                if (password != confirmPassword)
                {
                    window.alert("Passwords do not match")
                    return@onClick
                }

                coroutineScope.launch {
                    plenrClient.setPassword(tokenUrlDecoded, password)
                    window.alert("Password set")
                }
            }
            +"Set Password"
        }
    }
}