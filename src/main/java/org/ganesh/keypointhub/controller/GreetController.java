package org.ganesh.keypointhub.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GreetController {

    @RequestMapping("/")
    public String greet(){
        return "";
    }
    @RequestMapping("/webpage")
    public String webpage(){
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>My Basic Page</title>
                    <style>
                        #h1{
                        color:red;
                        }
                    </style>
                </head>
                <body>
                    <h1 id="h1">Hello, World!</h1>
                    <p>This is my first basic HTML page through Spring 🧠🔥.</p>
                </body>
                </html>
                """;
    }
}
