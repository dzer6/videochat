<div class="topbar">
  <div class="fill">
    <div class="container">
      <a class="brand" href="${applicationContext.config.WEB_SITE_URL}">${applicationContext.config.WEB_SITE_URL}</a>
      <ul class="nav">
        <li>
          <a href="#" data-controls-modal="about-modal-from-dom" data-backdrop="true" data-keyboard="true">About</a>
        </li>
        <li>
          <a href="#" data-controls-modal="chat-with-me-modal-from-dom" data-backdrop="true" data-keyboard="true">Chat with me</a>
        </li>
      </ul>
    </div>
  </div>
</div>
<% include "/WEB-INF/includes/about.gtpl" %>
<% include "/WEB-INF/includes/chatWithMe.gtpl" %>
<% include "/WEB-INF/includes/messageDialog.gtpl" %>
<% include "/WEB-INF/includes/progressBarUserBlockedDialog.gtpl" %>