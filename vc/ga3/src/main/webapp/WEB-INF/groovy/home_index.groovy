import com.dzer6.ga3.domain.*
import com.dzer6.ga3.services.*

log.info("index() ip = ${request.remoteAddr}")
log.info("index() id = ${request.id}")

def config = applicationContext.config
def sessionStorageService = applicationContext.sessionStorageService
def webToolService = applicationContext.webToolService
def rtmpService = applicationContext.rtmpService

if (rtmpService.isThereRemainsFreeRtmpServers()) {
    String sessionId = webToolService.getSessionId(request)
  
    log.info("index() sessionId = $sessionId")  
  
    if (sessionStorageService.sessionExists(sessionId)) {
        User me = webToolService.getUser(request, forward)
        if (me == null) {
            return
        }
    } else {
        sessionId = sessionStorageService.createSession()
        log.info("index() create new session, sessionId = $sessionId")
        webToolService.saveSessionIdToCookie(sessionId, response)
    } 
    	
    request.sessionId = sessionId
    request.laszloSwfTarget = config.LASZLO_SWF_TARGET
    request.quality = config.PICTURE_QUALITY
    request.bandwidth = config.STREAM_BANDWIDTH

    forward "/WEB-INF/pages/index.gtpl"
} else {
    request.message = config.THERE_IS_NO_FREE_RTMP_SERVERS
    forward "/WEB-INF/pages/outOfService.gtpl"
}