package de.plpt.ArgumentParser;

public class ArgumentParserExecutionException extends ArgumentParserException {


    /**
     * Initializes a new ArgumentExecutionException
     *
     * @param message Error Exception Message
     */
    public ArgumentParserExecutionException(String message) {
        super(message);
    }

    /**
     * Initializes a new ArgumentExecutionException
     *
     * @param message Error Exception Message
     * @param cause   Exception which caused this Exception
     */
    public ArgumentParserExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Returns typed cause Exception
     *
     * @param <T> Type of cause Exception
     * @return Typed Cause Exception
     */
    @SuppressWarnings("unchecked")
    public <T extends Exception> T getTypedCause() {
        return (T) this.getCause();
    }
}
