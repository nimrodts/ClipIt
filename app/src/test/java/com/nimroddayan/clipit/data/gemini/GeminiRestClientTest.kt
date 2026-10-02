package com.nimroddayan.clipit.data.gemini

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GeminiRestClientTest {

    private lateinit var server: MockWebServer
    private lateinit var client: GeminiRestClient

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        client = GeminiRestClient(OkHttpClient(), server.url("/v1beta/").toString())
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    // ========== listModels ==========

    @Test
    fun `listModels keeps only generateContent models and strips prefix`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"models":[
                  {"name":"models/gemini-2.5-flash","displayName":"Gemini 2.5 Flash",
                   "supportedGenerationMethods":["generateContent","countTokens"]},
                  {"name":"models/text-embedding-004","displayName":"Text Embedding 004",
                   "supportedGenerationMethods":["embedContent"]}]}"""
            )
        )

        val models = client.listModels("test-key")

        assertEquals(listOf(GeminiModelInfo("gemini-2.5-flash", "Gemini 2.5 Flash", null)), models)
    }

    @Test
    fun `listModels sends key in header and never in url`() = runTest {
        server.enqueue(MockResponse().setBody("""{"models":[]}"""))

        client.listModels("secret-key")

        val request = server.takeRequest()
        assertEquals("secret-key", request.getHeader("x-goog-api-key"))
        assertEquals("/v1beta/models?pageSize=1000", request.path)
        assertFalse(request.path!!.contains("secret-key"))
    }

    @Test
    fun `listModels follows nextPageToken`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"models":[{"name":"models/a","supportedGenerationMethods":["generateContent"]}],
                   "nextPageToken":"abc"}"""
            )
        )
        server.enqueue(
            MockResponse().setBody(
                """{"models":[{"name":"models/b","supportedGenerationMethods":["generateContent"]}]}"""
            )
        )

        val models = client.listModels("k")

        assertEquals(listOf("a", "b"), models.map { it.id })
        server.takeRequest()
        assertTrue(server.takeRequest().path!!.contains("pageToken=abc"))
    }

    // ========== generateJson ==========

    @Test
    fun `generateJson returns text and sends instruction, schema and temperature`() = runTest {
        server.enqueue(MockResponse().setBody(candidateBody("""{"storeName":"KSP"}""")))
        val schema = buildJsonObject { put("type", "OBJECT") }

        val result = client.generateJson("k", "gemini-2.5-flash", "SYSTEM", "USER", 0.2f, schema)

        assertEquals("""{"storeName":"KSP"}""", result)
        val request = server.takeRequest()
        assertEquals("/v1beta/models/gemini-2.5-flash:generateContent", request.path)
        assertEquals("k", request.getHeader("x-goog-api-key"))
        val body = Json.parseToJsonElement(request.body.readUtf8()).jsonObject
        assertEquals("SYSTEM", body["systemInstruction"]!!.firstPartText())
        assertEquals("USER", body["contents"]!!.jsonArray[0].firstPartText())
        val config = body["generationConfig"]!!.jsonObject
        assertEquals("application/json", config["responseMimeType"]!!.jsonPrimitive.content)
        assertEquals(0.2, config["temperature"]!!.jsonPrimitive.double, 0.0001)
        assertEquals(schema, config["responseSchema"])
    }

    @Test
    fun `generateJson concatenates multi-part text`() = runTest {
        server.enqueue(MockResponse().setBody(candidateBody("""{"storeName":""", """"KSP"}""")))

        val result = client.generateJson("k", "m", "S", "U", 0f)

        assertEquals("""{"storeName":"KSP"}""", result)
    }

    @Test
    fun `generateJson maps 429 to GeminiQuotaException`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(429).setBody(
                """{"error":{"code":429,"message":"Resource exhausted","status":"RESOURCE_EXHAUSTED"}}"""
            )
        )

        val error = runCatching { client.generateJson("k", "m", "S", "U", 0f) }.exceptionOrNull()

        assertTrue("got $error", error is GeminiQuotaException)
        assertEquals("Resource exhausted", error!!.message)
    }

    @Test
    fun `generateJson maps API_KEY_INVALID to InvalidApiKeyException`() = runTest {
        server.enqueue(
            MockResponse().setResponseCode(400).setBody(
                """{"error":{"code":400,"message":"API key not valid.","status":"INVALID_ARGUMENT",
                   "details":[{"@type":"type.googleapis.com/google.rpc.ErrorInfo","reason":"API_KEY_INVALID"}]}}"""
            )
        )

        val error = runCatching { client.generateJson("bad", "m", "S", "U", 0f) }.exceptionOrNull()

        assertTrue("got $error", error is InvalidApiKeyException)
    }

    @Test
    fun `generateJson maps non-JSON server error to GeminiApiException`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500).setBody("oops"))

        val error = runCatching { client.generateJson("k", "m", "S", "U", 0f) }.exceptionOrNull()

        assertTrue("got $error", error is GeminiApiException)
        assertEquals(500, (error as GeminiApiException).httpCode)
        assertEquals("HTTP 500", error.message)
    }

    @Test
    fun `generateJson throws GeminiEmptyResponseException when prompt is blocked`() = runTest {
        server.enqueue(MockResponse().setBody("""{"promptFeedback":{"blockReason":"SAFETY"}}"""))

        val error = runCatching { client.generateJson("k", "m", "S", "U", 0f) }.exceptionOrNull()

        assertTrue("got $error", error is GeminiEmptyResponseException)
        assertTrue(error!!.message!!.contains("SAFETY"))
    }

    private fun candidateBody(vararg texts: String): String =
        buildJsonObject {
            putJsonArray("candidates") {
                addJsonObject {
                    putJsonObject("content") {
                        putJsonArray("parts") { texts.forEach { t -> addJsonObject { put("text", t) } } }
                    }
                    put("finishReason", "STOP")
                }
            }
        }.toString()

    private fun JsonElement.firstPartText(): String =
        jsonObject["parts"]!!.jsonArray[0].jsonObject["text"]!!.jsonPrimitive.content
}
