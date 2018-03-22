package de.plpt.ArgumentParserTest;

import de.plpt.ArgumentParser.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ArgumentObject {

    //region varDef
    private boolean quit = false;
    private  ArgumentParser argumentParser;
    private List<String> list = new ArrayList<>();
    //endregion
    public ArgumentObject(String[] args) {
         argumentParser = new ArgumentParser(this,true);
    }

    public  void processCommands(){
        Scanner s = new Scanner(System.in);
        while (!this.quit) {
            {
                try {
                    String cmd = s.nextLine().trim();
                    String res = null;

                    res = argumentParser.parse(cmd);

                    if(res!=null)
                        System.out.println("-->" + res);
                }
                catch (ArgumentParserExecutionException execException){
                    System.out.println(String.format("Error: {0} - {1}",execException.getTypedCause().getClass().getName(),execException.getTypedCause().getMessage()));
                }
                catch (ArgumentParserException e) {
                    System.out.println(e.getMessage());
                } catch (IntervalViolationException e) {
                    System.out.println(e.getMessage());
                }
            }

        }

    }


    @CommandInfo(command = "add-item ([^;\\n]+);([^;\\n]+);(\\d{2})", description = "Adds a new Item to collections")
    public String processGetItem(String name, float id, @ParameterInfo(minValue = 55, maxValue = 65) int z) {
        return "name" + " " + id;
    }

    @CommandInfo(command = "readArray ([^;\\n]+);([^;\\n]+)",description = "reads input to an array of two elements")
    public void readArray(@ParameterInfo(arrayLenght = 2) String[] names) {
        list.addAll(List.of(names));
    }

    @CommandInfo(command = "put ([^;\\n]+)",description = "puts a new value into a map")
    public void put(String name) {
       list.add(name);
    }

    @CommandInfo(command = "print",description = "Prints internal list")
    public String print() {
        return String.join("; ",list);
    }

    @CommandInfo(command = "quit",description = "exits the program")
    public void quit() {
        this.quit = true;
    }


}
