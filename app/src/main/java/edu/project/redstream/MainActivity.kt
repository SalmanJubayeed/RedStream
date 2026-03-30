package edu.project.redstream

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import edu.project.redstream.data.model.UserRole
import edu.project.redstream.data.repository.UserRepository
import edu.project.redstream.ui.RedStreamNavHost
import edu.project.redstream.ui.Route
import edu.project.redstream.ui.theme.RedStreamTheme
import edu.project.redstream.viewmodel.AuthUiState
import edu.project.redstream.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RedStreamTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val navController = rememberNavController()
                val uiState by authViewModel.uiState.collectAsState()
                val scope = rememberCoroutineScope()

                // Always start at Welcome — LaunchedEffect below
                // will immediately redirect if already signed in
                RedStreamNavHost(
                    navController    = navController,
                    startDestination = Route.Welcome.path,
                    authViewModel    = authViewModel
                )

                // On first launch, check if already signed in
                LaunchedEffect(Unit) {
                    val uid = authViewModel.currentUid
                    if (uid != null) {
                        // Already signed in — check profile exists
                        val user = userRepository.getUser(uid)
                        if (user == null) {
                            // Auth exists but no Firestore doc → setup profile
                            navController.navigate(Route.RoleSelect.withUid(uid)) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            // Profile exists → go to correct home
                            val destination = when (user.toRole()) {
                                UserRole.RECIPIENT -> Route.RecipientHome.path
                                UserRole.ADMIN     -> Route.AdminHome.path
                                else               -> Route.DonorHome.path
                            }
                            navController.navigate(destination) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                    // If uid is null — stay on Welcome screen, do nothing
                }

                // Watch auth state changes during the session
                LaunchedEffect(uiState) {
                    when (uiState) {

                        is AuthUiState.Success -> {
                            val uid = (uiState as AuthUiState.Success).uid
                            scope.launch {
                                val user = userRepository.getUser(uid)
                                val destination = when (user?.toRole()) {
                                    UserRole.RECIPIENT -> Route.RecipientHome.path
                                    UserRole.ADMIN     -> Route.AdminHome.path
                                    else               -> Route.DonorHome.path
                                }
                                navController.navigate(destination) {
                                    popUpTo(0) { inclusive = true }
                                }
                                authViewModel.clearState()
                            }
                        }

                        is AuthUiState.NeedsProfile -> {
                            val uid = (uiState as AuthUiState.NeedsProfile).uid
                            navController.navigate(Route.RoleSelect.withUid(uid)) {
                                popUpTo(0) { inclusive = true }
                            }
                            authViewModel.clearState()
                        }

                        is AuthUiState.SignUpSuccess -> {
                            // SignUpScreen handles this — do nothing here
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}
