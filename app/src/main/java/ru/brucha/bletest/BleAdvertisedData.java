package ru.brucha.bletest;

import java.util.List;
import java.util.UUID;

/**
 * Created by Prog on 27.05.2015.
 */
public class BleAdvertisedData {
    private List<UUID> mUuids;
    private String mName;
    public BleAdvertisedData(List<UUID> uuids, String name){
        mUuids = uuids;
        mName = name;
    }

    public List<UUID> getUuids(){
        return mUuids;
    }

    public String getName(){
        return mName;
    }
}
