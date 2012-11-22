<script type="text/javascript">
\$(document).ready(function() {
  var onResize = function() {
    var width = \$("div.content").width();
    var height = \$(document).height() - 114;
    \$("#user-banned-block").css("height", height);
    \$("#user-banned-block").css("width", width);
  }
  
  \$(window).resize(onResize);
  
  onResize();

  \$.post("/home/bannedtill", function(data) {
    \$('#userBlockedProgressbarMargined').anim_progressbar({start: new Date().setTime(new Date().getTime()),
                                                             finish: new Date().setTime(new Date().getTime() + data.bannedTillDelta),
                                                             interval: 100});

    \$("#preloader").hide();
  });
  

})
/*{}*/
</script>