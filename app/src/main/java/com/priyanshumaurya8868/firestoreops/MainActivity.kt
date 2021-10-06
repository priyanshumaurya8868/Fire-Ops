package com.priyanshumaurya8868.firestoreops

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.priyanshumaurya8868.firestoreops.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val personsCollectionRef = Firebase.firestore.collection("persons")
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.apply {
            btnUploadData.setOnClickListener {
                val person = getOldPerson()
                save(person)
            }
            btnRetrieveData.setOnClickListener {
                retrieveData()
            }

            btnUpdatePerson.setOnClickListener {
                val oldPerson = getOldPerson()
                val newPerson = getNewPerson()
                updatePerson(oldPerson, newPerson)
            }

        }
        subscribeRealtimeUpdates()
    }

    private fun updatePerson(oldPerson: Person, newPerson: Map<String, Any>) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val querySnapshot = personsCollectionRef
                    .whereEqualTo("firstName", oldPerson.firstName)
                    .whereEqualTo("lastName", oldPerson.lastName)
                    .whereEqualTo("age", oldPerson.age)
                    .get()
                    .await()
                if (querySnapshot.documents.isNotEmpty()) {
                    for (doc in querySnapshot) {
                        personsCollectionRef.document(doc.id).set(
                            newPerson,
                            SetOptions.merge()
                        ).await()
                    }
                } else
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@MainActivity,
                            "No persons matched the query.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT).show()
                }
            }

        }

    private fun getNewPerson(): Map<String, Any> {

        val fName = binding.etNewFirstName.text.toString()
        val lName = binding.etNewLastName.text.toString()
        val age = binding.etNewAge.text.toString()

        val map = mutableMapOf<String, Any>()
        if (fName.isNotBlank())
            map["firstName"] = fName
        if (lName.isNotBlank())
            map["lastName"] = lName
        if (age.isNotBlank())
            map["age"] = age.toInt()

        return map
    }

    private fun getOldPerson(): Person {
        val fName = binding.etFirstName.text.toString()
        val lName = binding.etLastName.text.toString()
        val age = binding.etAge.text.toString().takeIf { it.isNotBlank() }?.toInt()
        return Person(fName, lName, age)
    }

    private fun subscribeRealtimeUpdates() {
        personsCollectionRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            querySnapshot?.let {
                val sb = StringBuilder()
                for (doc in it.documents) {
                    val p = doc.toObject<Person>()
                    sb.append("$p\n")
                }
                binding.tvPersons.text = sb.toString()
                Toast.makeText(this@MainActivity, "Data Retrieved...!", Toast.LENGTH_LONG).show()

            }
            firebaseFirestoreException?.let {
                Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun retrieveData() = CoroutineScope(Dispatchers.IO).launch {
        binding.apply {
            val from = etFrom.text.toString().toInt()
            val to = etTo.text.toString().toInt()
            try {
                val querySnapshot = personsCollectionRef
                    .whereGreaterThan("age", from)
                    .whereLessThan("age", to)
                    .get()
                    .await()

                val sb = StringBuilder()
                for (doc in querySnapshot.documents) {
                    val p = doc.toObject<Person>()
                    sb.append("$p\n")
                }
                withContext(Dispatchers.Main) {
                    binding.tvPersons.text = sb.toString()
                    Toast.makeText(this@MainActivity, "Data Retrieved...!", Toast.LENGTH_LONG)
                        .show()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun save(person: Person) = CoroutineScope(Dispatchers.IO).launch {
        try {
            personsCollectionRef.add(person).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Succeed...!", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }


}