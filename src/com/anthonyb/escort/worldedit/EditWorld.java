package com.anthonyb.escort.worldedit;

import org.bukkit.Location;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSession.ReorderMode;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockState;

public class EditWorld {

	public static World convertWorld(org.bukkit.World world) {
		return new BukkitWorld(world);
	}

	public static BlockVector3 convertLocation(Location loc) {
		return BlockVector3.at(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	public static EditSession set(World world, BlockState blockState, BlockVector3 startPos, BlockVector3 endPos)
			throws WorldEditException {
		CuboidRegion region = new CuboidRegion(world, startPos, endPos);
		// My way of getting around setBlocks not working:
		// BlockArrayClipboard blockArrayClipboard = new BlockArrayClipboard(region);
		// for (BlockVector3 bv3 : region) {
		// blockArrayClipboard.setBlock(bv3, blockState);
		// }
		// paste(new PasteClipboardBuilder(world, blockArrayClipboard, startPos),
		// false);
		// == My way end ==

		// Anthony TODO - check if the proper way actually works now in 1.18

		// Proper (non-working) way to set using WorldEditAPI:

		EditSession editSession = WorldEdit.getInstance().newEditSession(world);
		editSession.setReorderMode(ReorderMode.FAST);

		// CuboidRegion region = new CuboidRegion(world, startPos, endPos);

		// This method (setBlocks) doesn't seem to work when using FAWE, so instead we
		// will create a fake clipboard to paste
		// Note: editSession.setBlock works, but setting blocks one by one runs each
		// call to the bukkit world separately and is laggy
		editSession.setBlocks(region, blockState);

		editSession.close();
		return editSession;
	}
/*
	public static void clear(World world, BlockVector3 startPos, BlockVector3 endPos) throws WorldEditException {
		set(world, BlockTypes.AIR.getDefaultState(), startPos, endPos);
	}

	public static BlockArrayClipboard copy(World world, BlockVector3 startPos, BlockVector3 endPos)
			throws WorldEditException {
		Bukkit.broadcastMessage("COPYING FROM " + startPos.toString());
		Bukkit.broadcastMessage("COPYING TO " + endPos.toString());
		CuboidRegion region = new CuboidRegion(world, startPos, endPos);
		BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
		EditSession editSession = WorldEdit.getInstance().newEditSession(world);
		ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard,
				region.getMinimumPoint());
		forwardExtentCopy.setCopyingEntities(true);
		forwardExtentCopy.setCopyingBiomes(true);
		Operations.complete(forwardExtentCopy);
		// According to documentation, close edit session after every operation.
		editSession.close();
		return clipboard;
	}

	public static void paste(PasteClipboardBuilder pasteClipboardBuilder, boolean ignoreAirBlocks)
			throws WorldEditException {
		// Fawe fawe = Fawe.instance();
		World world = pasteClipboardBuilder.getWorld();
		Bukkit.broadcastMessage("WORLD IS " + world.toString());
		Bukkit.broadcastMessage(world.getName() + " worldname");
		ClipboardHolder clipboardHolder = pasteClipboardBuilder.getClipboardHolder();
		BlockVector3 toPos = pasteClipboardBuilder.getToPosition();

		EditSession editSession = WorldEdit.getInstance().newEditSession(world);
		Bukkit.broadcastMessage("Pasting to toPos: " + toPos.toString());
		Bukkit.broadcastMessage("Transform: " + clipboardHolder.getTransform().toString());
		Bukkit.broadcastMessage(clipboardHolder.getClipboard().getOrigin().toString());
		Operation paste = clipboardHolder.createPaste(editSession).to(toPos).copyEntities(true)
				.copyBiomes(true)
				.ignoreAirBlocks(ignoreAirBlocks).build();
		Operations.complete(paste);
		// According to documentation, close edit session after every operation.
		editSession.close();
	}

	public static void saveToSchematic(OutputStream outputStream, World world, BlockVector3 start, BlockVector3 end)
			throws IOException, WorldEditException {
		BlockArrayClipboard clipboard = copy(world, start, end);
		try (ClipboardWriter writer = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getWriter(outputStream)) {
			writer.write(clipboard);
		}
	}

	public static void saveToSchematic(File file, World world, BlockVector3 start, BlockVector3 end)
			throws IOException, WorldEditException {
		saveToSchematic(new FileOutputStream(file), world, start, end);
	}

	public static Clipboard loadSchematicToClipboard(InputStream inputStream) throws IOException {
		try (ClipboardReader reader = BuiltInClipboardFormat.SPONGE_SCHEMATIC.getReader(inputStream)) {
			return reader.read();
		}
	}

	public static Clipboard loadSchematicToClipboard(File file) throws IOException {
		return loadSchematicToClipboard(new FileInputStream(file));
	}

	public static void paste(Clipboard clipboard, World world, BlockVector3 location)
			throws WorldEditException {
		try (EditSession editSession = WorldEdit.getInstance().newEditSession(world)) {
			Operation operation = new ClipboardHolder(clipboard).createPaste(editSession).to(location).build();
			Operations.complete(operation);
		}
	}

	public static void copyAndPasteTEST(World world, BlockVector3 startPos, BlockVector3 endPos, BlockVector3 location)
			throws WorldEditException {
		Bukkit.broadcastMessage("COPYING FROM " + startPos.toString());
		Bukkit.broadcastMessage("COPYING TO " + endPos.toString());
		CuboidRegion region = new CuboidRegion(world, startPos, endPos);
		BlockArrayClipboard clipboard = new BlockArrayClipboard(region);
		EditSession editSession = WorldEdit.getInstance().newEditSession(world);
		ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, clipboard,
				region.getMinimumPoint());
		forwardExtentCopy.setCopyingEntities(true);
		forwardExtentCopy.setCopyingBiomes(true);
		Operations.complete(forwardExtentCopy);

		Operation operation = new ClipboardHolder(clipboard).createPaste(editSession).to(location).build();
		Operations.complete(operation);
		// According to documentation, close edit session after every operation.
		editSession.close();
	}
	 */
}
