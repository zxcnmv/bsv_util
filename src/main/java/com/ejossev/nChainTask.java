package com.ejossev;
import static java.lang.System.exit;

import com.ejossev.util.AddressBalances;
import com.ejossev.util.HttpAsyncClient;

import java.util.*;
import java.util.concurrent.*;
import java.io.*;


/**
 * Hello world!
 *
 */
public class nChainTask
{
    public static void main( String[] args )
    {
        HttpAsyncClient client = new HttpAsyncClient();

        if (args.length == 2 && args[0].equals("-f")) {
            // get balances for all addresses in a file and print them
            List<String> addresses = getAddressesFromFile(args[1]);
            AbstractMap<String, AddressBalances> balances = client.getMultipleBalances(addresses);
            balances.forEach((addr, balance) -> System.out.println(addr + ":\t" + balance));
        } else if (args.length == 2 && args[0].equals("-b")) {
            // get bulk balance for addresses in the file
            List<String> addresses = getAddressesFromFile(args[1]);
            try {
                AbstractMap<String, AddressBalances> balances = client.getBulkBalance(addresses);
                balances.forEach((addr, balance) -> System.out.println(addr + ":\t" + balance));
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Error retrieving bulk balances, call stack:");
                e.printStackTrace();
            }
        } else if (args.length == 2 && args[0].equals("-a")) {
            // get balance of an address specified as argument
            try {
                System.out.println(client.getOneBalance(args[1]));
            } catch (InterruptedException | ExecutionException e) {
                System.out.println("Error retrieving balance for address " + args[1] +", call stack:");
                e.printStackTrace();
            }
        } else {
            System.out.println("Usage: \n" +
                    "\t<appname> [-f <file> | -b <file> | -a <address>]");
        }
        exit(0);
    }

    private static List<String> getAddressesFromFile(String filename) {
        File file = new File(filename);
        Vector<String> addresses = new Vector<String>();
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                addresses.add(line);
            }
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return addresses;
    }
}
