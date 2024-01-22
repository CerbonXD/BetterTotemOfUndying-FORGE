package com.cerbon.better_totem_of_undying.neoforge;


import com.cerbon.better_totem_of_undying.BetterTotemOfUndying;
import com.cerbon.better_totem_of_undying.util.BTUConstants;
import com.cerbon.cerbons_api.api.static_utilities.MiscUtils;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.common.Mod;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

@Mod(BTUConstants.MOD_ID)
public class BetterTotemOfUndyingNeo {

    public BetterTotemOfUndyingNeo() {
        BetterTotemOfUndying.init();

        if (MiscUtils.isModLoaded(BTUConstants.CURIOS_MOD_ID)) {
            InterModComms.sendTo(BTUConstants.CURIOS_MOD_ID, SlotTypeMessage.REGISTER_TYPE, () -> SlotTypePreset.CHARM.getMessageBuilder().build());
            BTUConstants.LOGGER.debug("Enqueued IMC to {}", BTUConstants.CURIOS_MOD_ID);
        }
    }
}
