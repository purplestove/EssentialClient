package me.senseiwells.essentialclient.clientscript.definitions;

import me.senseiwells.arucas.api.docs.ClassDoc;
import me.senseiwells.arucas.api.docs.FunctionDoc;
import me.senseiwells.arucas.classes.ClassInstance;
import me.senseiwells.arucas.classes.PrimitiveDefinition;
import me.senseiwells.arucas.core.Interpreter;
import me.senseiwells.arucas.utils.Arguments;
import me.senseiwells.arucas.utils.LocatableTrace;
import me.senseiwells.arucas.utils.MemberFunction;
import me.senseiwells.arucas.utils.Util;
import me.senseiwells.essentialclient.clientscript.core.MinecraftAPI;
import me.senseiwells.essentialclient.utils.clientscript.ScreenRemapper;
import me.senseiwells.essentialclient.utils.render.Texts;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.ItemGroup;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.List;

import static me.senseiwells.arucas.utils.Util.Types.STRING;
import static me.senseiwells.essentialclient.clientscript.core.MinecraftAPI.SCREEN;

@ClassDoc(
	name = SCREEN,
	desc = "This allows you to get information about the player's current screen.",
	importPath = "Minecraft",
	language = Util.Language.Java
)
public class ScreenDef extends PrimitiveDefinition<Screen> {
	public ScreenDef(Interpreter interpreter) {
		super(MinecraftAPI.SCREEN, interpreter);
	}

	@Deprecated
	@Override
	public ClassInstance create(Screen value) {
		return super.create(value);
	}

	@Override
	public String toString$Arucas(ClassInstance instance, Interpreter interpreter, LocatableTrace trace) {
		return "Screen{screen=" + ScreenRemapper.getScreenName(instance.asPrimitive(this).getClass()) + "}";
	}

	@Override
	public List<MemberFunction> defineMethods() {
		return List.of(
			MemberFunction.of("getName", this::getName),
			MemberFunction.of("getTitle", this::getTitle)
		);
	}

	@FunctionDoc(
		name = "getName",
		desc = "Gets the name of the specific screen",
		returns = {STRING, "the screen name, if you are in the creative menu it will return the name of the tab you are on"},
		examples = "screen.getName()"
	)
	private String getName(Arguments arguments) {
		return ScreenRemapper.getScreenName(arguments.nextPrimitive(this).getClass());
	}

	@FunctionDoc(
		name = "getTitle",
		desc = "Gets the title of the specific screen",
		returns = {STRING, "the screen title as text, this may include formatting, and custom names for the screen if applicable"},
		examples = "screen.getTitle()"
	)
	private MutableText getTitle(Arguments arguments) {
		Screen screen = arguments.nextPrimitive(this);
		Text title = screen.getTitle();
		if (screen instanceof CreativeInventoryScreen creativeInventoryScreen) {
			int tabIndex = creativeInventoryScreen.getSelectedTab();
			return Texts.literal(ItemGroup.GROUPS[tabIndex].getName());
		}
		return title == null ? null : title.copy();
	}
}
