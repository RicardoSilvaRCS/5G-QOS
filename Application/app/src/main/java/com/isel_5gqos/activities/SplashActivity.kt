package com.isel_5gqos.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.ViewModelProviders
import androidx.work.WorkManager
import com.isel_5gqos.R
import com.isel_5gqos.common.TOKEN
import com.isel_5gqos.common.USER
import com.isel_5gqos.factories.QosFactory
import com.isel_5gqos.models.QosViewModel
import com.isel_5gqos.workers.scheduleRefreshTokenWorker

class SplashActivity : AppCompatActivity() {

    private lateinit var qosFactory: QosFactory
    private val model: QosViewModel by lazy {
        ViewModelProviders.of(this, qosFactory)[QosViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        qosFactory = QosFactory(savedInstanceState)

        WorkManager.getInstance(applicationContext).cancelAllWork()

        model.getLoggedUser().observe(this) {
            if(it == null){
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                model.refreshToken(it.username,it.token)
            }
        }

        model.liveData.observe(this) {
            if(it.userToken.isEmpty()){
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                val intent = Intent(this, DashboardActivity::class.java)
                //TODO: Debate if intent is really needed
                intent.putExtra(TOKEN, it.userToken)
                intent.putExtra(USER, it.username)

                /**Launch Refresh Token Worker**/
                scheduleRefreshTokenWorker(it.username,it.userToken,it.username)

                startActivity(intent)
                finish()
            }
        }
    }
}