package essentialclient.clientscript.values;

import essentialclient.utils.EssentialUtils;
import me.senseiwells.arucas.api.ArucasClassExtension;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.utils.ArucasFunctionMap;
import me.senseiwells.arucas.utils.Context;
import me.senseiwells.arucas.utils.impl.ArucasList;
import me.senseiwells.arucas.values.ListValue;
import me.senseiwells.arucas.values.NullValue;
import me.senseiwells.arucas.values.StringValue;
import me.senseiwells.arucas.values.Value;
import me.senseiwells.arucas.values.functions.MemberFunction;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class RecipeValue extends Value<Recipe<?>> {
	public RecipeValue(Recipe<?> value) {
		super(value);
	}

	@Override
	public Value<Recipe<?>> copy(Context context) throws CodeError {
		return this;
	}

	@Override
	public String getAsString(Context context) throws CodeError {
		return "Recipe{" + this.value.getId() + "}";
	}

	@Override
	public int getHashCode(Context context) throws CodeError {
		return this.value.hashCode();
	}

	@Override
	public boolean isEquals(Context context, Value<?> value) throws CodeError {
		return this.value == value.value;
	}

	public static class ArucasRecipeClass extends ArucasClassExtension {
		public ArucasRecipeClass() {
			super("Recipe");
		}

		@Override
		public Map<String, Value<?>> getDefinedStaticVariables() {
			ClientPlayNetworkHandler networkHandler = EssentialUtils.getNetworkHandler();
			if (networkHandler == null) {
				return super.getDefinedStaticVariables();
			}
			Map<String, Value<?>> recipeMap = new HashMap<>();
			ArucasList recipeList = new ArucasList();
			for (Recipe<?> recipe : networkHandler.getRecipeManager().values()) {
				RecipeValue recipeValue = new RecipeValue(recipe);
				recipeMap.put(recipe.getId().getPath().toUpperCase(Locale.ROOT), recipeValue);
				recipeList.add(recipeValue);
			}
			recipeMap.put("ALL", new ListValue(recipeList));
			return recipeMap;
		}

		@Override
		public ArucasFunctionMap<MemberFunction> getDefinedMethods() {
			return ArucasFunctionMap.of(
				new MemberFunction("getId", this::getId),
				new MemberFunction("getCraftingType", this::getCraftingType),
				new MemberFunction("getOutput", this::getOutput),
				new MemberFunction("getIngredients", this::getIngredients)
			);
		}

		private Value<?> getId(Context context, MemberFunction function) throws CodeError {
			RecipeValue thisValue = function.getThis(context, RecipeValue.class);
			return StringValue.of(thisValue.value.getId().getPath());
		}

		private Value<?> getCraftingType(Context context, MemberFunction function) throws CodeError {
			RecipeValue thisValue = function.getThis(context, RecipeValue.class);
			Identifier identifier = Registry.RECIPE_TYPE.getId(thisValue.value.getType());
			return identifier == null ? NullValue.NULL :StringValue.of(identifier.getPath());
		}

		private Value<?> getOutput(Context context, MemberFunction function) throws CodeError {
			RecipeValue thisValue = function.getThis(context, RecipeValue.class);
			return new ItemStackValue(thisValue.value.getOutput());
		}

		private Value<?> getIngredients(Context context, MemberFunction function) throws CodeError {
			RecipeValue thisValue = function.getThis(context, RecipeValue.class);
			ArucasList recipeIngredients = new ArucasList();
			for (Ingredient ingredient : thisValue.value.getIngredients()) {
				ArucasList slotIngredients = new ArucasList();
				for (ItemStack itemStack : ingredient.getMatchingStacks()) {
					slotIngredients.add(new ItemStackValue(itemStack));
				}
				recipeIngredients.add(new ListValue(slotIngredients));
			}
			return new ListValue(recipeIngredients);
		}

		@Override
		public Class<?> getValueClass() {
			return RecipeValue.class;
		}
	}
}
