package com.dayakar.instantsave.Insta

import com.dayakar.instantsave.Insta.exceptions.InstaPrivateAccountException
import com.dayakar.instantsave.Insta.exceptions.InstaSaverException
import com.dayakar.instantsave.Insta.model.InstaPost
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.UnsupportedMimeTypeException
import java.io.IOException
import java.net.MalformedURLException
import java.net.SocketTimeoutException

/*
* Created By DAYAKAR GOUD BANDARI on 03-02-2021.
*/
class PostsExtracter{

    companion object{
        const val GRAPHQL="graphql"
        const val SHORT_CODE_MEDIA="shortcode_media"
        const val DISPLAY_URL="display_url"
        const val IS_VIDEO="is_video"
        const val VIDEO_URL="video_url"
        const val EDGE_WEB_MEDIA_TO_RELATED_MEDIA="edge_web_media_to_related_media"
        const val EDGES="edges"
        const val EDGE_SIDE_CAR_TO_CHILDREN="edge_sidecar_to_children"
        const val NODE="node"
        const val INVALID_STRING_REGEX="\"entry_data\":{\"HttpErrorPage\""
        const val DATA_PATTERN_START = "{\"config\":{\"csrf_token\""
        const val DATA_PATTERN_END = ",\"frontend_env\":\"prod\"}"
        const val DATA_PATTERN_END_LENGTH= DATA_PATTERN_END.length
        const val ENTRY_DATA="entry_data"
        const val POST_PAGE="PostPage"
        const val OWNER="owner"
        const val USERNAME="username"
        const val  EDGE_MEDIA_TO_CAPTION="edge_media_to_caption"
        const val TEXT="text"
        const val TAKEN_AT_TIMESTAMP="taken_at_timestamp"

    }


    suspend fun fetchPostFromServer(url: String):InstaPost?{

        try {
            val response = Jsoup.connect(url).get()
            val htmlRawString = response.toString()
            if (htmlRawString.contains(INVALID_STRING_REGEX)) {
                //Unable to load post due to some errors
                // return@withContext
                return null
            }
            return extractDataFromHtml(htmlRawString)

        } catch (e: IOException) {

            throw InstaSaverException("${e.message}")

        } catch (e: MalformedURLException) {
            e.printStackTrace()
            return null

        } catch (e: HttpStatusException) {

                throw InstaSaverException("${e.message}")
        } catch (e: UnsupportedMimeTypeException) {
            e.printStackTrace()
            return null
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
            return null
        }catch (e:StringIndexOutOfBoundsException){
            throw InstaSaverException("Unable load post")
        }

    }

    private suspend fun  extractDataFromHtml(htmlString:String):InstaPost?{

        val  startIndex = htmlString.indexOf(DATA_PATTERN_START)
        val endIndex = htmlString.indexOf(DATA_PATTERN_END)+ DATA_PATTERN_END_LENGTH
        val responseBody= htmlString.substring(startIndex, endIndex)

        if (responseBody.isNotEmpty()){

            return parseJsonFromHtml(responseBody)
        }else{
            throw InstaSaverException("Error parsing input data check entered url")
        }

    }

    private suspend fun parseJsonFromHtml(rawData:String):InstaPost? {
        val jsonObj = JSONObject(rawData)
        val jsonData = jsonObj.getJSONObject(ENTRY_DATA)

        if (!jsonData.has(POST_PAGE)) {
            throw InstaPrivateAccountException("Unable to load posts, Account may be private")
        } else {
            val jsonPostData=jsonData.getJSONArray(POST_PAGE)

            val finalJsonData=jsonPostData.get(0) as JSONObject

            return PostsExtracter().getPostForPublicProfile(finalJsonData.toString())
        }
    }


    private fun getPostForPublicProfile(jsonString: String): InstaPost? {
        var instaPost: InstaPost?=null
        var videoUrl: String? = null
        val displayUrls: ArrayList<String> = ArrayList()

        try {
            val jsonObj = JSONObject(jsonString)
            val graphql: JSONObject = jsonObj.getJSONObject(GRAPHQL)
            val shortCodeMedia = graphql.getJSONObject(SHORT_CODE_MEDIA)
            val displayUrl = shortCodeMedia.getString(DISPLAY_URL)
            val isVideo = shortCodeMedia.getBoolean(IS_VIDEO)
            val caption= getCaption(shortCodeMedia)
            val userName=getUserName(shortCodeMedia)
            val timeStamp=shortCodeMedia.getLong(TAKEN_AT_TIMESTAMP)
            if (isVideo) {
                videoUrl = shortCodeMedia.getString(VIDEO_URL)
            }

            val edgeWebMediaToRelatedMedia =
                    shortCodeMedia.getJSONObject(EDGE_WEB_MEDIA_TO_RELATED_MEDIA)
            val edges = edgeWebMediaToRelatedMedia.getJSONArray(EDGES)
            var edges1: JSONArray? = null

            try {
                try {
                    val edgeSidecarToChildren =
                            shortCodeMedia.getJSONObject(EDGE_SIDE_CAR_TO_CHILDREN)
                    edges1 = edgeSidecarToChildren.getJSONArray(EDGES)
                } catch (e: JSONException) {

                }

                //Checks for multiple posts
                if (edges != null && edges.length() > 0) {

                    val multiplePostUrls = getAvailablePosts(edges)
                    instaPost = InstaPost(userName,multiplePostUrls,caption,timeStamp)

                } else if (edges1 != null && edges1.length() > 0) {

                    val multiplePostUrls = getAvailablePosts(edges1)
                    instaPost = InstaPost(userName,multiplePostUrls,caption,timeStamp)

                } else {
                    if (isVideo) {
                        videoUrl?.let { it1 -> displayUrls.add(it1) }
                    } else {
                        displayUrls.add(displayUrl)
                    }

                    instaPost = InstaPost(userName,displayUrls,caption,timeStamp)

                }

                return instaPost
            } catch (e: Exception) {
                throw InstaSaverException("Please enter correct link")

            }

        } catch (e: Exception) {
            throw InstaSaverException("Please enter correct link")

        }


    }

    private fun getUserName(shortCodeMedia: JSONObject): String? {
        return try {
            shortCodeMedia.getJSONObject(OWNER)
                .getString(USERNAME)
                .toString()
        }catch (e:JSONException){
            null
        }

    }

    private fun getCaption(shortCodeMedia: JSONObject): String? {
        return try {
            shortCodeMedia.getJSONObject(EDGE_MEDIA_TO_CAPTION)
                .getJSONArray(EDGES)
                .getJSONObject(0)
                .getJSONObject(NODE)
                .getString(TEXT)
                .toString()
                .replace("\n", " ")
        }catch (e:JSONException){
            null
        }

    }

    private fun getAvailablePosts(
            edges: JSONArray): List<String> {
        val multiPostUrls = ArrayList<String>()
        var edgeDisplayUrl: String
        var edgeIsVideo: Boolean
        var edgeVideoUrl: String
        for (i in 0 until edges.length()) {
            val node = edges.getJSONObject(i).getJSONObject(NODE)

            edgeDisplayUrl = node.getString(DISPLAY_URL)
            edgeIsVideo = node.getBoolean(IS_VIDEO)

            if (edgeIsVideo) {
                edgeVideoUrl = node.getString(VIDEO_URL)
                multiPostUrls.add(edgeVideoUrl)
            } else {
                multiPostUrls.add(edgeDisplayUrl)
            }
        }
        return multiPostUrls
    }
}

