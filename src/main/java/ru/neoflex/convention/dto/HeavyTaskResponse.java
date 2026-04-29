package ru.neoflex.convention.dto;

public class HeavyTaskResponse {

	private long iterations;
	private long result;
	private long elapsedMs;

	public HeavyTaskResponse() {
	}

	public HeavyTaskResponse(long iterations, long result, long elapsedMs) {
		this.iterations = iterations;
		this.result = result;
		this.elapsedMs = elapsedMs;
	}

	public long getIterations() {
		return iterations;
	}

	public void setIterations(long iterations) {
		this.iterations = iterations;
	}

	public long getResult() {
		return result;
	}

	public void setResult(long result) {
		this.result = result;
	}

	public long getElapsedMs() {
		return elapsedMs;
	}

	public void setElapsedMs(long elapsedMs) {
		this.elapsedMs = elapsedMs;
	}
}

