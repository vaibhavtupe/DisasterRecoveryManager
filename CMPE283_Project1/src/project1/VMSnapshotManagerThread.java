package project1;

import project1.Project1_Config;
import project1.Util;

import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VMSnapshotManagerThread implements Runnable {
	public void run(){

		try{
			while(true){

				takeHostSnapshot();
				
				takeVMSnapshot();

				Thread.currentThread().sleep(Project1_Config.getSnapshotThreadDelay()); 
			}
		}
		catch(Exception e){
			System.out.println("Snapshot capture Thread Eroor : "+ e);

		}
	}

	public  void takeVMSnapshot(){

		//Taking VM snapshots
		for (int j=0; j<Util.vms.length; j++)
		{
			if(Util.vms[j] instanceof VirtualMachine)
			{
				removeOld((VirtualMachine)Util.vms[j]);
				takeNew((VirtualMachine)Util.vms[j]);
			}
		}
	}

	public void takeHostSnapshot(){
		VirtualMachine vmHost;
		try{
			for(int j=0;j<Util.hosts.length;j++){

				if(Util.hosts[j] instanceof HostSystem){
					String hostInaVcenter = Util.getHostInVcenter(Util.hosts[j].getName());
					vmHost=(VirtualMachine)new InventoryNavigator(Util.vCenterManagerRootFolder).searchManagedEntity("VirtualMachine", hostInaVcenter);
					removeOld(vmHost);  
					takeNew(vmHost);
				}

			}
		}
		catch(Exception e){

		}
	}

	public void removeOld(VirtualMachine vm){
		try{
			Task task = ((VirtualMachine) vm).removeAllSnapshots_Task();      
			if(task.waitForMe()== Task.SUCCESS) 
			{
				//System.out.println("Removed all old  snapshots for : "+ vm.getName());
			}
		}
		catch(Exception e){
			System.out.println("Eroor while removing old snapshot for : "+vm.getName()+ "   "+ e);
		}

	}

	@SuppressWarnings("deprecation")
	public void takeNew(VirtualMachine vm){
		synchronized(vm){

			try{
				Task task = ((VirtualMachine) vm).createSnapshot_Task(
						vm.getName()+"_snapshot", null,false, false);

				if(task.waitForMe()==Task.SUCCESS)
				{
					//System.out.println("Snapshot was created. for : "+vm.getName());
				}

			}
			catch(Exception e){

				System.out.println("Eroor in taking snapshot");
			}
		}
	}

}
