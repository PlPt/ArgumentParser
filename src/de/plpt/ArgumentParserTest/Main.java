package de.plpt.ArgumentParserTest;

import de.plpt.ArgumentParser.ArgumentParser;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        ArgumentObject argumentObject = new ArgumentObject(args);
        argumentObject.processCommands();
    }
}
