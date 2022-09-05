package de.m_marvin.holostructures.client.blueprints;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import de.m_marvin.holostructures.ILevelAccessor;
import net.minecraft.core.BlockPos;

public class BlueprintManager {
	
	protected Blueprint clipboard;
	protected Thread workerThread;
	private Boolean workerResult;
	
	public void copySelection(ILevelAccessor accessor, BlockPos corner1, BlockPos corner2, boolean copyEntities, @Nullable Runnable onCompleted) {
		createWorkerThread(() -> {
			this.clipboard = Blueprint.createBlueprint(accessor, corner1, corner2, copyEntities);
			if (onCompleted != null) onCompleted.run();
		});
	}
	
	public boolean pasteClipboard(ILevelAccessor accessor, BlockPos originPosition, boolean pasteEntities, @Nullable Runnable onCompleted) {
		if (!accessor.hasOPAccess()) return false;
		if (this.clipboard == null) return false;
		createWorkerThread(() -> this.clipboard.pasteBlueprint(accessor, originPosition, pasteEntities), onCompleted);
		return true;
	}
	
	public Blueprint getClipboard() {
		return clipboard;
	}
	
	private void createWorkerThread(Supplier<Boolean> task, Runnable onCompleted) {
		if (this.workerThread != null && this.workerThread.isAlive()) throw new IllegalStateException("BlueprintManager is still executing last tasks");
		this.workerThread = new Thread(() -> {
			workerResult = task.get();
			onCompleted.run();
		}, "BlueprintWorker");
		this.workerThread.start();
	}
	
	private void createWorkerThread(Runnable task) {
		if (this.workerThread != null && this.workerThread.isAlive()) throw new IllegalStateException("BlueprintManager is still executing last tasks");
		this.workerThread = new Thread(task, "BlueprintWorker");
		this.workerThread.start();
	}
	
	public boolean getResult() {
		return workerResult;
	}
	
	public boolean isWorking() {
		return this.workerThread != null && this.workerThread.isAlive();
	}
	
	public void abbortTask() {
		this.workerThread = null; // Clearing the thread will cause the loop in Blueprint and so the thread to run out.
	}
	
}
