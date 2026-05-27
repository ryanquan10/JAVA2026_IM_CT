/*
 * nqfiplabsahpx本软件由黄庆辉采购自杭州钛特云科技有限公司，黄庆辉需严格遵守合同，不得以任何形式转卖源代码，不得利用本软件从事违法犯罪活动sphrr
 */
package org.tio.webpack.compress.html;

public class HtmlOptions {
    private boolean minifyHtml = true;
    private boolean preventCaching = true;
    private boolean removeComments = true;
    private boolean removeMutliSpaces = true;
    private boolean removeIntertagSpaces = false;
    private boolean removeQuotes = false;
    private boolean simpleDoctype = false;
    private boolean removeScriptAttributes = false;
    private boolean removeStyleAttributes = false;
    private boolean removeLinkAttributes = false;
    private boolean removeFormAttributes = false;
    private boolean removeInputAttributes = false;
    private boolean simpleBooleanAttributes = false;
    private boolean removeJavaScriptProtocol = false;
    private boolean removeHttpProtocol = false;
    private boolean removeHttpsProtocol = false;
    private boolean preserveLineBreaks = false;

    public boolean isMinifyHtml() {
	return minifyHtml;
    }

    public boolean isPreserveLineBreaks() {
	return preserveLineBreaks;
    }

    public boolean isPreventCaching() {
	return preventCaching;
    }

    public boolean isRemoveComments() {
	return removeComments;
    }

    public boolean isRemoveFormAttributes() {
	return removeFormAttributes;
    }

    public boolean isRemoveHttpProtocol() {
	return removeHttpProtocol;
    }

    public boolean isRemoveHttpsProtocol() {
	return removeHttpsProtocol;
    }

    public boolean isRemoveInputAttributes() {
	return removeInputAttributes;
    }

    public boolean isRemoveIntertagSpaces() {
	return removeIntertagSpaces;
    }

    public boolean isRemoveJavaScriptProtocol() {
	return removeJavaScriptProtocol;
    }

    public boolean isRemoveLinkAttributes() {
	return removeLinkAttributes;
    }

    public boolean isRemoveMutliSpaces() {
	return removeMutliSpaces;
    }

    public boolean isRemoveQuotes() {
	return removeQuotes;
    }

    public boolean isRemoveScriptAttributes() {
	return removeScriptAttributes;
    }

    public boolean isRemoveStyleAttributes() {
	return removeStyleAttributes;
    }

    public boolean isSimpleBooleanAttributes() {
	return simpleBooleanAttributes;
    }

    public boolean isSimpleDoctype() {
	return simpleDoctype;
    }

    public void setMinifyHtml(boolean minifyHtml) {
	this.minifyHtml = minifyHtml;
    }

    public void setPreserveLineBreaks(boolean preserveLineBreaks) {
	this.preserveLineBreaks = preserveLineBreaks;
    }

    public void setPreventCaching(boolean preventCaching) {
	this.preventCaching = preventCaching;
    }

    public void setRemoveComments(boolean removeComments) {
	this.removeComments = removeComments;
    }

    public void setRemoveFormAttributes(boolean removeFormAttributes) {
	this.removeFormAttributes = removeFormAttributes;
    }

    public void setRemoveHttpProtocol(boolean removeHttpProtocol) {
	this.removeHttpProtocol = removeHttpProtocol;
    }

    public void setRemoveHttpsProtocol(boolean removeHttpsProtocol) {
	this.removeHttpsProtocol = removeHttpsProtocol;
    }

    public void setRemoveInputAttributes(boolean removeInputAttributes) {
	this.removeInputAttributes = removeInputAttributes;
    }

    public void setRemoveIntertagSpaces(boolean removeIntertagSpaces) {
	this.removeIntertagSpaces = removeIntertagSpaces;
    }

    public void setRemoveJavaScriptProtocol(boolean removeJavaScriptProtocol) {
	this.removeJavaScriptProtocol = removeJavaScriptProtocol;
    }

    public void setRemoveLinkAttributes(boolean removeLinkAttributes) {
	this.removeLinkAttributes = removeLinkAttributes;
    }

    public void setRemoveMutliSpaces(boolean removeMutliSpaces) {
	this.removeMutliSpaces = removeMutliSpaces;
    }

    public void setRemoveQuotes(boolean removeQuotes) {
	this.removeQuotes = removeQuotes;
    }

    public void setRemoveScriptAttributes(boolean removeScriptAttributes) {
	this.removeScriptAttributes = removeScriptAttributes;
    }

    public void setRemoveStyleAttributes(boolean removeStyleAttributes) {
	this.removeStyleAttributes = removeStyleAttributes;
    }

    public void setSimpleBooleanAttributes(boolean simpleBooleanAttributes) {
	this.simpleBooleanAttributes = simpleBooleanAttributes;
    }

    public void setSimpleDoctype(boolean simpleDoctype) {
	this.simpleDoctype = simpleDoctype;
    }
}
