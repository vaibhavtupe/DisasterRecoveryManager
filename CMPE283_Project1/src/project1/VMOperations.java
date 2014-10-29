package project1;

import java.net.URL;

import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;

public class VMOperations {

	public  String getVMIp(VirtualMachine vm){
		return vm.getGuest().getIpAddress();
	}


	public boolean vmPowerOn(VirtualMachine vm){
		boolean powerOnSuccess=false;
		try{
			if(vm!=null){
				Task task = vm.powerOnVM_Task(null);
				String status = 	task.waitForMe();
				if(status==Task.SUCCESS)
				{
					System.out.println("vm:" + vm.getName() + " powered On.");
					powerOnSuccess=true;
				}
				else
				{
					System.out.println("vm:" + vm.getName() + " Failure while powered On.");
					powerOnSuccess=false;
				}			
			}

		}
		catch (Exception e){
			System.out.println("Exception while Power on : "+e);
		}
		return powerOnSuccess;

	}

	public boolean vmPowerOff(VirtualMachine vm){
		boolean powerOffSuccess=false;
		try{
			if(vm!=null){
				Task task = vm.powerOffVM_Task();
				String status = 	task.waitForMe();
				if(status==Task.SUCCESS)
				{
					System.out.println("vm:" + vm.getName() + " powered Off.");
					powerOffSuccess=true;
				}
				else
				{
					System.out.println("vm:" + vm.getName() + " Failure while powered Off.");
					powerOffSuccess=false;
				}			
			}

		}
		catch (Exception e){
			System.out.println("Exception while Power off : "+e);
		}
		return powerOffSuccess;

	}

}



