package next.service;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import java.util.Date;

import next.CannotOperateException;
import next.dao.AnswerDao;
import next.dao.QuestionDao;
import next.model.Answer;
import next.model.Question;
import next.model.User;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;

import com.google.common.collect.Lists;

@RunWith(MockitoJUnitRunner.class)
public class QnaServiceTest {
	@Autowired
    private QuestionDao questionDao;
	@Autowired
    private AnswerDao answerDao;
    
    @Autowired
    private QnaService qnaService;

    @Before
    public void setup() {

    }
    
    @Test(expected = EmptyResultDataAccessException.class)
    public void deleteQuestion_없는_질문() throws Exception {
        when(questionDao.findById(1L)).thenReturn(null);
        qnaService.deleteQuestion(1L, newUser("userId"));
    }
    
    @Test(expected = CannotOperateException.class)
    public void deleteQuestion_다른_사용자() throws Exception {
    	Question question = newQuestion(1L, "javajigi");
        when(questionDao.findById(1L)).thenReturn(question);
        when(answerDao.findAllByQuestionId(1L)).thenReturn(Lists.newArrayList());
        qnaService.deleteQuestion(1L, newUser("sanjigi"));
    }

    @Test
    public void deleteQuestion_같은_사용자_답변없음() throws Exception {
    	Question question = newQuestion(1L, "javajigi");
        when(questionDao.findById(1L)).thenReturn(question);
        when(answerDao.findAllByQuestionId(1L)).thenReturn(Lists.newArrayList());
        qnaService.deleteQuestion(1L, newUser("javajigi"));
        verify(questionDao).delete(1L);
    }
    
    @Test
    public void deleteQuestion_질문_답변_글쓴이_같음() throws Exception {
    	String userId = "javajigi";
		Question question = newQuestion(1L, userId);
        when(questionDao.findById(1L)).thenReturn(question);
        when(answerDao.findAllByQuestionId(1L)).thenReturn(Lists.newArrayList(newAnswer(userId), newAnswer(userId)));
        qnaService.deleteQuestion(1L, newUser(userId));
        verify(questionDao).delete(1L);
    }
    
    @Test(expected = CannotOperateException.class)
    public void deleteQuestion_질문_답변_글쓴이_다름() throws Exception {
    	String userId = "javajigi";
		Question question = newQuestion(1L, userId);
        when(questionDao.findById(1L)).thenReturn(question);
        when(answerDao.findAllByQuestionId(1L)).thenReturn(Lists.newArrayList(newAnswer(userId), newAnswer("sanjigi")));
        qnaService.deleteQuestion(1L, newUser(userId));
    }

    private User newUser(String userId) {
		return new User(userId, "password", "name", "test@sample.com");
	}
    
    private Question newQuestion(long questionId, String userId) {
    	return new Question(questionId, userId, "title", "contents", new Date(), 0);
    }
    
    private Answer newAnswer(String userId) {
		return new Answer(userId, "contents", 3L);
	}
}
