/* ****************************************************************************
 * AccentedTextExample.java
 * ****************************************************************************/

/* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2004 Laszlo Systems, Inc.  All Rights Reserved.              *
* Use is subject to license terms.                                            *
* J_LZ_COPYRIGHT_END *********************************************************/
package examples;


import java.util.ArrayList;

//------------------------------------------------------------------------------
// See examples/javarpc/accentedtext.lzx
//------------------------------------------------------------------------------

public class AccentedTextExample {

    public static void getVoid() { }

    public static String getJapaneseChars() {
        return "\u3080 \u30c0 \u30d0 \u30e0 \u30f0 \u30a1 \u30b1 \u30c1 \u30d1 \u30e1 \u30f1 ";
    }

    public static String getAccentedChars() {
        return "\u0063\u0068 \u0078\u0323 \u019B\u0313";
    }

    public static ArrayList getArrayListOfString() {
        ArrayList list = new ArrayList();
        list.add("\u00c0");
        list.add("\u00d0");
        list.add("\u00e0");
        list.add("\u00f0");
        list.add("\u00c1");
        list.add("\u00d1");
        list.add("\u00e1");
        list.add("\u00f1");
        list.add("\u00c2");
        list.add("\u00d2");
        list.add("\u00e2");
        list.add("\u00f2");
        list.add("\u00c3");
        list.add("\u00d3");
        list.add("\u00e3");
        list.add("\u00f3");
        return list;
    }
}
