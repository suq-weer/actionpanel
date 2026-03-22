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

package top.xiaosuoaa.actionpanel.client.util.gui;

import net.minecraft.client.gui.GuiGraphics;

public class GUIStyleUtil {
	/**
	 * 渲染一个正方形
	 *
	 * @param guiGraphics 参见 {@link GuiGraphics}
	 * @param color       颜色
	 * @param x           X坐标
	 * @param y           Y坐标
	 * @param scale       边长
	 */
	public static void displayBlock(GuiGraphics guiGraphics, int color, int x, int y, int scale) {
		guiGraphics.fill(x, y, x + scale, y + scale, color);
	}
}
