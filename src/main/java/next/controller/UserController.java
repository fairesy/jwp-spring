package next.controller;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import next.dao.UserDao;
import next.model.User;

@Controller
@RequestMapping("/users")
public class UserController{
	
	private UserDao userDao = UserDao.getInstance();
	private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class);

	@RequestMapping(value = "", method = RequestMethod.GET)
	public ModelAndView userList(HttpSession session) throws Exception{
		if (!UserSessionUtils.isLogined(session)) {
			return new ModelAndView("/users/loginForm");
//			return "redirect:/users/loginForm";
		}
    	
        ModelAndView mav = new ModelAndView("/user/list.jsp");
        mav.addObject("users", userDao.findAll());
        return mav;
	}
	
	@RequestMapping(value = "/form", method = RequestMethod.GET)
	public String joinForm() throws Exception {
		return "user/form";
	}
	
	@RequestMapping(value = "/create", method = RequestMethod.POST)
	public String join(@RequestParam String userId, @RequestParam String password, 
			@RequestParam String name, @RequestParam String email) throws Exception {
		User user = new User(userId, password, name, email);
        LOGGER.debug("User : {}", user);
        userDao.insert(user);
		return "redirect:/";
	}
	
	@RequestMapping(value = "/loginForm", method = RequestMethod.GET)
	public String loginForm() throws Exception {
		return "user/login";
	} 
	
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public String login(HttpSession session, @RequestParam String userId, @RequestParam String password) throws Exception {
		User user = userDao.findByUserId(userId);
        
        if (user == null) {
            throw new NullPointerException("사용자를 찾을 수 없습니다.");
        }
        
        if (user.matchPassword(password)) {
            session.setAttribute("user", user);
            return "redirect:/";
        } else {
            throw new IllegalStateException("비밀번호가 틀립니다.");
        }
	}
	
	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(HttpSession session) throws Exception {
		session.removeAttribute("user");
        return "redirect:/";
	}
	
	@RequestMapping(value = "/profile", method = RequestMethod.GET)
	public ModelAndView showProfile(@RequestParam String userId) throws Exception {
        ModelAndView mav = new ModelAndView("/user/profile");
        mav.addObject("user", userDao.findByUserId(userId));
        return mav;
	}
	
	@RequestMapping(value = "/updateForm", method = RequestMethod.GET)
	public ModelAndView updateForm(HttpSession session, @RequestParam String userId) throws Exception {
		User user = userDao.findByUserId(userId);

    	if (!UserSessionUtils.isSameUser(session, user)) {
        	throw new IllegalStateException("다른 사용자의 정보를 수정할 수 없습니다.");
        }
    	ModelAndView mav = new ModelAndView("/user/updateForm");
    	mav.addObject("user", user);
    	return mav;
	}
	
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public String update(HttpSession session, @RequestParam String userId, @RequestParam String password, @RequestParam String name, @RequestParam String email){
		User user = userDao.findByUserId(userId);
		
        if (!UserSessionUtils.isSameUser(session, user)) {
        	throw new IllegalStateException("다른 사용자의 정보를 수정할 수 없습니다.");
        }
        
        User updateUser = new User(userId, password, name, email);

        LOGGER.debug("Update User : {}", updateUser);
        user.update(updateUser);
        return "redirect:/";
	}
}