package com.example.todolist2.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Module 3: Firestore data source for gamification data
 * Currently using local database, but can sync to Firestore if needed
 */
@Singleton
class FirestoreGamificationDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Future: Can add Firestore sync methods here if needed
    // For now, gamification data is calculated from local database
}














