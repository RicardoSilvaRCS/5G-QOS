package com.isel_5gqos.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.work.WorkManager
import com.isel_5gqos.R
import com.isel_5gqos.common.MOBILE_ID_KEY
import com.isel_5gqos.common.TOKEN
import com.isel_5gqos.common.USER
import com.isel_5gqos.factories.QosFactory
import com.isel_5gqos.models.QosViewModel
import com.isel_5gqos.models.observeOnce
import com.isel_5gqos.utils.android_utils.AndroidUtils
import com.isel_5gqos.workers.scheduleRefreshTokenWorker

class SplashActivity : AppCompatActivity() {

    private lateinit var qosFactory: QosFactory
    private val model: QosViewModel by lazy {
        ViewModelProvider(this, qosFactory)[QosViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        qosFactory = QosFactory(savedInstanceState)

        WorkManager.getInstance(applicationContext).cancelAllWork()

        model.getLoggedUser().observeOnce(this) {
            if(it == null){
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{

                val mobileId = AndroidUtils.getPreferences( MOBILE_ID_KEY, applicationContext)

                Log.v("TESTE","The mobile id is : "+mobileId)

                model.refreshToken(it.user.username,it.login.token,mobileId!!)

                model.liveData.observeOnce(this) { user ->

                    if(user.userToken.isEmpty()){
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                    else{
                        val intent = Intent(this, DashboardActivity::class.java)
                        //TODO: Debate if intent is really needed
                        intent.putExtra(TOKEN, user.userToken)
                        intent.putExtra(USER, user.username)

                        /**Launch Refresh Token Worker**/
                        scheduleRefreshTokenWorker(user.username,user.userToken,it.user.credentials)

                        startActivity(intent)
                        finish()
                    }
                }

            }
        }
    }

}