package ru.neoflex.convention.book.api;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.test.mock.mockito.MockBean;

import ru.neoflex.convention.book.BookService;

@WebMvcTest(controllers = HeavyTaskController.class)
@Import(GlobalExceptionHandler.class)
class HeavyTaskControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BookService bookService;

	@Test
	void heavyTask_returnsResult() throws Exception {
		long durationMs = 12000L;
		long minIterations = 10L;
		when(bookService.heavyTaskForDuration(durationMs, minIterations))
				.thenReturn(new BookService.HeavyTaskOutcome(123L, 10L, 12000L));

		mockMvc.perform(get("/heavy-task")
				.param("iterations", "10")
				.param("durationMs", "12000")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.iterations", is(10)))
				.andExpect(jsonPath("$.result", is(123)))
				.andExpect(jsonPath("$.elapsedMs", is(12000)));
	}

	@Test
	void heavyTask_negativeIterations_returns400() throws Exception {
		when(bookService.heavyTaskForDuration(anyLong(), eq(-1L)))
				.thenThrow(new IllegalArgumentException("iterations must be >= 0"));

		mockMvc.perform(get("/heavy-task")
				.param("iterations", "-1")
				.param("durationMs", "12000")
				.accept(MediaType.APPLICATION_JSON))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status", is(400)))
				.andExpect(jsonPath("$.message", is("iterations must be >= 0")));
	}
}

