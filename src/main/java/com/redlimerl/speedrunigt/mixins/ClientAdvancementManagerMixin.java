package com.redlimerl.speedrunigt.mixins;

import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerAdvancementTracker;
import com.redlimerl.speedrunigt.timer.TimerPacketHandler;
import com.redlimerl.speedrunigt.timer.TimerStatus;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.category.condition.AdvancementCategoryCondition;
import com.redlimerl.speedrunigt.timer.category.condition.CategoryCondition;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Objects;

@Mixin(ClientAdvancementManager.class)
public abstract class ClientAdvancementManagerMixin {

    @Shadow @Final private AdvancementManager manager;

    @Redirect(method = "onAdvancements", at = @At(value = "INVOKE", target = "Ljava/util/Map$Entry;getValue()Ljava/lang/Object;"))
    public Object advancement(Map.Entry<Identifier, AdvancementProgress> entry) {
        InGameTimer timer = InGameTimer.getInstance();
        
        Advancement advancement = this.manager.get(entry.getKey());
        AdvancementProgress advancementProgress = entry.getValue();
        assert advancement != null;
        advancementProgress.init(advancement.getCriteria(), advancement.getRequirements());

        if (advancementProgress.isDone() && timer.getStatus() != TimerStatus.NONE) {

            // For Timelines
            if (timer.getCategory() == RunCategories.ANY) {
                if (Objects.equals(advancement.getId().getPath(), "story/follow_ender_eye")) {
                    timer.tryInsertNewTimeline("enter_stronghold");
                } else if (Objects.equals(advancement.getId().getPath(), "nether/find_bastion")) {
                    timer.tryInsertNewTimeline("enter_bastion");
                } else if (Objects.equals(advancement.getId().getPath(), "nether/find_fortress")) {
                    timer.tryInsertNewTimeline("enter_fortress");
                }
            }
            timer.tryInsertNewAdvancement(advancement.getId().toString(), null, true,advancement.getDisplay() != null);

            // Custom Json category
            if (timer.getCategory().getConditionJson() != null) {
                for (CategoryCondition.Condition<?> condition : timer.getCustomCondition().getConditionList()) {
                    if (condition instanceof AdvancementCategoryCondition) {
                        if (timer.updateCondition((AdvancementCategoryCondition) condition, advancement) && timer.isCoop())
                            TimerPacketHandler.clientSend(InGameTimer.getInstance(), InGameTimer.getCompletedInstance());
                    }
                }
                timer.checkConditions();
            }

            //How Did We Get Here
            if (timer.getCategory() == RunCategories.HOW_DID_WE_GET_HERE && Objects.equals(advancement.getId().toString(), new Identifier("nether/all_effects").toString())) {
                InGameTimer.complete();
            }

            //Hero of Village
            if (timer.getCategory() == RunCategories.HERO_OF_VILLAGE && Objects.equals(advancement.getId().toString(), new Identifier("adventure/hero_of_the_village").toString())) {
                InGameTimer.complete();
            }

            //Arbalistic
            if (timer.getCategory() == RunCategories.ARBALISTIC && Objects.equals(advancement.getId().toString(), new Identifier("adventure/arbalistic").toString())) {
                InGameTimer.complete();
            }
        }
        return entry.getValue();
    }

    @Inject(at = @At("RETURN"), method = "onAdvancements")
    public void onComplete(AdvancementUpdateS2CPacket packet, CallbackInfo ci) {
        InGameTimer timer = InGameTimer.getInstance();

        //All Advancements
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.ALL_ADVANCEMENTS) {
            if (getCompleteAdvancementsCount() >= 80) InGameTimer.complete();
        }

        //Half%
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.HALF) {
            if (getCompleteAdvancementsCount() >= 40) InGameTimer.complete();
        }

        //(PogLoot) Quater
        if (timer.getStatus() != TimerStatus.NONE && timer.getCategory() == RunCategories.POGLOOT_QUATER) {
            if (getCompleteAdvancementsCount() >= 20) InGameTimer.complete();
        }
    }

    private int getCompleteAdvancementsCount() {
        int count = 0;
        for (Map.Entry<String, TimerAdvancementTracker.AdvancementTrack> track : InGameTimer.getInstance().getAdvancementsTracker().getAdvancements().entrySet()) {
            if (track.getValue().isAdvancement() && track.getValue().isComplete()) count++;
        }
        return count;
    }
}