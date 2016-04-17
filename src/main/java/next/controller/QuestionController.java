package next.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import core.jdbc.DataAccessException;
import next.CannotDeleteException;
import next.dao.AnswerDao;
import next.dao.QuestionDao;
import next.model.Answer;
import next.model.Question;
import next.model.Result;
import next.model.User;
import next.service.QnaService;

@Controller
public class QuestionController {
	private QuestionDao questionDao = QuestionDao.getInstance();
	private AnswerDao answerDao = AnswerDao.getInstance();
	private QnaService qnaService = QnaService.getInstance();
	
	private static final Logger LOGGER = LoggerFactory.getLogger(QuestionController.class);
	
	@RequestMapping(value= "/qna/show", method = RequestMethod.GET)
	public ModelAndView showQuestion(@RequestParam String questionId) throws Exception{
		long id = Long.parseLong(questionId);
		
        Question question = questionDao.findById(id);
        List<Answer> answers = answerDao.findAllByQuestionId(id);
        
        ModelAndView mav = new ModelAndView("/qna/show");
        mav.addObject("question", question);
        mav.addObject("answers", answers);
        return mav;
	}
	
	@RequestMapping(value="/qna/form", method = RequestMethod.GET)
	public String createQuestionForm(HttpSession session) throws Exception{
		if (!UserSessionUtils.isLogined(session)) {
			return "redirect:/users/loginForm";
		}
		return "/qna/form";
	}
	
	@RequestMapping(value="/qna/create", method = RequestMethod.POST)
	public String createQuestion(HttpSession session, @RequestParam String title, @RequestParam String contents) throws Exception{
		if (!UserSessionUtils.isLogined(session)) {
			return "redirect:/users/loginForm";
		}
		User user = UserSessionUtils.getUserFromSession(session);
    	Question question = new Question(user.getUserId(), title, contents);
    	questionDao.insert(question);
		return "redirect:/";		
	}
	
	@RequestMapping(value="/qna/updateForm", method = RequestMethod.GET)
	public ModelAndView updateQuestionForm(HttpSession session, @RequestParam String questionId) throws Exception{
		/*
		@RequestParam을 쓸 경우, 값이 넘어오지 않으면 에러가 발생한다. 
		방지하기 위해 required값을 false로 하거나, 
		defaultValue를 설정해준다. 
		*/
		if (!UserSessionUtils.isLogined(session)) {
			return new ModelAndView("user/login");
		}
		
		long qId = Long.parseLong(questionId);
		Question question = questionDao.findById(qId);
		if (!question.isSameUser(UserSessionUtils.getUserFromSession(session))) {
			throw new IllegalStateException("다른 사용자가 쓴 글을 수정할 수 없습니다.");
			//에러를 던지고 나서 다른 페이지로 이동하려면...??
		}
		ModelAndView mav = new ModelAndView("/qna/update");
		mav.addObject("question", question);
		return mav;
		
	}
	
	@RequestMapping(value="/qna/update", method = RequestMethod.POST)
	public String updateQuestion(HttpSession session, @RequestParam String questionId, @RequestParam String title, @RequestParam String contents) throws Exception{
		if (!UserSessionUtils.isLogined(session)) {
			return "redirect:/users/loginForm";
		}
		
		long qId = Long.parseLong(questionId);
		Question question = questionDao.findById(qId);
		if (!question.isSameUser(UserSessionUtils.getUserFromSession(session))) {
			throw new IllegalStateException("다른 사용자가 쓴 글을 수정할 수 없습니다.");
		}
		
		Question newQuestion = new Question(question.getWriter(), title, contents);
		question.update(newQuestion);
		questionDao.update(question);
		return "redirect:/";		
	}
	
	@RequestMapping(value="/qna/delete", method = RequestMethod.POST)
	public ModelAndView deleteQuestion(HttpSession session, @RequestParam String questionId) throws Exception{
		if (!UserSessionUtils.isLogined(session)) {
			return new ModelAndView("/users/login");
		}
		long qId = Long.parseLong(questionId);
		try {
			qnaService.deleteQuestion(qId, UserSessionUtils.getUserFromSession(session));
			return new ModelAndView("redirect:/");
		} catch (CannotDeleteException e) {
			return new ModelAndView("/qna/show")
					.addObject("question", qnaService.findById(qId))
					.addObject("answers", qnaService.findAllByQuestionId(qId))
					.addObject("errorMessage", e.getMessage());
		}
		
	}
	
	@RequestMapping(value="/api/qna/deleteQuestion", method = RequestMethod.POST)
	public ModelAndView apiDeleteQuestion(HttpSession session, @RequestParam String id) throws Exception{
		if (!UserSessionUtils.isLogined(session)) {
			return new ModelAndView().addObject("result", Result.fail("Login is required"));
		}
		
		long questionId = Long.parseLong(id);
		try {
			qnaService.deleteQuestion(questionId, UserSessionUtils.getUserFromSession(session));
			return new ModelAndView().addObject("result", Result.ok());
		} catch (CannotDeleteException e) {
			return new ModelAndView().addObject("result", Result.fail(e.getMessage()));
		}
	}
	
	@RequestMapping(value="/api/qna/list", method = RequestMethod.GET)
	public ModelAndView apiQuestionList() throws Exception{
		return null;
		
	}
	
	@RequestMapping(value="/api/qna/deleteAnswer", method = RequestMethod.GET)
	public ModelAndView addAnswer(HttpSession session, @RequestParam String answerId) throws Exception{
		if (!UserSessionUtils.isLogined(session)) {
			return new ModelAndView().addObject("result", Result.fail("Login is required"));
		}
		Long aId = Long.parseLong(answerId);
        
		ModelAndView mav = new ModelAndView();
		try {
			answerDao.delete(aId);
			mav.addObject("result", Result.ok());
		} catch (DataAccessException e) {
			mav.addObject("result", Result.fail(e.getMessage()));
		}
		return mav;
	}
	
	@RequestMapping(value="/api/qna/addAnswer", method = RequestMethod.POST)
	public ModelAndView deleteAnswer(HttpSession session, @RequestParam String contents, @RequestParam String questionId) throws Exception{
		LOGGER.debug("before login!!");
		if (!UserSessionUtils.isLogined(session)) {
			Map<String, Result> resultMap = new HashMap<String, Result>();
			resultMap.put("result", Result.fail("Login is required"));
			return new ModelAndView("jsonView", resultMap);
		}
    	User user = UserSessionUtils.getUserFromSession(session);
		Answer answer = new Answer(user.getUserId(), contents, Long.parseLong(questionId));
		LOGGER.debug("answer : {}", answer);
		
		Answer savedAnswer = answerDao.insert(answer);
		questionDao.updateCountOfAnswer(savedAnswer.getQuestionId());
		ModelAndView mav = new ModelAndView();
		mav.addObject("answer", savedAnswer);
		mav.addObject("result", Result.ok());
		mav.setViewName("jsonView");
		return mav;
	}
}
