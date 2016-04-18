package next.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import core.jdbc.DataAccessException;
import next.dao.AnswerDao;
import next.dao.QuestionDao;
import next.model.Answer;
import next.model.Result;
import next.model.User;

@RestController
public class AnswerController {
	private QuestionDao questionDao = QuestionDao.getInstance();
	private AnswerDao answerDao = AnswerDao.getInstance();
	private static final Logger LOGGER = LoggerFactory.getLogger(AnswerController.class);
	
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
