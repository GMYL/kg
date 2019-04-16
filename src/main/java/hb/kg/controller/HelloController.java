package hb.kg.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController

@RequestMapping("/hello")
public class HelloController {
    @RequestMapping("")

    public String hello() {

           return "helloworld";

    }
}
