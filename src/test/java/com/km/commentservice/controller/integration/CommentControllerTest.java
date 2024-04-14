package com.km.commentservice.controller.integration;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.km.commentservice.CommentserviceApplication;
import com.km.commentservice.controller.CommentController;
import com.km.commentservice.dao.CommentDAO;
import com.km.commentservice.dto.CommentPostRequest;
import com.km.commentservice.dto.CommentReply;
import com.km.commentservice.dto.NestedCommentReply;
import com.km.commentservice.model.Comment;
import com.km.commentservice.service.CommentService;
import com.km.commentservice.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@ActiveProfiles(value = "test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = CommentserviceApplication.class)
class CommentControllerTest {
	@InjectMocks
	private CommentController commentController;

	@MockBean
	private CommentDAO commentDAO;

	@Autowired
	private CommentService commentService;

	@Autowired
	private WebApplicationContext webApplicationContext;
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	void getSingleComment() throws Exception {
		when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
				new Comment(1, "test", 0, "1", 0, "km", false)));

		MockHttpServletResponse result = mockMvc.perform(get("/v1/comment/1")).andReturn().getResponse();

		assertEquals(200, result.getStatus());
	}

	@Test
	void getFullTreeWithMaxDepth() throws Exception {
		List<CommentReply> commentDbResponse = objectMapper.readValue(
				TestUtils.getFileContents("testing/comment-dao-flat-response.json"),
				new TypeReference<List<CommentReply>>() {});

		when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
				new Comment(1, "test", 0, "1", 0, "km", false)));

		when(commentDAO.getCommentTreeById(anyString(), anyInt(), anyInt())).thenReturn(commentDbResponse);

		MockHttpServletResponse result = mockMvc
				.perform(get("/v1/comment/1/fulltree?maxDepth=2"))
				.andReturn().getResponse();

		assertEquals(200, result.getStatus());

		NestedCommentReply expectedNestedCommentReply = objectMapper.readValue(
				TestUtils.getFileContents("testing/comment-nested-response.json"),
				NestedCommentReply.class);

		NestedCommentReply actualNestedCommentReply = objectMapper.readValue(result.getContentAsString(),
				NestedCommentReply.class);

		assertTrue(TestUtils.assertNestedReplyEqual(expectedNestedCommentReply, actualNestedCommentReply));
	}

	@Test
	void getCommentsAtLevelPaginated() throws Exception {
		List<CommentReply> commentDbResponse = objectMapper.readValue(
				TestUtils.getFileContents("testing/comment-dao-flat-response-single-level.json"),
				new TypeReference<List<CommentReply>>() {});

		when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
				new Comment(1, "test", 0, "1", 0, "km", false)));

		when(commentDAO.getCommentsAtLevel(anyString(), anyInt(), any())).thenReturn(commentDbResponse);

		MockHttpServletResponse result = mockMvc
				.perform(get("/v1/comment/1/nextlevel?pageNo=0&pageSize=5"))
				.andReturn().getResponse();

		assertEquals(200, result.getStatus());

		NestedCommentReply expectedNestedCommentReply = objectMapper.readValue(
				TestUtils.getFileContents("testing/comment-single-level-response.json"),
				NestedCommentReply.class);

		NestedCommentReply actualNestedCommentReply = objectMapper.readValue(result.getContentAsString(),
				NestedCommentReply.class);

		assertTrue(TestUtils.assertNestedReplyEqual(expectedNestedCommentReply, actualNestedCommentReply));
	}

	@Test
	void postComment() throws Exception {
		String commentPostRequest = TestUtils.getFileContents("testing/comment-post-request.json");
		when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
				new Comment(1, "test", 0, "1", 0, "km", false)));
		when(commentDAO.save(any(Comment.class))).thenAnswer(i -> i.getArguments()[0]);

		MockHttpServletResponse result = mockMvc
				.perform(post("/v1/comment/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(commentPostRequest)).andReturn().getResponse();

		CommentReply actualReply = objectMapper.readValue(result.getContentAsString(), CommentReply.class);
		CommentReply expectedReply = objectMapper.readValue(commentPostRequest, CommentReply.class);

		assertEquals(200, result.getStatus());
		assertEquals(expectedReply.getBody(), actualReply.getBody());
		assertEquals(expectedReply.getParentId(), actualReply.getParentId());
		assertEquals(expectedReply.getUser(),actualReply.getUser());
	}

	@Test
	void postCommentValidationFailure() throws Exception {
		String commentPostRequest = TestUtils.getFileContents("testing/comment-incorrect-request.json");
		MockHttpServletResponse result = mockMvc
				.perform(post("/v1/comment/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(commentPostRequest)).andReturn().getResponse();
		assertEquals(400, result.getStatus());
	}

	@Test
	void updateComment() throws Exception {
		String commentPostRequest = TestUtils.getFileContents("testing/comment-put-request.json");
		when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
				new Comment(1, "test", 0, "1", 0, "John Doe", false)));
		when(commentDAO.save(any(Comment.class))).thenAnswer(i -> i.getArguments()[0]);

		MockHttpServletResponse result = mockMvc
				.perform(put("/v1/comment/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(commentPostRequest)).andReturn().getResponse();

		CommentReply actualReply = objectMapper.readValue(result.getContentAsString(), CommentReply.class);
		CommentReply expectedReply = objectMapper.readValue(commentPostRequest, CommentReply.class);

		assertEquals(200, result.getStatus());
		assertEquals(expectedReply.getBody(), actualReply.getBody());
		assertEquals(expectedReply.getUser(),actualReply.getUser());
	}

	@Test
	void updateCommentValidationFailure() throws Exception {
		String commentPostRequest = TestUtils.getFileContents("testing/comment-incorrect-request.json");
		MockHttpServletResponse result = mockMvc
				.perform(put("/v1/comment/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(commentPostRequest)).andReturn().getResponse();
		assertEquals(400, result.getStatus());
	}

	@Test
	void deleteComment() throws Exception {
		String user = "TestUser";

		Comment existingComment = new Comment();
		existingComment.setUser(user);
		when(commentDAO.findById(anyInt())).thenReturn(Optional.of(existingComment));
		when(commentDAO.save(any(Comment.class))).thenAnswer(i -> i.getArguments()[0]);

		MockHttpServletResponse result = mockMvc
				.perform(delete("/v1/comment/1?user=TestUser")).andReturn().getResponse();

		assertEquals(200, result.getStatus());

		CommentReply actualReply = objectMapper.readValue(result.getContentAsString(), CommentReply.class);
		assertTrue(actualReply.getIsDeleted());
	}

}