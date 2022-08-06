package com.androiddevs.data

import com.androiddevs.data.collections.Note
import com.androiddevs.data.collections.User
import com.androiddevs.security.checkHashForPassword
import org.litote.kmongo.contains

import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue

//equivalent to entities
private val client = KMongo.createClient().coroutine //specify that we use coroutines for all db operations
private val database = client.getDatabase("NotesDatabase")

private val users = database.getCollection<User>()
private val notes = database.getCollection<Note>()

suspend fun registerUser(user: User): Boolean = users.insertOne(user).wasAcknowledged()

suspend fun checkIfUserExists(email: String): Boolean { //go through all the user documents and use the email to compare to email
    return users.findOne(User::email eq email) != null //user.email == email
}

suspend fun checkPasswordForEmail(email: String, passwordToCheck: String): Boolean {
    val actualPassword = users.findOne(User::email eq email)?.password ?: return false
    return checkHashForPassword(passwordToCheck, actualPassword)
}

suspend fun getNotesForUser(email: String): List<Note> {
    return notes.find(Note::owners contains email).toList()
}

suspend fun saveNote(note: Note): Boolean {
    val noteExists = notes.findOneById(id = note.id) != null
    return if (noteExists) {
        notes.updateOneById(note.id, note).wasAcknowledged()
    } else {
        notes.insertOne(note).wasAcknowledged()
    }
}

suspend fun deleteNoteForUser(email: String, noteID: String): Boolean {
    //get the note and check if user has permission to delete , -> is the same as and eq and contains same
    val note = notes.findOne(Note::id eq noteID, Note::owners.contains(email))
    note?.let { note ->
        if (note.owners.size > 1) {
            //multiple owners only delete email
            val newOwners = note.owners.filter { it != email }
            val updatedNote = notes.updateOne(Note::id eq note.id, setValue(Note::owners, newOwners))
            return updatedNote.wasAcknowledged()
        }
        //user is the sole owner of note delete the whole document
        return notes.deleteOneById(noteID).wasAcknowledged()
    } ?: return false
}

suspend fun isOwnerOfNote(noteID: String, owner: String): Boolean {
    return notes.findOneById(noteID)?.owners?.contains(owner) ?: return false
}

suspend fun addOwnerToNote(noteID: String, owner: String): Boolean {
    val owners = notes.findOneById(noteID)?.owners ?: return false
    val newOwners = owners + owner
    return notes.updateOneById(noteID, setValue(Note::owners, newOwners)).wasAcknowledged()
}
