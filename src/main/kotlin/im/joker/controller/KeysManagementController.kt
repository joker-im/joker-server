package im.joker.controller

import im.joker.api.vo.key.KeyQueryRequest
import im.joker.api.vo.key.KeyQueryResponse
import im.joker.api.vo.key.KeysUploadRequest
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController
class KeysManagementController : BaseController() {


    @PostMapping("/keys/upload")
    suspend fun upload(@RequestBody req: KeysUploadRequest): Map<String, Int> {
        return mapOf("one_time_key_counts" to 0)
    }


    @PostMapping("/keys/query")
    suspend fun query(req: KeyQueryRequest): KeyQueryResponse {
        return KeyQueryResponse()
    }
}