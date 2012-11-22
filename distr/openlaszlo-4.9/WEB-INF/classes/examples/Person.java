/* ****************************************************************************
 * Person.java
 * ****************************************************************************/

/* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2004 Laszlo Systems, Inc.  All Rights Reserved.              *
* Use is subject to license terms.                                            *
* J_LZ_COPYRIGHT_END *********************************************************/

package examples;

public class Person {

    public String mName;
    public String mAddress;
    public String mCity;

    public Person(String name, String address, String city) {
        mName = name;
        mAddress = address;
        mCity = city;
    }

    public String getName() {
        return mName;
    }
    public void setName(String name) {
        mName = name;
    }
    public String getAddress() {
        return mAddress;
    }
    public void setAddress(String address) {
        mAddress = address;
    }
    public String getCity() {
        return mCity;
    }
    public void setCity(String city) {
        mCity = city;
    }
}
