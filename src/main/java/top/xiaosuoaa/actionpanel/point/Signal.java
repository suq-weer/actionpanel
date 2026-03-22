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

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static top.xiaosuoaa.actionpanel.register.PointRegister.POINT_REGISTRY;

public record Signal(PointData pointData, UUID playerId, Vec3 pos, ResourceKey<Level> signalLevel) {
	public static Signal SignalFromDataTag(CompoundTag tag) {
		ResourceKey<Level> level = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dimension")));
		PointData pointData = POINT_REGISTRY.get(ResourceLocation.parse(tag.getString("id")));
		UUID playerId = UUID.fromString(tag.getString("sourcePlayerUUID"));
		Vec3 pos = new Vec3(tag.getDouble("x"), tag.getDouble("y"), tag.getDouble("z"));
		return new Signal(pointData, playerId, pos, level);
	}

	public CompoundTag SignalToDataTag() {
		CompoundTag tag = new CompoundTag();
		tag.putString("id", this.pointData().getResourceLocation().toString());
		tag.putString("sourcePlayerUUID", this.playerId().toString());
		tag.putString("dimension", this.signalLevel().location().toString());
		tag.putDouble("x", this.pos().x());
		tag.putDouble("y", this.pos().y());
		tag.putDouble("z", this.pos().z());
		return tag;
	}

	public PointRunner getPointRunner() {
		return pointData.runner();
	}

	public double getX() {
		return pos.x();
	}

	public double getY() {
		return pos.y();
	}

	public double getZ() {
		return pos.z();
	}

	public @NotNull String toString() {
		return "Signal{" +
				"pointData=" + pointData +
				", playerId=" + playerId +
				", pos=" + pos +
				", signalLevel=" + signalLevel +
				'}';
	}
}
