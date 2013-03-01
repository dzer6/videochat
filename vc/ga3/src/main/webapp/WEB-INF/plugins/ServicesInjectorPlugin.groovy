import com.dzer6.ga3.*

binding {
    applicationContext = ApplicationContextWrapper.getInstance()
}

handleException {
    def servletExceptionHandler = ApplicationContextWrapper.getInstance().servletExceptionHandler
    log.info("Invoke servlet exception handler = $servletExceptionHandler")
    servletExceptionHandler.handle(e, request, response)
}