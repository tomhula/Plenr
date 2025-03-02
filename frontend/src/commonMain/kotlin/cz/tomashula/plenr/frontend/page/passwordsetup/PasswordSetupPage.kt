package cz.tomashula.plenr.frontend.page.passwordsetup

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import dev.kilua.core.IComponent
import dev.kilua.form.InputType
import dev.kilua.form.form
import dev.kilua.form.text.text
import dev.kilua.html.*
import io.ktor.http.*
import kotlinx.coroutines.launch
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.applyColumn
import cz.tomashula.plenr.frontend.component.formField
import cz.tomashula.plenr.frontend.component.onSubmit
import web.window

@Composable
fun IComponent.passwordSetupPage(mainViewModel: MainViewModel, token: String)
{
    val coroutineScope = rememberCoroutineScope()
    val router = Router.current

    val tokenUrlDecoded = token.decodeURLPart()

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }


    form(className = "form") {
        applyColumn(alignItems = AlignItems.Center)
        onSubmit {
            if (password != confirmPassword)
            {
                window.alert("Passwords do not match")
                return@onSubmit
            }

            coroutineScope.launch {
                mainViewModel.setPassword(tokenUrlDecoded, password)
                router.navigate(Route.LOGIN)
            }
        }

        h1t("Password Setup")

        formField(
            inputId = "password-field",
            label = "Password",
            value = password,
            type = InputType.Password,
            onChange = { password = it }
        )

        formField(
            inputId = "confirm-password-field",
            label = "Confirm Password",
            value = confirmPassword,
            type = InputType.Password,
            onChange = { confirmPassword = it }
        )

        button("Set Password", type = ButtonType.Submit, className = "primary-button")
    }
}
