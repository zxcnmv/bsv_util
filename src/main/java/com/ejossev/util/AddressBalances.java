package com.ejossev.util;
import org.json.*;


import java.math.BigInteger;
import java.util.*;

public class AddressBalances {
    private BigInteger confirmed;
    private BigInteger unconfirmed;

    public AddressBalances() {
        confirmed = new BigInteger("0");
        unconfirmed = new BigInteger("0");
    }

    public AddressBalances(String json) {
        JSONObject obj = new JSONObject(json);
        confirmed = obj.getBigInteger("confirmed");
        unconfirmed = obj.getBigInteger("unconfirmed");
    }

    public AddressBalances(JSONObject obj) {
        confirmed = obj.getBigInteger("confirmed");
        unconfirmed = obj.getBigInteger("unconfirmed");
    }

    public String toString() {
        return ("{\"confirmed\": " + confirmed + ", \"unconfirmed\": " + unconfirmed +"}");
    }

    public void add(AddressBalances other) {
        confirmed = confirmed.add(other.confirmed);
        unconfirmed = unconfirmed.add(other.unconfirmed);
    }

    static public AbstractMap<String, AddressBalances> fromJsonArray(String json) {
        AbstractMap<String, AddressBalances> rv = new HashMap<String, AddressBalances>();
        JSONArray arr = new JSONArray(json);
        for (int i = 0; i < arr.length(); i++) {
            JSONObject o = arr.getJSONObject(i);
            if (!o.getString("error").equals(""))
                continue;
            String address = o.getString("address");
            AddressBalances balance = new AddressBalances(o.getJSONObject("balance"));
            rv.put(address, balance);
        }
        return rv;
    }
}
