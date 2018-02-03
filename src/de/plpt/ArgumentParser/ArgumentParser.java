package de.plpt.ArgumentParser;

//region Imports

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
    //endregion

    //region Methods

    //region parse

    /**
     * Parses a given command and looks for a matching regex definition in executableObject.
     * When a regex method definition matches command string, the method is executed with regex group parameters
     * which were parsed to strongly typed values if possible
     *
     * @param command InputCommand from Terminal
     * @param <T>     Return type of matching Method defined in executable Object.
     * @return Result value (return type) of matching Method in executableObject. In worst case use as Object an call
     * Object#toString() on it.
     */
    public <T extends Object> T parse(String command) {


        Method meth = getMatchingMethod(command);

        if (meth == null) {

            Method mm = getStartsWithMethod(command);

            if (mm != null) {
                String regex = mm.getAnnotation(CommandInfo.class).command();
                throw new IllegalArgumentException(String.format("Command '%s' does not match regex '%s'", command, regex));
            }

            throw new NullPointerException("No matching Method found // unkonown command");
        }

        CommandInfo anno = meth.getAnnotation(CommandInfo.class);
        String annoCommand = anno.command();
        Pattern pattern = Pattern.compile(annoCommand);
        Matcher matcher = pattern.matcher(command);
        matcher.matches();

        Object[] z = new Object[meth.getParameterCount()];

        for (int i = 0; i < meth.getParameterCount(); i++) {

            Class<?> type = meth.getParameterTypes()[i];
            Annotation[] paramAnno = meth.getParameterAnnotations()[i];


            Object parsedValue;
            String paramString = matcher.group(i + 1);
            try {
                parsedValue = parseValue(paramString, type);
            } catch (NumberFormatException nfe) {
                NumberFormatException nfex = new NumberFormatException(String.format("Parameter[%s] with value '%s' can't be parsed to specified type %s", i, paramString, type.getName()));
                nfex.initCause(nfe);
                throw nfex;
            }


            if (paramAnno.length > 0) {
                ParameterInfo parameterInfo = (ParameterInfo) paramAnno[0];
                if (parsedValue instanceof Number && ((int) parsedValue > parameterInfo.maxValue() || (int) parsedValue < parameterInfo.minValue())) {
                    throw new IllegalArgumentException(String.format("Value '%s' is not Element of interval [%s,%s]", parsedValue, parameterInfo.minValue(), parameterInfo.maxValue()));
                }
            }

            z[i] = parsedValue;
        }

        try {
            return (T) (meth.invoke(executableObject, z));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
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
    private Object parseValue(String paramString, Class<?> type) {
        Object parsedValue = null;
        if (type == int.class || type == Integer.class) {
            parsedValue = Integer.parseInt(paramString);
        } else if (type == long.class || type == Long.class) {
            parsedValue = Long.parseLong(paramString);
        } else if (type == boolean.class || type == Boolean.class) {
            parsedValue = Boolean.valueOf(paramString);
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
            if ((inputCommand.contains(" ") && annoCommand.contains(inputCommand.split(" ")[0])) || annoCommand.contains(inputCommand))
                return m;

        }
        return null;
    }
    //endregion

    //endregion
}
