package simsoft;

import java.util.Queue;
import java.util.Random;

public class SimModel {

    // Simulation Model Variables
    public static int Clock;                     // We use these to track our steps in the simulation
    public static int LastL1IdleTime, LastL2IdleTime, LastScaleIdleTime;
    private static Queue<SimEvent> FEL;                                             // This is our FEL!
    public static Random RNGloading, RNGscale, RNGtravel;                           // Variables for random numbers generated for each event
    public static Queue<dumpTruck> LQ, WQ;                                          // Queue lines for the loader unit and weighing unit
    private static boolean isL1Busy, isL2Busy, isWBusy;     
    

    private static Integer getRandomTime(double TD[][], Random RNV) {
        Integer randomTime = -1;
        double temp = RNV.nextDouble();
        for (int i = 0; i < TD.length; i++){
            if (Double.compare(temp,TD[i][1]) < 0) {
                randomTime = ((int) TD[i][0]);
                break;
            }
        }
        return randomTime;
    }
    
}
