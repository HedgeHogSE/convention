package ru.neoflex.convention.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.neoflex.convention.exception.BookNotFoundException;
import ru.neoflex.convention.model.Book;
import ru.neoflex.convention.repository.BookRepository;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

	@Mock
	private BookRepository repository;

	@InjectMocks
	private BookService service;

	@Test
	void create_validBook_savesAndReturns() {
		Book input = new Book(null, "Title", "Author", 2020);
		Book saved = new Book(1L, "Title", "Author", 2020);

		when(repository.save(any(Book.class))).thenReturn(saved);

		Book result = service.create(input);
		assertEquals(1L, result.getId());
		verify(repository).save(input);
	}

	@Test
	void create_blankTitle_throwsIllegalArgumentException() {
		Book input = new Book(null, " ", "Author", 2020);

		assertThrows(IllegalArgumentException.class, () -> service.create(input));
		verifyNoInteractions(repository);
	}

	@Test
	void getById_notFound_throwsBookNotFoundException() {
		when(repository.findById(42L)).thenReturn(Optional.empty());

		assertThrows(BookNotFoundException.class, () -> service.getById(42L));
	}

	@Test
	void update_notFound_throwsBookNotFoundException() {
		Book update = new Book(null, "New Title", "New Author", 1999);
		when(repository.findById(5L)).thenReturn(Optional.empty());

		assertThrows(BookNotFoundException.class, () -> service.update(5L, update));
	}

	@Test
	void delete_notFound_throwsBookNotFoundException() {
		when(repository.existsById(10L)).thenReturn(false);

		assertThrows(BookNotFoundException.class, () -> service.delete(10L));
	}

	@Test
	void heavyTask_negativeIterations_throwsIllegalArgumentException() {
		assertThrows(IllegalArgumentException.class, () -> service.heavyTask(-1));
	}

	@Test
	void heavyTask_capsToMaxIterations_andComputesResult() {
		int iterations = BookService.MAX_ITERATIONS + 5;

		long expected = computeHeavyTaskResult(BookService.MAX_ITERATIONS);
		long actual = service.heavyTask(iterations);

		assertEquals(expected, actual);
	}

	@Test
	void heavyTaskForDuration_negativeMinIterations_throwsIllegalArgumentException() {
		assertThrows(IllegalArgumentException.class, () -> service.heavyTaskForDuration(100, -1));
	}

	@Test
	void heavyTaskForDuration_runsAndReturnsOutcome() {
		BookService.HeavyTaskOutcome outcome = service.heavyTaskForDuration(20, 0);
		assertEquals(true, outcome.iterations() >= 0);
		assertEquals(true, outcome.elapsedMs() >= 0);
	}

	@Test
	void findByIdOptional_nullId_returnsEmpty() {
		Optional<Book> result = service.findByIdOptional(null);
		assertEquals(Optional.empty(), result);
		verifyNoInteractions(repository);
	}

	@Test
	void findAll_delegatesToRepository() {
		List<Book> books = Collections.singletonList(new Book(1L, "T", "A", 2000));
		when(repository.findAll()).thenReturn(books);

		List<Book> result = service.findAll();
		assertEquals(books, result);
		verify(repository).findAll();
	}

	private static long computeHeavyTaskResult(int effectiveIterations) {
		long result = 0L;
		for (int i = 0; i < effectiveIterations; i++) {
			result = (result + (long) i) ^ ((long) i * 31L);
		}
		return result;
	}
}

