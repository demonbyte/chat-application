package com.java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

//@SpringBootApplication 
//@ComponentScan(scanBasePackages = {"com.java"})
@SpringBootApplication(scanBasePackages = "com.java")
public class ChatMain {
	
	public static void main(String[] args)  
	{    
	SpringApplication.run(ChatMain.class, args);    
	}   

}
