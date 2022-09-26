package de.m_marvin.holostructures.client.blueprints;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import de.m_marvin.holostructures.UtilHelper;
import de.m_marvin.holostructures.client.Config;
import de.m_marvin.holostructures.client.blueprints.BlueprintLoader.BlueprintFormat;
import de.m_marvin.holostructures.client.worldaccess.ILevelAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;

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
		if (!accessor.hasWriteAccess()) return false;
		if (this.clipboard == null) return false;
		createWorkerThread(() -> this.clipboard.pasteBlueprint(accessor, originPosition, pasteEntities), onCompleted);
		return true;
	}

	public boolean saveClipboard(String path, boolean override) throws CommandSyntaxException {
		try {
			int fileExtensionSepperator = path.lastIndexOf(".");
			String fileExtension = path.substring(fileExtensionSepperator + 1);
			BlueprintFormat format = BlueprintFormat.getFormat(fileExtension);
			if (format == null) format = Config.DEFAULT_BLUEPRINT_FORMAT.get();
			File file = UtilHelper.resolvePath(path.substring(0, fileExtensionSepperator) + "." + format.getFileExtension());
			if (file.isFile() && !override) throw new SimpleCommandExceptionType(new TranslatableComponent("loader.error.cantoverride")).create();
			File folder = file.getParentFile();
			if (!folder.isDirectory()) folder.mkdirs();
			OutputStream outputStream = new FileOutputStream(file);
			BlueprintLoader.saveBlueprint(outputStream, format, this.clipboard);
			outputStream.close();
		} catch (IOException e) {
			throw new SimpleCommandExceptionType(new TranslatableComponent("loader.error.ioexception", e.getMessage())).create();
		}
		return true;
	}
	
	public boolean loadClipboard(String path) throws CommandSyntaxException {
		try {
			InputStream inputSteam = new FileInputStream(UtilHelper.resolvePath(path));
			int fileExtensionSepperator = path.lastIndexOf(".");
			String fileExtension = path.substring(fileExtensionSepperator + 1);
			BlueprintFormat format = BlueprintFormat.getFormat(fileExtension);
			if (format == null) return false;
			Optional<Blueprint> blueprint = BlueprintLoader.loadBlueprint(inputSteam, format);
			if (!blueprint.isPresent()) return false;
			this.clipboard = blueprint.get();
			inputSteam.close();
		} catch (IOException e) {
			throw new SimpleCommandExceptionType(new TranslatableComponent("loader.error.ioexception", e.getMessage())).create();
		}
		return true;
	}
	
	public Blueprint getClipboard() {
		return clipboard;
	}
	
	/* Worker Thread */
	
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
