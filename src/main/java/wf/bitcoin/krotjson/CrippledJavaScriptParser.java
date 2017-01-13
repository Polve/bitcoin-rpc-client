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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author Mikhail Yevchenko m.ṥῥẚɱ.ѓѐḿởύḙ@azazar.com
 */
public class CrippledJavaScriptParser {
    
    private static boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    private static boolean isIdStart(char ch) {
        return ch == '_' || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }
    
    private static boolean isId(char ch) {
        return isDigit(ch) || ch == '_' || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    private static Object parseJSString(StringParser jsString, char delim) {
        StringBuilder b = new StringBuilder();
        while ((!jsString.isEmpty())) {
            char sc = jsString.poll();
            if (sc == '\\') {
                char cc = jsString.poll();
                switch (cc) {
                    case 't':
                        b.append('\t');
                        break;
                    case 'r':
                        b.append('\r');
                        break;
                    case 'n':
                        b.append('\n');
                        break;
                    case 'f':
                        b.append('\f');
                        break;
                    case 'b':
                        b.append('\b');
                        break;
                    case 'u':
                        try {
                            char ec = (char) Integer.parseInt(jsString.peek(4), 16);
                            b.append(ec);
                            jsString.forward(4);
                        } catch (NumberFormatException ex) {
                            b.append("\\u");
                        }
                        break;
                    default:
                        b.append(cc);
                }
            } else if (sc == delim) {
                break;
            } else {
                b.append(sc);
            }
        }
        return b.toString();
    }

    private static List parseJSArray(StringParser jsArray) {
        ArrayList rv = new ArrayList();
        jsArray.trim();
        if (jsArray.peek()==']') {
            jsArray.forward(1);
            return rv;
        }
        while (!jsArray.isEmpty()) {
            rv.add(parseJSExpr(jsArray));
            jsArray.trim();
            if (!jsArray.isEmpty()) {
                char ch = jsArray.poll();
                if (ch == ']')
                    return rv;
                if (ch != ',')
                    throw new RuntimeException(jsArray.toString());
                jsArray.trim();
            }
        }
        return rv;
    }

    private static String parseId(StringParser jsId) {
        StringBuilder b = new StringBuilder();
        b.append(jsId.poll());
        char ch;
        while (isId(ch = jsId.peek())) {
            b.append(ch);
            jsId.forward(1);
        }
        return b.toString();
    }

    private static HashMap parseJSHash(StringParser jsHash) {
        LinkedHashMap rv = new LinkedHashMap();
        jsHash.trim();
        if (jsHash.peek()=='}') {
            jsHash.forward(1);
            return rv;
        }
        while (!jsHash.isEmpty()) {
            Object key;
            if (isIdStart(jsHash.peek())) {
                key = parseId(jsHash);
            } else {
                key = parseJSExpr(jsHash);
            }
            jsHash.trim();
            if (!jsHash.isEmpty()) {
                if (jsHash.peek() != ':')
                    throw new RuntimeException(jsHash.toString());
                jsHash.forward(1);
                jsHash.trim();
            } else
                throw new IllegalArgumentException();
            Object value = parseJSExpr(jsHash);
            jsHash.trim();
            if (!jsHash.isEmpty()) {
                char ch = jsHash.poll();
                if (ch == '}') {
                    rv.put(key, value);
                    return rv;
                }
                if (ch != ',')
                    throw new RuntimeException(jsHash.toString());
                jsHash.trim();
            }
            rv.put(key, value);
        }
        return rv;
    }

    private static class Keyword {
        public final String keyword;
        public final Object value;
        
        public final char firstChar;
        public final String keywordFromSecond;

        public Keyword(String keyword, Object value) {
            this.keyword = keyword;
            this.value = value;
            firstChar = keyword.charAt(0);
            keywordFromSecond = keyword.substring(1);
        }
    }
    private static Keyword[] keywords = {
        new Keyword("null", null),
        new Keyword("true", Boolean.TRUE),
        new Keyword("false", Boolean.FALSE),
    };

    public static Object parseJSExpr(StringParser jsExpr) {
        if (jsExpr.isEmpty())
            throw new IllegalArgumentException();
        jsExpr.trim();
        char start = jsExpr.poll();
        if (start == '[')
            return parseJSArray(jsExpr);
        if (start == '{')
            return parseJSHash(jsExpr);
        if (start == '\'' || start == '\"')
            return parseJSString(jsExpr, start);
        if (isDigit(start) || start == '-' || start == '+') {
            StringBuilder b = new StringBuilder();
            if (start != '+')
                b.append(start);
            char sc, psc = 0;
            boolean exp = false;
            boolean dot = false;
            for(;;) {
                if (jsExpr.isEmpty())
                    break;
                sc = jsExpr.peek();
                if (!isDigit(sc)) {
                    if (sc == 'E' || sc == 'e') {
                        if (exp)
                            throw new NumberFormatException(b.toString() + jsExpr.toString());
                        exp = true;
                    } else if (sc == '.') {
                        if (dot || exp)
                            throw new NumberFormatException(b.toString() + jsExpr.toString());
                        dot = true;
                    } else if ((sc == '-' || sc == '+') && (psc == 'E' || psc == 'e')) {
                        // it's ok
                    } else
                        break;
                }

                b.append(sc);
                jsExpr.forward(1);
                
                psc = sc;
            }
            //System.out.println("Str: " + b.toString() + "; Rem: " + jsExpr);
            return dot || exp ? (Object)Double.parseDouble(b.toString()) : (Object)Long.parseLong(b.toString());
        }
        for (Keyword keyword : keywords) {
        	int keywordlen = keyword.keywordFromSecond.length();
            if (start == keyword.firstChar && jsExpr.peek(keywordlen).equals(keyword.keywordFromSecond)) {
                if (jsExpr.length() == keyword.keywordFromSecond.length()) {
                    jsExpr.forward(keyword.keywordFromSecond.length());
                    return keyword.value;
                }
                if (!isId(jsExpr.charAt(keyword.keywordFromSecond.length()))) {
                    jsExpr.forward(keyword.keywordFromSecond.length());
                    jsExpr.trim();
                    return keyword.value;
                } else {
                    throw new IllegalArgumentException(jsExpr.toString());
                }
            }
        }
        if (start == 'n' && jsExpr.peek("ew Date(".length()).equals("ew Date(")) {
            jsExpr.forward("ew Date(".length());
            Number date = (Number) parseJSExpr(jsExpr);
            jsExpr.trim();
            if (jsExpr.poll() != ')')
                throw new RuntimeException("Invalid date");
            return new Date(date.longValue());
        }
        throw new UnsupportedOperationException("Unparsable javascript expression: \""+start+jsExpr+"\"");
    }

    public static Object parseJSExpr(String jsExpr) {
        return parseJSExpr(new StringParser(jsExpr));
    }

    public static LinkedHashMap<String, Object> parseJSVars(String javascript) {
        try {
            BufferedReader r = new BufferedReader(new StringReader(javascript));
            LinkedHashMap<String, Object> rv = new LinkedHashMap();
            String l;
            while ((l = r.readLine()) != null) {
                l = l.trim();
                if (l.isEmpty() || !l.startsWith("var"))
                    continue;
                l = l.substring(3).trim();
                int i = l.indexOf('=');
                if (i == -1)
                    continue;
                String varName = l.substring(0, i).trim();
                String expr = l.substring(i + 1).trim();
                if (expr.endsWith(";"))
                    expr = expr.substring(0, expr.length() - 1).trim();
                rv.put(varName, parseJSExpr(expr));
            }
            return rv;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

//    public static void main(String[] args) {
//        System.out.println(parseJSExpr("[ ]"));
//        System.out.println(parseJSExpr("[]"));
//        System.out.println(parseJSExpr("[1,2,3]"));
//        String test =
//                  "var hash = { 1:1, 2:2, 3:\"cds\", 'asc': 'dvaev', 'x': null };\n"
//                + "var array = [null, 15765 , 16167 , 15997 , 16288 , 16289 , 'veffv' , \"\\'sadasd\\'\" ];\n"
//                + "var mixed = [ [], [ ], {}, { }, { 'x': 'y', 'y': 'z', id: 'value' }, { 1:2 }, {3:2, 4:[1,2,3,-1,111,-111,true,false,null]} ];\n";
//        System.out.println(parseJSVars(test));
//    }

}
