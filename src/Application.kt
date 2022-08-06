package com.androiddevs


import com.androiddevs.data.checkPasswordForEmail
import com.androiddevs.routes.logInRoute
import com.androiddevs.routes.noteRoute
import com.androiddevs.routes.registerRoute
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@JvmOverloads
fun Application.module(testing: Boolean = false) { //all config options are lambdas
    install(DefaultHeaders) //install the feature for default headers
    install(CallLogging) //optional features
    install(ContentNegotiation) {//negotiates content type that it will respond with - HTML , JSON , etc.
        gson {
            setPrettyPrinting() //json will be formatted
        }
    }
    install(Authentication) { //needs to come before any feature that uses authentication
        configureAuth()
    }
    install(Routing){// define url endpoints that can be connected to
        registerRoute()
        logInRoute()
        noteRoute()
    }
}

private fun Authentication.Configuration.configureAuth() {
    basic {  //with oauth open that
        realm = "Note Server" //name of server that will pop up in the browser
        validate { credentials -> //need to check name and password and authenticate
            val email = credentials.name
            val password = credentials.password
            if (checkPasswordForEmail(email, password)) {
                UserIdPrincipal(email) //will be attached to the request can filter who is making the request and get their data
            } else null
        }
    }
}
