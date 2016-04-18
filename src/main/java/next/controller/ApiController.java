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
import next.CannotDeleteException;
import next.dao.AnswerDao;
import next.dao.QuestionDao;
import next.model.Answer;
import next.model.Result;
import next.model.User;
import next.service.QnaService;

@RestController
@RequestMapping("/api/qna")
public class ApiController {
	private QuestionDao questionDao = QuestionDao.getInstance();
	private AnswerDao answerDao = AnswerDao.getInstance();
	private QnaService qnaService = QnaService.getInstance();
	private static final Logger LOGGER = LoggerFactory.getLogger(ApiController.class);
	
	@RequestMapping(value="/deleteAnswer", method = RequestMethod.POST)
	public Map<String, Object> addAnswer(HttpSession session, @RequestParam String answerId) throws Exception{
		Map<String, Object> result = new HashMap<String, Object>();
		if (!UserSessionUtils.isLogined(session)) {
			result.put("result", Result.fail("Login is required"));
			return result;
		}
		try {
			answerDao.delete(Long.parseLong(answerId));
			result.put("result", Result.ok());
		} catch (DataAccessException e) {
			result.put("result", Result.fail(e.getMessage()));
		}
		return result;
	}
	
	@RequestMapping(value="/addAnswer", method = RequestMethod.POST)
	public Map<String, Object> deleteAnswer(HttpSession session, @RequestParam String contents, @RequestParam String questionId) throws Exception{
		Map<String, Object> result = new HashMap<String, Object>();
		
		if (!UserSessionUtils.isLogined(session)) {
			result.put("result", Result.fail("Login is required"));
			return result;
		}
    	
    	User user = UserSessionUtils.getUserFromSession(session);
		Answer answer = new Answer(user.getUserId(), 
				contents, 
				Long.parseLong(questionId));
		LOGGER.debug("answer : {}", answer);
		
		Answer savedAnswer = answerDao.insert(answer);
		questionDao.updateCountOfAnswer(savedAnswer.getQuestionId());
		result.put("answer", savedAnswer);
		result.put("result", Result.ok());
		return result;
	}
	
	@RequestMapping(value="/list", method = RequestMethod.GET)
	public Map<String, Object> getQuestionList() throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("questions", questionDao.findAll());
		return result;
	}
	
	@RequestMapping(value="/deleteQuestion", method = RequestMethod.POST)
	public Map<String, Object> deleteQuestion(HttpSession session, @RequestParam String questionId) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		if (!UserSessionUtils.isLogined(session)) {
			result.put("result", Result.fail("Login is required"));
			return result;
		}
		
		try {
			qnaService.deleteQuestion(Long.parseLong(questionId), UserSessionUtils.getUserFromSession(session));
			result.put("result", Result.ok());
			return result;
		} catch (CannotDeleteException e) {
			result.put("result", Result.fail(e.getMessage()));
			return result;
		}
	}
}

