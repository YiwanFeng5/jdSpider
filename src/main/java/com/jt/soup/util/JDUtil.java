package com.jt.soup.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jt.soup.pojo.Item;
import com.jt.soup.pojo.ItemDesc;

//抓取京东网站工具类
public class JDUtil {
	private static final Logger log = Logger.getLogger(JDUtil.class);
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	//抓取所有的三级分类，排除非正常的链接	三级分类：1259，有效分类：1183
	public static List<String> getItemCatLevel3(String itemCatAllUrl){
		try {
			List<String> itemCatLevel3List = new ArrayList<String>();
			
			Elements eles = Jsoup.connect(itemCatAllUrl).get().select(".items .clearfix dd a");
			for(int i=0;i<eles.size();i++){
				String href = eles.get(i).attr("href");
				//有效链接
				if(href.startsWith("//list.jd.com/list.html?cat=")){
					itemCatLevel3List.add("https:"+href);	//抓取到3级分类
					//break;
				}
				
				log.debug((i+1)+"----"+href);
			}
			log.debug("分类总数:" + itemCatLevel3List.size());
			return itemCatLevel3List;
		} catch (Exception e) {
			//爬虫不能直接抛出异常，忽略，写日志，另外程序再来处理这些异常
			log.error("[getItemCatLevel3]"+e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	//获取某个分类总页数，http://list.jd.com/list.html?cat=9987,653,655&page=2
	public static List<String> getListPageUrl(String itemCatUrl){
		List<String> pageList = new ArrayList<String>();
		
		Integer pageNum = getPageNum(itemCatUrl);
		for(int i=1;i<=pageNum;i++){
			String pageUrl = itemCatUrl + "&page="+i;
			log.debug(i+" ------- "+pageUrl);
			pageList.add(pageUrl);
		}
		return pageList;
	}
	
	//获取某个分类的总页数
	public static Integer getPageNum(String itemCatUrl){
		try {
			String pageNum = Jsoup.connect(itemCatUrl).get()
					.select("#J_topPage .fp-text i").get(0).text();
			if(StringUtils.isNotEmpty(pageNum)){
				return Integer.parseInt(pageNum);	//强制转换
			}
		} catch (Exception e) {
			log.error("[getPageNum]"+e.getMessage());
			e.printStackTrace();
		}
		return 0;
	}
	
	//抓取列表某页所有的商品链接
	public static List<String> getItemUrl(String itemCatPageUrl){
		List<String> itemUrlList = new ArrayList<String>();
		try {
			Elements eles = Jsoup.connect(itemCatPageUrl).get()
				.select(".gl-i-wrap")
				.select(".j-sku-item .p-img a");
			for(int i=0;i<eles.size();i++){
				String href = "http:" + eles.get(i).attr("href");
				itemUrlList.add(href);
				log.debug(i+" ------- "+href);
			}
		} catch (Exception e) {
			log.error("[getItemUrl]"+e.getMessage());
			e.printStackTrace();
		}
		return itemUrlList;
	}
	
	//获取某个商品的标题
	public static String getItemTitle(String itemUrl){
		//通过两重选择，必须嵌套（父子），可以通过tag,id,class，多个样式用空格隔开
		
		try {
			Elements eles = Jsoup.connect(itemUrl).get().select(".itemInfo-wrap .sku-name");
			if(null!=eles && eles.size()>0){
				Element ele = eles.get(0);	
				return  ele.text();	//获取这个元素里的文本
			}else{
				Elements eles2 = Jsoup.connect(itemUrl).get().select("#itemInfo #name h1");
				if(null!=eles2 && eles2.size()>0){
					Element ele = eles2.get(0);	
					return ele.text();	//获取这个元素里的文本
				}
			}
		} catch (Exception e) {
			log.equals(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	//获取某个商品卖点
	public static String getSellPoint(String itemId){
		try{
			String url = "http://ad.3.cn/ads/mgets?skuids=AD_"+itemId;
			String json = Jsoup.connect(url).ignoreContentType(true).execute().body();
			JsonNode jsonNode = MAPPER.readTree(json);
			JsonNode sellPontJsonNode = jsonNode.get(0);	//获取数组的第一个元素
			String sellPoint = sellPontJsonNode.get("ad").asText();
			return sellPoint;
		}catch(Exception e){
			log.error(e.getMessage());
		}
		return null;
	}
	
	//获取某个商品的各个信息
	public static Item getItem(String itemUrl){
		Item item = new Item();
		String id = itemUrl.substring(itemUrl.lastIndexOf("/")+1,itemUrl.lastIndexOf("."));
		if(StringUtils.isNotEmpty(id)){
			item.setId(Long.parseLong(id));
			item.setTitle(getItemTitle(itemUrl));
			item.setSellPoint(getSellPoint(id));
			
			item.setCwhere("JD");
			item.setCreated(new Date());
			item.setUpdated(item.getCreated());
		}
		
		log.debug(item);
		return item;
	}
	
	//获取某个商品详细信息
	public static ItemDesc getItemDesc(Long itemId){
		String desc = JDUtil.getDesc(String.valueOf(itemId));
		ItemDesc itemDesc = new ItemDesc();
		itemDesc.setItemId(itemId);
		itemDesc.setItemDesc(desc);
		itemDesc.setCreated(new Date());
		itemDesc.setUpdated(itemDesc.getCreated());
		
		return itemDesc;
	}

	//获取某个商品的图片
	public static String getImage(String itemUrl){
		try {
			String image = "";
			Elements eles = Jsoup.connect(itemUrl).get().select(".lh li img");
			for(Element ele : eles){
				String img = "http:" + ele.attr("src");
				image += img + ",";
			}
			if(image.length()>0){
				image = image.substring(0, image.length()-1);
			}
			return image;
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	//商品详情 jsonp
	public static String getDesc(String itemId){
		String url = "http://d.3.cn/desc/"+itemId;
		String jsonp;
		try {
			jsonp = Jsoup.connect(url).ignoreContentType(true).execute().body();
			String json = jsonp.substring(9, jsonp.length()-1);	//jsonp截串获取它的json内容
			JsonNode jsonNode = MAPPER.readTree(json);
			//从json串中获取商品描述信息
			String desc = jsonNode.get("content").asText();
			return desc;
		} catch (Exception e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	//获取一个分类下的所有的ItemUrl
	public static List<String> getItemUrlCatAll(String itemCatUrl){
		List<String> itemUrlCatAllList = new ArrayList<String>();
		for(String itemCatPageUrl: JDUtil.getListPageUrl(itemCatUrl)){
			for(String itemUrl : JDUtil.getItemUrl(itemCatPageUrl)){
				itemUrlCatAllList.add(itemUrl);
			}
		}
		return itemUrlCatAllList;
	}
	
	public void run(){
		//String itemCatUrl = "https://www.jd.com/allSort.aspx";
		//JDUtil.getItemCatLevel3(itemCatUrl);
		
		//String itemCatUrl = "http://list.jd.com/list.html?cat=9987,653,655";
		//JDUtil.getListPageUrl(itemCatUrl);
		
		//String itemCatPageUrl = "http://list.jd.com/list.html?cat=9987,653,655&page=141";
		//JDUtil.getItemUrl(itemCatPageUrl);
		
		String itemUrl = "https://item.jd.hk/1951570607.html";
		//JDUtil.getItem(itemUrl);
		
		String image = JDUtil.getImage(itemUrl);
		log.debug(image);
	}	
}
