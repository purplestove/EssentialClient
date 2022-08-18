package me.senseiwells.essentialclient.utils.clientscript.impl;

import me.senseiwells.arucas.classes.ClassInstance;
import me.senseiwells.arucas.core.Interpreter;
import me.senseiwells.arucas.utils.ArucasFunction;
import me.senseiwells.essentialclient.clientscript.events.CancelEvent;
import me.senseiwells.essentialclient.clientscript.events.MinecraftScriptEvent;
import me.senseiwells.essentialclient.utils.EssentialUtils;

import java.util.List;
import java.util.UUID;

public class ScriptEvent {
	private final Interpreter interpreter;
	private final MinecraftScriptEvent event;
	private final ArucasFunction function;
	private final boolean cancellable;

	public ScriptEvent(Interpreter interpreter, MinecraftScriptEvent event, ArucasFunction function, boolean cancellable) {
		this.interpreter = interpreter.branch();
		this.event = event;
		this.function = function;
		this.cancellable = cancellable;
	}

	public UUID getId() {
		return this.interpreter.getProperties().getId();
	}

	public Interpreter getInterpreter() {
		return this.interpreter.branch();
	}

	public boolean isRegistered() {
		return this.event.isEventRegistered(this);
	}

	public void register() {
		this.event.registerEvent(this);
	}

	public boolean unregister() {
		return this.event.unregisterEvent(this);
	}

	public boolean invoke(List<ClassInstance> arguments) {
		Interpreter branch = this.interpreter.branch();
		int count = this.function.getCount();
		List<ClassInstance> newArgs = count < arguments.size() ? arguments.subList(0, count) : arguments;

		if (this.event.isThreadDefinable() && !this.cancellable && EssentialUtils.getClient().isOnThread()) {
			branch.getThreadHandler().runAsync(() -> {
				this.function.invoke(branch, newArgs);
				return null;
			});
			return false;
		}
		Boolean shouldCancel = branch.getThreadHandler().wrapSafe(() -> {
			try {
				this.function.invoke(branch, newArgs);
				return Boolean.FALSE;
			} catch (CancelEvent cancelEvent) {
				if (this.event.canCancel() && EssentialUtils.getClient().isOnThread()) {
					return Boolean.TRUE;
				}
				throw cancelEvent;
			}
		});
		// It's nullable
		return shouldCancel == Boolean.TRUE;
	}
}