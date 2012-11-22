/* ****************************************************************************
 * ConstructExample.java
 * ****************************************************************************/

/* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2004 Laszlo Systems, Inc.  All Rights Reserved.              *
* Use is subject to license terms.                                            *
* J_LZ_COPYRIGHT_END *********************************************************/

package examples;

public class ConstructExample {

    int mInt = 0;
    String mString = "";
    double mDouble = 0.0;

    public ConstructExample(int i) {
        mInt = i;
    }

    public ConstructExample(int i, String s, double d) {
        mInt = i;
        mString = s;
        mDouble = d;
    }

    public String getInfo() {
        return "int: " + mInt + "\n"
            + "string: " + mString + "\n"
            + "double: " + mDouble + "\n";
    }
}
