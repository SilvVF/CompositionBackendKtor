package com.androiddevs.routes

import com.androiddevs.data.checkIfUserExists
import com.androiddevs.data.collections.User
import com.androiddevs.data.registerUser
import com.androiddevs.data.requests.AccountRequest
import com.androiddevs.data.response.SimpleResponse
import com.androiddevs.security.getHashWithSalt
import io.ktor.application.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*


fun Route.registerRoute() {
    val alreadyExists = true
    val doesNotExist = false
    route("/register") {
        post { // everything inside the post block is executed inside a coroutine
            val request = try {
                //if everything goes right parse request to account request
                call.receive<AccountRequest>()
            } catch (e: ContentTransformationException) {
                //current incoming request
                call.respond(BadRequest) //respond with http status
                return@post
            }
            when (checkIfUserExists(request.email)) {
                doesNotExist -> {
                    if (registerUser(User(request.email, getHashWithSalt(request.password)))) {
                        call.respond(
                            status = OK,
                            message = SimpleResponse(
                                successful = true,
                                message = "Successfully Created Account"
                            )
                        )
                    } else {
                        call.respond(
                            status = OK,
                            message = SimpleResponse(
                                successful = false,
                                message = "Unknown error Occurred"
                            )
                        )
                    }
                }
                alreadyExists -> {
                    call.respond(
                        status = OK,
                        message = SimpleResponse(
                            successful = false,
                            message = "User with the email already exists"
                        )
                    )
                }
            }
        }
    }
}
