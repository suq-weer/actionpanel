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
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.progress.ProgressMeter;
import net.neoforged.fml.loading.progress.StartupNotificationManager;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.slf4j.Logger;
import top.xiaosuoaa.actionpanel.client.controller.SignalDisplayer;
import top.xiaosuoaa.actionpanel.client.gui.PointPanel;
import top.xiaosuoaa.actionpanel.client.gui.PointSeek;
import top.xiaosuoaa.actionpanel.client.keymapping.KeyMappingRegistry;
import top.xiaosuoaa.actionpanel.client.keymapping.OpenPanel;
import top.xiaosuoaa.actionpanel.client.point.PointClient;
import top.xiaosuoaa.actionpanel.client.util.PointPanelStatus;
import top.xiaosuoaa.actionpanel.point.PointData;
import top.xiaosuoaa.actionpanel.util.ResUtil;

import java.util.HashMap;
import java.util.Set;

import static top.xiaosuoaa.actionpanel.client.keymapping.KeyMappingRegistry.OPEN_POINT_PANEL;
import static top.xiaosuoaa.actionpanel.register.PointRegister.POINT_REGISTRY;

@Mod(value = ActionPanel.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ActionPanel.MOD_ID, value = Dist.CLIENT)
public class ActionPanelClient {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static HashMap<ResourceLocation, PointClient> pointDataPointClientHashMap = new HashMap<>();
	public static SignalDisplayer signalDisplayer = null;
	public static double fov = 0;
	private static PointPanelStatus pointPanelStatus = PointPanelStatus.DISABLED;
	private static boolean press = false;
	private static int currentSector = 0;

	public ActionPanelClient(ModContainer container, IEventBus modEventBus) {
	}

	@SubscribeEvent
	static void onClientSetup(FMLClientSetupEvent event) {
		// 渲染标点
		Set<ResourceLocation> resourceLocations = POINT_REGISTRY.keySet();
		int size = resourceLocations.size();
		StartupNotificationManager.addModMessage("[Action Panel] Rendering " + size + " points...");
		ProgressMeter progressMeter = StartupNotificationManager.addProgressBar("[Action Panel] Rendering Points...", size);
		int i = 0;
		for (ResourceLocation key : resourceLocations) {
			progressMeter.setAbsolute(++i);
			progressMeter.label("[Action Panel] Rendering Points... (" + i + "/" + resourceLocations.size() + ")");
			PointData pointData = POINT_REGISTRY.get(key);
			if (pointData != null) {
				pointDataPointClientHashMap.put(pointData.getResourceLocation(), new PointClient(pointData));
			}
		}
		progressMeter.complete();
		StartupNotificationManager.addModMessage("[Action Panel] Rendered " + size + " points.");
		LOGGER.info("已渲染 {} 个快捷标点", size);
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		// 面板打开逻辑
		boolean isDown = OPEN_POINT_PANEL.get().isDown();

		if (isDown && !press) {
			// 按键刚按下
			OpenPanel.run();
			press = true;
		} else if (!isDown && press) {
			// 按键刚释放
			OpenPanel.post();
			press = false;
		}
	}

	// 注册按键
	@SubscribeEvent
	static void registerKeyMappings(RegisterKeyMappingsEvent event) {
		KeyMappingRegistry.register(event);
	}

	public static int getPointPanelStatus() {
		return pointPanelStatus.ordinal();
	}

	public static void setPointPanelStatus(PointPanelStatus pointPanelStatus) {
		ActionPanelClient.pointPanelStatus = pointPanelStatus;
	}

	public static int getCurrentSector() {
		return currentSector;
	}

	public static void setCurrentSector(int sector) {
		currentSector = sector;
	}

	// 注册GUI层
	@SubscribeEvent
	public static void registerGuiLayers(RegisterGuiLayersEvent event) {
		event.registerBelowAll(ResUtil.getRes("point_panel"), PointPanel::new);
		event.registerBelowAll(ResUtil.getRes("point_seek"), PointSeek::new);
	}

	// 渲染世界时
	@SubscribeEvent
	public static void renderLevel(LevelEvent.Load event) {
		signalDisplayer = new SignalDisplayer(event.getLevel());
	}
}
