package com.redlimerl.speedrunigt.gui.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.redlimerl.speedrunigt.SpeedRunIGT;
import com.redlimerl.speedrunigt.version.ScreenTexts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.Locale;

import static com.redlimerl.speedrunigt.SpeedRunIGTUpdateChecker.*;

public class SpeedRunIGTInfoScreen extends Screen {

    private final Screen parent;

    private ButtonWidget update;

    public SpeedRunIGTInfoScreen(Screen parent) {
        super(new TranslatableText("speedrunigt.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        checkUpdate();

        update = addButton(new ButtonWidget(width / 2 - 155, height - 104, 150, 20, new TranslatableText("speedrunigt.menu.download_update").asFormattedString(), (ButtonWidget button) -> Util.getOperatingSystem().open(UPDATE_URL)));
        update.active = false;
        addButton(new ButtonWidget(width / 2 + 5, height - 104, 150, 20, new TranslatableText("speedrunigt.menu.latest_change_log").asFormattedString(), (ButtonWidget button) -> Util.getOperatingSystem().open("https://github.com/RedLime/SpeedRunIGT/releases/latest")));

        addButton(new ButtonWidget(width / 2 - 155, height - 80, 150, 20, new TranslatableText("speedrunigt.menu.open_github_repo").asFormattedString(), (ButtonWidget button) -> Util.getOperatingSystem().open("https://github.com/RedLime/SpeedRunIGT/")));
        addButton(new ButtonWidget(width / 2 + 5, height - 80, 150, 20, new TranslatableText("speedrunigt.menu.open_support_page").asFormattedString(), (ButtonWidget button) -> Util.getOperatingSystem().open("https://ko-fi.com/redlimerl")));
        addButton(new ButtonWidget(width / 2 - 100, height - 40, 200, 20, ScreenTexts.BACK, (ButtonWidget button) -> onClose()));
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        this.renderBackground();
        RenderSystem.pushMatrix();
        RenderSystem.scalef(1.5f, 1.5f, 1.5f);
        this.drawCenteredString(this.textRenderer, this.title.asFormattedString(), this.width / 3, 15, 16777215);
        RenderSystem.popMatrix();
        this.drawCenteredString(this.textRenderer, new LiteralText("Made by RedLime").asFormattedString(), this.width / 2, 50, 16777215);
        this.drawCenteredString(this.textRenderer, new LiteralText("Discord : RedLime#0817").asFormattedString(), this.width / 2, 62, 16777215);
        this.drawCenteredString(this.textRenderer,
                new LiteralText("Version : "+ SpeedRunIGT.MOD_VERSION.split("\\+")[0]).asFormattedString(), this.width / 2, 78, 16777215);
        if (UPDATE_STATUS != UpdateStatus.NONE) {
            if (UPDATE_STATUS == UpdateStatus.OUTDATED) {
                update.active = true;
                this.drawCenteredString(this.textRenderer, new LiteralText("Updated Version : "+ UPDATE_VERSION).formatted(Formatting.YELLOW).asFormattedString(), this.width / 2, 88, 16777215);
            }
            this.drawCenteredString(this.textRenderer,
                    new TranslatableText("speedrunigt.message.update."+UPDATE_STATUS.name().toLowerCase(Locale.ROOT)).asFormattedString(),
                    this.width / 2, 116, 16777215);
        }

        super.render(mouseX, mouseY, delta);
    }

    @Override
    public void onClose() {
        if (this.client != null) {
            this.client.openScreen(parent);
        }
    }
}
