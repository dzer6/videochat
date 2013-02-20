/* * X_LZ_COPYRIGHT_BEGIN ***************************************************
* Copyright 2001-2011 Laszlo Systems, Inc.  All Rights Reserved.              *
* Use is subject to license terms.                                            *
* X_LZ_COPYRIGHT_END ****************************************************** -->
* @LZX_VERSION@
*/


// Support functions for the solo deploy jsp pages

     function toggleLineThrough(cb) {
         console.log(cb, 'checked', cb.checked);
         var textnode;
         var span = cb.parentNode;
         var siblings = cb.parentNode.childNodes;
         for (var i = 0; i < siblings.length; i++) {
             var n = siblings[i];
             console.log(i, n);
             if (n.nodeName == "FONT") textnode = n;
         }
       if (cb.checked == true) {
           //           cb.parentNode.style.textDecoration = 'line-through';
           textnode.style.textDecoration = 'line-through';
           span.style.textDecoration = 'line-through';
       } else {
           // cb.parentNode.style.textDecoration = 'none';
           span.style.textDecoration = 'none';
           textnode.style.textDecoration = 'none';
       }
     }

        
