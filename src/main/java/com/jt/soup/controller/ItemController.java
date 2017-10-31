package com.jt.soup.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.jt.soup.service.ItemService;

@Controller
public class ItemController {
	@Autowired
	private ItemService itemService;

	//初始化
	@RequestMapping("/index")
	public String index(){
		return "index";
	}
	
	//初始化
	@RequestMapping("/init/cat")
	public String saveCatUrl(Model model){
		itemService.saveCatUrl();
		
		model.addAttribute("catUrlResult", "Init CatUrl successful!");
		return "index";
	}
	
	//初始化
	@RequestMapping("/init/item")
	public String saveItemUrl(Model model){
		itemService.saveItemUrl();
		
		model.addAttribute("itemUrlResult", "Init ItemUrl successful!");
		return "index";
	}
	
	//新增保存
	@RequestMapping("/go")
	public String saveItem(Model model){
		itemService.go();
		
		model.addAttribute("go", "Catch successful!");
		return "index";
	}
}
