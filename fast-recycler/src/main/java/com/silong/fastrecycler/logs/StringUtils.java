/*
 * Copyright (C) 2016 MySQUAR. All rights reserved.
 *
 * This software is the confidential and proprietary information of MySQUAR or one of its
 * subsidiaries. You shall not disclose this confidential information and shall use it only in
 * accordance with the terms of the license agreement or other applicable agreement you entered into
 * with MySQUAR.
 *
 * MySQUAR MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE, OR NON-INFRINGEMENT. MySQUAR SHALL NOT BE LIABLE FOR ANY LOSSES
 * OR DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR
 * ITS DERIVATIVES.
 */

package com.silong.fastrecycler.logs;

import android.text.TextUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.zip.CRC32;

/**
 * Created by lamtn on 7/11/16.
 */
public class StringUtils {

  private static final int DEFAULT_BUFFER_SIZE = 4096;

  public static String capitalize(String s) {
    String c = toString((Object) s);
    return c.length() >= 2 ? c.substring(0, 1).toUpperCase(Locale.getDefault()) + c.substring(1)
        : (c.length() >= 1 ? c.toUpperCase(Locale.getDefault()) : c);
  }

  public static String[] chunk(String str, int chunkSize) {
    if (!isEmpty(str) && chunkSize != 0) {
      int len = str.length();
      int arrayLen = (len - 1) / chunkSize + 1;
      String[] array = new String[arrayLen];

      for (int i = 0; i < arrayLen; ++i) {
        array[i] = str.substring(i * chunkSize, i * chunkSize + chunkSize < len ? i * chunkSize + chunkSize : len);
      }

      return array;
    } else {
      return new String[0];
    }
  }

  public static int copy(Reader input, Writer output) {
    long count = copyLarge(input, output);
    return count > 2147483647L ? -1 : (int) count;
  }

  public static long copyLarge(Reader input, Writer output) throws RuntimeException {
    try {
      char[] e = new char[4096];

      long count;
      int n;
      for (count = 0L; -1 != (n = input.read(e)); count += (long) n) {
        output.write(e, 0, n);
      }

      return count;
    } catch (IOException var6) {
      throw new RuntimeException(var6);
    }
  }

  public static boolean equals(Object a, Object b) {
    return toString(a).equals(toString(b));
  }

  public static boolean equalsIgnoreCase(Object a, Object b) {
    return toString(a).toLowerCase(Locale.getDefault()).equals(toString(b).toLowerCase(Locale.getDefault()));
  }

  public static long getCRC32CodeFromString(String string) {
    if (TextUtils.isEmpty(string)) {
      return 0;
    }
    CRC32 crc32 = new CRC32();
    byte[] buffer = string.getBytes();
    crc32.update(buffer);
    return crc32.getValue();
  }

  public static String getRange(int range) {
    return "bytes=" + range + "-";
  }

  public static boolean isEmpty(Object o) {
    return toString(o).trim().length() == 0;
  }

  public static <T> String join(String delimiter, Collection<T> objs) {
    if (objs != null && !objs.isEmpty()) {
      Iterator iter = objs.iterator();
      StringBuilder buffer = new StringBuilder(toString(iter.next()));

      while (iter.hasNext()) {
        Object obj = iter.next();
        if (notEmpty(obj)) {
          buffer.append(delimiter).append(toString(obj));
        }
      }

      return buffer.toString();
    } else {
      return "";
    }
  }

  public static <T> String join(String delimiter, T... objects) {
    return join(delimiter, (Collection) Arrays.asList(objects));
  }

  public static <T> String joinAnd(String delimiter, String lastDelimiter, Collection<T> objs) {
    if (objs != null && !objs.isEmpty()) {
      Iterator iter = objs.iterator();
      StringBuilder buffer = new StringBuilder(toString(iter.next()));
      int i = 1;

      while (iter.hasNext()) {
        Object obj = iter.next();
        if (notEmpty(obj)) {
          ++i;
          buffer.append(i == objs.size() ? lastDelimiter : delimiter).append(toString(obj));
        }
      }

      return buffer.toString();
    } else {
      return "";
    }
  }

  public static <T> String joinAnd(String delimiter, String lastDelimiter, T... objs) {
    return joinAnd(delimiter, lastDelimiter, (Collection) Arrays.asList(objs));
  }

  public static String md5(String s) {
    try {
      byte[] e = MessageDigest.getInstance("MD5").digest(s.getBytes("UTF-8"));
      StringBuilder hashString = new StringBuilder();
      byte[] arr$ = e;
      int len$ = e.length;

      for (int i$ = 0; i$ < len$; ++i$) {
        byte aHash = arr$[i$];
        String hex = Integer.toHexString(aHash);
        if (hex.length() == 1) {
          hashString.append('0');
          hashString.append(hex.charAt(hex.length() - 1));
        } else {
          hashString.append(hex.substring(hex.length() - 2));
        }
      }

      return hashString.toString();
    } catch (Exception var8) {
      throw new RuntimeException(var8);
    }
  }

  public static String namedFormat(String str, Map<String, String> substitutions) {
    String key;
    for (Iterator i$ = substitutions.keySet().iterator(); i$.hasNext();
        str = str.replace('$' + key, (CharSequence) substitutions.get(key))) {
      key = (String) i$.next();
    }

    return str;
  }

  public static String namedFormat(String str, Object... nameValuePairs) {
    if (nameValuePairs.length % 2 != 0) {
      throw new InvalidParameterException("You must include one value for each parameter");
    } else {
      HashMap map = new HashMap(nameValuePairs.length / 2);

      for (int i = 0; i < nameValuePairs.length; i += 2) {
        map.put(toString(nameValuePairs[i]), toString(nameValuePairs[i + 1]));
      }

      return namedFormat(str, (Map) map);
    }
  }

  public static boolean notEmpty(Object o) {
    return toString(o).trim().length() != 0;
  }

  public static String toString(InputStream input) {
    StringWriter sw = new StringWriter();
    copy(new InputStreamReader(input), sw);
    return sw.toString();
  }

  public static String toString(Reader input) {
    StringWriter sw = new StringWriter();
    copy(input, sw);
    return sw.toString();
  }

  public static String toString(Object o) {
    return toString(o, "");
  }

  public static String toString(Object o, String def) {
    return o == null ? def : (o instanceof InputStream ? toString((InputStream) o) : (o instanceof Reader ? toString((Reader) o)
        : (o instanceof Object[] ? join(", ", (Object[]) ((Object[]) o))
            : (o instanceof Collection ? join(", ", (Collection) o) : o.toString()))));
  }

  public static String trimSpacesAndLines(String text) {
    if (text != null && !TextUtils.isEmpty(text)) {
      return text.trim().replaceAll("\\n\\n+", "\n\n");
    }
    return "";
  }

  public StringUtils() {
  }
}