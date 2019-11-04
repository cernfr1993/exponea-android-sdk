package com.exponea.sdk.models

import com.google.gson.annotations.SerializedName

internal data class ExponeaFetchId(
    @SerializedName("customer_ids")
    var customerIds: CustomerIds = CustomerIds(),
    var id: String? = null
)
