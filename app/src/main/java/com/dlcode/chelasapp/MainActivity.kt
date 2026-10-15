package com.dlcode.chelasapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.dlcode.chelasapp.data.AppDatabase
import com.dlcode.chelasapp.repository.BusinessRepository
import com.dlcode.chelasapp.ui.navigation.AppNavigation
import com.dlcode.chelasapp.ui.theme.ChelasAppTheme
import com.dlcode.chelasapp.viewmodel.BusinessViewModel
import com.dlcode.chelasapp.viewmodel.BusinessViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Instalamos la Splash Screen de la API de Android ANTES de super.onCreate
        // Esto permite que el sistema oculte la pantalla de inicio por defecto
        installSplashScreen()

        super.onCreate(savedInstanceState)
        
        // Inicialización de la base de datos Room
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "sanjuan-db"
        ).fallbackToDestructiveMigration().build()

        // Lógica temporal para borrar datos locales (Solicitado por el usuario para prueba real)
        java.lang.Thread {
            db.clearAllTables()
        }.start()
        
        val repository = BusinessRepository(db.businessDao())
        val viewModelFactory = BusinessViewModelFactory(repository)

        enableEdgeToEdge()
        setContent {
            ChelasAppTheme {
                val navController = rememberNavController()
                val viewModel: BusinessViewModel = viewModel(factory = viewModelFactory)
                
                AppNavigation(navController = navController, viewModel = viewModel)
            }
        }
    }
}
