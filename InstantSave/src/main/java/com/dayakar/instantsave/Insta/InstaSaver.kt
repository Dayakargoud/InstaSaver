package com.dayakar.instantsave.Insta

import com.dayakar.instantsave.Insta.model.InstaPost
import kotlinx.coroutines.*
import java.lang.Exception

/*
* Created By DAYAKAR GOUD BANDARI on 08-01-2021.
*/

object InstaSaver {


      private val scope= CoroutineScope(Dispatchers.Main+ Job())
     suspend fun getInstaPost(url:String):InstaPost?{

       val result= scope.async(Dispatchers.IO){ PostsExtracter().fetchPostFromServer(url)}
        return try {
            result.await()
        }catch (e:Exception){
            e.printStackTrace()
            null
        }

    }

}



