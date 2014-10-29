package project1;

import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;

import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ManagedEntity;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.VirtualMachine;

public class Util {

	//Global variables
	public  static ServiceInstance si;
	public static ServiceInstance vCenterManagerSi;
	public static Folder rootFolder;
	public static Folder vCenterManagerRootFolder;
	public  static ManagedEntity[] dcs;
	public  static ManagedEntity[] hosts;
	public  static ManagedEntity[] vms;

	public Util(){

		try{

			si = new ServiceInstance(new URL(Project1_Config.getVCenterURL()), Project1_Config.getVCenterUsername(), 
					Project1_Config.getVCenterPassword(), true);

			vCenterManagerSi= new ServiceInstance(new URL("https://130.65.132.14/sdk"), Project1_Config.getVCenterUsername(), 
					Project1_Config.getVCenterPassword(), true);

			rootFolder = si.getRootFolder();
			vCenterManagerRootFolder=vCenterManagerSi.getRootFolder();

			dcs = new InventoryNavigator(rootFolder).searchManagedEntities(
					new String[][] { {"Datacenter", "name" }, }, true);

			hosts = new InventoryNavigator(rootFolder).searchManagedEntities(
					new String[][] { {"HostSystem", "name" }, }, true);

			vms = new InventoryNavigator(rootFolder).searchManagedEntities(
					new String[][] { {"VirtualMachine", "name" }, }, true);
		}
		catch (Exception e){
			System.out.println("VMMonitor object initialization eroor : " + e);

		}	
	}

	// pinging the given ip to check whether it is reachable

	public static boolean ping(String ip) throws Exception {
		String cmd = "";

		if (System.getProperty("os.name").startsWith("Windows")) {
			// For Windows
			cmd = "ping -n 3 " + ip;
		} else {
			// For Linux and OSX
			cmd = "ping -c 3 " + ip;
		}

		System.out.println("Ping "+ ip + "......");
		Process process = Runtime.getRuntime().exec(cmd);
		process.waitFor();		
		return process.exitValue() == 0;
	}


	//to get vHost name in Vcenter Manager

	public static String getHostInVcenter(String host){

		return VHOSTMAP.get(host);
	}

	// stored mapping of vHost Names in Vcenter Manager

	public static final HashMap<String, String> VHOSTMAP = new HashMap<String, String>() {
		{
			put("130.65.132.161", "T04-vHost01-cum1_IP=.132.161");
			put("130.65.132.162", "T04-vHost02-cum1_IP=.132.162");
			put("130.65.132.163", "T04-vHost03-cum1");
			put("130.65.132.164","T04-vHost04-cum1");
		}
	};

}
