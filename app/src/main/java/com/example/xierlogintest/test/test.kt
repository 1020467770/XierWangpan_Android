package com.example.xierlogintest.test



fun main() {
    val fileName = "9d87f8e2915884211aec316553e8c2d5@IMG_20210217_140028.jpg"
    val start = fileName.lastIndexOf("@") + 1
    val name = fileName.substring(start)
    println(name)
}
