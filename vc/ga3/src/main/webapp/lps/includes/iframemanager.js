var $runtime="dhtml";var $dhtml=true;var $as3=false;var $as2=false;var $swf10=false;var $swf11=false;var $j2me=false;var $debug=false;var $js1=true;var $backtrace=false;var $swf7=false;var $swf9=false;var $svg=false;var $swf8=false;var $mobile=false;var $profile=false;pmrpc=self.pmrpc=(function(){
var generateUUID;var convertWildcardToRegex;var checkACL;var invokeProcedure;var encode;var decode;var createJSONRpcBaseObject;var createJSONRpcRequestObject;var createJSONRpcErrorObject;var createJSONRpcResponseObject;var register;var unregister;var fetchRegisteredService;var processPmrpcMessage;var processJSONRpcRequest;var processJSONRpcResponse;var call;var sendPmrpcMessage;var waitAndSendRequest;var addCrossBrowserEventListerner;var createHandler;var $2;var $3;var $4;var findAllWindows;var findAllWorkers;var findAllReachableContexts;var $5;var $6;generateUUID=function(){
var $0=[],$1="89AB",$2="0123456789ABCDEF";for(var $3=0;$3<36;$3++){
$0[$3]=$2[Math.floor(Math.random()*16)]
};$0[14]="4";$0[19]=$1[Math.floor(Math.random()*4)];$0[8]=$0[13]=$0[18]=$0[23]="-";return $0.join("")
};convertWildcardToRegex=function($0){
var $1=$0.replace(__wildcardRegexp,"\\$1");$1="^"+$1.replace(__starRegexp,".*")+"$";return $1
};checkACL=function($0,$1){
var $2=$0.whitelist;var $3=$0.blacklist;var $4=false;var $5=false;for(var $6=0;$6<$2.length;++$6){
var $7=convertWildcardToRegex($2[$6]);if($1.match($7)){
$4=true;break
}};for(var $8=0;$6<$3.length;++$8){
var $7=convertWildcardToRegex($3[$8]);if($1.match($7)){
$5=true;break
}};return $4&&!$5
};invokeProcedure=function($0,$1,$2,$3){
if(!($2 instanceof Array)){
var $4=$0.toString();var $5=$4.substring($4.indexOf("(")+1,$4.indexOf(")"));$5=$5===""?[]:$5.split(", ");var $6={};for(var $7=0;$7<$5.length;$7++){
$6[$5[$7]]=$7
};var $8=[];for(var $9 in $2){
if(typeof $6[$9]!=="undefined"){
$8[$6[$9]]=$2[$9]
}else{
throw "No such param!"
}};$2=$8
};if(typeof $3!=="undefined"){
$2=$2.concat($3)
};return $0.apply($1,$2)
};encode=function($0){
return "pmrpc."+JSON.stringify($0)
};decode=function($0){
return JSON.parse($0.substring("pmrpc.".length))
};createJSONRpcBaseObject=function(){
var $0={};$0.jsonrpc="2.0";return $0
};createJSONRpcRequestObject=function($0,$1,$2){
var $3=createJSONRpcBaseObject();$3.method=$0;$3.params=$1;if(typeof $2!=="undefined"){
$3.id=$2
};return $3
};createJSONRpcErrorObject=function($0,$1,$2){
var $3={};$3.code=$0;$3.message=$1;$3.data=$2;return $3
};createJSONRpcResponseObject=function($0,$1,$2){
var $3=createJSONRpcBaseObject();$3.id=$2;if(typeof $0==="undefined"||$0===null){
$3.result=$1==="undefined"?null:$1
}else{
$3.error=$0
};return $3
};register=function($0){
if($0.publicProcedureName in reservedProcedureNames){
return false
}else{
registeredServices[$0.publicProcedureName]={"publicProcedureName":$0.publicProcedureName,"procedure":$0.procedure,"context":$0.procedure.context,"isAsync":typeof $0.isAsynchronous!=="undefined"?$0.isAsynchronous:false,"acl":typeof $0.acl!=="undefined"?$0.acl:{whitelist:["*"],blacklist:[]}};return true
}};unregister=function($0){
if($0 in reservedProcedureNames){
return false
}else{
delete registeredServices[$0];return true
}};fetchRegisteredService=function($0){
return registeredServices[$0]
};processPmrpcMessage=function($0){
var $1=$0.event;var $2=$0.source;var $3=typeof $2!=="undefined"&&$2!==null;if($1.data.indexOf("pmrpc.")!==0){
return
}else{
var $4=decode($1.data);if(typeof $4.method!=="undefined"){
var $5={data:$1.data,source:$3?$2:$1.source,origin:$3?"*":$1.origin,shouldCheckACL:!$3};response=processJSONRpcRequest($4,$5);if(response!==null){
sendPmrpcMessage($5.source,response,$5.origin)
}}else{
processJSONRpcResponse($4)
}}};processJSONRpcRequest=function($0,serviceCallEvent,$1){
if($0.jsonrpc!=="2.0"){
return createJSONRpcResponseObject(createJSONRpcErrorObject(-32600,"Invalid request.","The recived JSON is not a valid JSON-RPC 2.0 request."),null,null)
};var id=$0.id;var $2=fetchRegisteredService($0.method);if(typeof $2!=="undefined"){
if(!serviceCallEvent.shouldCheckACL||checkACL($2.acl,serviceCallEvent.origin)){
try{
if($2.isAsync){
var $3=function($0){
sendPmrpcMessage(serviceCallEvent.source,createJSONRpcResponseObject(null,$0,id),serviceCallEvent.origin)
};invokeProcedure($2.procedure,$2.context,$0.params,[$3,serviceCallEvent]);return null
}else{
var $4=invokeProcedure($2.procedure,$2.context,$0.params,[serviceCallEvent]);return typeof id==="undefined"?null:createJSONRpcResponseObject(null,$4,id)
}}
catch($5){
if(typeof id==="undefined"){
return null
};if($5==="No such param!"){
return createJSONRpcResponseObject(createJSONRpcErrorObject(-32602,"Invalid params.",$5.description),null,id)
};return createJSONRpcResponseObject(createJSONRpcErrorObject(-1,"Application error.",$5.description),null,id)
}}else{
return typeof id==="undefined"?null:createJSONRpcResponseObject(createJSONRpcErrorObject(-2,"Application error.","Access denied on server."),null,id)
}}else{
return typeof id==="undefined"?null:createJSONRpcResponseObject(createJSONRpcErrorObject(-32601,"Method not found.","The requestd remote procedure does not exist or is not available."),null,id)
}};processJSONRpcResponse=function($0){
var $1=$0.id;var $2=callQueue[$1];if(typeof $2==="undefined"||$2===null){
return
}else{
delete callQueue[$1]
};if(typeof $0.error==="undefined"){
$2.onSuccess({"destination":$2.destination,"publicProcedureName":$2.publicProcedureName,"params":$2.params,"status":"success","returnValue":$0.result})
}else{
$2.onError({"destination":$2.destination,"publicProcedureName":$2.publicProcedureName,"params":$2.params,"status":"error","description":$0.error.message+" "+$0.error.data})
}};call=function($0){
if($0.retries&&$0.retries<0){
throw new Exception("number of retries must be 0 or higher")
};var $1=[];if(typeof $0.destination==="undefined"||$0.destination===null||$0.destination==="workerParent"){
$1=[{context:null,type:"workerParent"}]
}else if($0.destination==="publish"){
$1=findAllReachableContexts()
}else if($0.destination instanceof Array){
for(var $2=0;$2<$0.destination.length;$2++){
if($0.destination[$2]==="workerParent"){
$1.push({context:null,type:"workerParent"})
}else if(typeof $0.destination[$2].frames!=="undefined"){
$1.push({context:$0.destination[$2],type:"window"})
}else{
$1.push({context:$0.destination[$2],type:"worker"})
}}}else{
if(typeof $0.destination.frames!=="undefined"){
$1.push({context:$0.destination,type:"window"})
}else{
$1.push({context:$0.destination,type:"worker"})
}};for(var $2=0;$2<$1.length;$2++){
var $3={destination:$1[$2].context,destinationDomain:typeof $0.destinationDomain==="undefined"?["*"]:(typeof $0.destinationDomain==="string"?[$0.destinationDomain]:$0.destinationDomain),publicProcedureName:$0.publicProcedureName,onSuccess:typeof $0.onSuccess!=="undefined"?$0.onSuccess:(function(){}),onError:typeof $0.onError!=="undefined"?$0.onError:(function(){}),retries:typeof $0.retries!=="undefined"?$0.retries:5,timeout:typeof $0.timeout!=="undefined"?$0.timeout:500,status:"requestNotSent"};isNotification=typeof $0.onError==="undefined"&&typeof $0.onSuccess==="undefined";params=typeof $0.params!=="undefined"?$0.params:[];callId=generateUUID();callQueue[callId]=$3;if(isNotification){
$3.message=createJSONRpcRequestObject($0.publicProcedureName,params)
}else{
$3.message=createJSONRpcRequestObject($0.publicProcedureName,params,callId)
};waitAndSendRequest(callId)
}};sendPmrpcMessage=function($0,$1,$2){
if(typeof $0==="undefined"||$0===null){
self.postMessage(encode($1))
}else if(typeof $0.frames!=="undefined"){
return $0.postMessage(encode($1),$2)
}else if($0&&$0.postMessage){
$0.postMessage(encode($1))
}};waitAndSendRequest=function(callId){
var $0=callQueue[callId];if(typeof $0==="undefined"){
return
}else if($0.retries<=-1){
processJSONRpcResponse(createJSONRpcResponseObject(createJSONRpcErrorObject(-4,"Application error.","Destination unavailable."),null,callId))
}else if($0.status==="requestSent"){
return
}else if($0.retries===0||$0.status==="available"){
$0.status="requestSent";$0.retries=-1;callQueue[callId]=$0;for(var $1=0;$1<$0.destinationDomain.length;$1++){
sendPmrpcMessage($0.destination,$0.message,$0.destinationDomain[$1],$0);self.setTimeout(function(){
waitAndSendRequest(callId)
},$0.timeout)
}}else{
$0.status="pinging";$0.retries=$0.retries-1;call({"destination":$0.destination,"publicProcedureName":"receivePingRequest","onSuccess":function($0){
if($0.returnValue===true&&typeof callQueue[callId]!=="undefined"){
callQueue[callId].status="available";waitAndSendRequest(callId)
}},"params":[$0.publicProcedureName],"retries":0,"destinationDomain":$0.destinationDomain});callQueue[callId]=$0;self.setTimeout(function(){
waitAndSendRequest(callId)
},$0.timeout/$0.retries)
}};addCrossBrowserEventListerner=function($0,$1,$2,$3){
if("addEventListener" in $0){
$0.addEventListener($1,$2,$3)
}else{
$0.attachEvent("on"+$1,$2)
}};createHandler=function(method,source,destinationType){
return( function($0){
var $1={event:$0,source:source,destinationType:destinationType};method($1)
})
};$2=function($0){
return typeof fetchRegisteredService($0)!=="undefined"
};findAllWindows=function(){
var $0=[];if(typeof window!=="undefined"){
$0.push({context:window.top,type:"window"});for(var $1=0;typeof $0[$1]!=="undefined";$1++){
var $2=$0[$1];for(var $3=0;$3<$2.context.frames.length;$3++){
$0.push({context:$2.context.frames[$3],type:"window"})
}}}else{
$0.push({context:this,type:"workerParent"})
};return $0
};findAllWorkers=function(){
return allWorkers
};findAllReachableContexts=function(){
var $0=findAllWindows();var $1=findAllWorkers();var $2=$0.concat($1);return $2
};$5=function(){
var $0=[];var $1=typeof this.frames!=="undefined"?window.location.protocol+"//"+window.location.host+(window.location.port!==""?":"+window.location.port:""):"";for(publicProcedureName in registeredServices){
if(publicProcedureName in reservedProcedureNames){
continue
}else{
$0.push({"publicProcedureName":registeredServices[publicProcedureName].publicProcedureName,"acl":registeredServices[publicProcedureName].acl,"origin":$1})
}};return $0
};$6=function(params){
var addToDiscoveredMethods;addToDiscoveredMethods=function($0,$1){
for(var $2=0;$2<$0.length;$2++){
if($0[$2].origin.match(originRegex)&&$0[$2].publicProcedureName.match(nameRegex)){
discoveredMethods.push({publicProcedureName:$0[$2].publicProcedureName,destination:$1,procedureACL:$0[$2].acl,destinationOrigin:$0[$2].origin})
}}};var $0=null;if(typeof params.destination==="undefined"){
$0=findAllReachableContexts();for(var $1=0;$1<$0.length;$1++){
$0[$1]=$0[$1].context
}}else{
$0=params.destination
};var originRegex=typeof params.origin==="undefined"?".*":params.origin;var nameRegex=typeof params.publicProcedureName==="undefined"?".*":params.publicProcedureName;var counter=$0.length;var discoveredMethods=[];pmrpc.call({destination:$0,destinationDomain:"*",publicProcedureName:"getRegisteredProcedures",onSuccess:function($0){
counter--;addToDiscoveredMethods($0.returnValue,$0.destination);if(counter===0){
params.callback(discoveredMethods)
}},onError:function($0){
counter--;if(counter===0){
params.callback(discoveredMethods)
}}})
};if(typeof JSON==="undefined"||typeof JSON.stringify==="undefined"||typeof JSON.parse==="undefined"){
window.console&&console.error&&console.error("pmrpc requires the JSON library")
};if(typeof this.postMessage==="undefined"&&typeof this.onconnect==="undefined"){
window.console&&console.error&&console.error("pmrpc requires the HTML5 cross-document messaging and worker APIs")
};var __wildcardRegexp=new RegExp("([\\^\\$\\.\\+\\?\\=\\!\\:\\|\\\\/\\(\\)\\[\\]\\{\\}])","g");var __starRegexp=new RegExp("\\*","g");var registeredServices={};var callQueue={};var reservedProcedureNames={};if("window" in this){
var $0=createHandler(processPmrpcMessage,null,"window");addCrossBrowserEventListerner(this,"message",$0,false)
}else if("onmessage" in this){
var $0=createHandler(processPmrpcMessage,this,"worker");addCrossBrowserEventListerner(this,"message",$0,false)
}else if("onconnect" in this){
var $1=function($0){
var $1=createHandler(processPmrpcMessage,$0.ports[0],"sharedWorker");addCrossBrowserEventListerner($0.ports[0],"message",$1,false);$0.ports[0].start()
};addCrossBrowserEventListerner(this,"connect",$1,false)
}else{
throw "Pmrpc must be loaded within a browser window or web worker."
};var createDedicatedWorker=this.Worker;this.nonPmrpcWorker=createDedicatedWorker;var createSharedWorker=this.SharedWorker;this.nonPmrpcSharedWorker=createSharedWorker;var allWorkers=[];this.Worker=function($0){
var $1=new createDedicatedWorker($0);allWorkers.push({context:$1,type:"worker"});var $2=createHandler(processPmrpcMessage,$1,"worker");addCrossBrowserEventListerner($1,"message",$2,false);return $1
};this.SharedWorker=function($0,$1){
var newWorker=new createSharedWorker($0,$1);allWorkers.push({context:newWorker,type:"sharedWorker"});var $2=createHandler(processPmrpcMessage,newWorker.port,"sharedWorker");addCrossBrowserEventListerner(newWorker.port,"message",$2,false);newWorker.postMessage=function($0,$1){
return newWorker.port.postMessage($0,$1)
};newWorker.port.start();return newWorker
};register({"publicProcedureName":"receivePingRequest","procedure":$2});register({"publicProcedureName":"getRegisteredProcedures","procedure":$5});reservedProcedureNames={"getRegisteredProcedures":null,"receivePingRequest":null};return {register:register,unregister:unregister,call:call,discover:$6}})();lz.embed.iframemanager={__counter:0,__frames:{},__ownerbyid:{},__loading:{},__callqueue:{},__calljsqueue:{},__sendmouseevents:{},__hidenativecontextmenu:{},__selectionbookmarks:{},__browser:"",FOCUS_KEYBOARD_PREV:-2,FOCUS_KEYBOARD:-1,FOCUS_MANUAL:0,FOCUS_MOUSE:1,__srcbyid:{},create:function($0,$1,$2,$3,$4,$5,$6,$7){
var $8=lz.embed,$9="__lz"+$8.iframemanager.__counter++;if(typeof $0=="string"){
$8.iframemanager.__ownerbyid[$9]=$0
};var $a='javascript:""';var $b='lz.embed.iframemanager.__gotload("'+$9+'")';if($1==null||$1=="null"||$1=="")$1=$9;if($5==null||$5=="undefined"){
$5=document.body
};var $c=null;if(typeof $0=="string"){
if(!document.getElementById("__post__Trap1")){
this.__createPostTrap("__post__",$5)
};$c=document.getElementById("__post__Trap1")
};if(document.all){
var $d="<iframe name='"+$1+"' id='"+$9+"' src='"+$a+"' onload='"+$b+"' frameBorder='0' tabindex='0'";if($2!=true)$d+=" scrolling='no'";$d+="></iframe>";var $e=document.createElement("div");$e.setAttribute("id",$9+"Container");if($c){
$5.insertBefore($e,$c)
}else{
$5.appendChild($e)
};$e.style.display="none";if($2!=true){
$e.style.overflow="hidden"
};if($3){
$e.style.width=$3
};if($4){
$e.style.height=$4
};$e.innerHTML=$d;var $f=document.getElementById($9);if(typeof $0=="string"){
this.__createPreTrap($9,$e,$f);this.__createPostTrap($9,$e)
}}else{
var $e=document.createElement("div");$e.setAttribute("id",$9+"Container");$e.style.display="none";if($2!=true){
$e.style.overflow="hidden"
};$e.style.top="0px";$e.style.left="0px";$e.style.padding="0px";$e.style.margin="0px";if($3){
$e.style.width=$3
};if($4){
$e.style.height=$4
};if($c){
$5.insertBefore($e,$c)
}else $5.appendChild($e);var $f=document.createElement("iframe");$f.setAttribute("name",$1);$f.setAttribute("src",$a);$f.setAttribute("id",$9);$f.setAttribute("onload",$b);$f.setAttribute("tabindex","0");if($2!=true)$f.setAttribute("scrolling","no");this.appendTo($f,$e);if(typeof $0=="string"){
this.__createPreTrap($9,$e,$f);this.__createPostTrap($9,$e)
}};if($f){
this.__finishCreate($9,$0,$1,$2,$5,$6,$7)
}else{
this.__callqueue[$9]=[["__finishCreate",$9,$0,$1,$2,$5,$6,$7]];setTimeout('lz.embed.iframemanager.__checkiframe("'+$9+'")',10)
};return $9+""
},__createTrap:function($0){
var $1=document.createElement("span");$1.setAttribute("id",$0);$1.setAttribute("tabindex",0);$1.style.width="1px";$1.style.height="1px";if(document.all){
$1.contentEditable="true"
};return $1
},__createPostTrap:function($0,$1){
var $2=this.__createTrap($0+"Trap1");lz.embed.attachEventHandler($2,"focus",lz.embed.iframemanager,"__iframetrap_post",$0);this.appendTo($2,$1)
},__createPreTrap:function($0,$1,$2){
var $3=this.__createTrap($0+"Trap0");lz.embed.attachEventHandler($3,"focus",lz.embed.iframemanager,"__iframetrap_pre",$0);$1.insertBefore($3,$2)
},__checkiframe:function($0){
var $1=document.getElementById($0),$2=lz.embed.iframemanager;if($1){
var $3=$2.__callqueue[$0];delete $2.__callqueue[$0];$2.__playQueue($3)
}else{
setTimeout('lz.embed.iframemanager.__checkiframe("'+$0+'")',10)
}},__playQueue:function($0){
var $1=lz.embed.iframemanager;for(var $2=0;$2<$0.length;$2++){
var $3=$0[$2];var $4=$3.splice(0,1);$1[$4].apply($1,$3)
}},__finishCreate:function($0,$1,$2,$3,$4,$5,$6){
var $7=document.getElementById($0),$8=lz.embed;if(!$7)return;if(typeof $1=="string"){
$7.appcontainer=$8.applications[$1]._getSWFDiv()
};var $9=$8.iframemanager;$7.__owner=$1;$9.__frames[$0]=$7;var $a=$9.getFrame($0);$a.__gotload=$9.__gotload;$a.__zoffset=$5!=null?$5:this.__bottomz;if(document.getElementById&&!document.all){
$a.style.border="0"
}else if(document.all){
$a.setAttribute("allowtransparency","true");var $b=$8[$a.owner];if($b&&$b.runtime=="swf"){
var $c=$b._getSWFDiv();$c.onfocus=$9.__refresh
}};$a.style.position="absolute";if(typeof $1=="string"){
setTimeout("lz.embed.applications."+$1+".callMethod('lz.embed.iframemanager.__setiframeid(\""+$0+"\")')",0)
}else{
$1.__setiframeid($0)
}},appendTo:function($0,$1){
if($1.__appended==$1)return;if($0.__appended){
old=$0.__appended.removeChild($0);$1.appendChild(old)
}else{
$1.appendChild($0)
};$0.__appended=$1
},getFrame:function($0){
return lz.embed.iframemanager.__frames[$0]
},getFrameWindow:function($0){
if(!this["framesColl"]){
if(document.frames){
this.framesColl=document.frames
}else{
this.framesColl=window.frames
}};return this.framesColl[$0]
},setHTML:function($0,$1){
if($1){
var $2=lz.embed.iframemanager.getFrameWindow($0);if($2){
$2.document.body.innerHTML=$1
}}},setSrc:function($0,$1,$2){
if(this.__callqueue[$0]){
this.__callqueue[$0].push(["setSrc",$0,$1,$2]);return
};this.__setSendMouseEvents($0,false);if($2){
var $3=lz.embed,$4=$3.iframemanager.getFrame($0);if(!$4)return;$4.setAttribute("src",$1)
}else{
var $4=this.getFrameWindow($0);if(!$4)return;$4.location.replace($1)
};this.__srcbyid[$0]=$1;this.__loading[$0]=true
},__lastzoom:-1,__positionbyid:{},setPosition:function($0,$1,$2,$3,$4,$5,$6,$7){
if(this.__callqueue[$0]){
this.__callqueue[$0].push(["setPosition",$0,$1,$2,$3,$4,$5,$6,$7]);return
};if($7&&$7>0){
var $8=-1;try{
$8=document.body.clientWidth;var $9=$8/$7;$9=Math.round($9*1000)/1000;if($9>0&&$9!=this.__lastzoom){
this.__lastzoom=$9
};if($9!=1){
if($1!=null)$1=Math.round($1*$9);if($2!=null)$2=Math.round($2*$9);if($3!=null)$3=Math.round($3*$9);if($4!=null)$4=Math.round($4*$9)
}}
catch($a){}};var $b=lz.embed,$c=$b.iframemanager.getFrame($0);if(!$c)return;if($c.appcontainer){
var $d=$b.getAbsolutePosition($c.appcontainer)
}else{
var $d={x:0,y:0}};var $e=null;if($c.parentElement)$e=$c.parentElement;if($c.parentNode)$e=$c.parentNode;if($e){
if($e.style.position!="absolute")$e.style.position="absolute";if($1!=null&&!isNaN($1))$e.style.left=$1+$d.x+"px";if($2!=null&&!isNaN($2))$e.style.top=$2+$d.y+"px";if($e.style.width.indexOf("%")==-1){
if($3!=null&&!isNaN($3))$e.style.width=$3+"px"
};if($e.style.height.indexOf("%")==-1){
if($4!=null&&!isNaN($4))$e.style.height=$4+"px"
}};$c.style.left="0px";$c.style.top="0px";if($3!=null&&!isNaN($3))$c.style.width="100%";if($4!=null&&!isNaN($4))$c.style.height="100%";if($5!=null){
$b.iframemanager.setVisible($0,$5)
};if($6!=null){
$b.iframemanager.setZ($0,$6)
}},setVisible:function($0,$1){
if(this.__callqueue[$0]){
this.__callqueue[$0].push(["setVisible",$0,$1]);return
};if(typeof $1=="string"){
$1=$1=="true"
};lz.embed.iframemanager.setBothStyles($0,null,"display",$1?"block":"none")
},__topz:100000,forceSWFFocus:function($0){
var $1=lz.embed.iframemanager.getFrame($0);if($1&&$1.appcontainer){
setTimeout('lz.embed.iframemanager.getFrame("'+$0+'").appcontainer.focus()',1)
}},bringToFront:function($0,$1){
if(this.__callqueue[$0]){
this.__callqueue[$0].push(["bringToFront",$0,$1]);return
};var $2=lz.embed,$3=$2.iframemanager.getFrame($0);if(!$3)return;if($0!==$2.iframemanager.__front){
$3.__zoffset=++this.__topz;$2.iframemanager.__front=$0;this.setZ($0,$1)
}},__bottomz:99000,sendToBack:function($0,$1){
if(this.__callqueue[$0]){
this.__callqueue[$0].push(["sendToBack",$0,$1]);return
};var $2=lz.embed,$3=$2.iframemanager.getFrame($0);if(!$3)return;if($0!==$2.iframemanager.__back){
$3.__zoffset=--this.__bottomz;$2.iframemanager.__back=$0;this.setZ($0,$1)
}},setStyle:function($0,$1,$2,$3){
var $4;if($1==null){
$4=lz.embed.iframemanager.getFrame($0)
}else{
var $5=lz.embed.iframemanager.getFrameWindow($0);if(!$5)return;$4=$5.document.getElementById($1)
};try{
$4.style[$2]=$3
}
catch($6){}},setDivStyle:function($0,$1,$2,$3){
var $4;if($1==null){
$4=lz.embed.iframemanager.getFrame($0)
}else{
var $5=lz.embed.iframemanager.getFrameWindow($0);if(!$5)return;$4=$5.document.getElementById($1)
};try{
var $6=null;if($4.parentElement)$6=$4.parentElement;if($4.parentNode)$6=$4.parentNode;if($6)$6.style[$2]=$3
}
catch($7){}},setBothStyles:function($0,$1,$2,$3){
var $4;if($1==null){
$4=lz.embed.iframemanager.getFrame($0)
}else{
var $5=lz.embed.iframemanager.getFrameWindow($0);if(!$5)return;$4=$5.document.getElementById($1)
};try{
$4.style[$2]=$3;var $6=null;if($4.parentElement)$6=$4.parentElement;if($4.parentNode)$6=$4.parentNode;if($6)$6.style[$2]=$3
}
catch($7){}},asyncCallback:function($0,$1,$2,$3){
var $4=lz.embed.iframemanager.getFrame($0);if(!$4||!$4.__owner)return;if($4.__owner.__iframecallback){
if(typeof $2==="string"){
try{
$2=JSON.parse($2)||$2
}
catch($5){}};$4.__owner.__iframecallback($1,$2)
}else{
if(lz.embed[$4.__owner]){
$2=$2!=null?","+$2:"";$2+=$3!=null?","+$3+"":"";lz.embed[$4.__owner].callMethod("lz.embed.iframemanager.__iframecallback('"+$0+"','"+$1+"'"+$2+")")
}else{
return
}}},__gotunload:function($0,$1){
lz.embed.iframemanager.__destroy($1)
},__gotload:function($0){
var $1=lz.embed.iframemanager.getFrame($0);if(!$1||!$1.__owner)return;if(this.__loading[$0]==true){
this.__loading[$0]=false;var $2=null;if($1.parentElement)$2=$1.parentElement;if($1.parentNode)$2=$1.parentNode;if($2){
$2.style.display=""
};if(!this.__sendmouseevents[$0]){
this.__setSendMouseEvents($0,true)
};if(this.__calljsqueue[$0]){
this.__playQueue(this.__calljsqueue[$0]);delete this.__calljsqueue[$0]
}};var $3=this.getFrameWindow($0);var $4;try{
$4=$3&&$3.location&&$3.location.href
}
catch($5){};var $6=$4||$1.src;if($6&&this.__srcbyid[$0]!==$6&&$6==='javascript:""'){
return
};setTimeout("lz.embed.iframemanager.asyncCallback('"+$0+"', 'load')",1);try{
$1.contentWindow.document.__iframeid=$0;$1.contentWindow.document.onclick=function(){
var $0=window.lz.embed.iframemanager;var $1=$0.getFrame(this.__iframeid);var $2=$1.__owner;if($2){
if(typeof $2=="string"){

}else{
$2.onclick.sendEvent();LzMouseKernel.__scope[LzMouseKernel.__callback]("onclick",$2,null)
}}};$1.contentWindow.document.onmousedown=function(){
var $0=window.lz.embed.iframemanager;var $1=$0.getFrame(this.__iframeid);var $2=$1.__owner;if($2){
if(typeof $2=="string"){

}else{
$2.onmousedown.sendEvent();LzMouseKernel.__scope[LzMouseKernel.__callback]("onmousedown",$2,null)
}}};$1.contentWindow.document.onmouseup=function(){
var $0=window.lz.embed.iframemanager;var $1=$0.getFrame(this.__iframeid);var $2=$1.__owner;if($2){
if(typeof $2=="string"){

}else{
$2.onmouseup.sendEvent();LzMouseKernel.__scope[LzMouseKernel.__callback]("onmouseup",$2,null)
}}}}
catch($5){}},__refresh:function(){
var $0=lz.embed.iframemanager.__frames;for(var $1 in $0){
var $2=$0[$1];if($2&&$2.style.display=="block"){
$2.style.display="none";$2.style.display="block"
}}},setZ:function($0,$1){
if(this.__callqueue[$0]){
this.__callqueue[$0].push(["setZ",$0,$1]);return
};var $2=lz.embed.iframemanager.getFrame($0);if(!$2)return;$1+=$2.__zoffset;lz.embed.iframemanager.setBothStyles($0,null,"zIndex",$1)
},scrollBy:function($0,$1,$2){
if(this.__callqueue[$0]){
this.__callqueue[$0].push(["scrollBy",$0,$1,$2]);return
};var $3=this.getFrameWindow($0);if(!$3)return;$3.scrollBy($1,$2)
},setFocus:function($0,$1){
if(this.__callqueue[$0]){
this.__callqueue[$0].push(["setFocus",$0]);return
};if(this.__browser.length==0){
this.__browser="{";this.__browser+="'browser':'"+lz.embed.browser.browser+"',";this.__browser+="'version':'"+lz.embed.browser.version+"',";this.__browser+="'subversion':'"+lz.embed.browser.subversion+"',";this.__browser+="'OS':'"+lz.embed.browser.OS+"'";this.__browser+="}"
};var $2=lz.embed.iframemanager.getFrame($0);if(!$2)return;if($2.appcontainer){
if(lz.embed.browser.isChrome)$2.appcontainer.blur()
};var $3="var iframe = lz.embed.iframemanager.getFrameWindow('"+$0+"');";var $4=$3;$4=$4+'if (iframe.startfocus) { setTimeout("'+$3+"iframe.startfocus("+$1+", "+this.__browser+');", 10);}';if(!document.all)$4=$4+"iframe.focus();";setTimeout($4,2)
},setBlur:function($0){
if(this.__callqueue[$0]){
this.__callqueue[$0].push(["setBlur",$0]);return
};if(lz.embed.browser.isIE&&lz.embed.browser.version<=7){
var $1=lz.embed.iframemanager.getFrame($0);if(!$1)return;if($1.appcontainer){
setTimeout("lz.embed.iframemanager.getFrameWindow('"+$0+"').document.body.focus()",0);setTimeout("lz.embed.iframemanager.getFrame('"+$0+"').appcontainer.focus()",1)
}}},__destroy:function($0){
if(this.__callqueue[$0]){
this.__callqueue[$0].push(["__destroy",$0]);return
};var $1=lz.embed.iframemanager,$2=$1.__frames[$0];if($2){
if(this.__sendmouseevents[$0]){
this.__setSendMouseEvents($0,false)
};$2.__owner=null;$2.appcontainer=null;var $3=document.getElementById($0+"Container");if(document.all){
if($3.parentElement){
$3.parentElement.removeChild($3)
}}else if($2.parentNode){
$2.parentNode.removeChild($2);if($3.parentNode){
$3.parentNode.removeChild($3)
}};delete $1.__frames[$0];delete $1.__srcbyid[$0];delete $1.__ownerbyid[$0]
}},callJavascript:function($0,$1,$2,$3){
if(this.__callqueue[$0]){
this.__callqueue[$0].push(["callJavascript",$0,$1,$2,$3]);return
};if(this.__loading[$0]){
if(!this.__calljsqueue[$0]){
this.__calljsqueue[$0]=[]
};this.__calljsqueue[$0].push(["callJavascript",$0,$1,$2,$3]);return
};var $4=lz.embed.iframemanager.getFrameWindow($0);if(!$3)$3=[];try{
var $5=$4.eval($1);if($5){
var $6=$5.apply($4,$3);if($2)$2.execute($6);return $6
}}
catch($7){
window.console&&console.error&&console.error("callJavascript() caught error:",$7,",",$0,",",$1,",",$2,",",$3)
}},callRPC:function(id,$0,callback,$1){
var iframemanager=lz.embed.iframemanager,$2=iframemanager.getFrameWindow(id);var $3={destination:$2,publicProcedureName:$0,params:$1};if(callback!=null){
if(typeof callback=="number"){
$3.onSuccess=function($0){
iframemanager.asyncCallback(id,"__lzcallback",JSON.stringify($0.returnValue),callback)
}}else{
$3.onSuccess=function($0){
callback($0.returnValue)
}}};pmrpc.call($3)
},__getRPCMethods:function(id){
var iframemanager=lz.embed.iframemanager,iframe=iframemanager.getFrameWindow(id);var $0=function($0){
var $1=[];for(var $2=0,$3=$0.length;$2<$3;$2++){
var $4=$0[$2];if($4.destination===iframe){
$1.push($4.publicProcedureName)
}};var $5=JSON.stringify($1);iframemanager.asyncCallback(id,"__rpcmethods",$5)
};pmrpc.discover({callback:$0})
},__mouseEvent:function($0,$1){
var $2=lz.embed;var $3=$2.iframemanager.getFrame($1);if(!$3)return;if(!$0){
$0=window.event
};var $4="on"+$0.type;if($3.__owner&&$3.__owner.sprite&&$3.__owner.sprite.__mouseEvent){
if($4=="oncontextmenu"){
if(!$2.iframemanager.__hidenativecontextmenu[$1]){
return
}else{
var $5=$2.getAbsolutePosition($3);LzMouseKernel.__sendMouseMove($0,$5.x,$5.y);return LzMouseKernel.__showContextMenu($0)
}};$3.__owner.sprite.__mouseEvent($4);if($4=="onmouseup"){
if(LzMouseKernel.__lastMouseDown==$3.__owner.sprite){
LzMouseKernel.__lastMouseDown=null
}}}else{
if($4=="onmouseleave"){
$4="onmouseout"
}else if($4=="onmouseenter"){
$4="onmouseover"
}else if($4=="oncontextmenu"){
return
};$2.iframemanager.asyncCallback($1,"__mouseevent","'"+$4+"'")
}},__iframetrap_pre:function($0,$1){
lz.embed.iframemanager.asyncCallback($1,"__focusevent","'-move'");lz.embed.iframemanager.forceSWFFocus($1)
},__iframetrap_post:function($0,$1){
if($1=="__post__"){
$1="__lz0"
};lz.embed.iframemanager.asyncCallback($1,"__focusevent","'+move'");lz.embed.iframemanager.forceSWFFocus($1)
},__focusEvent:function($0,$1){
var $2=lz.embed;var $3=$2.iframemanager.getFrame($1);if(!$3)return;if(!$0){
$0=window.event
};var $4="on"+$0.type;if($4=="onfocus"||$4=="onfocusin"){
$2.iframemanager.asyncCallback($1,"__focusevent","'focus'")
};return;if($4=="onblur"||$4=="onfocusout"){
$2.iframemanager.asyncCallback($1,"__focusevent","'blur'");if($0.preventDefault){
$0.preventDefault();$0.stopPropagation();console.log("stopped event in Firefox and others");var $5="document.body.focus();";var $6="lz.embed.iframemanager.getFrame('"+$1+"').focus();";var $7="lz.embed.iframemanager.getFrameWindow('"+$1+"').document.body.focus();";var $8="setTimeout('"+$6+"', 10);";var $9="setTimeout('"+$7+"', 20);";var $a=$5+$8+$9;console.log("js:",$a)
};return false;var $b=$3.appcontainer.id;console.log("going to blur. appcontainer=",$3.appcontainer);if(document.all){
var $c=document.getElementById($1);if($c&&$c.__activeelement){
console.log("lastactive: ",$c.__activeelement," currentactive: ",document.activeElement);if($c.__activeelement!=document.activeElement){
return
}}};$2.iframemanager.asyncCallback($1,"__focusevent","'blur'");if($3.appcontainer){
var $d=null;if($3.parentElement)$d=$3.parentElement;if($3.parentNode)$d=$3.parentNode;var $a="var i = lz.embed.iframemanager.getFrame('"+$1+"'); i.appcontainer.focus();console.log('focus to appcontainer', i.appcontainer);";var $c=document.getElementById($b);console.log("js:",$a);setTimeout($a,100)
};if($0.preventDefault){
$0.preventDefault();$0.stopPropagation();console.log("stopped event in Firefox and others")
};return false
}},setSendMouseEvents:function($0,$1){
if(this.__callqueue[$0]){
this.__callqueue[$0].push(["setSendMouseEvents",$0,$1]);return
};lz.embed.iframemanager.__setSendMouseEvents($0,$1)
},__setSendMouseEvents:function(id,$0){
var iframemanager=lz.embed.iframemanager;var $1=iframemanager.__sendmouseevents[id]||false;if($0===$1)return;iframemanager.__sendmouseevents[id]=$0;try{
var $2=iframemanager.getFrameWindow(id).document;if(!$2)return;if(!$2.all&&!$2.valueOf)return
}
catch($3){
return
};var $4=lz.embed[$0?"attachEventHandler":"removeEventHandler"];if($0){
var $5=document.all?"beforeunload":"unload";$4(window,$5,iframemanager,"__gotunload",id)
};$4($2,"mousedown",iframemanager,"__mouseEvent",id);$4($2,"mouseup",iframemanager,"__mouseEvent",id);$4($2,"click",iframemanager,"__mouseEvent",id);$2.oncontextmenu=function($0){
if(!$0)$0=window.event;return iframemanager.__mouseEvent($0,id)
};if(lz.embed.browser.isIE){
$4($2,"mouseenter",iframemanager,"__mouseEvent",id);$4($2,"mouseleave",iframemanager,"__mouseEvent",id);$4($2,"focus",iframemanager,"__focusEvent",id);$4($2,"blur",iframemanager,"__focusEvent",id)
}else{
var $6=$2;if(lz.embed.browser.isSafari||lz.embed.browser.isChrome)$6=$2.defaultView;$4($2,"mouseover",iframemanager,"__mouseEvent",id);$4($2,"mouseout",iframemanager,"__mouseEvent",id);$4($6,"focus",iframemanager,"__focusEvent",id);$4($6,"blur",iframemanager,"__focusEvent",id)
}},setShowNativeContextMenu:function($0,$1){
this.__hidenativecontextmenu[$0]=!$1
},storeSelection:function($0){
var $1=lz.embed.iframemanager;var $2=$1.getFrameWindow($0);if($2&&$2.document&&$2.document.selection&&$2.document.selection.type=="Text"){
$1.__selectionbookmarks[$0]=$2.document.selection.createRange().getBookmark()
}},restoreSelection:function($0){
var $1=lz.embed.iframemanager;var $2=$1.getFrameWindow($0);if($1.__selectionbookmarks[$0]&&$2){
var $3=$1.__selectionbookmarks[$0];var $4=$2.document.body.createTextRange();$4.moveToBookmark($3);$4.select()
}},__reset:function($0){
var $1=lz.embed.iframemanager;if($1.__counter){
var $2=$1.__ownerbyid;for(var $3 in $2){
if($0===$2[$3]){
$1.__destroy($3)
}}}}};pmrpc.register({publicProcedureName:"asyncCallback",procedure:lz.embed.iframemanager.asyncCallback});