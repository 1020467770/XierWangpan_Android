package com.example.xierlogintest.test

import java.util.regex.Pattern


fun main() {
    val s1 = "dakowd!."
    val s2 = "dawdadz123"
    val s3 = "dawdadz."
    val pattern = "^[A-Za-z0-9]+$"
    println(Pattern.matches(pattern,s1))
    println(Pattern.matches(pattern,s2))
    println(Pattern.matches(pattern,s3))
}
