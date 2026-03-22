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

package top.xiaosuoaa.actionpanel.client.controller;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import top.xiaosuoaa.actionpanel.client.util.LookTraceUtil;
import top.xiaosuoaa.actionpanel.network.packet.ClientSignalSendPacket;
import top.xiaosuoaa.actionpanel.point.PointData;
import top.xiaosuoaa.actionpanel.point.Signal;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SignalDisplayer {
	private static final Minecraft MINECRAFT = Minecraft.getInstance();
	private final ArrayList<Signal> signals = new ArrayList<>();
	private final LevelAccessor levelAccessor;
	private ArrayList<UUID> members = new ArrayList<>();

	public SignalDisplayer(LevelAccessor levelAccessor) {
		this.levelAccessor = levelAccessor;
	}

	public void scanNewSignal(ArrayList<Signal> newSignals) {
		ArrayList<Signal> removeSignals = new ArrayList<>();
		signals.forEach(signal -> {
			if (!newSignals.contains(signal)) {
				// NOTE: 客户端删除信号
				signal.getPointRunner().clientPost(levelAccessor, signal, MINECRAFT.player);
				removeSignals.add(signal);
			}
		});
		signals.removeAll(removeSignals);

		newSignals.forEach(signal -> {
			if (!signals.contains(signal)) {
				// NOTE: 客户端添加信号
				signal.getPointRunner().clientPre(levelAccessor, signal, MINECRAFT.player);
				signals.add(signal);
			}
		});
	}

	public void sendSignal(PointData data) {
		if (MINECRAFT.player != null && MINECRAFT.level != null) {
			Vec3 lookLocation = LookTraceUtil.getLookLocation(MINECRAFT.level);
			Signal signal = new Signal(data, MINECRAFT.player.getUUID(), lookLocation, MINECRAFT.level.dimension());
			PacketDistributor.sendToServer(new ClientSignalSendPacket(signal.SignalToDataTag()));
		}
	}

	public LevelAccessor getLevelAccessor() {
		return levelAccessor;
	}

	public ArrayList<UUID> getMembers() {
		return members;
	}

	public ArrayList<Signal> getSignals() {
		return signals;
	}

	/**
	 * 扫描信号
	 *
	 * @param playerUUIDStr 玩家 UUID
	 * @param signals       信号
	 */
	public void scan(List<String> playerUUIDStr, ArrayList<Signal> signals) {
		this.members = new ArrayList<>();
		playerUUIDStr.forEach(uuidStr -> this.members.add(UUID.fromString(uuidStr)));
		scanNewSignal(signals);
	}
}
