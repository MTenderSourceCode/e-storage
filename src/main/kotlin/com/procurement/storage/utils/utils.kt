package com.procurement.storage.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.procurement.storage.databinding.JsonDateDeserializer
import com.procurement.storage.databinding.JsonDateSerializer
import com.procurement.storage.domain.fail.Fail
import com.procurement.storage.domain.util.Result
import java.io.IOException
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.util.*

private object JsonMapper {

    val mapper: ObjectMapper = ObjectMapper()
    var dateTimeFormatter: DateTimeFormatter

    init {
        val module = SimpleModule()
        module.addSerializer(LocalDateTime::class.java, JsonDateSerializer())
        module.addDeserializer(LocalDateTime::class.java, JsonDateDeserializer())

        mapper.registerModule(module)
        mapper.registerKotlinModule()
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        mapper.configure(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS, true)

        dateTimeFormatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .appendLiteral('Z')
            .toFormatter()
    }
}

/*Date utils*/
fun String.toLocal(): LocalDateTime {
    return LocalDateTime.parse(this, JsonMapper.dateTimeFormatter)
}

fun nowUTC(): LocalDateTime = LocalDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)

fun LocalDateTime.toDate(): Date {
    return Date.from(this.toInstant(ZoneOffset.UTC))
}

fun Date.toLocal(): LocalDateTime {
    return LocalDateTime.ofInstant(this.toInstant(), ZoneOffset.UTC)
}

fun milliNowUTC(): Long {
    return nowUTC().toInstant(ZoneOffset.UTC).toEpochMilli()
}

fun <T> toObject(clazz: Class<T>, json: JsonNode): T {
    try {
        return JsonMapper.mapper.treeToValue(json, clazz)
    } catch (e: IOException) {
        throw IllegalArgumentException(e)
    }
}

fun <Any> toJson(obj: Any): String {
    try {
        return JsonMapper.mapper.writeValueAsString(obj)
    } catch (e: JsonProcessingException) {
        throw RuntimeException(e)
    }
}

fun <T : Any> JsonNode.tryToObject(target: Class<T>): Result<T, Fail.Incident.Parsing> = try {
    Result.success(JsonMapper.mapper.treeToValue(this, target))
} catch (expected: Exception) {
    Result.failure(Fail.Incident.Parsing(className = target.canonicalName, exception = expected))
}

fun <T : Any> String.tryToObject(target: Class<T>): Result<T, Fail.Incident.Parsing> = try {
    Result.success(JsonMapper.mapper.readValue(this, target))
} catch (expected: Exception) {
    Result.failure(Fail.Incident.Parsing(className = target.canonicalName, exception = expected))
}

fun String.toNode(): Result<JsonNode, Fail.Incident.Transforming> = try {
    Result.success(JsonMapper.mapper.readTree(this))
} catch (exception: JsonProcessingException) {
    Result.failure(Fail.Incident.Transforming(exception = exception))
}
