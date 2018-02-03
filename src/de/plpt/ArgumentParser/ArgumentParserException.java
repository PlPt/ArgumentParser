package de.plpt.ArgumentParser;

/**
 * ArgumentParserException for all Exceptions around ArgumentParser
 */
public class ArgumentParserException extends Exception {

    //region constructors

    /**
     * Initializes a new ArgumentParser Eception
     *
     * @param message message of exception
     */
    public ArgumentParserException(String message) {
        super(message);
    }

    /**
     * Initializes a new ArgumentParserException with it's cause
     *
     * @param message message of Exception
     * @param cause   cause of Exception
     */
    public ArgumentParserException(String message, Throwable cause) {
        this(message);
        this.initCause(cause);
    }
    //endregion

}
