package com.androiddevs.routes

import com.androiddevs.data.checkPasswordForEmail
import com.androiddevs.data.requests.AccountRequest
import com.androiddevs.data.response.SimpleResponse
import io.ktor.application.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.logInRoute() {
    route("/login") {
        post {
            val request = try {
                call.receive<AccountRequest>() //parse JSON request
            } catch (e: ContentTransformationException) {
                call.respond(BadRequest)
                return@post
            }
            val isPasswordCorrect = checkPasswordForEmail(request.email, request.password)
            if (isPasswordCorrect){
                call.respond(
                    status = OK,
                    message = SimpleResponse(
                        successful = true,
                        message = "You are now logged in"
                    )
                )
            }else {
                call.respond(
                    status = OK,
                    message = SimpleResponse(
                        successful = false,
                        message = "The E-mail or password was incorrect"
                    )
                )
            }
        }
    }
}