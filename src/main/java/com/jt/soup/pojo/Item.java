package com.jt.soup.pojo;

import javax.persistence.Id;
import javax.persistence.Table;


@Table(name="tb_item_other")
public class Item extends BasePojo{
	private String cwhere;
	@Id
	private Long id;	//不是自增，有抓取页面的itemId
	private String title;
	private String sellPoint;
	private Long price;
	private Integer num;
	private String barcode;
	private String image;
	private Long cid;
	private Integer status;
	public String getCwhere() {
		return cwhere;
	}
	public void setCwhere(String cwhere) {
		this.cwhere = cwhere;
	}
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSellPoint() {
		return sellPoint;
	}
	public void setSellPoint(String sellPoint) {
		this.sellPoint = sellPoint;
	}
	public Long getPrice() {
		return price;
	}
	public void setPrice(Long price) {
		this.price = price;
	}
	public Integer getNum() {
		return num;
	}
	public void setNum(Integer num) {
		this.num = num;
	}
	public String getBarcode() {
		return barcode;
	}
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public Long getCid() {
		return cid;
	}
	public void setCid(Long cid) {
		this.cid = cid;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	@Override
	public String toString() {
		return "Item [cwhere=" + cwhere + ", id=" + id + ", title=" + title + ", sellPoint=" + sellPoint + ", price="
				+ price + ", num=" + num + ", barcode=" + barcode + ", image=" + image + ", cid=" + cid + ", status="
				+ status + "]";
	}
	
}
