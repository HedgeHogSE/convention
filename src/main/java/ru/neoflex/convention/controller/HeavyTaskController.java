package ru.neoflex.convention.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.neoflex.convention.dto.HeavyTaskResponse;
import ru.neoflex.convention.service.BookService;

@RestController
@RequestMapping
public class HeavyTaskController {

	private static final Logger log = LoggerFactory.getLogger(HeavyTaskController.class);

	private final BookService bookService;

	public HeavyTaskController(BookService bookService) {
		this.bookService = bookService;
	}

	@GetMapping("/heavy-task")
	public HeavyTaskResponse heavyTask(
			@RequestParam(name = "durationMs", defaultValue = "12000") long durationMs,
			@RequestParam(name = "iterations", required = false) Integer iterations) {

		long minIterations = iterations == null ? 0L : iterations.longValue();

		log.trace("heavy-task endpoint request: durationMs={}, iterations(minIterations)={}", durationMs, minIterations);

		BookService.HeavyTaskOutcome outcome = bookService.heavyTaskForDuration(durationMs, minIterations);
		return new HeavyTaskResponse(outcome.iterations(), outcome.result(), outcome.elapsedMs());
	}
}

