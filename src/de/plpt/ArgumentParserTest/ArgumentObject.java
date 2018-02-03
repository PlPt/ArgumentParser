package de.plpt.ArgumentParserTest;

import de.plpt.ArgumentParser.CommandInfo;
import de.plpt.ArgumentParser.ParameterInfo;

import java.util.ArrayList;
import java.util.List;

public class ArgumentObject {

    private boolean quit = false;
    List<String> list = new ArrayList<>();
    public ArgumentObject() {
    }


    @CommandInfo(command = "add-item ([^;\\n]+);([^;\\n]+);(\\d{2})")
    public String processGetItem(String name, float id, @ParameterInfo(minValue = 55, maxValue = 65) int z) {
        return "name" + " " + id;
    }

    @CommandInfo(command = "put ([^;\\n]+)")
    public void put(String name) {
       list.add(name);
    }

    @CommandInfo(command = "print")
    public String print() {
        return String.join("; ",list);
    }

    @CommandInfo(command = "quit")
    public void quit() {
        this.quit = true;
    }

    public boolean getQuit() {
        return quit;
    }
}
