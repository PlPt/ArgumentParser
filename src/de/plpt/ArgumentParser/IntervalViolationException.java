package de.plpt.ArgumentParser;

/**
 * Represents a CustomException  for Number value Interval violations
 */
public class IntervalViolationException extends Exception {

    //region constructor

    /**
     * Initializes a new IntervalViolationException
     * @param message message of exception
     */
    public IntervalViolationException(String message) {
        super(message);
    }
    //endregion
}
