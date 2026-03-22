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

package top.xiaosuoaa.actionpanel.client.keymapping;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import top.xiaosuoaa.actionpanel.ActionPanelClient;
import top.xiaosuoaa.actionpanel.client.gui.PointPanel;
import top.xiaosuoaa.actionpanel.client.util.PointPanelStatus;
import top.xiaosuoaa.actionpanel.util.ResUtil;

public class OpenPanel extends KeyMapping {
	private static final Minecraft MINECRAFT = Minecraft.getInstance();

	public OpenPanel() {
		super(
				ResUtil.getI18nPath(ResUtil.Category.KEY, "open_point_panel"),
				InputConstants.Type.KEYSYM,
				GLFW.GLFW_KEY_P,
				ResUtil.getI18nPath(ResUtil.Category.CATEGORY, "action_panel")
		);
	}

	public static void run() {
		ActionPanelClient.setPointPanelStatus(PointPanelStatus.ENABLED);
		MINECRAFT.mouseHandler.releaseMouse();
	}

	public static void post() {
		// 释放按键时立即执行仅一次 sendSignal()
		PointPanel.releaseKey(ActionPanelClient.getCurrentSector());
		ActionPanelClient.setPointPanelStatus(PointPanelStatus.DISABLED);
		MINECRAFT.mouseHandler.grabMouse();
	}
}