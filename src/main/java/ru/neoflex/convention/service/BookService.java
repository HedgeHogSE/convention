package ru.neoflex.convention.service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ru.neoflex.convention.exception.BookNotFoundException;
import ru.neoflex.convention.model.Book;
import ru.neoflex.convention.repository.BookRepository;

@Service
public class BookService {

	private static final Logger log = LoggerFactory.getLogger(BookService.class);

	public static final int MAX_ITERATIONS = 200_000;
	private static final long DEFAULT_HEAVY_TASK_TARGET_MS = 12_000L;
	private static final BCryptPasswordEncoder BCRYPT = new BCryptPasswordEncoder(8);

	private final BookRepository repository;

	public BookService(BookRepository repository) {
		this.repository = repository;
	}

	public record HeavyTaskOutcome(long result, long iterations, long elapsedMs) {
	}

	public Book create(Book book) {
		if (book == null) {
			log.error("Create book request is null");
			throw new IllegalArgumentException("Book must not be null");
		}
		if (isBlank(book.getTitle()) || isBlank(book.getAuthor())) {
			log.debug("Create request validation failed: title='{}', author='{}'", book.getTitle(), book.getAuthor());
			throw new IllegalArgumentException("Title and author must not be blank");
		}

		log.trace("create() request: title='{}', author='{}'", book.getTitle(), book.getAuthor());
		Book saved = repository.save(book);
		log.info("Book created: id={}", saved.getId());
		return saved;
	}

	public List<Book> findAll() {
		return repository.findAll();
	}

	public Optional<Book> findByIdOptional(Long id) {
		if (id == null) {
			log.warn("findByIdOptional called with null id");
			return Optional.empty();
		}
		return repository.findById(id);
	}

	public Book getById(Long id) {
		if (id == null) {
			log.warn("getById called with null id");
			throw new IllegalArgumentException("Book id must not be null");
		}

		return repository.findById(id).orElseThrow(() -> {
			log.warn("Book not found: id={}", id);
			return new BookNotFoundException("Book not found: id=" + id);
		});
	}

	public Book update(Long id, Book update) {
		if (id == null) {
			log.error("Update called with null id");
			throw new IllegalArgumentException("Book id must not be null");
		}
		if (update == null) {
			log.error("Update called with null body");
			throw new IllegalArgumentException("Book must not be null");
		}
		if (isBlank(update.getTitle()) || isBlank(update.getAuthor())) {
			log.debug("Update validation failed: id={}, title='{}', author='{}'", id, update.getTitle(), update.getAuthor());
			throw new IllegalArgumentException("Title and author must not be blank");
		}

		Book existing = repository.findById(id).orElseThrow(() -> {
			log.warn("Cannot update. Book not found: id={}", id);
			return new BookNotFoundException("Book not found: id=" + id);
		});

		existing.setTitle(update.getTitle());
		existing.setAuthor(update.getAuthor());
		existing.setPublishedYear(update.getPublishedYear());

		Book saved = repository.save(existing);
		log.info("Book updated: id={}", saved.getId());
		return saved;
	}

	public void delete(Long id) {
		if (id == null) {
			log.error("Delete called with null id");
			throw new IllegalArgumentException("Book id must not be null");
		}

		if (!repository.existsById(id)) {
			log.warn("Delete requested for non-existing book: id={}", id);
			throw new BookNotFoundException("Book not found: id=" + id);
		}

		repository.deleteById(id);
		log.info("Book deleted: id={}", id);
	}

	public long heavyTask(int iterations) {
		log.trace("heavyTask(): request iterations={}", iterations);

		if (iterations < 0) {
			log.error("heavyTask(): invalid iterations={}", iterations);
			throw new IllegalArgumentException("iterations must be >= 0");
		}

		int effectiveIterations = iterations;
		if (iterations > MAX_ITERATIONS) {
			log.warn("heavyTask(): iterations too large ({}), capping to {}", iterations, MAX_ITERATIONS);
			effectiveIterations = MAX_ITERATIONS;
		}

		log.debug("heavyTask(): effectiveIterations={}", effectiveIterations);

		long result = 0L;
		long startNs = System.nanoTime();
		for (int i = 0; i < effectiveIterations; i++) {
			result = (result + (long) i) ^ ((long) i * 31L);
		}
        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

		log.info("heavyTask finished: iterations={}, effectiveIterations={}, elapsedMs={}, result={}",
				iterations, effectiveIterations, elapsedMs, result);
		return result;
	}

	public HeavyTaskOutcome heavyTaskForDuration(long targetMs, long minIterations) {
		long effectiveTargetMs = targetMs;
		if (effectiveTargetMs <= 0) {
			log.error("heavyTaskForDuration(): invalid targetMs={}", targetMs);
			throw new IllegalArgumentException("targetMs must be > 0");
		}

		if (minIterations < 0) {
			log.error("heavyTaskForDuration(): invalid iterations(minIterations)={}", minIterations);
			throw new IllegalArgumentException("iterations must be >= 0");
		}

		log.trace("heavyTaskForDuration(): request targetMs={}, minIterations={}", effectiveTargetMs, minIterations);

		long result = 0L;
		long startNs = System.nanoTime();
		long durationNs = TimeUnit.MILLISECONDS.toNanos(effectiveTargetMs);
		long elapsedNs = 0L;
		long iterations = 0L;

		while (iterations < minIterations || elapsedNs < durationNs) {
			String hash = BCRYPT.encode("book-heavy-task:" + iterations);
			result ^= (long) hash.hashCode();

			iterations++;
			elapsedNs = System.nanoTime() - startNs;
		}

		long elapsedMs = TimeUnit.NANOSECONDS.toMillis(elapsedNs);
		log.info("heavyTaskForDuration finished: targetMs={}, elapsedMs={}, iterations={}, result={}",
				effectiveTargetMs, elapsedMs, iterations, result);
		return new HeavyTaskOutcome(result, iterations, elapsedMs);
	}

	public HeavyTaskOutcome heavyTaskForDuration(long targetMs) {
		return heavyTaskForDuration(targetMs, 0L);
	}

	public HeavyTaskOutcome heavyTaskForDefaultDuration() {
		return heavyTaskForDuration(DEFAULT_HEAVY_TASK_TARGET_MS, 0L);
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}
}

