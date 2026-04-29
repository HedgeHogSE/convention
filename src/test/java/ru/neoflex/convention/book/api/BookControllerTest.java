package ru.neoflex.convention.book.api;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import ru.neoflex.convention.book.Book;
import ru.neoflex.convention.book.BookNotFoundException;
import ru.neoflex.convention.book.BookService;

import org.springframework.boot.test.mock.mockito.MockBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookController.class)
@Import(GlobalExceptionHandler.class)
class BookControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private BookService bookService;

	@Test
	void create_returns201() throws Exception {
		Book request = new Book(null, "Title", "Author", 2020);
		Book saved = new Book(1L, "Title", "Author", 2020);

		when(bookService.create(any(Book.class))).thenReturn(saved);

		mockMvc.perform(post("/books")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.title", is("Title")));
	}

	@Test
	void getById_returnsBook() throws Exception {
		when(bookService.getById(1L)).thenReturn(new Book(1L, "T", "A", 2000));

		mockMvc.perform(get("/books/1"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id", is(1)))
				.andExpect(jsonPath("$.author", is("A")));
	}

	@Test
	void findAll_returnsList() throws Exception {
		List<Book> books = List.of(new Book(1L, "T", "A", 2000));
		when(bookService.findAll()).thenReturn(books);

		mockMvc.perform(get("/books"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id", is(1)));
	}

	@Test
	void update_whenNotFound_returns404() throws Exception {
		when(bookService.update(eq(1L), any(Book.class)))
				.thenThrow(new BookNotFoundException("Book not found: id=1"));

		Book request = new Book(null, "New Title", "New Author", 1999);

		mockMvc.perform(put("/books/1")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)))
				.andExpect(jsonPath("$.message", is("Book not found: id=1")));
	}

	@Test
	void delete_whenNotFound_returns404() throws Exception {
		org.mockito.Mockito.doThrow(new BookNotFoundException("Book not found: id=10")).when(bookService).delete(10L);

		mockMvc.perform(delete("/books/10"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status", is(404)));
	}

	@Test
	void delete_whenExists_returns204() throws Exception {
		doNothing().when(bookService).delete(1L);

		mockMvc.perform(delete("/books/1"))
				.andExpect(status().isNoContent());
	}
}

