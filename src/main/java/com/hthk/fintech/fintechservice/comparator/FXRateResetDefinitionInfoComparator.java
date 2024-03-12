package com.hthk.fintech.fintechservice.comparator;

import com.hthk.calypsox.model.staticdata.fxrate.definition.FXRateResetDefinitionInfo;

import java.util.Comparator;

/**
 * @Author: Rock CHEN
 * @Date: 2024/3/7 9:36
 */
public class FXRateResetDefinitionInfoComparator implements Comparator<FXRateResetDefinitionInfo> {

    @Override
    public int compare(FXRateResetDefinitionInfo f1, FXRateResetDefinitionInfo f2) {

        String prim1 = f1.getPrim();
        String prim2 = f2.getPrim();

        String id1 = f1.getId();
        String id2 = f2.getId();

        if (prim1.compareTo(prim2) != 0) {
            return prim1.compareTo(prim2);
        }

        return id1.compareTo(id2);
    }
}
