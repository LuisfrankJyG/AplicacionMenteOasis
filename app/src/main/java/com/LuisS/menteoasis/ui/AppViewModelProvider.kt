package com.LuisS.menteoasis.ui

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.LuisS.menteoasis.MenteOasisApplication
import com.LuisS.menteoasis.data.MenteOasisRepository
import com.LuisS.menteoasis.ui.features.asistencia.AsistenciaViewModel
import com.LuisS.menteoasis.ui.features.cumpleanos.CumpleanosViewModel

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer {
            AsistenciaViewModel(
                menteOasisApplication().repository
            )
        }
        initializer {
            CumpleanosViewModel(
                menteOasisApplication().repository,
                menteOasisApplication()
            )
        }
    }
}

fun CreationExtras.menteOasisApplication(): MenteOasisApplication =
    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as MenteOasisApplication)
