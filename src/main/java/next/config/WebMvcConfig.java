package next.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

@Configuration
@EnableWebMvc 
@ComponentScan(basePackages = { "next.controller" })
public class WebMvcConfig extends WebMvcConfigurerAdapter {
    private static final int CACHE_PERIOD = 31556926; // one year
    //@EnableWebMvc MVC프레임워크에서 필요한 설정을 해준다. 
    @Bean
    public ViewResolver viewResolver() {
        InternalResourceViewResolver bean = new InternalResourceViewResolver();
        bean.setViewClass(JstlView.class);
        bean.setPrefix("/WEB-INF/jsp/");
        bean.setSuffix(".jsp"); //view네임을 짧게 지정할 수 있다. 
        return bean;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/resources/**") 
                .addResourceLocations("/WEB-INF/static_resources/") // /WEB-INF/static_resources/ 을 /resources/라는 경로에 맵핑 
                .setCachePeriod(CACHE_PERIOD); //정적 자원을 캐시해두는 기간 설정 - 성능 개선에 도움이 된다. 
    }
    //200(from cache), 304 : 서버에 변경되었는지 먼저 물어본다. 서버는 변경이 안됐으면 안됐다는 정보만 줌. 
    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        // Serving static files using the Servlet container's default Servlet.
        configurer.enable();
    }    
}
