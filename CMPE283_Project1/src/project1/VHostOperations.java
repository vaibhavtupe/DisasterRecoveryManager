package project1;

import com.vmware.vim25.ComputeResourceConfigSpec;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.Permission;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.Datacenter;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

public class VHostOperations  {


	public ManagedEntityStatus  getVhostHeartbeat(HostSystem vHost){

		return vHost.getOverallStatus();
	}

	public static String getHostIp(HostSystem host){

		return host.getName();
	}

	public HostSystem returnParentVhost(VirtualMachine vm){
		HostSystem vHost=null;

		for(int j=0; j<Util.hosts.length;j++){
			ManagedEntity[] vmInVhost;

			if(Util.hosts[j] instanceof HostSystem)
			{
				try{
					vmInVhost=((HostSystem) Util.hosts[j]).getVms();

					for(int i =0;i<vmInVhost.length;i++)
					{
						if(vmInVhost[i].getName().equalsIgnoreCase(vm.getName()))	
						{
							vHost=(HostSystem)Util.hosts[j];
							break;
						}
					}
				}catch (Exception e){
					System.out.println("Exception in Returning parent Vhost : "+e);
				}
			}
		}

		return vHost;
	}


	public String  addNewVhost(){
		
		String newHostIp="130.65.132.164";
		HostConnectSpec hcSpec = new HostConnectSpec();
		hcSpec.setHostName("130.65.132.164");
		hcSpec.setUserName("root");
		hcSpec.setPassword("12!@qwQW");
		hcSpec.setSslThumbprint("74:DE:ED:D1:99:B4:98:51:FA:8D:64:3A:97:D4:3B:10:D5:41:6E:48");
		ComputeResourceConfigSpec compResSpec = new ComputeResourceConfigSpec();
		Task task  = null;
		try {
			
			Permission permission = new Permission();
			permission.setPropagate(true);
			permission.setEntity(Util.si.getMOR());

			task = ((Datacenter)Util.dcs[0]).getHostFolder().addStandaloneHost_Task(hcSpec, compResSpec, true);
			try {
				if(task.waitForMe() == Task.SUCCESS){
					System.out.println("Host Created Succesfully");
					return newHostIp;
				}
			} catch (Exception e) {
				System.out.println("Error in creating a new vHost2 : " + e);
			}
		} catch (Exception e) {
			System.out.println("Error in creating a new vHost : " + e);
		}

		return "";
	}

	public VirtualMachinePowerState getHostPowerState(HostSystem host){
		try{
			VirtualMachine vmHost;
			String hostInaVcenter = Util.getHostInVcenter(host.getName());
			vmHost=(VirtualMachine)new InventoryNavigator(Util.vCenterManagerRootFolder).searchManagedEntity("VirtualMachine", hostInaVcenter);
			return vmHost.getRuntime().getPowerState();
		}
		catch(Exception e){

			System.out.println("Exception while getting host power sate : "+e);
		}
		return null;

	}

	public boolean powerOnHost(HostSystem host){

		boolean powerOnSuccess=false;
		VirtualMachine vmHost;

		try{
			String hostInaVcenter = Util.getHostInVcenter(host.getName());
			vmHost=(VirtualMachine)new InventoryNavigator(Util.vCenterManagerRootFolder).searchManagedEntity("VirtualMachine", hostInaVcenter);


			Task task = vmHost.powerOnVM_Task(null);
			String status = 	task.waitForTask();
			if(status==Task.SUCCESS)
			{
				powerOnSuccess=true;
			}
			else
			{
				powerOnSuccess=false;
			}			


		}
		catch (Exception e){
			System.out.println("Exception while Power on : "+e);
		}
		return powerOnSuccess;
	}

	public boolean revertHostToSnapshot(HostSystem host){
		try {
			VirtualMachine vmHost;
			String hostInaVcenter = Util.getHostInVcenter(host.getName());
			vmHost=(VirtualMachine)new InventoryNavigator(Util.vCenterManagerRootFolder).searchManagedEntity("VirtualMachine", hostInaVcenter);

			Task t= vmHost.revertToCurrentSnapshot_Task(null);
			if( t.waitForTask()==t.SUCCESS)
			{
				System.out.println("Vhost  "+ host.getName() + " reverted to snapshot successfully !!!");
				return true;
			}
			else
			{
				System.out.println("Vhost snapshot reversal unsuccessful.");
			    return false;
			}

		}
		catch (Exception e){
			System.out.println("error in reverting : "+e);

		}
		return false;

	}

}
