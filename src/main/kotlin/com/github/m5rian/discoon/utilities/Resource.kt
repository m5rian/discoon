package com.github.m5rian.discoon.utilities

import java.io.FileNotFoundException
import java.io.InputStream
import java.net.URL

object Resource {

    /**
     * @return Returns the content of the specified file.
     */
    fun loadString(path: String): String {
        return loadUrl(path).readText()
    }

    fun loadUrl(path: String): URL {
        return Resource::class.java.classLoader.getResource(path) ?: throw FileNotFoundException()
    }

    fun loadIs(path: String): InputStream {
        return Resource::class.java.classLoader.getResourceAsStream(path) ?: throw FileNotFoundException()
    }

}