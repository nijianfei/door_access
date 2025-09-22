package com.csc.door.service;

import com.csc.door.query.RecordDownloadQuery;
import com.csc.door.request.DoorCntrRequest;
import com.csc.door.request.DoorSetRequest;
import com.csc.door.request.LightCntrRequest;
import com.csc.door.response.ClockSyncResponse;
import com.csc.door.response.ControllerStatusResponse;

import java.util.List;

public interface DoorCmdService {
    void openDoor(DoorCntrRequest request);

    void doorSet(DoorSetRequest request);

    List<ClockSyncResponse> clockSync();
    List<ControllerStatusResponse> controlStatus();

    Object accessRecord(RecordDownloadQuery query);

    void lightCntr(List<LightCntrRequest> requests);


    void searchDevice();
}
