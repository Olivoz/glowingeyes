package me.andreasmelone.glowingeyes.common.packets;

import io.netty.buffer.ByteBuf;
import me.andreasmelone.glowingeyes.GlowingEyes;
import me.andreasmelone.glowingeyes.client.data.ByteArray;
import me.andreasmelone.glowingeyes.common.capability.GlowingEyesCapability;
import me.andreasmelone.glowingeyes.common.capability.GlowingEyesProvider;
import me.andreasmelone.glowingeyes.common.capability.IGlowingEyesCapability;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;

import java.awt.*;
import java.util.List;
import java.util.UUID;

public class ClientCapabilityMessage extends MessageBase<ClientCapabilityMessage> {
    public IGlowingEyesCapability cap = new GlowingEyesCapability();
    public EntityPlayer player;

    @Override
    public void handleClientSide(ClientCapabilityMessage message, EntityPlayer player) {
        IGlowingEyesCapability capability = message.cap;

        if (player != null) {
            IGlowingEyesCapability old = player.getCapability(GlowingEyesProvider.CAPABILITY, EnumFacing.UP);

            old.setGlowingEyesMap(capability.getGlowingEyesMap());
        }
    }

    @Override
    public void handleServerSide(ClientCapabilityMessage message, EntityPlayer player) {
        EntityPlayerMP playerMP = (EntityPlayerMP)player;
        IGlowingEyesCapability capibility = message.cap;
        IGlowingEyesCapability old = playerMP.getCapability(GlowingEyesProvider.CAPABILITY, EnumFacing.UP);

        old.setGlowingEyesMap(capibility.getGlowingEyesMap());

        // This is done to be compatible with replaymod, but it doesn't work and Idk why
        NetworkHandler.sendToClient(new ClientCapabilityMessage(capibility, player), playerMP);

        List<UUID> playersTracking = GlowingEyes.proxy.getPlayersTracking().get(player);
        if(playersTracking == null) return;
        for(UUID pUUID : playersTracking) {
            EntityPlayerMP p = playerMP.getServer().getPlayerList().getPlayerByUUID(pUUID);
            if(p == null) return;
            NetworkHandler.sendToClient(new OtherPlayerCapabilityMessage(player, old), p);
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        if(buf.isReadable()) {
            int length = buf.getInt(0);
            byte[] bytes = new byte[length];
            buf.readBytes(bytes);

            for(int i = 0; i < length; i += 6) {
                int x = bytes[i];
                int y = bytes[i + 1];
                int red = bytes[i + 2];
                int green = bytes[i + 3];
                int blue = bytes[i + 4];
                int alpha = bytes[i + 5];
                cap.getGlowingEyesMap().put(new Point(x, y), new Color(red, green, blue, alpha));
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(cap.getGlowingEyesMap().size() * 6);
        for(Point point : cap.getGlowingEyesMap().keySet()) {
            buf.writeInt(point.x);
            buf.writeInt(point.y);
            buf.writeInt(cap.getGlowingEyesMap().get(point).getRed());
            buf.writeInt(cap.getGlowingEyesMap().get(point).getGreen());
            buf.writeInt(cap.getGlowingEyesMap().get(point).getBlue());
            buf.writeInt(cap.getGlowingEyesMap().get(point).getAlpha());
        }
    }

    public ClientCapabilityMessage(IGlowingEyesCapability cap, EntityPlayer player) {
        this.cap = cap;
        this.player = player;
    }

    public ClientCapabilityMessage() {

    }
}
