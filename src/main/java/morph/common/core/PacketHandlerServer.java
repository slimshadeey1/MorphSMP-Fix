package morph.common.core;

import cpw.mods.fml.common.network.*;
import net.minecraft.entity.player.*;
import net.minecraft.network.*;
import net.minecraft.network.packet.*;

import java.io.*;

public class PacketHandlerServer
        implements IPacketHandler {
    @Override
    public void onPacketData(INetworkManager manager, Packet250CustomPayload packet, Player plyr) {
        EntityPlayerMP player = (EntityPlayerMP) plyr;
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(packet.data));
        try {
            int id = stream.readByte();
            switch (id) {
                case 0: {
                    break;
                }
            }
        } catch (IOException e) {
        }
    }
}
