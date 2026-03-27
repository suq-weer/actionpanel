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

package top.xiaosuoaa.actionpanel.point.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import top.xiaosuoaa.actionpanel.point.PointData;
import top.xiaosuoaa.actionpanel.point.PointRunner;
import top.xiaosuoaa.actionpanel.point.PointRunnerUtil;
import top.xiaosuoaa.actionpanel.util.ResUtil;

public class AttackPoint extends PointData {
	public static final int COLOR = 0xFF4000;
	public static final ResourceLocation RES = ResUtil.getRes("textures/gui/custom/point_attack.png");
	private static final PointRunner RUNNER = PointRunnerUtil
			.displayFirstMessage(COLOR, Component.translatable("point.attack.message"))
			.build();

	public AttackPoint() {
		super(
				Component.translatable("point.attack.name"),
				RES,
				true,
				COLOR,
				RUNNER,
				false,
				Component.translatable("point.attack.description")
		);
	}
}
