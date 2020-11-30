package im.joker.controller

import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping(path = ["/_matrix/client/r0"], produces = [MediaType.APPLICATION_JSON_VALUE])
@RestController
class GroupController : BaseController() {


    @GetMapping("/joined_groups")
    suspend fun joinedGroup(): String {
        return """{"groups":[]}"""
    }
}