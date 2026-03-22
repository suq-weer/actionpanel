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

package top.xiaosuoaa.actionpanel.client.point;

import net.minecraft.resources.ResourceLocation;
import top.xiaosuoaa.actionpanel.client.util.gui.ColorUtil;
import top.xiaosuoaa.actionpanel.point.PointData;

import javax.annotation.Nullable;

public class PointClient {
	private final PointData pointData;
	// 存储染色后的贴图资源位置
	private final ResourceLocation tintedIcon;

	public PointClient(PointData pointData) {
		this.pointData = pointData;

		// 根据 isImportant 状态选择对应的贴图文件
		String texturePath = pointData.isImportant() ? "action_panel:textures/gui/custom/point_border.png" : "action_panel:textures/gui/custom/point.png";

		// 使用 ColorUtil 对贴图进行染色
		this.tintedIcon = ColorUtil.makeTintedRL(ResourceLocation.parse(texturePath), pointData.color());
	}

	public ResourceLocation getTintedIcon() {
		return tintedIcon;
	}

	public PointData getPointData() {
		return pointData;
	}

	@Nullable
	public ResourceLocation getIcon() {
		return pointData.icon();
	}
}