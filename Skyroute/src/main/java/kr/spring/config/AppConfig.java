package kr.spring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//자바코드 기반 설정 클래스
@Configuration
public class AppConfig implements WebMvcConfigurer{
	
	//Spring MVC에서 별도의 Controller 클래스 없이 특정 URL 요청을 바로 View로 연결
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/main/resultView").setViewName("thviews/common/resultView");
        registry.addViewController("/main/resultError").setViewName("thviews/common/resultError");
    }
    
}
