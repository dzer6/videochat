/* ****************************************************************************
 * ReturnPOJOExample.java
 * ****************************************************************************/

/* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2004 Laszlo Systems, Inc.  All Rights Reserved.              *
* Use is subject to license terms.                                            *
* J_LZ_COPYRIGHT_END *********************************************************/
package examples;

/**
 * Example that returns only public members. See
 * examples/javarpc/returnpojo.lzx.
 */
public class ReturnPOJOExample {

    public static MyPOJO getPOJO() {
        return new MyPOJO();
    }

}
