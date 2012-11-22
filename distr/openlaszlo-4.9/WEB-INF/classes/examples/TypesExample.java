/* ****************************************************************************
 * TypesExample.java
* ****************************************************************************/

/* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2004 Laszlo Systems, Inc.  All Rights Reserved.              *
* Use is subject to license terms.                                            *
* J_LZ_COPYRIGHT_END *********************************************************/

package examples;

import java.util.Vector;
import java.util.Hashtable;

public class TypesExample {

    public static String passInteger(int i) {
        return "got integer parameter: " + i;
    }

    public static String passDouble(double d) {
        return "got double parameter: " + d;
    }

    public static String passBoolean(boolean b) {
        return "got boolean parameter: " + b;
    }

    public static String passClientArray(Vector v) {
        return "got vector parameter: " + v;
    }

    public static String passClientObject(Hashtable t) {
        return "got hashtable parameter: " + t;
    }

}
