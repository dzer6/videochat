/* ****************************************************************************
 * ReturnTypeExample.java
* ****************************************************************************/

/* J_LZ_COPYRIGHT_BEGIN *******************************************************
* Copyright 2001-2004 Laszlo Systems, Inc.  All Rights Reserved.              *
* Use is subject to license terms.                                            *
* J_LZ_COPYRIGHT_END *********************************************************/

package examples;

import java.util.List;
import java.util.Vector;
import java.util.Map;
import java.util.HashMap;

public class ReturnTypeExample {

    public static int returnInteger() {
        return 1;
    }

    public static Integer returnIntegerObject() {
        return new Integer(2);
    }

    public static short returnShort() {
        return 3;
    }

    public static Short returnShortObject() {
        return new Short((short)4);
    }

    public static long returnLong() {
        return 5;
    }

    public static Long returnLongObject() {
        return new Long(6);
    }

    public static float returnFloat() {
        return 7;
    }

    public static Float returnFloatObject() {
        return new Float(8);
    }

    public static double returnDouble() {
        return 3.14159;
    }

    public static Double returnDoubleObject() {
        return new Double(3.14159);
    }

    public static byte returnByte() {
        return (byte)11;
    }

    public static Byte returnByteObject() {
        return new Byte((byte)12);
    }

    public static boolean returnBoolean() {
        return true;
    }

    public static Boolean returnBooleanObject() {
        return new Boolean(false);
    }

    public static char returnCharacter() {
        return 'a';
    }

    public static Character returnCharacterObject() {
        return new Character('b');
    }

    public static String returnString() {
        return "returing a string";
    }

    public static Coordinate returnCoordinateObject() {
        return new Coordinate(4,2);
    }

    public static int[] returnIntegerArray() {
        int[] intarr = { 1, 2, 3, 4, 5 };
        return intarr;
    }

    public static String[] returnStringArray() {
        String[] strarr = { "one", "two", "three", "four", "five" };
        return strarr;
    }

    public static Coordinate[] returnCoordinateObjectArray() {
        Coordinate[] coarr =  { new Coordinate(1,1), 
                                new Coordinate(2,2),
                                new Coordinate(3,3),
                                new Coordinate(4,4),
                                new Coordinate(5,5) };
        return coarr;
    }

    public static List returnIntegerList() {
        List list = new Vector();
        list.add(new Integer(1));
        list.add(new Integer(2));
        list.add(new Integer(3));
        list.add(new Integer(4));
        list.add(new Integer(5));
        return list;
    }

    public static Map returnIntegerMap() {
        Map map = new HashMap();
        map.put("one", new Integer(1));
        map.put("two", new Integer(2));
        map.put("three", new Integer(3));
        map.put("four", new Integer(4));
        map.put("five", new Integer(5));
        return map;
    }

    public static List returnCoordinateObjectList() {
        List list = new Vector();
        list.add(new Coordinate(1,1));
        list.add(new Coordinate(2,2));
        list.add(new Coordinate(3,3));
        list.add(new Coordinate(4,4));
        list.add(new Coordinate(5,5));
        return list;
    }

    public static Map returnCoordinateObjectMap() {
        Map map = new HashMap();
        map.put("one", new Coordinate(1,1));
        map.put("two", new Coordinate(2,3));
        map.put("three", new Coordinate(5,8));
        map.put("four", new Coordinate(13,21));
        map.put("five", new Coordinate(34,55));
        return map;
    }

    static public class Coordinate {
        public int x;
        public int y;

        public Coordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public String toString() {
            return "x: " + this.x + ", y: " + this.y;
        }
    }

}
