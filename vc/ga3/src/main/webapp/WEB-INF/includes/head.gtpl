<title>${applicationContext.config.WEB_SITE_URL}</title>
<meta charset="utf-8"></meta>
<meta name="description" content="video chat"></meta>
<meta name="author" content="${applicationContext.config.WEB_SITE_URL}"></meta>

<!-- Le HTML5 shim, for IE6-8 support of HTML elements -->
<!--[if lt IE 9]>
<script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
<![endif]-->
<% if (applicationContext.config.DEBUG_WEB_PAGES == "false") { %>
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js" type="text/javascript"></script>
<script src="http://ajax.googleapis.com/ajax/libs/jqueryui/1.8.16/jquery-ui.min.js" type="text/javascript"></script>
<% } else { %>
<script src="/js/jquery.min.1.7.1.js" type="text/javascript"></script>
<script src="/js/jquery-ui.min.1.8.16.js" type="text/javascript"></script>
<script src="/js/jquery.cookie.js" type="text/javascript"></script>
<% } %>

<script src="/js/bootstrap-alerts.js" type="text/javascript"></script>
<script src="/js/bootstrap-dropdown.js" type="text/javascript"></script>
<script src="/js/bootstrap-modal.js" type="text/javascript"></script>
<script src="/js/bootstrap-twipsy.js" type="text/javascript"></script>
<script src="/js/bootstrap-popover.js" type="text/javascript"></script>
<script src="/js/bootstrap-scrollspy.js" type="text/javascript"></script>
<script src="/js/bootstrap-tabs.js" type="text/javascript"></script>
<script src="/js/progressbar.js" type="text/javascript"></script>

<script src="/lps/includes/embed-compressed.js" type="text/javascript"></script>

<script src="/js/jquery.dateFormat-1.0.js" type="text/javascript"></script>
<script src="/js/google-code-prettify.js" type="text/javascript"></script>
<script src="/js/jquery.scrollTo-min.js" type="text/javascript"></script>
<script>\$(function () { prettyPrint() })</script>
<% if (applicationContext.config.DEBUG_WEB_PAGES == "false") { %>
<script src="/evercookie/swfobject-2.2.min.js" type="text/javascript"></script>
<script src="/evercookie/evercookie.js" type="text/javascript"></script>
<% } %>
<% include "/WEB-INF/includes/googleAnalytics.gtpl" %>
<link href="/css/bootstrap.min.css" rel="stylesheet" type="text/css"></link>
<link href="http://fonts.googleapis.com/css?family=Leckerli+One" rel="stylesheet" type="text/css"></link>
<link href="/css/google-code-prettify.css" rel="stylesheet" type="text/css"></link>
<link href="/css/jquery-ui-1.8.16.custom.css" rel="stylesheet" type="text/css"/>
<link href="/css/chat.css" rel="stylesheet" type="text/css"></link>
<link rel="shortcut icon" href="/images/ch.png" type="image/png"></link>