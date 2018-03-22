# ArgumentParser
A simple regex argument/command parser vor Java
Completly object oriented, because every defined command (as Method with annotation) invokes a Java Method with parameters.

Let's look at the example in the project:

A Command can be defined a a class like this: 

    @CommandInfo(command = "put ([^;\\n]+)",description = "puts a new value into a map")
    public void put(String name) {
         list.add(name);
     }
     
 The CommandInfo Interface contains the "command" as regex definition with regex groups as parameters.
 Every regex group is connected to a Method-parameter by it's index.
 In context it means, that the first parameter of command "put" will be result ind the Method parameter "name" as String.
 
 But you can use all primitive DataType and even it's array as Parameter:
 
      @CommandInfo(command = "add-item ([^;\\n]+);([^;\\n]+);(\\d{2})", description = "Adds a new Item to collections")
      public String processGetItem(String name, float id, @ParameterInfo(minValue = 55, maxValue = 65) int z) {
          //do your things...
          return "name" + " " + id;
      }
      
      
       @CommandInfo(command = "readArray ([^;\\n]+);([^;\\n]+)",description = "reads input to an array of two elements")
      public void readArray(@ParameterInfo(arrayLenght = 2) String[] names) {
          list.addAll(List.of(names));
      }
      
      
The ParameterInfo AnnotationInterface can hold additional infomation about a parameter, e.g. an integer range or an array length.
    
    
    
   When yout want to process your commands then initialize the argument parser like this:
   
       ArgumentParser argumentParser = new ArgumentParser(this,true); // "this" is in this contect the Object reference holding all CommendInfo definitions

       
For a simple loop for commands you can use something like this:
       
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


Internally this ArgumentParser uses JavaReflections to read MethodDefinition and Command Definitions,
and parses regex values and groups in the right datatype.
Then the Method is called by invoke with parameters.
