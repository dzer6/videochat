/* ****************************************************************************
 * ReturnPersonExample.java
 * ****************************************************************************/

/* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2004 Laszlo Systems, Inc.  All Rights Reserved.              *
* Use is subject to license terms.                                            *
* J_LZ_COPYRIGHT_END *********************************************************/

package examples;

import java.util.ArrayList;

//------------------------------------------------------------------------------
// See examples/javarpc/returnperson.lzx
//------------------------------------------------------------------------------

public class ReturnPersonExample {

    static ArrayList list = new ArrayList();

    public static Person getPerson() {
        return new Person("eric", "2600 campus drive", "san mateo");
    }

    public static ArrayList getArrayListOfPerson() {
        return list;
    }

    static 
    {
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
        list.add(new Person("kathryn", "2600 campus drive", "san mateo"));
        list.add(new Person("scott",   "2600 campus drive", "san mateo"));
        list.add(new Person("sarah",   "2600 campus drive", "san mateo"));
        list.add(new Person("kirsten", "2600 campus drive", "san mateo"));
    }

}
