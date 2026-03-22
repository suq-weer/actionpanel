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

package top.xiaosuoaa.actionpanel.register;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;
import top.xiaosuoaa.actionpanel.point.PointData;
import top.xiaosuoaa.actionpanel.point.entity.EnemyPoint;
import top.xiaosuoaa.actionpanel.point.entity.NormalPoint;
import top.xiaosuoaa.actionpanel.util.ResUtil;

import java.util.function.Supplier;

import static top.xiaosuoaa.actionpanel.ActionPanel.MOD_ID;

public class PointRegister {
	public static final ResourceKey<Registry<PointData>> POINT_REGISTRY_KEY = ResourceKey.createRegistryKey(ResUtil.getRes("point"));
	public static final Registry<PointData> POINT_REGISTRY = new RegistryBuilder<>(POINT_REGISTRY_KEY)
			.sync(true)
			.defaultKey(ResUtil.getRes("default_point"))
			.maxId(256)
			.create();
	public static final DeferredRegister<PointData> POINTS = DeferredRegister.create(POINT_REGISTRY, MOD_ID);

	public static final Supplier<NormalPoint> DEFAULT_POINT = POINTS.register("default_point", NormalPoint::new);
	public static final Supplier<EnemyPoint> ENEMY_POINT = POINTS.register("enemy_point", EnemyPoint::new);

	public static void registerRegistry(NewRegistryEvent event) {
		event.register(POINT_REGISTRY);
	}

	public static void register(IEventBus bus) {
		POINTS.register(bus);
	}
}
