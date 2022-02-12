package essentialclient.clientscript.extensions;

import essentialclient.clientscript.events.CancelEvent;
import essentialclient.clientscript.events.MinecraftScriptEvent;
import essentialclient.clientscript.events.MinecraftScriptEvents;
import me.senseiwells.arucas.api.ArucasThreadHandler;
import me.senseiwells.arucas.api.ISyntax;
import me.senseiwells.arucas.api.wrappers.ArucasConstructor;
import me.senseiwells.arucas.api.wrappers.ArucasFunction;
import me.senseiwells.arucas.api.wrappers.ArucasWrapper;
import me.senseiwells.arucas.api.wrappers.IArucasWrappedClass;
import me.senseiwells.arucas.throwables.CodeError;
import me.senseiwells.arucas.throwables.RuntimeError;
import me.senseiwells.arucas.utils.Context;
import me.senseiwells.arucas.values.BooleanValue;
import me.senseiwells.arucas.values.NullValue;
import me.senseiwells.arucas.values.StringValue;
import me.senseiwells.arucas.values.Value;
import me.senseiwells.arucas.values.functions.FunctionValue;

import java.util.List;

@ArucasWrapper(name = "GameEvent")
public class GameEventWrapper implements IArucasWrappedClass {
	private Context eventContext;
	private MinecraftScriptEvent minecraftEvent;
	private FunctionValue function;
	private boolean runOnMainThread;

	@ArucasConstructor
	public void constructor(Context eventContext, StringValue eventName, FunctionValue function, BooleanValue cancellable) {
		this.minecraftEvent = MinecraftScriptEvents.getEvent(eventName.value);
		if (this.minecraftEvent == null) {
			throw new RuntimeException("No such event '%s'".formatted(eventName.value));
		}
		this.eventContext = eventContext;
		this.function = function;
		this.runOnMainThread = cancellable.value;
	}

	@ArucasConstructor
	public void constructor(Context eventContext, StringValue eventName, FunctionValue function) {
		this.constructor(eventContext, eventName, function, BooleanValue.FALSE);
	}

	public boolean callFunction(List<Value<?>> arguments) {
		Context branchContext = this.eventContext.createBranch();
		ArucasThreadHandler threadHandler = this.eventContext.getThreadHandler();
		if (!this.runOnMainThread) {
			threadHandler.runAsyncFunctionInContext(
				branchContext,
				context -> this.function.call(context, arguments),
				"ClientScript Event"
			);
			return false;
		}
		try {
			this.function.call(branchContext, arguments);
			return false;
		}
		catch (CancelEvent cancelEvent) {
			if (this.minecraftEvent.canCancel()) {
				return true;
			}
			threadHandler.tryError(branchContext, new RuntimeError(
				"Cannot cancel event '%s'".formatted(this.minecraftEvent),
				ISyntax.empty(),
				branchContext
			));
		}
		catch (CodeError codeError) {
			threadHandler.tryError(branchContext, codeError);
		}
		threadHandler.stop();
		return false;
	}

	@ArucasFunction
	public BooleanValue isRegistered(Context context) {
		return BooleanValue.of(this.minecraftEvent.isEventRegistered(this));
	}

	@ArucasFunction
	public NullValue register(Context context) {
		this.minecraftEvent.registerEvent(this);
		return NullValue.NULL;
	}

	@ArucasFunction
	public BooleanValue unregister(Context context) {
		return BooleanValue.of(this.minecraftEvent.unregisterEvent(this));
	}

	@ArucasFunction
	public static NullValue cancel(Context context) throws CancelEvent {
		throw CancelEvent.INSTANCE;
	}

	@ArucasFunction
	public static NullValue unregisterAll(Context context) {
		MinecraftScriptEvents.clearEventFunctions();
		return NullValue.NULL;
	}
}
