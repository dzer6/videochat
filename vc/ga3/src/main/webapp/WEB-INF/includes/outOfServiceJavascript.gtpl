<script type="text/javascript">
\$(document).ready(function() {
  var onResize = function() {
    var width = \$("div.content").width();
    var height = \$(document).height() - 114;
    \$("#out-of-service-message-block").css("height", height);
    \$("#out-of-service-message-block").css("width", width);
  }
  
  \$(window).resize(onResize);
  
  onResize();
  
  \$("#preloader").hide();
})
/*{}*/
</script>