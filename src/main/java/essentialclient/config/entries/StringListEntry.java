package essentialclient.config.entries;

import com.google.common.collect.ImmutableList;
import essentialclient.config.ConfigListWidget;
import essentialclient.config.clientrule.ClientRuleHelper;
import essentialclient.config.clientrule.StringClientRule;
import essentialclient.config.rulescreen.RulesScreen;
import essentialclient.config.rulescreen.ServerRulesScreen;
import essentialclient.feature.EssentialCarpetClient;
import essentialclient.utils.render.RuleWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class StringListEntry extends BaseListEntry {
	private final TextFieldWidget textField;
	private boolean invalid;
	private boolean changed = false;

	public StringListEntry(final StringClientRule clientRule, final MinecraftClient client, final RulesScreen rulesScreen) {
		super(clientRule, client, rulesScreen, rulesScreen instanceof ServerRulesScreen);
		TextFieldWidget stringField = new TextFieldWidget(client.textRenderer, 0, 0, 96, 14, new LiteralText("Type a string value"));
		stringField.setText(clientRule.getValue());
		stringField.setChangedListener(s -> this.checkForInvalid(stringField));
		this.textField = stringField;
		this.resetButton = new ButtonWidget(0, 0, 50, 20, new LiteralText(I18n.translate("controls.reset")), (buttonWidget) -> {
			if (this.isServerScreen && EssentialCarpetClient.handleRuleChange(clientRule.getName(), clientRule.getDefaultValue())) {
				return;
			}
			clientRule.resetToDefault();
			stringField.setText(clientRule.getDefaultValue());
			ClientRuleHelper.writeSaveFile();
			clientRule.run();
		});
		rulesScreen.getStringFieldList().add(this.textField);
	}

	@Override
	public List<? extends Element> children() {
		return ImmutableList.of(this.resetButton, this.textField);
	}
	
	@Override
	public boolean charTyped(char chr, int keyCode) {
		return this.textField.charTyped(chr, keyCode);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER) {
			this.textField.setText(this.textField.getText());
			this.textField.changeFocus(false);
			if (!this.invalid && this.isServerScreen && EssentialCarpetClient.canChangeRule()) {
				EssentialCarpetClient.handleRuleChange(this.ruleName, this.clientRule.getValue().toString());
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers) || this.textField.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void render(MatrixStack matrices, int index, int y, int x, int width, int height, int mouseX, int mouseY, boolean hovering, float delta) {
		TextRenderer font = this.client.textRenderer;
		float fontX = (float)(x + 90 - ConfigListWidget.length);
		float fontY = (float)(y + height / 2 - 9 / 2);

		this.ruleWidget = new RuleWidget(this.ruleName, x - 50, y + 2, 200, 15);
		this.ruleWidget.drawRule(matrices, font, fontX, fontY, 16777215);

		this.resetButton.x = x + 290;
		this.resetButton.y = y;

		this.resetButton.active = this.clientRule.isNotDefault() || this.invalid;

		this.textField.x = x + 182;
		this.textField.y = y + 3;
		this.textField.setEditableColor(this.invalid ? 16733525 : 16777215);
		if (this.invalid) {
			DiffuseLighting.enableGuiDepthLighting();
			this.client.getItemRenderer().renderGuiItemIcon(new ItemStack(Items.BARRIER), this.textField.x + this.textField.getWidth() - 18, this.textField.y- 1);
			DiffuseLighting.disableGuiDepthLighting();
		}

		this.textField.render(new MatrixStack(), mouseX, mouseY, delta);
		this.resetButton.render(new MatrixStack(), mouseX, mouseY, delta);
	}
	
	private void setInvalid(boolean invalid) {
		this.invalid = invalid;
		this.rulesScreen.setInvalid(invalid);
	}
	
	private void checkForInvalid(TextFieldWidget widget) {
		this.changed = true;
		boolean empty = widget.getText().isEmpty();
		if (empty) {
			this.rulesScreen.setEmpty(true);
			this.setInvalid(true);
		}
		else {
			this.setInvalid(false);
			if (!this.isServerScreen) {
				((StringClientRule) this.clientRule).setValue(widget.getText());
				ClientRuleHelper.writeSaveFile();
			}
		}
	}

	@Override
	public List<? extends Selectable> selectableChildren() {
		return ImmutableList.of(this.editButton, this.resetButton, this.textField);
	}

	@Override
	public void updateEntryOnClose() {
		if (this.invalid) {
			this.clientRule.resetToDefault();
		}
		if (this.changed && EssentialCarpetClient.canChangeRule() && this.isServerScreen) {
			EssentialCarpetClient.handleRuleChange(this.ruleName, this.clientRule.getValue().toString());
		}
	}
}
