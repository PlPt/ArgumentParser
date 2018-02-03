package de.plpt.ArgumentParserTest;

import de.plpt.ArgumentParser.ArgumentParser;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        ArgumentObject argumentObject = new ArgumentObject();
        ArgumentParser arg = new ArgumentParser(argumentObject);

        Scanner s = new Scanner(System.in);
        while (!argumentObject.getQuit()) {
            try {
                String cmd = s.nextLine().trim();
                String res = arg.parse(cmd);
                if(res!=null)
                System.out.println("-->" + res);
            } catch (Exception x) {
                System.out.println(String.format("[%s] %s",x.getClass().getName(),x.getMessage()));

            }
        }

    }
}
