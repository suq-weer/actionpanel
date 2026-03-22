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

package top.xiaosuoaa.actionpanel.controller;

import com.mojang.logging.LogUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.slf4j.Logger;
import top.xiaosuoaa.actionpanel.network.packet.ToSyncClientSignalsPacket;
import top.xiaosuoaa.actionpanel.point.PointData;
import top.xiaosuoaa.actionpanel.point.Signal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import static top.xiaosuoaa.actionpanel.config.MainConfig.expiration_time_mills;

public class SignalManager {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final HashMap<ServerPlayer, Member> playerMemberHashMap;
	private final Team defaultTeam = new Team();
	private final ArrayList<Team> teams;
	private final LevelAccessor levelAccessor;
	private final AppliedSignals appliedSignals;

	/**
	 * 标点管理服务端对象
	 *
	 * @param accessor 绑定的存档
	 */
	public SignalManager(LevelAccessor accessor) {
		this.levelAccessor = accessor;
		playerMemberHashMap = new HashMap<>();
		teams = new ArrayList<>();
		this.appliedSignals = new AppliedSignals(this);
	}

	/**
	 * 接收信号数据 0
	 *
	 * @param packet       数据包
	 * @param serverPlayer 发送信号的玩家（从网络上下文获取，更可靠）
	 */
	public void receiveSignalData0(CompoundTag packet, ServerPlayer serverPlayer) {
		// 收到信号创建包
		Signal signal = Signal.SignalFromDataTag(packet);
		Member member = this.getMember(serverPlayer);
		// 给自定义信号点运行器执行标点前检查
		if (signal.pointData().runner().serverPre(levelAccessor, signal, serverPlayer, member.getBelongTeam()) == 0) {
			MinecraftServer server = levelAccessor.getServer();
			this.createSignal(this.getMember(serverPlayer), signal.pointData(), signal.pos());
			LOGGER.info("玩家 {} 创建了信号点 {}", serverPlayer.getName().getString(), signal.pointData().displayName().getString());
			LOGGER.info("所有点信息：{}", appliedSignals.getSignalMap());
		}
	}

	public void syncDataToAllMembers() {
		syncDataToMembers(defaultTeam);
		for (Team team : teams) {
			syncDataToMembers(team);
		}
	}

	public void syncDataToMembers(Team team) {
		ArrayList<String> playerUUIDs = new ArrayList<>();
		ArrayList<CompoundTag> signalTags = new ArrayList<>();

		// 收集团队所有成员 UUID
		for (Member member : team.getMembers()) {
			playerUUIDs.add(member.getPlayer().getUUID().toString());
		}

		// 收集团队所有信号点数据
		for (Signal signal : appliedSignals.getSignalMap().values()) {
			signalTags.add(signal.SignalToDataTag());
		}

		// 创建同步数据包
		ToSyncClientSignalsPacket packet = new ToSyncClientSignalsPacket(playerUUIDs, signalTags);

		// 发送给团队所有成员
		for (Member member : team.getMembers()) {
			if (member.getPlayer() instanceof ServerPlayer serverPlayer) {
				PacketDistributor.sendToPlayer(serverPlayer, packet);
			}
		}
	}

	/**
	 * 服务器 Tick 运行器
	 *
	 * @param event 服务器 Tick 事件
	 */
	public void serverTickBus(ServerTickEvent event) {
		for (Signal signal : appliedSignals.getSignalMap().values()) {
			ServerPlayer serverPlayer = event.getServer().getPlayerList().getPlayer(signal.playerId());
			signal.getPointRunner().serverTick(levelAccessor, signal, serverPlayer, playerMemberHashMap.get(serverPlayer).getBelongTeam());
		}
	}

	/**
	 * 创建信号标点
	 *
	 * @param member       玩家
	 * @param pointData    信号点
	 * @param lookLocation 玩家准心指向的位置
	 */
	public void createSignal(Member member, PointData pointData, Vec3 lookLocation) {
		ServerLevel serverLevel = (ServerLevel) member.getPlayer().level();
		Signal signal = new Signal(pointData, member.getPlayer().getUUID(), lookLocation, serverLevel.dimension());
		appliedSignals.addSignal(signal);
	}

	/**
	 * 获取玩家对应团队成员身份
	 *
	 * @param player 玩家（无团队会自动加入默认团队）
	 * @return 有团队的玩家
	 */
	public Member getMember(ServerPlayer player) {
		if (playerMemberHashMap.containsKey(player)) {
			return playerMemberHashMap.get(player);
		} else {
			Member member = new Member(player, defaultTeam);
			defaultTeam.addMember(member);
			playerMemberHashMap.put(player, member);
			return member;
		}
	}

	/**
	 * 设置玩家团队
	 *
	 * @param member 玩家
	 * @param team   团队
	 */
	public void setPlayerTeam(Member member, Team team) {
		member.setBelongTeam(team);
		team.addMember(member);
	}

	/**
	 * 创建团队（一个新团队要有一个玩家）
	 *
	 * @param mainPlayer 创建团队的玩家
	 */
	public void createTeam(ServerPlayer mainPlayer) {
		Team team = new Team();
		Member member = playerMemberHashMap.containsKey(mainPlayer) ? playerMemberHashMap.get(mainPlayer) : new Member(mainPlayer, team);
		setPlayerTeam(member, team);
		teams.add(team);
	}

	/**
	 * 获取所有团队
	 *
	 * @return 所有团队
	 */
	public ArrayList<Team> getTeams() {
		return teams;
	}

	public LevelAccessor getLevelAccessor() {
		return levelAccessor;
	}

	/**
	 * 团队信号点运行器
	 */
	public static class AppliedSignals {
		private final ConcurrentHashMap<Long, Signal> signalMap = new ConcurrentHashMap<>();
		private final ConcurrentHashMap<Long, Long> expirationTimeMap = new ConcurrentHashMap<>();
		private final SignalManager signalManager;
		Long maxID = 0L;
		PriorityQueue<Long> expirationIDs = new PriorityQueue<>();

		public AppliedSignals(SignalManager signalManager) {
			this.signalManager = signalManager;
		}

		public void addSignal(Signal signal) {
			removeExpiredSignals();
			if (expirationIDs.isEmpty()) {
				signalMap.put(maxID, signal);
				expirationTimeMap.put(maxID, System.currentTimeMillis() + expiration_time_mills);
				maxID++;
			} else {
				// 弃置 ID 回收
				long id = expirationIDs.poll();
				signalMap.put(id, signal);
				expirationTimeMap.put(id, System.currentTimeMillis() + expiration_time_mills);
			}
		}

		/**
		 * 移除过期信号点
		 */
		private void removeExpiredSignals() {
			if (expirationTimeMap.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis())) {
				signalMap.entrySet().removeIf(entry -> !expirationTimeMap.containsKey(entry.getKey()));
			}
		}

		public ConcurrentHashMap<Long, Signal> getSignalMap() {
			removeExpiredSignals();
			return signalMap;
		}

		public SignalManager getSignalManager() {
			return signalManager;
		}
	}
}
