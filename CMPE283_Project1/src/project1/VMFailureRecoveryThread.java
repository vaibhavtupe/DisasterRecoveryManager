package project1;

import java.util.ArrayList;

import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VMFailureRecoveryThread implements Runnable{


	VHostOperations vhop=new VHostOperations();
	VMOperations vmop=new VMOperations();
	public static ArrayList<String> ongoingRecoveryVM=new ArrayList<String>();



	public void run()
	{
		try{
			while(true){
				Thread.currentThread().sleep(Project1_Config.getHeartbeatThreadDelay()); 
				monitorVMHeartBeat();
			}	
		}
		catch (Exception e){
			System.out.println("Exception in VMFailure Recovery Thread :" + e);
		}
	}


	public void monitorVMHeartBeat(){

		System.out.println("-------------------------------Start Checking HeartBeat-------------------------------------------------");
		try{
			for(int j=0; j<Util.vms.length; j++)
			{
				if(Util.vms[j] instanceof VirtualMachine)
				{
					VirtualMachine vm = (VirtualMachine) Util.vms[j];

					System.out.println("-------VM Name : "+vm.getName()+" ----- "+"vm ip : "+vm.getGuest().getIpAddress() +" ------ " +vm.getGuestHeartbeatStatus());

					if(ongoingRecoveryVM.contains(vm.getName()))
					{
						if(!Util.ping(vm.getGuest().getIpAddress()))
						{

							if(vm.getRuntime().getPowerState()==vm.getRuntime().powerState.poweredOff)
							{
								vmop.vmPowerOn(vm);
							}
							System.out.println(vm.getName()+"  : Failure recovery already going on !!!!");
							continue;
						}
						else
							ongoingRecoveryVM.remove(vm.getName());
					}

					if(!Util.ping(vm.getGuest().getIpAddress()))  // VM not responding to ping
					{

						System.out.println("Ping to VM : "+ vm.getName()+"  Failed ");

						if(vm.getRuntime().getPowerState()==vm.getRuntime().powerState.poweredOff) 
						{

							if(VMAlarmManager.getAlarmStatus(vm))  // checking whether user has powered off by checking alarmstate
								System.out.println("-----"+vm.getName() + " powered off by user No failure recovery required-----");
							else
								System.out.println("User has not powered off the system, some other problem !!!");
						}else
							overcomeVMFailOver(vm);
					}
					else // VM is responding to ping
						System.out.println(vm.getName() +" : responding to ping and working Fine ");

				}
			}
			System.out.println("-----------------------------HeartBeat check Cycle Ends---------------------------------------------------");
		}
		catch (Exception e){
			System.out.println("heartbeat monitor exception : "+e);
		}
	}

	public void overcomeVMFailOver(VirtualMachine vm){
		System.out.println("------Failure Recovery for VM : "+ vm.getName() +"  started -------");

		HostSystem parentHost=null;

		parentHost=vhop.returnParentVhost(vm);

		System.out.println("ParentVhost of : " +vm.getName()+" is "+ parentHost.getName());

		try{

			if(Util.ping(vhop.getHostIp(parentHost))) //case 1 : vHost is running properly.restart the VM and apply the screenshot in same vhost.
			{
				System.out.println("ParentVhost is responding to ping ");
				Task task=vm.revertToCurrentSnapshot_Task(null);


				if(task.waitForTask()==Task.SUCCESS)
				{
					ongoingRecoveryVM.add(vm.getName());             //adding to the recovery list
					System.out.println("------------------"+vm.getName()+"reverted to snapshot---------------------");

					if(vm.getRuntime().getPowerState()==vm.getRuntime().powerState.poweredOff)
					{
						vmop.vmPowerOn(vm);
					}


				}else
					System.out.println("-----Failure in reverting  VM : "+vm.getName() +" to its snapshot.----");

			}
			else   //case 2  vhost ping failed overcome vhost failure
			{ 
				System.out.println("-------Parent vHost: "+ parentHost.getName()+" not Responding to ping,starting failure recovery for this vHost--------");
				if(vhop.revertHostToSnapshot(parentHost))//step 1 revert vhost to snapshot
				{
					ongoingRecoveryVM.add(vm.getName());     //adding to the recovery list
					System.out.println("--------------------vHost reverted to snapshot-------------------");
					//check host state and if powered off ..power it on.
					if(vhop.getHostPowerState(parentHost)==vm.getRuntime().getPowerState().poweredOff)
					{
						vhop.powerOnHost(parentHost);

					}

					if(vm.getRuntime().getPowerState()==vm.getRuntime().powerState.poweredOff)
					{
						vmop.vmPowerOn(vm);
					}

				}
				else   
				{

					System.out.println("vHost Unrecoverable by reverting to snapshot.");
					System.out.println("Searhing another vHost for migrating VM .......");
					String newHostIp= giveAnotherHostIp(parentHost);

					if(!newHostIp.isEmpty())  //case 3 migrate to another host
					{

						System.out.println("New alive host found : "+ newHostIp);

						System.out.println("---Starting Migration to New Host---- ");
						vm.powerOffVM_Task();
						migrateVM(vm,newHostIp);
						vm.powerOnVM_Task(null);

					}
					else                        //case 4 adding new host and migrating to that
					{
						System.out.println("No other alive Host Found. Adding New Host");
						String newIp=  vhop.addNewVhost();
						vm.powerOffVM_Task();
						migrateVM(vm,newIp);
						vm.powerOnVM_Task(null);
					}

				}

			}
		}
		catch (Exception e)
		{		
			System.out.println("Exception in handling vm failure : "+e);
		}
	}


	public String giveAnotherHostIp(HostSystem parentHost){
		String newHostIp=null;
		try{

			if(Util.hosts.length>1) 
			{
				for(int i=0 ;i<Util.hosts.length;i++)
				{
					if(Util.hosts[i].getName()!=parentHost.getName())
					{
						if(Util.ping(vhop.getHostIp((HostSystem)Util.hosts[i])))
						{
							newHostIp=vhop.getHostIp((HostSystem)Util.hosts[i]);
							System.out.println("New  alive Host : "+vhop.getHostIp((HostSystem)Util.hosts[i])+" found ");
							return newHostIp;
						}  
						//remove vhost from list as it is unreachable
					}
				}

				// call to addingVhost()  as we didnt found any working vshot
			}
		}
		catch (Exception e){}

		return newHostIp;

	}


	
	public static void migrateVM(VirtualMachine vm, String newHost) throws Exception {
		
		HostSystem hs= (HostSystem) new InventoryNavigator(Util.rootFolder).searchManagedEntity("HostSystem", newHost);
		ComputeResource cr = (ComputeResource) hs.getParent();
		Task task = vm.migrateVM_Task(cr.getResourcePool(), hs, VirtualMachineMovePriority.highPriority, VirtualMachinePowerState.poweredOff);
		System.out.println("Try to migrate " + vm.getName() + " to " + hs.getName());
		if (task.waitForTask() == task.SUCCESS) {
			System.out.println("Migrate virtual machine: " + vm.getName() + " successfully!");
		} else {
			System.out.println("Migrate vm failed!");
		}
	}


}
