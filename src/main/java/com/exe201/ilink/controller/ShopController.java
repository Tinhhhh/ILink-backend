package com.exe201.ilink.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("shop")
@Tag(name = "Shop", description = "Method for shop settings required access token to gain access")
public class ShopController {



}
