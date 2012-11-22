def evercookie_cache = request.cookies.find({ it.name == "evercookie_cache"})?.value
    
log.info("evercookie_cache = $evercookie_cache")
    
// we don"t have a cookie, user probably deleted it, force cache
if (evercookie_cache == null || evercookie_cache == "") {
    response.status = 304
} else {
    response.setHeader("Content-Type", "text/html")
    response.setHeader("Last-Modified", "Wed, 30 Jun 2000 21:36:48 GMT")
    response.setHeader("Expires", "Tue, 31 Dec 2030 23:30:45 GMT")
    response.setHeader("Cache-Control", "private, max-age=630720000")

    out << evercookie_cache
}
    
