package de.plpt.ArgumentParser;

//region Imports

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//endregion

/**
 * @author Pascal Petzoldt
 * <p>
 * Class for parsing TerminalInput Arguments and call defined Method in a Object with it's parameters
 */
public class ArgumentParser {

    //region varDef
    private Class<?> executableType;
    private Object executableObject;
    private boolean showHelp = false;
    //endregion

    //region constructor

    /**
     * Initializes a new ArgumentParser Object
     *
     * @param commandDefinitionObject Object instance which contains public methods with {@link CommandInfo} Annotation
     *                                for Regex Command definition
     */
    public ArgumentParser(Object commandDefinitionObject) {
        this.executableType = commandDefinitionObject.getClass();
        this.executableObject = commandDefinitionObject;
    }

    /**
     * Initializes a new ArgumentParser Object
     *
     * @param commandDefinitionObject Object instance which contains public methods with {@link CommandInfo} Annotation
     *                                for Regex Command definition
     * @param showHelp                Indicates whether a help over all available commands is shown on unknown command
     */
    public ArgumentParser(Object commandDefinitionObject, boolean showHelp) {
        this(commandDefinitionObject);
        this.showHelp = showHelp;
    }
    //endregion

    //region Methods

    //region parse

    /**
     * Parses a given command and looks for a matching regex definition in executableObject.
     * When a regex method definition matches command string, the method is executed with regex group parameters
     * which were parsed to strongly typed values if possible.
     * Parameters of defined Methods can only be primitive values or primitive array in Object wrapper array type
     *
     * @param command InputCommand from Terminal
     * @param <T>     Return type of matching Method defined in executable Object.
     * @return Result value (return type) of matching Method in executableObject. In worst case use as Object an call
     * Object#toString() on it.
     * @throws ArgumentParserException    Parser exception raised when there are problems
     *                                    parsing string and executing Methods.
     * @throws IntervalViolationException Raised when a given Integer interval is violated
     */
    @SuppressWarnings("unchecked")
    public <T> T parse(String command) throws ArgumentParserException, IntervalViolationException {

        Method meth = getMatchingMethod(command);

        if (meth == null) {

            Method mm = getStartsWithMethod(command);

            if (mm != null) {
                String regex = mm.getAnnotation(CommandInfo.class).command();
                throw new IllegalArgumentException(
                        String.format("Command '%s' does not match regex '%s'", command, regex));
            }
            String uncd = "Unknown Command";
            if (showHelp) {
                uncd += "\n" + getHelpString();
            }
            throw new ArgumentParserException(uncd);
        }

        CommandInfo anno = meth.getAnnotation(CommandInfo.class);
        String annoCommand = anno.command();
        Pattern pattern = Pattern.compile(annoCommand);
        Matcher matcher = pattern.matcher(command);
        if (!matcher.matches())
            throw new ArgumentParserException(
                    String.format("Command '%s' does not match pattern '%s'", command, annoCommand));

        Object[] values = new Object[meth.getParameterCount()];
        int indexOffset = 0;
        for (int i = 0; i < meth.getParameterCount(); i++) {

            Class<?> type = meth.getParameterTypes()[i];
            Annotation[] paramAnno = meth.getParameterAnnotations()[i];

            if (!type.isArray()) {
                values[i] = processNormalParameter(matcher, i + indexOffset, type, paramAnno);
            } else if ((paramAnno.length > 0)) {
                Object[] array = processArrayParameter(matcher, i + indexOffset, type, paramAnno[0]);
                values[i] = array;
                indexOffset += array.length - 1;
            } else {
                throw new ArgumentParserException("Given type is an array but no array lenght is defined!");
            }
        }

        try {
            return (T) (meth.invoke(executableObject, values));
        } catch (IllegalAccessException e) {
            throw new ArgumentParserException("Illegal Access on executable object: " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            if (e.getCause() == null) {
                throw new ArgumentParserException(
                        String.format("Method '%s' cannot be invoked: %s", meth.getName(), e.getMessage()), e);
            } else {
                throw new ArgumentParserExecutionException("There is an error inside invoked Method", e.getCause());
            }
        }


    }
    //endregion

    //region  processArrayParameter

    /**
     * Process array parameters of Method definition.
     * Preturn strongly typed primitive wrapper Object array
     *
     * @param matcher    Matcher to match given regex groups
     * @param i          Index of Matcher group
     * @param type       Type of array
     * @param annotation Parameter Annotation for additional array information
     * @return strongly typed pared object array
     * @throws ArgumentParserException    is thrown when there is an error parsing values
     * @throws IntervalViolationException is thrown when a given number is not in defined interval
     */
    private Object[] processArrayParameter(Matcher matcher, int i, Class<?> type, Annotation annotation)
            throws ArgumentParserException, IntervalViolationException {
        ParameterInfo parameterInfo = (ParameterInfo) annotation;
        Class<?> arrayType = type.getComponentType();
        if (arrayType.isPrimitive()) {
            throw new ArgumentParserException(
                    String.format("Array type '%s' is primitive. Please use object wrapper class instead."
                            , arrayType.getName()));
        }
        Object[] array = (Object[]) Array.newInstance(arrayType, parameterInfo.arrayLenght());

        for (int j = 0; j < array.length; j++) {
            String paramString = matcher.group(i + j + 1);
            try {
                Object parsedValue = parseValue(paramString, arrayType);
                if (parsedValue instanceof Number && ((int) parsedValue > parameterInfo.maxValue()
                        || (int) parsedValue < parameterInfo.minValue())) {
                    throw new IntervalViolationException(
                            String.format("Parameter[%s]'s value '%s' is not Element of interval [%s,%s]"
                                    , i, parsedValue, parameterInfo.minValue(), parameterInfo.maxValue()));
                }
                array[j] = parsedValue;
            } catch (NumberFormatException nfe) {
                NumberFormatException nfex = new NumberFormatException(
                        String.format("Parameter[%s] with value '%s' can't be parsed to specified type %s"
                                , i, paramString, type.getName()));
                nfex.initCause(nfe);
                throw nfex;
            }
        }
        return array;
    }
    //endregion

    //region processNormalParameter

    /**
     * Process normal primitive type parameters and cast them into it's required type
     * It Parameter type as an ParameterInfo annotation, this annotation will be applied on the given value
     *
     * @param matcher   Matcher to match input string to regex groups
     * @param index     GroupIndex of matcher
     * @param type      mathod defined parameter type
     * @param paramAnno Parameter Annotations
     * @return strongly typed primitive type value with evaluated annotations
     * @throws ArgumentParserException    is thrown when tere is an Error while converting value to it's primitive type
     * @throws IntervalViolationException is thrown when a given number is not in the given interval
     */
    private Object processNormalParameter(Matcher matcher, int index, Class<?> type, Annotation[] paramAnno)
            throws ArgumentParserException, IntervalViolationException {
        Object parsedValue;
        String paramString = matcher.group(index + 1);
        try {
            parsedValue = parseValue(paramString, type);
        } catch (NumberFormatException nfe) {
            NumberFormatException nfex = new NumberFormatException(
                    String.format("Parameter[%s] with value '%s' can't be parsed to specified type %s"
                            , index, paramString, type.getName()));
            nfex.initCause(nfe);
            throw nfex;
        }


        if (paramAnno.length > 0) {
            ParameterInfo parameterInfo = (ParameterInfo) paramAnno[0];
            if (parsedValue instanceof Number && ((int) parsedValue > parameterInfo.maxValue()
                    || (int) parsedValue < parameterInfo.minValue())) {
                throw new IntervalViolationException(
                        String.format("Parameter[%s]'s value '%s' is not Element of interval [%s,%s]"
                                , index, parsedValue, parameterInfo.minValue(), parameterInfo.maxValue()));
            }
        }
        return parsedValue;
    }
    //endregion

    //region parseValue

    /**
     * Parses a value from given String into expected (primitive) type
     *
     * @param paramString String representation of parameter to Parse
     * @param type        Type declaration to transform String
     * @return Object holding strongly typed parsed value
     */
    private Object parseValue(String paramString, Class<?> type) throws ArgumentParserException {
        Object parsedValue = null;
        if (type == int.class || type == Integer.class) {
            parsedValue = Integer.parseInt(paramString);
        } else if (type == long.class || type == Long.class) {
            parsedValue = Long.parseLong(paramString);
        } else if (type == boolean.class || type == Boolean.class) {

            int value = Integer.parseInt(paramString);
            if (value != 1 && value != 0) {
                throw new ArgumentParserException(String.format("Integer '%s' cannot be converted to boolean", value));
            }
            parsedValue = (boolean) (Boolean.valueOf(paramString) || value == 1);
        } else if (type == short.class || type == Short.class) {
            parsedValue = Short.parseShort(paramString);
        } else if (type == byte.class || type == Byte.class) {
            parsedValue = Byte.parseByte(paramString);
        } else if (type == String.class) {
            parsedValue = paramString;
        } else if (type == double.class || type == Double.class) {
            parsedValue = Double.parseDouble(paramString);
        } else if (type == float.class || type == Float.class) {
            parsedValue = Float.parseFloat(paramString);
        }


        return parsedValue;
    }
    //endregion

    //region getCommandMethods

    /**
     * Looks for Methods which are annotated with {@link CommandInfo}
     *
     * @return List of Methods defined as Command
     */
    private List<Method> getCommandMethods() {
        List<Method> commandMethods = new ArrayList<Method>();
        for (java.lang.reflect.Method m : executableType.getDeclaredMethods()) {

            if (m.isAnnotationPresent(CommandInfo.class)) {
                commandMethods.add(m);
            }
        }

        commandMethods.sort(new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                return o1.getAnnotation(CommandInfo.class).command().compareTo(o2.getAnnotation(CommandInfo.class).command());
            }
        });

        return commandMethods;
    }
    //endregion

    //region getMatchingMethod

    /**
     * Looks for a Method Definition where it's Regex pattern matches to the inputCommand
     *
     * @param inputCommand Command from Terminal
     * @return decaring Method of executableObject which is matching with current command
     */
    private Method getMatchingMethod(String inputCommand) {
        List<Method> commandMethods = getCommandMethods();
        for (Method m : commandMethods) {

            CommandInfo anno = m.getAnnotation(CommandInfo.class);
            String annoCommand = anno.command();
            Pattern pattern = Pattern.compile(annoCommand);
            Matcher matcher = pattern.matcher(inputCommand);

            if (matcher.matches()) return m;

        }
        return null;
    }
    //endregion

    //region getStartsWithMethod

    /**
     * Looks for a Method definition which starts with the inputCommand
     * Don't use for matching purposes, it's only for a error message, when no matching Method is found.
     *
     * @param inputCommand Command from CommandLine
     * @return Method whose regex definition starts with Command
     */
    private Method getStartsWithMethod(String inputCommand) {
        List<Method> commandMethods = getCommandMethods();
        for (Method m : commandMethods) {

            CommandInfo anno = m.getAnnotation(CommandInfo.class);
            String annoCommand = anno.command();
            if ((inputCommand.contains(" ") && annoCommand.contains(inputCommand.split(" ")[0]))
                    || annoCommand.contains(inputCommand))
                return m;

        }
        return null;
    }
    //endregion

    //region getHelpString

    /**
     * Builds an helper string with all available command Methods
     *
     * @return String with all commands
     */
    public String getHelpString() {

        StringBuilder builder = new StringBuilder();
        builder.append("This program contains the following commands: \n");
        List<Method> commandMethods = getCommandMethods();


        for (Method m : commandMethods) {
            CommandInfo anno = m.getAnnotation(CommandInfo.class);
            String annoCommand = anno.command();
            String description = anno.description();
            builder.append(String.format("%s -- %s (%s)", annoCommand, description, m.getName()));
            builder.append("\n");
        }
        return builder.toString().trim();
    }

    //endregion


    //endregion
}
