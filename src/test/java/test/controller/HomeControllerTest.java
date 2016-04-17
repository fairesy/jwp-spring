package test.controller;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import next.controller.HomeController;
import test.config.TestMvcConfig;

public class HomeControllerTest {
	private AnnotationConfigApplicationContext ac;
	@Before
	public void setup() {
	ac = new AnnotationConfigApplicationContext(TestMvcConfig.class); 
	}
	
    @Test
    public void equalsBean() throws Exception {
    	HomeController hc1 = ac.getBean(HomeController.class);
    	HomeController hc2 = ac.getBean(HomeController.class);
    	System.out.println("hc1 : " + hc1);
    	System.out.println("hc2 : " + hc2);
    	assertTrue(hc1 == hc2);
    }
    
    @After
    public void teardown() {
        ac.close();
    }
}
