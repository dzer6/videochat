<div class="topbar">
  <div class="fill">
    <div class="container">
      <a class="brand" href="${applicationContext.config.WEB_SITE_URL}">${applicationContext.config.WEB_SITE_URL}</a>
      <ul class="nav">
        <li>
          <a href="#" data-controls-modal="legal-modal-from-dom" data-backdrop="true" data-keyboard="true">Legal</a>
        </li>
        <% if (applicationContext.config.DEBUG_WEB_PAGES == "false") { %>
        <li>
          <a href="#" data-controls-modal="feedback-modal-from-dom" data-backdrop="true" data-keyboard="true">Feedback</a>
        </li>
        <% } %>
        <li>
          <a href="#" data-controls-modal="about-modal-from-dom" data-backdrop="true" data-keyboard="true">About</a>
        </li>
        <li>
          <a href="#" data-controls-modal="chat-with-me-modal-from-dom" data-backdrop="true" data-keyboard="true">Chat with me</a>
        </li>
      </ul>
      <div class="pull-right">
        <div class="span">
          <% if (applicationContext.config.DEBUG_WEB_PAGES == "false") { %>
          <%   include "/WEB-INF/includes/addThis.gtpl" %>
          <% } %>
        </div>
      </div>
    </div>
  </div>
</div>
<% if (applicationContext.config.DEBUG_WEB_PAGES == "false") { %>
<%   include "/WEB-INF/includes/feedbackDialog.gtpl" %>
<% } %>
<% include "/WEB-INF/includes/about.gtpl" %>
<% include "/WEB-INF/includes/legal.gtpl" %>
<% include "/WEB-INF/includes/chatWithMe.gtpl" %>
<% include "/WEB-INF/includes/messageDialog.gtpl" %>
<% include "/WEB-INF/includes/progressBarUserBlockedDialog.gtpl" %>