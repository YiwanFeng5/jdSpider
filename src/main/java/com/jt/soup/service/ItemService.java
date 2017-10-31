package com.jt.soup.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jt.soup.mapper.ItemDescMapper;
import com.jt.soup.mapper.ItemMapper;
import com.jt.soup.pojo.Item;
import com.jt.soup.pojo.ItemDesc;
import com.jt.soup.util.JDUtil;

//3. todo 实现redis记录下所有分类下链接个数，最终为形成ECharts实时统计图做准备
@Service
public class ItemService extends BaseService<Item>{
	@Autowired
	private ItemMapper itemMapper;
	@Autowired
	private ItemDescMapper itemDescMapper;
	
	@Autowired
	private RedisService redisService;
	private static final String catListName = "ITEM_CAT_URL_LIST";	//保存商品链接的Redis.list集合
	private static final String catListKeyPos = "ITEM_CAT_POS";		//记录抓到哪个分类了
	private static final String itemSetName = "ITEM_URL_SET";		//保存商品链接的Redis.set集合
	
	//初始化分类
	public void saveCatUrl(){
		//获取所有的商品的链接，写入redis
		String itemCatAllUrl = "https://www.jd.com/allSort.aspx";
		//所有分类，写redis
		//必须删除，否则redis中已经存在会报错WRONGTYPE Operation against a key holding the wrong kind of value
		redisService.delList(catListName);	
		redisService.set(catListKeyPos, "0");		//还原，防止之前值存在
		redisService.del(itemSetName);				//分类都重抓，商品链接就需要废除
		
		for(String itemCatUrl : JDUtil.getItemCatLevel3(itemCatAllUrl)){
			redisService.lpush(catListName, itemCatUrl);
		}
	}
	
	//初始化所有商品链接，做一个分类，完成在删除分类，这样实现断点续爬
	public void saveItemUrl(){
		//从redis读取每个分类链接，抓取当前分类下的所有链接
		Long size = redisService.llen(catListName);	//获取队列长度
		//获取列表最后一次的位置标识，可能是异常中断
		Integer pos = Integer.valueOf(redisService.get(catListKeyPos));	
		for(int i=pos;i<size;i++){
			String itemCatUrl = redisService.lindex(catListName, i);
			for(String itemUrl : JDUtil.getItemUrlCatAll(itemCatUrl)){
				redisService.sadd(itemSetName, itemUrl);	//利用redis的set集合排重
			}
			redisService.incr(catListKeyPos);			//做完，记录索引位置，当断点或者网站链接不到，则停止
		}
	}
	
	public void go(){
		//读一个分类，弹出栈一个分类，这样分类也就实现了断点续传，异常后不用重头抓，顶多当前分类部分数据重新抓取
		String curItemUrl = redisService.spop(itemSetName);
		while(StringUtils.isNotEmpty(curItemUrl)){
			saveItem(curItemUrl);
			curItemUrl = redisService.spop(itemSetName);	//获取元素并从set集合中删除
		}
	}
	
	//新增保存
	public void saveItem(String curItemUrl){
		Item item = JDUtil.getItem(curItemUrl);
		itemMapper.insertSelective(item);
		
		ItemDesc itemDesc = JDUtil.getItemDesc(item.getId());
		itemDescMapper.insertSelective(itemDesc);
	}

}
