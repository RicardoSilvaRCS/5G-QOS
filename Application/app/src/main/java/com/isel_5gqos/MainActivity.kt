package com.isel_5gqos

import android.os.Bundle
import android.os.Parcelable
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.qiniu.android.netdiag.Output
import com.qiniu.android.netdiag.Ping
import com.qiniu.android.netdiag.Ping.Result

open class MyModel():ViewModel() {
    val liveData: MutableLiveData<Result> by lazy {
        MutableLiveData<Result>()
    }

    val pingResult: Result? get() = liveData.value

    fun observe(owner: LifecycleOwner, observer: (Result) -> Unit) {
        liveData.observe(owner, androidx.lifecycle.Observer { observer(it) })
    }
}

open class Factory(private val bundle: Bundle?):ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val model: Parcelable? = bundle?.getParcelable(getParcelableValue())

        return if(model != null) {
            model as T
        } else {
            getModel() as T
        }
    }

    fun getModel(): ViewModel = InternetModel()
    fun getParcelableValue():String = "FACTORY"
}

class InternetModel() : MyModel() {
    lateinit var result: Result
    fun getResults () {
        Ping.start("www.google.com", 25, TestLogger(), Ping.Callback { result = it })
    }


}

class MainActivity : AppCompatActivity() {

    private lateinit var factory: Factory
    val model : InternetModel by lazy {
        ViewModelProviders.of(this,factory)[InternetModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        factory = Factory(savedInstanceState)

        val text = findViewById<TextView>(R.id.text)
        val texts = findViewById<TextView>(R.id.texts)

        model.observe(this) {
            print(model.result)
            text.text = model.result.result
            texts.text = model.result.avg.toString()
        }

        model.getResults()
    }
}

class TestLogger : Output {
    override fun write(line: String?) {
//        println(line)
    }

}
