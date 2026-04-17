package com.example.shoes_store.Controller;


import com.example.shoes_store.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/product")
public class ShopSingleController {

    @Autowired
    private ProductService productService;



}
