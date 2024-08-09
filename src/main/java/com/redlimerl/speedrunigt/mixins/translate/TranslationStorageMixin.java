package com.redlimerl.speedrunigt.mixins.translate;

import com.llamalad7.mixinextras.sugar.Local;
import com.redlimerl.speedrunigt.option.SpeedRunOption;
import com.redlimerl.speedrunigt.option.SpeedRunOptions;
import com.redlimerl.speedrunigt.utils.ResourcesHelper;
import net.minecraft.client.resource.language.TranslationStorage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Language;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(TranslationStorage.class)
public abstract class TranslationStorageMixin {

    @Shadow public abstract void load(ResourceManager container, List<String> list);

    @Shadow protected abstract void load(InputStream inputStream);

    @Inject(method = "load(Ljava/util/List;)V", at = @At("RETURN"))
    private void onLoad(List<Resource> resources, CallbackInfo ci) {
        resources.forEach(resource -> {
            // minecraft always loads en_us as a backup, if using only english translations just skip loading the other attempts
            if (SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS) && !resource.getId().getPath().equalsIgnoreCase("lang/en_us.json"))
                return;
            Optional.ofNullable(ResourcesHelper.toStream("/assets/speedrunigt/" + resource.getId().getPath())).ifPresent(this::load);
        });
    }

    @Inject(method = "load(Ljava/util/List;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/Resource;getInputStream()Ljava/io/InputStream;"), cancellable = true)
    private void cancelExternalLoadingOfTranslations(List<Resource> resources, CallbackInfo ci, @Local Resource resource) {
        if (SpeedRunOption.getOption(SpeedRunOptions.ALWAYS_ENGLISH_TRANSLATIONS) && resource.getId().getNamespace().equals("speedrunigt") && !resource.getId().getPath().equalsIgnoreCase("lang/en_us.json")) {
            ci.cancel();
        }
    }
}
