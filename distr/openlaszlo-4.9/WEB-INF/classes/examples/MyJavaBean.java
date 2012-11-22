/* ****************************************************************************
 * MyJavaBean.java
 * ****************************************************************************/

/* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2004 Laszlo Systems, Inc.  All Rights Reserved.              *
* Use is subject to license terms.                                            *
* J_LZ_COPYRIGHT_END *********************************************************/
package examples;

/**
 * Class used in ReturnJavaBeanExample.java.
 */
public class MyJavaBean {
    String name;
    String city;
    String state;
    String secret;

    public MyJavaBean(String name, String city, String state, String secret) {
        this.name = name;
        this.city = city;
        this.state = state;
        this.secret = secret;
    }

    public String getName() { return name; }
    public String getCity() { return city; }
    public String getState() { return state; }
}
