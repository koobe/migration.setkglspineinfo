/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.koobe.kgl.client.db;

import java.io.Serializable;

/**
 *
 * @author ThomasTarn
 */
public class SpineInfo implements Serializable {
    private String idRef;
    private String href;
    private String title;
    private String mediaType;
	
	public String getIdRef() { return idRef; }
	public String getHref() { return href; }
	public String getTitle() { return title; }
	public String getMediaType() { return mediaType; }
	public void setIdRef(String idRef) { this.idRef = idRef; }
	public void setHref(String href) { this.href = href; }
	public void setTitle(String title) { this.title = title; }
	public void setMediaType(String mediaType) { this.mediaType = mediaType; }
}
