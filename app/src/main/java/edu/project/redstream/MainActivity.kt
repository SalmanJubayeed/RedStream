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

                val startDestination = if (authViewModel.currentUid != null)
                    Route.DonorHome.path
                else
                    Route.Welcome.path

                // When auth succeeds → fetch role → navigate to correct home
                LaunchedEffect(uiState) {
                    if (uiState is AuthUiState.Success) {
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
                }

                RedStreamNavHost(
                    navController = navController,
                    startDestination = startDestination,
                    authViewModel = authViewModel
                )
            }
        }
    }
}