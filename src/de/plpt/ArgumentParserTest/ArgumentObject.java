package de.plpt.ArgumentParserTest;

import de.plpt.ArgumentParser.CommandInfo;
import de.plpt.ArgumentParser.ParameterInfo;

public class ArgumentObject {

    private boolean quit =false;
    public ArgumentObject(){
    }


    @CommandInfo(command = "add-item ([^;\\n]+);([^;\\n]+);(\\d{2})")
    public String processGetItem(String name,float id,@ParameterInfo(minValue  = 55,maxValue = 65) int z){
        return "name" + " " + id;
    }

    @CommandInfo(command = "print ([^;\\n]+)")
    public String print(String name){
        return "Hello " + name;
    }

    @CommandInfo(command = "quit")
    public void quit(){
        this.quit = true;
    }

    public boolean getQuit(){
        return quit;
    }
}
