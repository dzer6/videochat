<!DOCTYPE html>
<html lang="en">
  <head>
    <% include "/WEB-INF/includes/head.gtpl" %>
    <% include "/WEB-INF/includes/userBannedJavascript.gtpl" %>
  </head>
  <body>
    <div id="preloader"></div>
    <% include "/WEB-INF/includes/minimalTopbar.gtpl" %>
    <div class="container">
      <div class="content">
        <% include "/WEB-INF/includes/banner.gtpl" %>
        <div id="user-banned-block">
          <h1 class="user-banned-message"><%= applicationContext.config.BLOCK_USER_MESSAGE %></h1>
          <div id="userBlockedProgressbarMargined">
            <div class="percent"></div>
            <div class="pbar"></div>
            <div class="elapsed"></div>
          </div>
        </div>
      </div>
      <% include "/WEB-INF/includes/footer.gtpl" %>
    </div>
  </body>
</html>