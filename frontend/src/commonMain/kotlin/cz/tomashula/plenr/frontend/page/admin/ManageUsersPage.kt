package cz.tomashula.plenr.frontend.page.admin

import androidx.compose.runtime.*
import app.softwork.routingcompose.Router
import cz.tomashula.plenr.feature.user.UserDto
import cz.tomashula.plenr.frontend.MainViewModel
import cz.tomashula.plenr.frontend.Route
import cz.tomashula.plenr.frontend.component.applyColumn
import cz.tomashula.plenr.frontend.component.applyRow
import cz.tomashula.plenr.frontend.component.materialIconOutlined
import dev.kilua.core.IComponent
import dev.kilua.html.*
import web.window

@Composable
fun IComponent.manageUsersPage(viewModel: MainViewModel)
{
    val router = Router.current
    var allUsersExceptMe: List<UserDto>? by remember { mutableStateOf(listOf()) }

    LaunchedEffect(Unit) {
        allUsersExceptMe = viewModel.getAllUsers() - viewModel.user!!
    }

    div {
        applyColumn()
        rowGap(10.px)

        pt("Users:")
        if (allUsersExceptMe == null)
        {
            pt("Loading users...")
            return@div
        }

        div("manage-users-list") {
            applyColumn()
            rowGap(10.px)
            allUsersExceptMe!!.forEach { user ->
                manageUserCard(
                    user = user,
                    onEditClick = { window.alert("TODO: Edit clicked") },
                    onDeleteClick = { window.alert("TODO: Delete clicked") }
                )
            }
        }

        button("Add User", id = "add-user-button", className = "primary-button") {
            onClick {
                router.navigate(Route.ADD_USER)
            }
        }
    }

}

@Composable
fun IComponent.manageUserCard(
    user: UserDto,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
)
{
    div("user-card ${if (user.isAdmin) "admin" else ""}") {
        applyRow(
            justifyContent = JustifyContent.SpaceBetween,
            alignItems = AlignItems.Center
        )
        spant("${user.firstName} ${user.lastName}")

        div("manage-user-card-options") {
            applyRow(
                justifyContent = JustifyContent.SpaceBetween,
                alignItems = AlignItems.Center
            )
            button(className = "icon-button") {
                onClick { onEditClick() }
                materialIconOutlined("edit")
            }
            button(className = "icon-button") {
                onClick { onDeleteClick() }
                materialIconOutlined("delete")
            }
        }
    }
}
