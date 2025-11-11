package com.example.todolist2.data.remote.firebase

import com.example.todolist2.domain.model.User
import com.example.todolist2.util.Resource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Module 1: Firebase Authentication data source
 */
@Singleton
class FirebaseAuthDataSource @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    
    fun getCurrentUser(): FirebaseUser? = auth.currentUser
    
    fun getCurrentUserId(): String? = auth.currentUser?.uid
    
    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
    
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Resource<User> {
        return try {
            // Step 1: Create user in Firebase Authentication (password is handled securely)
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("User creation failed")
            
            // Step 2: Create user profile in Firestore (WITHOUT password!)
            val user = User(
                id = firebaseUser.uid,
                email = email,
                username = displayName,  // Changed from displayName to username
                createdAt = System.currentTimeMillis()
            )
            
            // Step 3: Save to Firestore collection "users" with attributes: username, email (NO PASSWORD!)
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()
            
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign up failed")
        }
    }
    
    suspend fun signInWithEmail(email: String, password: String): Resource<User> {
        return try {
            // Step 1: Authenticate with Firebase Authentication (password is verified securely)
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Resource.Error("Sign in failed")
            
            // Step 2: Fetch user profile from Firestore collection "users" (contains username, email - NO PASSWORD!)
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()
            
            val user = userDoc.toObject(User::class.java) ?: User(
                id = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                username = firebaseUser.displayName ?: ""
            )
            
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Sign in failed")
        }
    }
    
    suspend fun signInWithGoogle(idToken: String): Resource<User> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val firebaseUser = result.user ?: return Resource.Error("Google sign in failed")
            
            // Check if user exists, if not create profile
            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()
            
            val user = if (userDoc.exists()) {
                userDoc.toObject(User::class.java) ?: User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    username = firebaseUser.displayName ?: ""
                )
            } else {
                val newUser = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    username = firebaseUser.displayName ?: "",
                    avatarUrl = firebaseUser.photoUrl?.toString() ?: "",
                    createdAt = System.currentTimeMillis()
                )
                firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(newUser)
                    .await()
                newUser
            }
            
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Google sign in failed")
        }
    }
    
    suspend fun signOut() {
        auth.signOut()
    }
    
    suspend fun resetPassword(email: String): Resource<Unit> {
        return try {
            auth.sendPasswordResetEmail(email).await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Password reset failed")
        }
    }
    
    suspend fun getUserProfile(userId: String): Resource<User> {
        return try {
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            val user = doc.toObject(User::class.java) 
                ?: return Resource.Error("User not found")
            
            Resource.Success(user)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to fetch user profile")
        }
    }
    
    suspend fun updateUserProfile(user: User): Resource<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id)
                .set(user)
                .await()
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update profile")
        }
    }
}

