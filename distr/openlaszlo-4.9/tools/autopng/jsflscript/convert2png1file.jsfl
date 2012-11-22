function pausecomp(millis)
{
  date = new Date();
  var curDate = null;
  
  do { var curDate = new Date(); }
  while(curDate-date < millis);
}

var volroot = "/Users";
var rootpath = volroot + "/abenner";
var logpath = "file://"+rootpath+"/SWFtoPNGlog.txt";
// This is the hardcoded pathname where applescript will pass filenames to this flash action script
//var URI1 ="file://"+rootpath+"/pathPass.txt";
//alert("starting execution URI1:" + URI1);
//var swfURI = FLfile.read( URI1 );  // Get the file name
//alert ("Read from file: (before) " + escape(swfURI));

//swfURI = swfURI.substring(0, swfURI.length);  // dispose of null at end of file
//alert ("Read from file: (after null removal) " + escape(swfURI));
var swfURI="images/background.swf";
var URI1 = swfURI;
var retval = swfURI.lastIndexOf('/')
  if (retval==0) {
    alert("swfURI malformed: " + swfURI);
    exit;
  }

// Just the folder portion of the pathname. +1?
var swfFolder = swfURI.substring(0, retval + 1);

// Folder name with subdirectory appended
var pngFolder = swfFolder + "autoPng/";
alert ("swfURI: " + escape(swfURI));
var pngFullFolderPath = 'file://' + rootpath +"/"+ pngFolder;
var swfFullPath = 'file://' + rootpath +"/"+ swfURI;
var fnameBase = swfURI.substring(retval + 1, swfURI.indexOf('.swf'));
var pngFullPath = pngFullFolderPath + fnameBase + '.png';
var pngPatternPath = pngFullFolderPath + fnameBase+ '*.png';
alert("pngPatternPath: "+pngPatternPath);
alert (escape (pngFullPath));
alert ("pngFullFolderPath: " + pngFullFolderPath);
alert (swfURI+", swfFullPath: "+swfFullPath+", pngpath: "+pngFullFolderPath+", pngFullPath: "+ pngFullPath);
// Create the subfolder if it does not exist.
if (!FLfile.exists(pngFullFolderPath)) {
  FLfile.createFolder(pngFullFolderPath);
 }

//Debugging to list a directory
//var dirlist = FLfile.listFolder(pngFullFolderPath);
//if (dirlist) {
//    alert(pngFullFolderPath + " contains: " + dirlist.join(" "));
//}

if (!FLfile.exists(swfFullPath)) {
  alert( swfFullPath + " does not exist.");
 }


// If the file already exists, or numbered versions exist, do nothing.
if ((!FLfile.exists(pngFullPath)) & (!(FLfile.listFolder(pngPatternPath,"files").length > 0))) 
  {
    
    if (true) { // Dummy mode
      // FLfile.remove(pngFullPath);
      // Create an empty document.
      var d = fl.createDocument();
      
      try {
        ret = d.importFile( swfFullPath );
        //alert ("importFile complete for: "+swfFullPath+" ret: "+ret);
        ret =  d.selectAll();
        //alert ("selectAll complete for: "+swfFullPath+" ret: "+ret);
        var rect = d.getSelectionRect();
        
        var w = rect.left + rect.right;
        if (Math.floor(w) != w) w = Math.floor(w) + 1
        if (w > 2048) w = 2048;
        if (w < 2) w = 2;
        
        var h = rect.top + rect.bottom;
        if (Math.floor(h) != h) h = Math.floor(h) + 1
        if (h > 2048) h = 2048;
        if (h < 2) h = 2;
        
        //alert(w + ', ' + h);
        d.width = w;
        d.height = h;
        
        ret = d.exportPNG(pngFullPath, true, false);
        //alert ("exportPNG complete: ret= "+ret);
        //alert( "fl.closedocument: " + fl.closeDocument);
        //alert( "d.close: " + d.close);
        ret=fl.closeDocument(d, false);
      }
      catch (e)
        {
          var logstr = Date() + ' Caught Error: '+ e.toString() + ' in ' + pngFullPath +  '\n';
          FLfile.write(logpath, logstr, "append");
        }
      //alert ("closeDocument complete: ret= "+ret);
      finally
        {
          var logstr =  Date() + ' Wrote: ' + pngFullPath + '\n';
          FLfile.write(logpath, logstr, "append"); 
        }
    }
  }

 else
   
   {
     var logstr =  Date() + ' Skipped: ' + pngFullPath + '\n';
     FLfile.write(logpath, logstr, "append"); 
   }  

if(!FLfile.exists(URI1))
  {
    alert("File does not exist URI1: "+ URI1)
      }

if(!FLfile.remove(URI1))
  {
    pausecomp(8000); // Try again after 8 seconds.
    if(!FLfile.remove(URI1)) // The correct way to do this is by checking file creation time.
      {
        //If it still doesn't work, notify and try one last time.
        alert("Can't delete. Try again" + URI1);
        if(!FLfile.remove(URI1))
          {
            alert("Unable to delete URI1: "+ URI1);
          }
      }
  }
