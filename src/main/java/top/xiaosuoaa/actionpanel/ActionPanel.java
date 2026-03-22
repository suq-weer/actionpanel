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

package top.xiaosuoaa.actionpanel;

import com.mojang.logging.LogUtils;
import eu.midnightdust.lib.config.MidnightConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.slf4j.Logger;
import top.xiaosuoaa.actionpanel.client.network.HandlerClient;
import top.xiaosuoaa.actionpanel.config.MainConfig;
import top.xiaosuoaa.actionpanel.controller.SignalManager;
import top.xiaosuoaa.actionpanel.network.HandlerServer;
import top.xiaosuoaa.actionpanel.network.packet.ClientSignalSendPacket;
import top.xiaosuoaa.actionpanel.network.packet.ToSyncClientSignalsPacket;
import top.xiaosuoaa.actionpanel.register.PointRegister;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ActionPanel.MOD_ID)
@EventBusSubscriber(modid = ActionPanel.MOD_ID)
public class ActionPanel {
	// Define mod id in a common place for everything to reference
	public static final String MOD_ID = "action_panel";
	// Directly reference a slf4j logger
	public static final Logger LOGGER = LogUtils.getLogger();
	public static SignalManager signalManager = null;

	public ActionPanel(IEventBus modEventBus, ModContainer modContainer) {
		PointRegister.register(modEventBus);
		MidnightConfig.init(MOD_ID, MainConfig.class);
	}

	@SubscribeEvent
	public static void commonSetup(FMLCommonSetupEvent event) {

	}

	@SubscribeEvent
	public static void registerRegistries(NewRegistryEvent event) {
		PointRegister.registerRegistry(event);
	}

	@SubscribeEvent
	public static void loadLevel(LevelEvent.Load event) {
		// 初始化信号管理
		signalManager = new SignalManager(event.getLevel());
	}

	@SubscribeEvent
	public static void unloadLevel(LevelEvent.Unload event) {
		// 卸载信号管理
		signalManager = null;
	}

	@SubscribeEvent
	public static void register(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar("1")
				.executesOn(HandlerThread.NETWORK); // 所有后续有效载荷都会在网络线程上注册
		registrar.playBidirectional(
				ClientSignalSendPacket.TYPE,
				ClientSignalSendPacket.CODEC,
				new DirectionalPayloadHandler<>(
						(clientSignalSendPacket, iPayloadContext) -> {
						},
						HandlerServer::signalDataOnNetwork
				)
		);
		registrar.playBidirectional(
				ToSyncClientSignalsPacket.TYPE,
				ToSyncClientSignalsPacket.CODEC,
				new DirectionalPayloadHandler<>(
						HandlerClient::syncClientPoints,
						(toSyncClientSignalsPacket, iPayloadContext) -> {
						}
				)
		);
	}

	@SubscribeEvent
	public static void ServerTickEvent(ServerTickEvent.Post event) {
		if (signalManager != null) {
			signalManager.syncDataToAllMembers();
			signalManager.serverTickBus(event);
		}
	}
}
