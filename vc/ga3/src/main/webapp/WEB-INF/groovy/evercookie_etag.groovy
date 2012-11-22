import javax.servlet.http.Cookie

def evercookie_etag = request.cookies.find({ it.name == "evercookie_etag"})?.value

log.info("evercookie_etag = $evercookie_etag")

if (evercookie_etag == null || evercookie_etag == "") {
    // read our etag and pass back
    def ifNoneMatch = request.getHeader("If-None-Match") 
    log.info("ifNoneMatch = " + ifNoneMatch)
    if (ifNoneMatch != null) {
        out << ifNoneMatch
    }
} else {
    response.setHeader("Etag", evercookie_etag)
    def cookie = new Cookie("evercookie_etag", evercookie_etag)
    response.addCookie(cookie)

    out << evercookie_etag
}