<!DOCTYPE html>
<html lang="en">
  <head>
    <% include "/WEB-INF/includes/head.gtpl" %>
    <% include "/WEB-INF/includes/outOfServiceJavascript.gtpl" %>
  </head>
  <body>
    <div id="preloader"></div>
    <% include "/WEB-INF/includes/minimalTopbar.gtpl" %>
    <div class="container">
      <div class="content">
        <div id="out-of-service-message-block">
          <h1 class="out-of-service-message"><%= applicationContext.config.MESSAGE_500 %></h1>
          <% include "/WEB-INF/includes/feedback.gtpl" %>
          <h1 class="centered-block">
            <a href="${applicationContext.config.WEB_SITE_URL}">back to main page</a>
          </h1>
        </div>
      </div>
      <% include "/WEB-INF/includes/footer.gtpl" %>
    </div>
  </body>
</html>