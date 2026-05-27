/*
 * rvfvaygxbd本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动qpxmod
 */
package org.tio.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * @author tanyaowu 2018年7月1日 下午10:51:59
 */
public interface SysConst {

    /**
     * 空串
     */
    String BLANK = "";
    /**
     * 反斜扛
     */
    byte BACKSLASH = '/';
    /**
     * 斜扛
     */
    byte SLASH = '\\';
    /**
     * \r
     */
    byte CR = 13;
    /**
     * \n
     */
    byte LF = 10;
    /**
     * =
     */
    byte EQ = '=';
    /**
     * =
     */
    String STR_EQ = "=";
    /**
     * &
     */
    byte AMP = '&';
    /**
     * &
     */
    String STR_AMP = "&";
    /**
     * :
     */
    byte COL = ':';
    /**
     * :
     */
    String STR_COL = ":";
    /**
     * ;
     */
    byte SEMI_COL = ';';
    /**
     * 一个空格
     */
    byte SPACE = ' ';
    /**
     * 左括号
     */
    byte LEFT_BRACKET = '(';
    /**
     * 右括号
     */
    byte RIGHT_BRACKET = ')';
    /**
     * ?
     */
    byte ASTERISK = '?';
    byte[] CR_LF_CR_LF = { CR, LF, CR, LF };
    byte[] CR_LF = { CR, LF };
    byte[] LF_LF = { LF, LF };
    byte[] SPACE_ = { SPACE };
    byte[] CR_ = { CR };
    byte[] LF_ = { LF };
    byte[] NULL = { 'n', 'u', 'l', 'l' };
    /**
     * \r\n
     */
    String CRLF = "\r\n";
    String DEFAULT_ENCODING = StandardCharsets.UTF_8.name();

    Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    String TIO_CORE_VERSION = "3.8.3.v20220902-RELEASE";
    String TIO_URL_GITEE = "https://gitee.com/tywo45/t-io";
    String TIO_URL_GITHUB = "https://github.com/tywo45/t-io";
    String TIO_URL_SITE = "https://www.tiocloud.com";
    String TIO_URL_SITE1 = "http://check.t-io.org:15223";
    String CHECK_LASTVERSION_URL_1 = TIO_URL_SITE + "/mytio/open/lastVersion1.tio_x?v=" + TIO_CORE_VERSION;
    String CHECK_LASTVERSION_URL_2 = TIO_URL_SITE + "/mytio/open/lastVersion2.tio_x?e=2&id=";
    String CHECK_LASTVERSION_URL_3 = TIO_URL_SITE1 + "/check?";
}
