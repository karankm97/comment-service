package com.km.commentservice.controller.integration;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.km.commentservice.CommentserviceApplication;
import com.km.commentservice.controller.CommentController;
import com.km.commentservice.controller.ReactionController;
import com.km.commentservice.dao.CommentDAO;
import com.km.commentservice.dao.ReactionDAO;
import com.km.commentservice.dto.CommentReply;
import com.km.commentservice.dto.NestedCommentReply;
import com.km.commentservice.dto.ReactionReply;
import com.km.commentservice.dto.ReactionRequest;
import com.km.commentservice.model.Comment;
import com.km.commentservice.model.Reaction;
import com.km.commentservice.model.ReactionId;
import com.km.commentservice.model.ReactionType;
import com.km.commentservice.service.CommentService;
import com.km.commentservice.service.ReactionService;
import com.km.commentservice.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
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
class ReactionControllerTest {
	@InjectMocks
	private ReactionController reactionController;

	@MockBean
	private CommentDAO commentDAO;

	@MockBean
	private ReactionDAO reactionDAO;

	@Autowired
	private ReactionService reactionService;

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
	void getUsersForReaction() throws Exception {
		when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
				new Comment(1, "test", 0, "1", 0, "km", false)));

		List<Reaction> reactionDbResponse = objectMapper.readValue(
				TestUtils.getFileContents("testing/reaction-dao-response.json"),
				new TypeReference<List<Reaction>>() {});

		when(reactionDAO.findByIdCommentIdAndReactionTypeOrderByUpdatedAtDesc(anyInt(), any(ReactionType.class), any(Pageable.class)))
				.thenReturn(reactionDbResponse);

		MockHttpServletResponse result = mockMvc
				.perform(get("/v1/comment/1/reaction/LIKE/users"))
				.andReturn().getResponse();

		assertEquals(200, result.getStatus());

		ReactionReply reactionReply = objectMapper.readValue(result.getContentAsString(),
				ReactionReply.class);
		assertEquals(3, reactionReply.getUsers().size());
	}

	@Test
	void postReactionToComment() throws Exception {
		String reactionPostRequest = TestUtils.getFileContents("testing/reaction-post-request.json");
		when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
				new Comment(1, "test", 0, "1", 0, "km", false)));
		when(reactionDAO.findById(any(ReactionId.class))).thenReturn(Optional.empty());

		ReactionId reactionId = new ReactionId();
		reactionId.setCommentId(1);
		reactionId.setUser("John Doe");
		Reaction newReaction = new Reaction();
		newReaction.setId(reactionId);
		newReaction.setReactionType(ReactionType.LIKE);
		when(reactionDAO.save(any(Reaction.class))).thenReturn(newReaction);

		MockHttpServletResponse result = mockMvc
				.perform(post("/v1/comment/reaction/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(reactionPostRequest)).andReturn().getResponse();

		assertEquals(200, result.getStatus());

		ReactionRequest actualReply = objectMapper.readValue(result.getContentAsString(), ReactionRequest.class);
		ReactionRequest expectedReply = objectMapper.readValue(reactionPostRequest, ReactionRequest.class);

		assertEquals(expectedReply.getCommentId(), actualReply.getCommentId());
		assertEquals(expectedReply.getUser(), actualReply.getUser());
		assertEquals(expectedReply.getReactionType(), actualReply.getReactionType());
	}

	@Test
	void postReactionValidationError() throws Exception {
		String commentPostRequest = TestUtils.getFileContents("testing/reaction-incorrect-request.json");
		MockHttpServletResponse result = mockMvc
				.perform(post("/v1/comment/reaction/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(commentPostRequest)).andReturn().getResponse();
		assertEquals(400, result.getStatus());
	}

	@Test
	void updateReaction() throws Exception {
		String reactionPatchRequest = TestUtils.getFileContents("testing/reaction-patch-request.json");
		when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
				new Comment(1, "test", 0, "1", 0, "km", false)));

		//different reaction as before
		ReactionId reactionId = new ReactionId();
		reactionId.setCommentId(1);
		reactionId.setUser("John Doe");
		Reaction previousReaction = new Reaction();
		previousReaction.setId(reactionId);
		previousReaction.setReactionType(ReactionType.LIKE);
		when(reactionDAO.findById(any(ReactionId.class))).thenReturn(Optional.of(previousReaction));

		Reaction newReaction = new Reaction();
		newReaction.setId(reactionId);
		newReaction.setReactionType(ReactionType.DISLIKE);
		when(reactionDAO.save(any(Reaction.class))).thenReturn(newReaction);

		MockHttpServletResponse result = mockMvc
				.perform(patch("/v1/comment/reaction/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(reactionPatchRequest)).andReturn().getResponse();

		assertEquals(200, result.getStatus());

		ReactionRequest acutualReply = objectMapper.readValue(result.getContentAsString(), ReactionRequest.class);
		ReactionRequest expectedReply = objectMapper.readValue(reactionPatchRequest, ReactionRequest.class);

		assertEquals(expectedReply.getCommentId(), acutualReply.getCommentId());
		assertEquals(expectedReply.getUser(), acutualReply.getUser());
		assertEquals(expectedReply.getReactionType(), acutualReply.getReactionType());
	}

	@Test
	void updateReactionValidationError() throws Exception {
		String commentPostRequest = TestUtils.getFileContents("testing/reaction-incorrect-request.json");
		MockHttpServletResponse result = mockMvc
				.perform(patch("/v1/comment/reaction/")
						.contentType(MediaType.APPLICATION_JSON)
						.content(commentPostRequest)).andReturn().getResponse();
		assertEquals(400, result.getStatus());
	}

	@Test
	void deleteReaction() throws Exception {
		when(commentDAO.findById(anyInt())).thenReturn(Optional.of(
				new Comment(1, "test", 0, "1", 0, "km", false)));
		ReactionId reactionId = new ReactionId();
		reactionId.setCommentId(1);
		reactionId.setUser("John Doe");
		Reaction reaction = new Reaction();
		reaction.setId(reactionId);
		reaction.setReactionType(ReactionType.DISLIKE);
		when(reactionDAO.findById(any(ReactionId.class))).thenReturn(Optional.of(reaction));
		when(reactionDAO.deleteById(any(ReactionId.class))).thenReturn(1);

		MockHttpServletResponse result = mockMvc
				.perform(delete("/v1/comment/1/reaction?user=John Doe")).andReturn().getResponse();

		assertEquals(200, result.getStatus());
		assertEquals("1", result.getContentAsString());
	}
}
