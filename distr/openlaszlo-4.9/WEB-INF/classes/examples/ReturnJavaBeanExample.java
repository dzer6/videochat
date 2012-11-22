/* ****************************************************************************
 * ReturnJavaBeanExample.java
 * ****************************************************************************/

/* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2004 Laszlo Systems, Inc.  All Rights Reserved.              *
* Use is subject to license terms.                                            *
* J_LZ_COPYRIGHT_END *********************************************************/
package examples;

/**
 * Example that returns only public members. See
 * examples/javarpc/returnjavabean.lzx.
 */
public class ReturnJavaBeanExample {

    public static MyJavaBean getJavaBean(String name, String city, 
                                         String state, String secret) {
        return new MyJavaBean(name, city, state, secret);
    }

}
