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

/**
 *
 * @author Mikhail Yevchenko m.ṥῥẚɱ.ѓѐḿởύḙ@azazar.com
 */
public class StringParser {

    private String string;
    int index;
    
    public int length(){
    	return string.length()-index;
    }

    public StringParser(String string) {
        this.string = string;
        index = 0;
    }

    public void forward(int chars) {
    	index += chars;
    }

    public char poll() {
        char c = string.charAt(index);
        forward(1);
        return c;
    }

    public String poll(int length) {
        String str = string.substring(index, length+index);
        forward(length);
        return str;
    }
    
    private void commit(){
    	string = string.substring(index);
    	index = 0;
    }

    public String pollBeforeSkipDelim(String s) {
    	commit();
        int i = string.indexOf(s);
        if (i == -1)
            throw new RuntimeException("\"" + s + "\" not found in \"" + string + "\"");
        String rv = string.substring(0, i);
        forward(i + s.length());
        return rv;
    }

    public char peek() {
        return string.charAt(index);
    }

    public String peek(int length) {
        return string.substring(index, length+index);
    }

    public String trim() {
    	commit();
        return string = string.trim();
    }
    
    public char charAt(int pos) {
		return string.charAt(pos+index);
	}

    public boolean isEmpty() {
    	return (string.length()<=index);
    }

    @Override
    public String toString() {
    	commit();
        return string;
    }

}
