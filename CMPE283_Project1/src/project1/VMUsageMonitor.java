package project1;

import project1.Util;
import project1.VMAlarmManager;
import project1.VMFailureRecoveryThread;
import project1.VMSnapshotManagerThread;

import com.vmware.vim25.VirtualMachineConfigInfo;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.VirtualMachine;

public class VMUsageMonitor {



	public static void displayStatics() {
		try{

			if(Util.hosts==null || Util.hosts.length==0) {
				return;
			}

			System.out.println("*****************vHost List*************************");
			for(int h=0; h<Util.hosts.length; h++) {
				System.out.println("Host IP " + (h+1) + ": "+ Util.hosts[h].getName());
			}

			System.out.println("***************************************************");

			System.out.println("********************VM List************************");

			for(int m=0; m<Util.vms.length; m++) {
				VirtualMachine vm = (VirtualMachine) Util.vms[m];
				VirtualMachineConfigInfo vminfo = vm.getConfig();
				VirtualMachinePowerState vmps = vm.getRuntime().getPowerState();
				vm.getResourcePool();
				System.out.println("---------------------------------------------------");
				System.out.println("Virtual Machine " + (m+1));
				System.out.println("VM Name: " + vm.getName());
				System.out.println("VM OS: " + vminfo.getGuestFullName());
				System.out.println("VM CPU Number: " + vm.getConfig().getHardware().numCPU);
				System.out.println("VM Memory: " + vm.getConfig().getHardware().memoryMB);
				System.out.println("VM Power State: " + vmps.name());
				System.out.println("VM Running State: " + vm.getGuest().guestState);
				System.out.println("VM IP: " + vm.getGuest().getIpAddress());
				System.out.println("VM CPU: " + vm.getConfig().getHardware().getNumCPU());
				System.out.println("VM Memory: " + vm.getConfig().getHardware().getMemoryMB());
				System.out.println("VM VMTools: " + vm.getGuest().toolsRunningStatus);
				System.out.println("---------------------------------------------------");
			}
		}catch (Exception e){

		}
	}



	public void exitConnection(){

		Util.si.getServerConnection().logout();
		System.out.println("Connection to Vcenter exited !!!");
	}
}
