/*
 * Copyright (c) 2026 Xiaosu
 *
 * 本程序是自由软件：你可以在 GNU Lesser General Public License v3.0
 * 的条款下重新发布和/或修改它，并附加以下限制：
 *
 * 1. 禁止商业用途：不得将本模组或其修改版本用于任何商业目的，
 *    包括但不限于付费服务器、付费整合包、周边商品销售等。
 * 2. 允许个人学习、研究及非盈利社区服务器使用。
 * 3. 修改版本必须保留本版权声明及原始许可证。
 *
 * 本程序按"原样"提供，不提供任何担保。详见 LICENSE 文件。
 */

package top.xiaosuoaa.actionpanel.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;
import top.xiaosuoaa.actionpanel.util.ResUtil;

/**
 * <h2><code>SignalDataClientSendPacket</code></h2>
 * <p>客户端发送数据点</p>
 *
 * @param packet
 */
public record ClientSignalSendPacket(CompoundTag packet) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ClientSignalSendPacket> TYPE = new CustomPacketPayload.Type<>(ResUtil.getRes("signal_send_data"));
	public static final StreamCodec<ByteBuf, ClientSignalSendPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.COMPOUND_TAG,
			ClientSignalSendPacket::packet,
			ClientSignalSendPacket::new
	);

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
