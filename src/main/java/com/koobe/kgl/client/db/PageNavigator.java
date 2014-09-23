/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.koobe.kgl.client.db;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author ThomasTarn
 */
public class PageNavigator implements Serializable {
    private String bookId;
    private int spineIndex = 0;
    private List<SpineInfo> spineInfoList;
    private List<String> spineContentList;
	
    /**
     * @return the bookId
     */
    public String getBookId() {
        return bookId;
    }

    /**
     * @param bookId the bookId to set
     */
    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    /**
     * @return the spineIndex
     */
    public int getSpineIndex() {
        return spineIndex;
    }

    /**
     * @param spineIndex the spineIndex to set
     */
    public void setSpineIndex(int index) {
        this.spineIndex = index;
    }

    /**
     * @return the spineInfoList
     */
    public List<SpineInfo> getSpineInfoList() {
        return spineInfoList;
    }

    /**
     * @param spineInfoList the spineInfoList to set
     */
    public void setSpineInfoList(List<SpineInfo> spineInfoList) {
        this.spineInfoList = spineInfoList;
    }

    /**
     * @return the spineContentList
     */
    public List<String> getSpineContentList() {
        return spineContentList;
    }

    /**
     * @param spineContentList the spineContentList to set
     */
    public void setSpineContentList(List<String> spineContentList) {
        this.spineContentList = spineContentList;
    }
}
