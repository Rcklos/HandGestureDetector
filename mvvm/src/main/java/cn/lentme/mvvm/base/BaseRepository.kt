package cn.lentme.mvvm.base

open class BaseRepository {

    private companion object {
        const val TAG = "BaseRepository"
    }

//    suspend fun <T: Any> request(call: suspend () -> ResponseData<T>): ResponseData<T> {
//        return withContext(Dispatchers.IO) {
//            call.invoke()
//        }.apply {
//            Log.e(_tag, "接口返回数据-----------> $this")
//            when(errCode) {
//                0, 200 -> this
//                else -> "err: $errCode"
//            }
//        }
//    }
}