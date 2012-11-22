//Under development
var ipfPath = determineipfPath();
while (true) {
	var thePathLine = getPassedValue(ipfPath);
	
	if (thePathLine == "") {
		return();
	}
	else
	{
		// Just the folder portion of the pathname. +1?
		var swfFolder = thePathLine.substring(0, retval+1);
		// Folder name with subdirectory appended
		var pngFolder = swfFolder+"autoPng/";
		//alert ("thePathLine: " + escape(thePathLine));
		var pngFullFolderPath = 'file://'+rootpath+pngFolder;
		var swfFullPath = 'file://'+thePathLine;
		var fnameBase = thePathLine.substring(retval+1, thePathLine.indexOf('.swf'));
		var pngFullPath = pngFullFolderPath+fnameBase+'.png';
		var pngPatternPath = pngFullFolderPath+fnameBase+'*.png';
		//alert("pngPatternPath: "+pngPatternPath);
		//alert (escape (pngFullPath));
		//alert ("pngFullFolderPath: " + pngFullFolderPath);
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
			alert(swfFullPath+" does not exist.");
		}
		// If the file already exists, or numbered versions exist, skip and log.       
		if ((!FLfile.exists(pngFullPath)) & (!(FLfile.listFolder(pngPatternPath, "files").length>0))) 
		{
				// FLfile.remove(pngFullPath);
				// Create an empty document.
				var d = fl.createDocument();
				try 
				{
					setBounds(d, swfFullPath, pngFullPath);
					}
				catch (myerr) {
						var logstr = (Date() + ' Caught Error: '+ e.toString() + ' in ' + pngFullPath +  '\n');
						FLfile.write(logpath, logstr, "append");
					}
				//alert ("closeDocument complete: ret= "+ret);
				finally {
						var logstr =  (Date() + ' Wrote: ' + pngFullPath + '\n');
						FLfile.write(logpath, logstr, "append"); 
					}
				}
				else 
				{
			var logstr = Date()+' Skipped: '+pngFullPath+'\n';
			FLfile.write(logpath, logstr, "append");
		}
		
		if (!FLfile.exists(ipfPath)) {
			alert("File does not exist ipfPath: "+ipfPath);
		}
		if (!FLfile.remove(ipfPath)) {
			pausecomp(8000);
			// Try again after 8 seconds.
			if (!FLfile.remove(ipfPath)) {
				// The correct way to do this is by checking file creation time.
				//If it still doesn't work, notify and try one last time.
				alert("Can't delete. Try again"+ipfPath);
				if (!FLfile.remove(ipfPath)) {
					alert("Unable to delete ipfPath: "+ipfPath);
				}
			}
		}
	}
}
function pausecomp(millis) {
	date = new Date();
	var curDate = null;
	do {
		var curDate = new Date();
	} while (curDate-date<millis);
}
function determineipfPath() {
	var volroot = "/Phill17";
	var rootpath = volroot+"/Users/papley";
	var logpath = "file://"+rootpath+"/SWFtoPNGlog.txt";
	// This is the hardcoded pathname where applescript will pass filenames to this flash action script
	var ipfPath = "file://"+rootpath+"/pathpass.txt";
}
function getPassedValue(ipfPath) {
	//alert("starting execution ipfPath:" + ipfPath);
	var thePathLine = FLfile.read(ipfPath);
	// Get the file name
	//alert ("Read from file: (before) " + escape(thePathLine));
	thePathLine = thePathLine.substring(0, thePathLine.length);
	// dispose of null at end of file
	//alert ("Read from file: (after null removal) " + escape(thePathLine));
	var retval = thePathLine.lastIndexOf('/');
	if (retval == 0) {
		throw Error("thePathLine malformed: + thePathLine");
	}
	return retval;
}
function setBounds(d, swfFullPath, pngFullPath) {
	ret = d.importFile(swfFullPath);
	//alert ("importFile complete for: "+swfFullPath+" ret: "+ret);
	//math
	ret = d.selectAll();
	//alert ("selectAll complete for: "+swfFullPath+" ret: "+ret);
	var rect = d.getSelectionRect();
	var w = rect.left+rect.right;
	if (Math.floor(w) != w) {
		w = Math.floor(w)+1;
	}
	if (w>500) {
		w = 500;
	}
	if (w<2) {
		w = 2;
	}
	var h = rect.top+rect.bottom;
	if (Math.floor(h) != h) {
		h = Math.floor(h)+1;
	}
	if (h>500) {
		h = 500;
	}
	if (h<2) {
		h = 2;
	}
	//alert(w + ', ' + h);      
	d.width = w;
	d.height = h;
	ret = d.exportPNG(pngFullPath, true, false);
	//alert ("exportPNG complete: ret= "+ret);
	//alert( "fl.closedocument: " + fl.closeDocument);
	//alert( "d.close: " + d.close);
	ret = fl.closeDocument(d, false);
}
