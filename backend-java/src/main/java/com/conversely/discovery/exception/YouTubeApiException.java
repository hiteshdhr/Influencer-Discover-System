package com.conversely.discovery.exception;

/**
 * Thrown when the YouTube Data API returns an error or is unreachable.
 */
public class YouTubeApiException extends RuntimeException {

    public YouTubeApiException(String message) {
        super(message);
    }

    public YouTubeApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
