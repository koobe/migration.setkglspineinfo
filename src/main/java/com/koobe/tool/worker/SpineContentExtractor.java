package com.koobe.tool.worker;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import nl.siegmann.epublib.domain.Book;
import nl.siegmann.epublib.domain.Resource;
import nl.siegmann.epublib.domain.SpineReference;
import nl.siegmann.epublib.epub.EpubReader;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.CompactXmlSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.koobe.common.crypto.UniqueID;
import com.koobe.kgl.client.db.PageNavigator;
import com.koobe.kgl.client.db.SpineInfo;

public class SpineContentExtractor {
	
	private File tempEpubFile;
	
	private String guid;
	
	private Boolean success;
	
	private String spineJson;
	
	private String spineInfoJson;
	
	private String exMsg;
	
	public SpineContentExtractor(File tempEpubFile, String guid) {
		this.tempEpubFile = tempEpubFile;
		this.guid = guid;
	}
	
	public void start() {
		
		FileInputStream fis = null;
		
		try {
			
			fis = new FileInputStream(tempEpubFile);
			
			EpubReader epubReader = new EpubReader();
			Book book = epubReader.readEpub(fis);
			
			HtmlCleaner cleaner = new HtmlCleaner();
            CleanerProperties props = cleaner.getProperties();
            props.setNamespacesAware(false);
            props.setOmitDoctypeDeclaration(true);
            props.setOmitXmlDeclaration(true);
			
			List<SpineReference> spineRefList = book.getSpine().getSpineReferences();
			List<SpineInfo> spineInfoList = new ArrayList<SpineInfo>();
			List<String> contentList = new ArrayList<String>();
			
			for (SpineReference spineRef : spineRefList) {
				Resource res = spineRef.getResource();
                String s = spineRef.getResourceId();
                
                if (s.compareTo("textspans") == 0) {
                    continue;
                }

                SpineInfo spine = new SpineInfo();
                spine.setIdRef(s);
                spine.setHref(res.getHref());
                spine.setTitle(res.getTitle());
                spine.setMediaType(res.getMediaType().getName());

                spineInfoList.add(spine);
                
                Resource rc = book.getResources().getByHref(spine.getHref());
                
                if (rc != null) {
                	
                	String data = new String(rc.getData(), "UTF-8");

                    // clean html
                    TagNode node = cleaner.clean(data);
                    CompactXmlSerializer serializer = new CompactXmlSerializer(props);
                    data = serializer.getAsString(node);
                    data = data.replaceAll("[\uFEFF-\uFFFF][\t\r\n]", "");
                    node = null;

                    org.jsoup.nodes.Document doc = Jsoup.parse(data);
                    // replace img tag
                    Elements els = doc.getElementsByTag("img");
                    if (els != null) {
                        for (int i = 0; i < els.size(); i++) {
                            org.jsoup.nodes.Element el = els.get(i);
                            String src = el.attr("src");
                            Resource imgRc = book.getResources().getByHref(src.replace("../", ""));
                            if (imgRc != null) {
                                String param = "id="
                                        + imgRc.getId() + "&link="
                                        + imgRc.getHref() + "&b=" + guid;
                                param = UniqueID.encode(param);
                                src = "/GlobalViewer/ImageService.jpg?sg=" + param;
                                el.attr("src", src);
                            }
                        }
                    }

                    // replace link tag
                    els = doc.getElementsByTag("link");
                    if (els != null) {
                        for (int i = 0; i < els.size(); i++) {
                            org.jsoup.nodes.Element el = els.get(i);
                            String src = el.attr("href");
                            String type = el.attr("type");
                            Resource imgRc = book.getResources().getByHref(src.replace("../", ""));
                            if (imgRc != null) {
                                String param = "id="
                                        + imgRc.getId() + "&link="
                                        + imgRc.getHref() + "&b="
                                        + guid
                                        + (type != null ? "&t=" + type : "");
                                param = UniqueID.encode(param);
                                src = "/GlobalViewer/BookResource?sg=" + param;
                                el.attr("href", src);
                            }
                        }
                    }

                    // replace audio tag
                    els = doc.getElementsByTag("source");
                    if (els != null) {
                        List<org.jsoup.nodes.Element> removeList = new ArrayList<org.jsoup.nodes.Element>();
                        for (int i = 0; i < els.size(); i++) {
                            org.jsoup.nodes.Element el = els.get(i);
                            String src = el.attr("src");
                            String type = el.attr("type");
                            Resource imgRc = book.getResources().findResourceByPartialHref(src.replace("../", ""));
                            if (imgRc != null) {
                                String param = "id="
                                        + imgRc.getId() + "&link="
                                        + imgRc.getHref() + "&b="
                                        + guid
                                        + (type != null ? "&t=" + type : "");
                                param = UniqueID.encode(param);
                                src = "/GlobalViewer/BookResource?sg=" + param;
                                        
                                el.attr("src", src);
                            } else {
                                // remove element which the resource cannot be found
                                removeList.add(el);
                            }
                        }

                        for (org.jsoup.nodes.Element el : removeList) {
                            el.remove();
                        }
                    }

                    data = doc.toString();
                    contentList.add(data);
                }
			}
			
			PageNavigator nav = new PageNavigator();
			nav.setBookId(guid);
	        nav.setSpineInfoList(spineInfoList);
	        nav.setSpineContentList(contentList);
	        
	        Gson gson = new Gson();
	        
	        spineInfoJson = gson.toJson(spineInfoList);
	        spineJson = gson.toJson(contentList);
			
	        success = true;
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			exMsg = sw.toString();
			
			success = false;
		} finally {
			try {
				fis.close();
			} catch (Exception e) {}
		}
	}

	public Boolean getSuccess() {
		return success;
	}

	public void setSuccess(Boolean success) {
		this.success = success;
	}

	public String getSpineJson() {
		return spineJson;
	}

	public void setSpineJson(String spineJson) {
		this.spineJson = spineJson;
	}

	public String getSpineInfoJson() {
		return spineInfoJson;
	}

	public void setSpineInfoJson(String spineInfoJson) {
		this.spineInfoJson = spineInfoJson;
	}

	public String getExMsg() {
		return exMsg;
	}

	public void setExMsg(String exMsg) {
		this.exMsg = exMsg;
	}

	
}
