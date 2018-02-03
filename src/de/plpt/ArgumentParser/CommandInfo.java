package de.plpt.ArgumentParser;

import java.lang.annotation.*;

//region CommandInfo

/**
 * Annotation Interface CommandInfo represents a MethodAnnotation for define Methods as TerminalCommand
 * connected via RegEx.
 */

@Documented
@Target(ElementType.METHOD)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandInfo {

    /**
     * Regex definition of a command.
     * If annotated Method has Parameter, use RegexGroups for them, so a {@link java.util.regex.Matcher} can get Values
     *
     * @return String regex definition
     */
    String command();

}
//endregion

