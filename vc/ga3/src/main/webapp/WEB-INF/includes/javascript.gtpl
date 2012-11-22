<script type="text/javascript">
  com = {};
  com.dzer6 = {};
  com.dzer6.vc = {};
  com.dzer6.vc.bayeux = {};
  com.dzer6.vc.bayeux.subscribeInfo = {};
    
  \$(document).ready(function() {

    <% if (applicationContext.config.DEBUG_WEB_PAGES == "false") { %>
    var evercookieInstance = new evercookie();
    <% } else { %>
    var evercookieInstance = {};
    evercookieInstance.get = function(cookieName, callback) {
      callback(\$.cookie(cookieName));
    };
    evercookieInstance.set = function(cookieName, value) {
      \$.cookie(cookieName, value);
    }
    <% } %>
    
    var playUrl = "/home/play";
    var stopUrl = "/home/stop";

    var playPauseButtonStateChanged = function(data) {
      if (data.error) {
        showMessageDialog("Alert", data.message);
      }
            
      \$("#playPauseButton").attr("playing", data.playing);
      \$("#playPauseButton").html(data.playing ? "Pause" : "Play");
    };
    
    var showProgressbarDialog = function(title, message) {
      \$("#modal-progressbar-dialog-text").html(message);
      \$("#modal-progressbar-dialog-title").html(title);
      \$("#modal-progressbar-dialog").modal('show');
    };
    
    var showMessageDialog = function(title, message) {
      \$("#modal-message-dialog-text").html(message);
      \$("#modal-message-dialog-title").html(title);
      \$("#modal-message-dialog").modal('show');
    };
    
    var nextUserSelected = function(data) {
      if (data.error) {
        showMessageDialog("Alert", data.message);
      }
      
      playPauseButtonStateChanged(data);
    };

    \$("#playPauseButton").click(function() {
      \$.post(\$("#playPauseButton").attr("playing") == "true" ? stopUrl : playUrl, playPauseButtonStateChanged);
    });
    
    \$("#nextButton").click(function() {
      \$.post("/home/next", nextUserSelected);
    });

    \$("#blockButton").click(function() {
      \$.post("/home/block");
    });
    
    var chatMessageEntered = function() {
      \$.post("/home/smtc", { message: \$("#chatEditor").val() });
      \$("#chatEditor").val("");
      \$("#chatEditor").focus();
    };
    
    \$("#chatEditor").keydown(function(e) {
      if (e.keyCode == 13 && e.ctrlKey) {
        chatMessageEntered();
      }
    });
    
    \$("#sendMessageButton").click(chatMessageEntered);
    
    var changeUserSelectionParameter = function(usp, value) {
      \$.post("/home/cusp", {usp: usp, value: value});
    };

    var myLifePeriodValue;
    var mySexSelectValue;
    var opponentLifePeriodValue;
    var opponentSexSelectValue;
    
    \$(".my-block .lifePeriodSelect").click(function(e) {
      if (myLifePeriodValue != e.target.value) {
        changeUserSelectionParameter("${applicationContext.config.MY_LIFE_PERIOD_PARAM}", e.target.value);
      }
      myLifePeriodValue = e.target.value;
    });

    \$(".my-block .sexSelect").click(function(e) {
      if (mySexSelectValue != e.target.value) {
        changeUserSelectionParameter("${applicationContext.config.MY_SEX_PARAM}", e.target.value);
      }
      mySexSelectValue = e.target.value;
    });

    \$(".opponent-block .lifePeriodSelect").click(function(e) {
      if (opponentLifePeriodValue != e.target.value) {
        changeUserSelectionParameter("${applicationContext.config.OPPONENT_LIFE_PERIOD_PARAM}", e.target.value);
      }
      opponentLifePeriodValue = e.target.value;
    });

    \$(".opponent-block .sexSelect").click(function(e) {
      if (opponentSexSelectValue != e.target.value) {
        changeUserSelectionParameter("${applicationContext.config.OPPONENT_SEX_PARAM}", e.target.value);
      }
      opponentSexSelectValue = e.target.value;
    });

    \$(".my-block .sexSelect").msDropDown();
    \$(".opponent-block .sexSelect").msDropDown();
    \$(".my-block .lifePeriodSelect").msDropDown();
    \$(".opponent-block .lifePeriodSelect").msDropDown();
    
    var bayeuxHandlerIdCounter = 0;
    var getBayeuxHandlerId = function() {
      bayeuxHandlerIdCounter+=1;
      return "bh" + bayeuxHandlerIdCounter;
    }
    
    com.dzer6.vc.bayeux.subscribe = function(channel, handler) {
      if (true == com.dzer6.vc.bayeux.isInit) {
        var id = getBayeuxHandlerId();
        com.dzer6.vc.bayeux.subscribeInfo[id] = {};
        com.dzer6.vc.bayeux.subscribeInfo[id].channel = channel;
        com.dzer6.vc.bayeux.subscribeInfo[id].handler = function (responseObject) {
          responseObject = responseObject.replace(/%22/g, "\"").
                                          replace(/%5c/g, "\\\\").
                                          replace(/%26/g, "&").
                                          replace(/%25/g, "%");
          handler(\$.parseJSON(responseObject));
        }

        console.log("com.dzer6.vc.bayeux.subscribe do it!");
        document.getElementById("bayeuxOpenlaszloApplication").subscribe(channel, "com.dzer6.vc.bayeux.subscribeInfo." + id + ".handler");
      } else {
        console.log("com.dzer6.vc.bayeux.subscribe waiting...");
        setTimeout(function() {
          com.dzer6.vc.bayeux.subscribe(channel, handler);
        }, 300);
      }

      return "success";
    };
    
    var createBayeux = function(rtmpServerUrl) {
      lz.embed.swf({ url: "lzx/bayeux-on-flash.${request.laszloSwfTarget}.swf?&lzproxied=false&rtmpServerUrl=" + rtmpServerUrl, 
                     appenddivid: "bayeuxOpenlaszloApplicationDiv",
                     id: "bayeuxOpenlaszloApplication",
                     allowfullscreen: false,
                     bgcolor: "#000000", 
                     width: 1, 
                     height: 1, 
                     accessible: false, 
                     cancelmousewheel: false
      });

      lz.embed.bayeuxOpenlaszloApplication.onload = function() {
        com.dzer6.vc.bayeux.isInit = true;
        console.log("com.dzer6.vc.bayeux.subscribe initialized!");
      };
    };
    
    var onBlock = function(data) {
      if (data.counting) {
        \$('#userBlockedProgressbar').anim_progressbar({start: new Date().setTime(new Date().getTime()), 
                                                       finish: new Date().setTime(new Date().getTime() + data.counting), 
                                                       interval: 100});
      }
      
      showProgressbarDialog("Alert", data.message);
      
      playPauseButtonStateChanged(data);
    };
    
    var onTurnOnBlocking = function() {
      \$("#blockButton").attr("disabled", false);
    };
    
    var onTurnOffBlocking = function() {
      \$("#blockButton").attr("disabled", true);
    };
    
    var onTurnOnChat = function() {
      \$("#chatViewer").attr("disabled", false);
      \$("#chatEditor").attr("disabled", false);
      \$("#sendMessageButton").attr("disabled", false);
    };
        
    var onTurnOffChat = function() {
      \$("#chatViewer").attr("disabled", true);
      \$("#chatEditor").attr("disabled", true);
      \$("#sendMessageButton").attr("disabled", true);
    };
    
    var onClearChat = function() {
      \$("#chatViewer").attr("innerHTML", "");
    };
      
    var onChatMessage = function(data) {
      if (data.error) {
        //window.location.href = "/home/index";
        return;
      }
          
      var date = \$.format.date(new Date(), "dd/MM/yyyy hh:mm:ss");
      var color = data.isMe ? "red" : "blue";
      \$("#chatViewer").html(\$("#chatViewer").html() + 
                            "<p style='color: " + color + ";'><b>" + date + "</b>: " + data.message + "</p>");
      
                  
      var chatViewerChildren = \$("#chatViewer").children();
      var lastElement = chatViewerChildren[chatViewerChildren.length - 1];
      \$("#chatViewer").scrollTo(lastElement);
    };
            
    var createChatInputScreen = function(uid, rtmpServerUrl) {
      var swfUrl = "lzx/in3.${request.laszloSwfTarget}.swf?&watcherId=" + uid +  
                                                  "&rtmpServerUrl=" + rtmpServerUrl;
	    
      lz.embed.swf({ url: swfUrl, 
                     appenddivid: "chatInputScreenPlace",
                     allowfullscreen: false,
                     bgcolor: "#FFFFFF", 
                     width: 1, 
                     height: 1, 
                     id: "in_lzapp", 
                     accessible: false, 
                     cancelmousewheel: false,
                     wmode: "opaque"
      });
	    	
      lz.embed.in_lzapp.onloadstatus = function(count) {
        if (count == 100 || \$.browser.msie) {
          if (\$("#lzSplashIn") == null) {
            return;
          }
          if (\$("#lzSplashIn")) \$("#lzSplashIn").remove();
          
          \$("#chatInputScreenPlace").addClass("thumbnail video");
          
          \$("#chatInputScreenPlace").css("width", "320px");
          \$("#chatInputScreenPlace").css("height", "240px");
          
          \$("#in_lzapp").attr("width", "320");
          \$("#in_lzapp").attr("height", "240");
        }
      }
    };

    var createChatOutputScreen = function(uid, rtmpServerUrl, playing, sessionId) {
      var swfUrl = "lzx/out3.${request.laszloSwfTarget}.swf?&cameraId=" + uid +
                                                   "&sessionId=" + sessionId +
                                                   "&rtmpServerUrl=" + rtmpServerUrl + 
                                                   "&isCameraOn=" + playing +
                                                   "&quality=${request.quality}" + 
                                                   "&bandwidth=${request.bandwidth}";

      lz.embed.swf({ url: swfUrl,
                     appenddivid: "chatOutputScreenPlace",
                     allowfullscreen: false, 
                     bgcolor: "#FFFFFF", 
                     width: 1,
                     height: 1, 
                     id: "out_lzapp", 
                     accessible: false, 
                     cancelmousewheel: false,
                     wmode: "opaque"
      });

      lz.embed.out_lzapp.onloadstatus = function(count) {
        if (count == 100 || \$.browser.msie) {
          if (\$("#lzSplashOut") == null) {
            return;
          }
          if (\$("#lzSplashOut")) \$("#lzSplashOut").remove();
          
          \$("#chatOutputScreenPlace").addClass("thumbnail video");
          
          \$("#chatOutputScreenPlace").css("width", "320px");
          \$("#chatOutputScreenPlace").css("height", "240px");
          
          \$("#out_lzapp").attr("width", "320");
          \$("#out_lzapp").attr("height", "240");
        }
      }
    };

    var initializeUserParametersUI = function(data) {
      myLifePeriodValue = data.myLifePeriod;
      mySexSelectValue = data.mySexType;
      opponentLifePeriodValue = data.opponentLifePeriod;
      opponentSexSelectValue = data.opponentSexType;
      \$("div.myLifePeriodDiv input[value=" + data.myLifePeriod + "]").attr("checked", true);
      \$("div.mySexDiv input[value=" + data.mySexType + "]").attr("checked", true);
      \$("div.opponentLifePeriodDiv input[value=" + data.opponentLifePeriod + "]").attr("checked", true);
      \$("div.opponentSexDiv input[value=" + data.opponentSexType + "]").attr("checked", true); 
      if (data.conversationInProcess) {
    	  onTurnOnChat();
          onTurnOnBlocking();
      } else {
    	  onTurnOffChat();
          onTurnOffBlocking();
      }     
    };
        
    var initializeUI = function(data) {
      if (data.ecuid) {
        try {
          evercookieInstance.set("ecuid", data.ecuid);

          com.dzer6.vc.bayeux.subscribe(data.ecuid + "${applicationContext.config.PUSH_CHANNEL_CHAT_MESSAGE}", onChatMessage);
          com.dzer6.vc.bayeux.subscribe(data.ecuid + "${applicationContext.config.PUSH_CHANNEL_TURN_ON_CHAT}", onTurnOnChat);
          com.dzer6.vc.bayeux.subscribe(data.ecuid + "${applicationContext.config.PUSH_CHANNEL_TURN_OFF_CHAT}", onTurnOffChat);
          com.dzer6.vc.bayeux.subscribe(data.ecuid + "${applicationContext.config.PUSH_CHANNEL_CLEAR_CHAT}", onClearChat);
          com.dzer6.vc.bayeux.subscribe(data.ecuid + "${applicationContext.config.PUSH_CHANNEL_BLOCK}", onBlock);
          com.dzer6.vc.bayeux.subscribe(data.ecuid + "${applicationContext.config.PUSH_CHANNEL_TURN_ON_BLOCKING}", onTurnOnBlocking);
          com.dzer6.vc.bayeux.subscribe(data.ecuid + "${applicationContext.config.PUSH_CHANNEL_TURN_OFF_BLOCKING}", onTurnOffBlocking);
          
          createBayeux(data.rtmpServerUrl + "${applicationContext.config.BAYEUX_APPLICATION_NAME_POSTFIX}");
          createChatInputScreen(data.ecuid, data.rtmpServerUrl + "${applicationContext.config.R5WA_APPLICATION_NAME_POSTFIX}");
          createChatOutputScreen(data.ecuid, data.rtmpServerUrl + "${applicationContext.config.R5WA_APPLICATION_NAME_POSTFIX}", data.playing, data.sessionId);
          
          playPauseButtonStateChanged(data);

          \$("#chatWithMeInput").val(data.chatWithMeURL);

          initializeUserParametersUI(data);
          \$("div.hiddenBlock").removeClass("hiddenBlock");
        } catch(e) {
          console.log(e);
          \$("#preloader").hide();
        }
        
        \$("#preloader").hide();
      }
    };
    	  		
    evercookieInstance.get("ecuid", function(value) {
      \$.post("/home/ec", {ecuid: value, id: "${request.id}"}, initializeUI);
    });
  });
  /*{}*/
</script>
