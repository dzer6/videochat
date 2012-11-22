import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*
import com.dzer6.ga3.services.*
import com.dzer6.vc.ga.*

log.info("play()")

PropertyPlaceholderConfigurer config = applicationContext.config
WebToolService webToolService =  applicationContext.webToolService
UserService userService =  applicationContext.userService
FlashClientInterface flashClient =  applicationContext.flashClient

User me = webToolService.getUser(request, forward)

if (me == null) {
    return
}

log.info("play() me = $me")
  
userService.changeUser(me, [playing: true])
flashClient.cameraOn(me.id)

webToolService.renderJson(response, [playing: true])