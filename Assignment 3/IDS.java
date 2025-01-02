import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;

public class IDS {
    public static int eventsLine;
    public static int statsLine;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Need 3 parameter");
            return;
        }

        String fileNameOne = args[0];
        String fileNameTwo = args[1];
        int totalDays = Integer.parseInt(args[2]);
        
        ArrayList<Events> myEvents = readEventFile(fileNameOne);
        boolean correctParameter = readStatFile(fileNameTwo, myEvents);

        if (!correctParameter) {
            System.out.println("Events and Stats Parameter is different");
            return;
        }

        Scanner sc = new Scanner(System.in);

        // Activity Engine
        ArrayList<String[][]> activityEngineLog =  generateLogs(myEvents,totalDays);

        // Analysis Engine
        displayLogInfo(activityEngineLog);
        System.out.println("Please enter to Continue");
        sc.nextLine();
        System.out.println("============================================================");
        
        newBaseLineData(activityEngineLog);
        
        boolean EndAlert = false;

        while (!EndAlert) {
            System.out.println("============================================================");
            System.out.println("Enter 'Exit' to Exit Program");
            System.out.println("Please enter New Stats.file with Days");
            String userInput = sc.nextLine();

            if (userInput.equalsIgnoreCase("Exit")) {
                EndAlert = true;
                continue;
            }

            String[] splitInput = userInput.split(" ");
            if (splitInput.length != 2) {
                System.out.println("Please Input Correctly");
                System.out.println("Exampe : 'Stats1.txt 100' ");
                continue;
            }

            int newDays = Integer.parseInt(splitInput[1]);
            correctParameter = readStatFile(splitInput[0], myEvents);

            if (!correctParameter) {
                System.out.println("Different Parameter, Please Change the parameter");
                continue;
            }

            ArrayList<String[][]> tempData = generateLogs(myEvents, newDays);

            alertEngine(tempData, myEvents);
            
            System.out.println("Please enter to Continue");
            sc.nextLine();
            
        }
        sc.close();
    }
    
    public static void alertEngine(ArrayList<String[][]> activityEngine, ArrayList<Events> myEvents) { 
        int maximumThreshold = 0 ;
        for (int i = 0 ; i < myEvents.size(); i++){
            maximumThreshold += myEvents.get(i).getWeight();
        }
        maximumThreshold *= 2;

        ArrayList<Stats> baseLineData = GetBaseLineData("BaseLine.txt");
        boolean alerted = false;
        for (int i = 0 ; i < activityEngine.size(); i++) {
            double anomalityContent[] = new double[activityEngine.get(i).length]; 
            for (int j = 0 ; j < activityEngine.get(i).length; j++) {
                double myValue = Double.parseDouble(activityEngine.get(i)[j][1]) ;

                myValue -= baseLineData.get(j).getMean();

                myValue /= baseLineData.get(j).getStd();

                myValue *= myEvents.get(j).getWeight();
                myValue = reduceToTwoDecimal(myValue);

                anomalityContent[j] = myValue;

            }
            double currentThreshold = 0;

            for (int j = 0 ; j < anomalityContent.length; j++) {
                currentThreshold += anomalityContent[j];
            }

            if (currentThreshold >= maximumThreshold){
                System.out.println("============================================================");
                System.out.println("Anomalities Found on Days " + (i + 1));
                System.out.println("Maximum Threshold is " + maximumThreshold);
                System.out.println("Current Threshold is " + reduceToTwoDecimal(currentThreshold));
                alerted = true;
            }
        }
        if (!alerted)
            System.out.println("No Anomality found");

        System.out.println("============================================================");

    }

    public static ArrayList<Stats> GetBaseLineData(String fileName){
        ArrayList<Stats> myTempStats = new ArrayList<Stats>();
        File myFile = new File(fileName);
        try(Scanner sc = new Scanner(myFile)){
            String temp;
            while (sc.hasNextLine()){
                temp = sc.nextLine();
                String[] splitTemp = temp.split(":");
                double mean = Double.parseDouble(splitTemp[1]);
                double std = Double.parseDouble(splitTemp[2]);
                Stats tempData = new Stats(mean, std);
                myTempStats.add(tempData);
                
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        
        return myTempStats;
    }

    public static ArrayList<Events> readEventFile(String fileName){
        ArrayList<Events> myEvents = new ArrayList<Events>();
        try{
            File myFile = new File(fileName);
            Scanner sc = new Scanner(myFile);
                
            String temp;
            while (sc.hasNextLine()){
                temp = sc.nextLine();
                String[] textSplit = temp.split(":");
                if (textSplit.length == 1){
                    IDS.eventsLine = Integer.parseInt(textSplit[0]);
                    continue;
                }

                String name = textSplit[0];
                String type = textSplit[1];
                double min = Double.parseDouble(textSplit[2]);
                
                double max = 100;
                String maxString = textSplit[3];
                if (!maxString.isEmpty())
                    max = Double.parseDouble(textSplit[3]);
                double weight = Double.parseDouble(textSplit[4]);

                Events tempEvents = new Events(name, type.charAt(0), min, max, weight);
                myEvents.add(tempEvents);

            }
            sc.close();
            
        }catch (Exception e){
            e.printStackTrace();
        }
        
        return myEvents;
    }

    public static boolean readStatFile (String fileName, ArrayList<Events> myEvents ){
        File myFile = new File(fileName);
        try(Scanner sc = new Scanner(myFile)){

            String temp;
            int currentLine = 0;
            while (sc.hasNextLine()) {
                temp = sc.nextLine();
                String[] textSplit = temp.split(":");
                if (textSplit.length == 1){
                    IDS.statsLine = Integer.parseInt(textSplit[0]);
                    if (IDS.statsLine != IDS. eventsLine)
                        return false;
                    continue;
                }

                double mean = Double.parseDouble(textSplit[1]);
                double std = Double.parseDouble(textSplit[2]);

                myEvents.get(currentLine).getStats().setMean(mean);
                myEvents.get(currentLine).getStats().setStd(std);
                currentLine++;

            }
            sc.close();
            
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static ArrayList<String[][]> generateLogs(ArrayList<Events> myEvents, int days){
        ArrayList<String[][]> MyEventLog = new ArrayList<String[][]>();
        for (int i = 0 ; i< days ; i++){
            int currentArray = 0;

            String[][] tempStoreData = new String[IDS.eventsLine][];

            for (int j = 0 ; j < myEvents.size();j++){
                String tempStore[] = new String[2];

                
                tempStore[0] = myEvents.get(j).getName();
                if (myEvents.get(j).getType() == 'C' ){
                    double value = myEvents.get(j).getRandomize();
                    value = reduceToTwoDecimal(value);
                    tempStore[1] = String.valueOf(value) ;
     
                }else if (myEvents.get(j).getType() == 'D'){
                    int value = (int) Math.round(myEvents.get(j).getRandomize());
                    
                    tempStore[1] = String.valueOf(value);
                }
                tempStoreData[currentArray] = tempStore;
                
                currentArray++;
            }
            
            MyEventLog.add(tempStoreData);

        }

        return MyEventLog;
    }

    public static void newBaseLineData(ArrayList<String[][]> myEventsLog){
        System.out.println("Create New BaseLine based on ActivityEngine...");
        double[] meanValue = new double[(myEventsLog.get(0).length)];
        double[] stdValue = new double[(myEventsLog.get(0).length)];
        for (int i = 0 ; i < myEventsLog.get(0).length;i++){
            for (int j = 0 ; j < myEventsLog.size(); j ++)
                meanValue[i] += Double.parseDouble(myEventsLog.get(j)[i][1]) ;
            
            meanValue[i] /= myEventsLog.size();
            meanValue[i] = reduceToTwoDecimal(meanValue[i]);

            double myVarience = 0;
            for (int j = 0 ; j < myEventsLog.size() ; j++)
                myVarience += Math.pow((meanValue[i] -  Double.parseDouble(myEventsLog.get(j)[i][1])), 2) ;  

            myVarience /= myEventsLog.size();

            stdValue[i] = Math.sqrt(myVarience);
            stdValue[i] = reduceToTwoDecimal(stdValue[i]);
        }
        
        try{
            FileWriter fw  = new FileWriter("BaseLine.txt");
            for (int i = 0 ; i < meanValue.length; i++)
                fw.write(myEventsLog.get(0)[i][0]+":"+meanValue[i]+":"+stdValue[i]+":\n" );
            
            fw.close();            
        }catch (Exception e ){
            System.err.println("Failed to create new baseline");
            return;
        }

        System.out.println("Sucessfully created new baseline");

    }

    public static double reduceToTwoDecimal(double value) {
        value = Math.round(value * 100);
        value /= 100;
        return value;
    }

    public static void displayLogInfo(ArrayList<String[][]> myEventsLog) {
        System.out.println("Displaying Events");
        System.out.println("------------------------------------------------");
        for (int i = 0 ; i < myEventsLog.size() ; i++){
            System.out.println("Days " + (i+1));
            System.out.println("===================================");
            System.out.println("User logged in for about " + myEventsLog.get(i)[0][1] + " times");
            System.out.println("User logged out for about " + myEventsLog.get(i)[1][1] + " minutes");
            System.out.println("User sent about  " + myEventsLog.get(i)[2][1] + " emails");
            System.out.println("User opened about  " + myEventsLog.get(i)[3][1] + " emails");
            System.out.println("User deleted about  " + myEventsLog.get(i)[4][1] + " emails");
            System.out.println("===================================");

        }
    }
}
