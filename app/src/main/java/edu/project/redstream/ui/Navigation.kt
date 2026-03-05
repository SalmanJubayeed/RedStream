package edu.project.redstream.ui

import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import edu.project.redstream.ui.admin.AdminHomeScreen
import edu.project.redstream.ui.auth.RoleSelectScreen
import edu.project.redstream.ui.auth.SignInScreen
import edu.project.redstream.ui.auth.SignUpScreen
import edu.project.redstream.ui.auth.WelcomeScreen
import edu.project.redstream.ui.donor.DonorHomeScreen
import edu.project.redstream.ui.profile.ProfileScreen
import edu.project.redstream.ui.recipient.RecipientHomeScreen
import edu.project.redstream.viewmodel.AuthViewModel

sealed class Route(val path: String) {
    object Welcome       : Route("welcome")
    object SignIn        : Route("sign_in")
    object SignUp        : Route("sign_up")
    object RoleSelect    : Route("role_select/{uid}") {
        fun withUid(uid: String) = "role_select/$uid"
    }
    object DonorHome     : Route("donor_home")
    object RecipientHome : Route("recipient_home")
    object AdminHome     : Route("admin_home")
    object Profile       : Route("profile")
}

@Composable
fun RedStreamNavHost(
    navController: NavHostController,
    startDestination: String,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Route.Welcome.path) {
            WelcomeScreen(
                onSignIn = { navController.navigate(Route.SignIn.path) },
                onSignUp = { navController.navigate(Route.SignUp.path) }
            )
        }

        composable(Route.SignIn.path) {
            SignInScreen(
                viewModel = authViewModel,
                onGoToSignUp = { navController.navigate(Route.SignUp.path) }
            )
        }

        composable(Route.SignUp.path) {
            SignUpScreen(
                viewModel = authViewModel,
                onGoToSignIn = { navController.navigate(Route.SignIn.path) },
                onSuccess = { uid ->
                    navController.navigate(Route.RoleSelect.withUid(uid)) {
                        popUpTo(Route.Welcome.path) { inclusive = true }
                    }
                }
            )
        }

        composable("role_select/{uid}") { backStack ->
            val uid = backStack.arguments?.getString("uid") ?: ""
            RoleSelectScreen(
                uid = uid,
                viewModel = authViewModel
            )
        }

        composable(Route.DonorHome.path)     { DonorHomeScreen(navController) }
        composable(Route.RecipientHome.path) { RecipientHomeScreen(navController) }
        composable(Route.AdminHome.path)     { AdminHomeScreen(navController) }
        composable(Route.Profile.path)       { ProfileScreen(navController, authViewModel) }
    }
}