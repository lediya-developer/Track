package com.lediya.trackingapp.data


import com.lediya.trackingapp.enums.ResultType

class ResultImp(var resultType: ResultType) {
    var message: String? = ""

    constructor(resultType: ResultType, message: String?) : this(resultType) {
        this.message = message
    }
}
