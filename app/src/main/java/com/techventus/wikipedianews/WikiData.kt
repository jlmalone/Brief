package com.techventus.wikipedianews

/**
 * Created by josephmalone on 16-08-23.
 */
data class WikiData(var text: String, var type: DataType) {
    enum class DataType {
        HEADER, POST
    }
}
