package com.priyanshumaurya8868.firestoreops

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.priyanshumaurya8868.firestoreops.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.StringBuilder

class MainActivity : AppCompatActivity() {

    val personsCollectionRef = Firebase.firestore.collection("persons")
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        binding.apply{
            btnUploadData.setOnClickListener {
                val fName = etFirstName.text.toString()
                val lName = etLastName.text.toString()
                val age = etAge.text.toString().toInt()
               val person = Person(fName,lName,age)
                    save(person)
            }
            btnRetrieveData.setOnClickListener {
                retrieveData()
            }
        }
        subscribeRealtimeUpdates()
    }

    private fun subscribeRealtimeUpdates() {
        personsCollectionRef.addSnapshotListener{ querySnapshot, firebaseFirestoreException ->
         querySnapshot?.let {
             val  sb = StringBuilder()
             for (doc in it.documents){
                 val p = doc.toObject<Person>()
                 sb.append("$p\n")
             }
                 binding.tvPersons.text = sb.toString()
                 Toast.makeText(this@MainActivity,"Data Retrieved...!",Toast.LENGTH_LONG).show()

         }
            firebaseFirestoreException?.let {
                Toast.makeText(this@MainActivity,it.message,Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun retrieveData() = CoroutineScope(Dispatchers.IO).launch{
        try {
          val querySnapshot =  personsCollectionRef.get().await()
            val  sb = StringBuilder()
            for (doc in querySnapshot.documents){
                val p = doc.toObject<Person>()
                sb.append("$p\n")
            }
            withContext(Dispatchers.Main){
                binding.tvPersons.text = sb.toString()
                Toast.makeText(this@MainActivity,"Data Retrieved...!",Toast.LENGTH_LONG).show()
            }

        }catch (e:Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MainActivity,e.message,Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun save(person: Person) = CoroutineScope(Dispatchers.IO).launch{
      try {
          personsCollectionRef.add(person).await()
          withContext(Dispatchers.Main){
              Toast.makeText(this@MainActivity,"Succeed...!",Toast.LENGTH_LONG).show()
          }
      }catch (e:Exception){
          withContext(Dispatchers.Main){
              Toast.makeText(this@MainActivity,e.message,Toast.LENGTH_LONG).show()
          }
      }
    }


}