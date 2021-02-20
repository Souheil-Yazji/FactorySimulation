package simsoft;

public class SimEvent implements Comparable<SimEvent>{

	    public static enum eventType {Inspect, AddToBuffer, Process};    // ALQ=Arrival at Loader Queue, EL=End of Loading, EW=End of Weighing, ES=End of Simulation
	    private eventType eType;        // Type of the event
	    private Integer eTime;          // Event Time
	    private Inspector inspector;      // Which inspector is this event for.
	    private Workstation workstation;

	    public SimEvent(eventType eType, int eTime, dumpTruck truckID) {
	        this.eType = eType;
	        this.eTime = eTime;
	        this.truckID = truckID;
	    }

	    @Override
	    public int compareTo(SimEvent ev) {
	        return this.geteTime().compareTo(ev.geteTime());
	    }

	    public eventType geteType() {
	        return eType;
	    }

	    public void seteType(eventType eType) {
	        this.eType = eType;
	    }

	    public Integer geteTime() {
	        return eTime;
	    }

	    public void seteTime(int eTime) {
	        this.eTime = eTime;
	    }

	    public dumpTruck getTruckID() {
	        return truckID;
	    }

	    public void setTruckID(dumpTruck truckID) {
	        this.truckID = truckID;
	    }
}
