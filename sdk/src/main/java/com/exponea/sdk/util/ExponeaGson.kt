package com.exponea.sdk.util

import com.exponea.sdk.models.CustomerRecommendation
import com.exponea.sdk.models.CustomerRecommendationDeserializer
import com.exponea.sdk.models.eventfilter.EventFilterAttribute
import com.exponea.sdk.models.eventfilter.EventFilterConstraint
import com.exponea.sdk.models.eventfilter.EventFilterOperator
import com.exponea.sdk.models.eventfilter.EventFilterOperatorDeserializer
import com.exponea.sdk.models.eventfilter.EventFilterOperatorSerializer
import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken

public class ExponeaGson {
    companion object {
        public val instance = GsonBuilder()
            // NaN and Infinity are serialized as strings.
            // Gson cannot serialize them, it can be setup to do it,
            // but then Exponea servers fail to process the JSON afterwards.
            // This way devs know there is something going on and find the issue
            .registerTypeAdapter(object : TypeToken<Double>() {}.type, JsonSerializer<Double> { src, _, _ ->
                if (src.isInfinite() || src.isNaN()) {
                    JsonPrimitive(src.toString())
                } else {
                    JsonPrimitive(src)
                }
            })
            .registerTypeAdapter(object : TypeToken<Float>() {}.type, JsonSerializer<Float> { src, _, _ ->
                if (src.isInfinite() || src.isNaN()) {
                    JsonPrimitive(src.toString())
                } else {
                    JsonPrimitive(src)
                }
            })
            // customer recommendation
            .registerTypeAdapter(CustomerRecommendation::class.java, CustomerRecommendationDeserializer())
            // event filter
            .registerTypeHierarchyAdapter(EventFilterOperator::class.java, EventFilterOperatorSerializer())
            .registerTypeHierarchyAdapter(EventFilterOperator::class.java, EventFilterOperatorDeserializer())
            .registerTypeAdapterFactory(EventFilterAttribute.typeAdapterFactory)
            .registerTypeAdapterFactory(EventFilterConstraint.typeAdapterFactory)
            .create()
    }
}
