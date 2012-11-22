import com.dzer6.ga3.ImageTool

def evercookie_png = request.cookies.find({ it.name == "evercookie_png"})?.value
    
log.info("evercookie_png = $evercookie_png")
    
// we don"t have a cookie, user probably deleted it, force cache
if (evercookie_png == null || evercookie_png == "") {
    response.status = 304
} else {
    response.setHeader("Content-Type", "image/png")
    response.setHeader("Last-Modified", "Wed, 30 Jun 2000 21:36:48 GMT")
    response.setHeader("Expires", "Tue, 31 Dec 2030 23:30:45 GMT")
    response.setHeader("Cache-Control", "private, max-age=630720000")
  
    response.contentType = "image/png"

    sout << ImageTool.getPNGBytes(evercookie_png.bytes)
    sout.flush()
}