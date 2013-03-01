import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*
import com.dzer6.ga3.services.*

WebToolService webToolService =  applicationContext.webToolService

User me = webToolService.getUser(request)

log.info("bannedtill() me = $me")

webToolService.renderJson(response, [bannedTillDelta: me.bannedTill.time - new Date().time])