def evercookie = request.cookies.find({ it.name == "evercookie"})?.value
    
log.info("evercookie_index = $evercookie")
    
// we don"t have a cookie, user probably deleted it, force cache
if (evercookie == null || evercookie == "") {
    response.status = 304
} else {
    response.setHeader("Content-Type", "image/png")
    response.setHeader("Last-Modified", "Wed, 30 Jun 2000 21:36:48 GMT")
    response.setHeader("Expires", "Tue, 31 Dec 2030 23:30:45 GMT")
    response.setHeader("Cache-Control", "private, max-age=630720000")

    sout << ImageTool.getPNGBytes(evercookie.bytes)
    sout.flush()
}