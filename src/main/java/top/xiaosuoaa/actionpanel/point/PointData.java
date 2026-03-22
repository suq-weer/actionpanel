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

package top.xiaosuoaa.actionpanel.point;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import static top.xiaosuoaa.actionpanel.register.PointRegister.POINT_REGISTRY;

public class PointData {
	private final Component displayName;
	private final ResourceLocation icon;
	private final boolean isImportant;
	private final int color;
	private final PointRunner runner;
	private final boolean needUnlock;

	/**
	 * 创建一个标点数据对象
	 *
	 * @param displayName 标点显示名称
	 * @param icon        标点图标
	 * @param isImportant 标点是否重要（带边框）
	 * @param color       标点颜色
	 * @param runner      标点逻辑函数
	 * @param needUnlock  标点是否需要解锁
	 * @param description 标点描述
	 */
	public PointData(Component displayName, ResourceLocation icon, boolean isImportant, int color, PointRunner runner, boolean needUnlock, Component description) {
		this.displayName = displayName;
		this.icon = icon;
		this.isImportant = isImportant;
		this.color = color;
		this.runner = runner;
		this.needUnlock = needUnlock;
	}

	public PointData(ResourceLocation dataLocation) {
		PointData data = POINT_REGISTRY.get(dataLocation);
		// 复制注册表的数据
		if (data != null) {
			this.displayName = data.displayName;
			this.icon = data.icon;
			this.isImportant = data.isImportant;
			this.color = data.color;
			this.runner = data.runner;
			this.needUnlock = data.needUnlock;
		} else {
			// 如果标点注册项不存在，则抛出异常
			throw new IllegalArgumentException("PointData not found for location: " + dataLocation);
		}
	}

	public static PointData fromString(String dataLocationString) {
		return new PointData(ResourceLocation.tryParse(dataLocationString));
	}

	public Component displayName() {
		return displayName;
	}

	public ResourceLocation icon() {
		return icon;
	}

	public boolean isImportant() {
		return isImportant;
	}

	public int color() {
		return color;
	}

	public PointRunner runner() {
		return runner;
	}

	public boolean isNeedUnlock() {
		return needUnlock;
	}

	public ResourceLocation getResourceLocation() {
		return POINT_REGISTRY.getKey(this);
	}

	public String getResourceLocationString() {
		return getResourceLocation().toString();
	}

	public String toString() {
		return getResourceLocationString();
	}
}
