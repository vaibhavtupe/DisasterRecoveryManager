package project1;


public class DisasterRecovery {
	public static void main(String args[]){

		new Util();

		VMAlarmManager amt=new VMAlarmManager();
		amt.setAlarmOnAllVM();

		VMUsageMonitor vmo =new VMUsageMonitor();
		vmo.displayStatics();

		try {
			new Thread(new VMSnapshotManagerThread()).start();
			Thread.sleep(20000);
			new Thread(new VMFailureRecoveryThread()).start();
		}
		catch (Exception e){
			System.out.println("Error while creating threads : "+e);
		}



	}

}
