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

import java.util.List;

/**
 * <h2><code>ToSyncClientPointsPacket</code></h2>
 * <p>服务端同步信号点到客户端</p>
 *
 * @param playerUUIDStr 玩家 UUID 字符串列表
 * @param pointData     信号点数据列表（每个 CompoundTag 包含一个信号点的完整信息）
 */
public record ToSyncClientSignalsPacket(List<String> playerUUIDStr,
                                        List<CompoundTag> pointData) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ToSyncClientSignalsPacket> TYPE = new CustomPacketPayload.Type<>(ResUtil.getRes("to_sync_client_points"));

	public static final StreamCodec<ByteBuf, ToSyncClientSignalsPacket> CODEC = StreamCodec.composite(
			ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
			ToSyncClientSignalsPacket::playerUUIDStr,
			ByteBufCodecs.COMPOUND_TAG.apply(ByteBufCodecs.list()),
			ToSyncClientSignalsPacket::pointData,
			ToSyncClientSignalsPacket::new
	);

	@Override
	public @NotNull Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
