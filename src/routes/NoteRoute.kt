package com.androiddevs.routes

import com.androiddevs.data.*
import com.androiddevs.data.collections.Note
import com.androiddevs.data.requests.AddOwnerRequest
import com.androiddevs.data.requests.DeleteNoteRequest
import com.androiddevs.data.response.SimpleResponse
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.noteRoute() {
    route("/getNotes") {
        authenticate {
            get {//get the user email attached to the authentication
                val email = call.principal<UserIdPrincipal>()!!.name
                val notes = getNotesForUser(email)
                call.respond(
                    status = OK,
                    message = notes
                )
            }
        }
    }

    route("/addNote") {
        authenticate {
            post {
                val note = try {
                    call.receive<Note>()
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                if (saveNote(note)) {
                    call.respond(OK)
                } else {
                    call.respond(Conflict)
                }
            }
        }
    }

    route("/deleteNote") {
        authenticate {
            post {
                val deleteRequest: Pair<DeleteNoteRequest, String> = try {
                    Pair(call.receive(), call.principal<UserIdPrincipal>()!!.name)
                } catch (e: ContentTransformationException) {
                    call.respond(BadRequest)
                    return@post
                }
                when (deleteNoteForUser(deleteRequest.second, deleteRequest.first.id)){
                    true -> call.respond(OK)
                    false -> call.respond(Conflict)
                }
            }
        }
    }

    route("/addOwnerToNote") {
        authenticate {
            post {
                val request = try {
                    call.receive<AddOwnerRequest>()
                } catch (e: Exception) {
                    call.respond(BadRequest)
                    return@post
                }
                if (!checkIfUserExists(request.owner)) {
                    call.respond(
                        status = Conflict,
                        message = SimpleResponse(
                            successful = false,
                            message = "User does not exist"
                        )
                    )
                    return@post
                }
                when (isOwnerOfNote(request.noteID, request.owner)) {
                    true -> {
                        call.respond(
                            status = Conflict,
                            message = SimpleResponse(
                                successful = false,
                                message = "User is Already an owner"
                            )
                        )
                        return@post
                    }
                    false -> {
                        if(!addOwnerToNote(request.noteID, request.owner)){
                            call.respond(
                                status = OK,
                                message = SimpleResponse(
                                    successful = false,
                                    message = "An Unknown Error Occurred could not add owner"
                                )
                            )
                            return@post
                        }
                        call.respond(
                            status = OK,
                            message = SimpleResponse(
                                successful = true,
                                message = "owner added successfully"
                            )
                        )
                        return@post
                    }
                }
            }
        }
    }
}