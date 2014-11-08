/*
 * KrotJSON License
 * 
 * Copyright (c) 2013, Mikhail Yevchenko.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the 
 * Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject
 * to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR
 * ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 * THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package wf.bitcoin.krotjson;

import java.util.Date;
import java.util.Map;

/**
 *
 * @author Mikhail Yevchenko m.ṥῥẚɱ.ѓѐḿởύḙ@azazar.com
 */
public class JSON {
    
    public static String stringify(Object o) {
        if (o == null)
            return "null";
        if ((o instanceof Number) || (o instanceof Boolean))
            return String.valueOf(o);
        if (o instanceof Date)
            return "new Date("+((Date)o).getTime()+")";
        if (o instanceof Map)
            return stringify((Map)o);
        if (o instanceof Iterable)
            return stringify((Iterable)o);
        if (o instanceof Object[])
            return stringify((Object[])o);
        return stringify(String.valueOf(o));
    }
    
    public static String stringify(Map m) {
        StringBuilder b = new StringBuilder();
        b.append('{');
        boolean first = true;
        for (Map.Entry e : ((Map<Object, Object>)m).entrySet()) {
            if (first)
                first = false;
            else
                b.append(",");
            b.append(stringify(e.getKey().toString()));
            b.append(':');
            b.append(stringify(e.getValue()));
            
        }
        b.append('}');
        return b.toString();
    }
    
    public static String stringify(Iterable c) {
        StringBuilder b = new StringBuilder();
        b.append('[');
        boolean first = true;
        for (Object o : c) {
            if (first)
                first = false;
            else
                b.append(",");
            b.append(stringify(o));
        }
        b.append(']');
        return b.toString();
    }
    
    public static String stringify(Object[] c) {
        StringBuilder b = new StringBuilder();
        b.append('[');
        boolean first = true;
        for (Object o : c) {
            if (first)
                first = false;
            else
                b.append(",");
            b.append(stringify(o));
        }
        b.append(']');
        return b.toString();
    }
    
    public static String stringify(String s) {
        StringBuilder b = new StringBuilder(s.length() + 2);
        b.append('"');
        for(; !s.isEmpty(); s = s.substring(1)) {
            char c = s.charAt(0);
            switch (c) {
                case '\t':
                    b.append("\\t");
                    break;
                case '\r':
                    b.append("\\r");
                    break;
                case '\n':
                    b.append("\\n");
                    break;
                case '\f':
                    b.append("\\f");
                    break;
                case '\b':
                    b.append("\\b");
                    break;
                case '"':
                case '\\':
                    b.append("\\");
                    b.append(c);
                    break;
                default:
                    b.append(c);
            }
        }
        b.append('"');
        return b.toString();
    }
    
    public static Object parse(String s) {
        return CrippledJavaScriptParser.parseJSExpr(s);
    }

//    public static void main(String[] args) {
//        String test =
//                  "[ { 'x': 'y', 'y': 'z', id: 'value' }, { 1:2 }, {3:2, 4:[null,1,2,3,null,-1,111,-111,null]} ];";
//        System.out.println(stringify(parse(test)));
//        System.out.println(stringify(new Object[] {1,2,3,"asd"}));
//    }

}
