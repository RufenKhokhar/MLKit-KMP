@file:Suppress("UNCHECKED_CAST")

package sample.app.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import sample.app.barcode.BarcodeScanViewModel
import kotlin.reflect.KClass

class ViewModelFactory: ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        if (modelClass.isInstance(BarcodeScanViewModel::class)){
            return BarcodeScanViewModel() as T
        }
        return super.create(modelClass, extras)
    }
}