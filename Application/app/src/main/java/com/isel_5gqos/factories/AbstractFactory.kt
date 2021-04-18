package com.isel_5gqos.factories

import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.isel_5gqos.Common.TAG


abstract class AbstractFactory(private val bundle: Bundle?) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val model: Parcelable? = bundle?.getParcelable(getParcelableValue())

        return if (model != null) {
            Log.v(TAG, " ** Restoring ${modelClass.name} from a bundle! **")
            model as T
        } else {
            Log.v(TAG, "** CREATING a new ${getModel()::class.simpleName}! **")
            getModel() as T
        }
    }

    abstract fun getModel(): ViewModel

    abstract fun getParcelableValue(): String

}
