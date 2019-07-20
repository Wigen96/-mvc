package com.wigen.web;

import com.wigen.annotations.Controller;
import com.wigen.annotations.RequestMapping;

/**
 * @Author wwq
 */
@Controller
@RequestMapping("/test")
public class Test {

    @RequestMapping("/map")
    public String test() {
        return "mvc test";
    }
}
