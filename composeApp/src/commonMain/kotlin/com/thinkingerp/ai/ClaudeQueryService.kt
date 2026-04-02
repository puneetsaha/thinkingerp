package com.thinkingerp.ai

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Sends a natural-language inventory query to Claude.
 * Claude receives the DB schema + user question, returns a SQL SELECT query,
 * which the caller executes against SQLDelight.
 */
class ClaudeQueryService(private val apiKey: String) {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val systemPrompt = """
        You are an assistant for a store inventory system (ThinkingERP).
        The SQLite database has the following schema:

        items(barcode TEXT PK, name TEXT, company TEXT, mrp REAL, hsn_code TEXT, gst_rate REAL, quantity INTEGER, unit TEXT)
        purchase_bills(id TEXT PK, bill_number TEXT, bill_date INTEGER, supplier_name TEXT, subtotal REAL, cgst REAL, sgst REAL, igst REAL, total REAL)
        purchase_bill_items(id TEXT PK, bill_id TEXT, barcode TEXT, item_name TEXT, quantity INTEGER, purchase_price REAL, gst_rate REAL, line_total REAL)
        sell_invoices(id TEXT PK, invoice_number TEXT, invoice_date INTEGER, customer_name TEXT, subtotal REAL, cgst REAL, sgst REAL, igst REAL, total REAL)
        sell_invoice_items(id TEXT PK, invoice_id TEXT, barcode TEXT, item_name TEXT, quantity INTEGER, sell_price REAL, gst_rate REAL, line_total REAL)

        Rules:
        - Dates are stored as Unix timestamps (milliseconds).
        - Respond ONLY with a valid SQLite SELECT query. No explanation, no markdown, no code fences.
        - Only generate SELECT queries; never INSERT, UPDATE, DELETE, or DROP.
    """.trimIndent()

    suspend fun generateSqlQuery(userQuestion: String): Result<String> = runCatching {
        val response: ClaudeResponse = client.post("https://api.anthropic.com/v1/messages") {
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            contentType(ContentType.Application.Json)
            setBody(
                ClaudeRequest(
                    model = "claude-sonnet-4-6",
                    maxTokens = 512,
                    system = systemPrompt,
                    messages = listOf(Message(role = "user", content = userQuestion))
                )
            )
        }.body()
        response.content.first().text.trim()
    }

    fun close() = client.close()
}

@Serializable
private data class ClaudeRequest(
    val model: String,
    @SerialName("max_tokens") val maxTokens: Int,
    val system: String,
    val messages: List<Message>,
)

@Serializable
private data class Message(val role: String, val content: String)

@Serializable
private data class ClaudeResponse(val content: List<ContentBlock>)

@Serializable
private data class ContentBlock(val type: String, val text: String)
