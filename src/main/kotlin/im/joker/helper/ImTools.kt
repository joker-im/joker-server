package im.joker.helper

import java.time.LocalDateTime

/**
 * @Author: mkCen
 * @Date: 2020/12/4
 * @Time: 22:09
 * @Desc:
 */
class ImTools {

    companion object {

        fun toMill(time: LocalDateTime): Long {
            return time.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    }

}

fun main() {
    val a = ArrayList<Int>()
    a.reduceIndexed() { index, acc, r ->
        println("index:$index")
        println("r:$r")
        println("acc:$acc")
        r
    }
}