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

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.LevelAccessor;
import top.xiaosuoaa.actionpanel.controller.Team;

public interface PointRunner {
	/**
	 * 客户端收到新标点时触发创建逻辑，注意在这里的逻辑只会运行一次<br /><b>注：被服务端创建标点逻辑阻挡的标点不会触发客户端的创建标点逻辑</b>
	 *
	 * @param levelAccessor 存档访问器，参见 {@link LevelAccessor}
	 * @param signal        标点，参见 {@link Signal}
	 * @param sourcePlayer  创建标点的玩家，参见 {@link LocalPlayer}
	 */
	void clientPre(LevelAccessor levelAccessor, Signal signal, LocalPlayer sourcePlayer);

	/**
	 * 客户端存在该标点时每<code>Tick</code>触发逻辑
	 *
	 * @param levelAccessor 存档访问器，参见 {@link LevelAccessor}
	 * @param signal        标点，参见 {@link Signal}
	 * @param sourcePlayer  创建标点的玩家，参见 {@link LocalPlayer}
	 */
	void clientTick(LevelAccessor levelAccessor, Signal signal, LocalPlayer sourcePlayer);

	/**
	 * 客户端删除标点时触发逻辑，注意在这里的逻辑只会运行一次
	 *
	 * @param levelAccessor 存档访问器，参见 {@link LevelAccessor}
	 * @param signal        标点，参见 {@link Signal}
	 * @param sourcePlayer  创建标点的玩家，参见 {@link LocalPlayer}
	 */
	void clientPost(LevelAccessor levelAccessor, Signal signal, LocalPlayer sourcePlayer);

	/**
	 * 服务端创建标点时触发逻辑，注意在这里的逻辑只会运行一次
	 *
	 * @param levelAccessor 存档访问器，参见 {@link LevelAccessor}
	 * @param signal        标点，参见 {@link Signal}
	 * @param sourcePlayer  创建标点的玩家，参见 {@link ServerPlayer}
	 * @param belongTeam    玩家所在的队伍，参见 {@link Team}
	 * @return 标点创建前是否阻止标点的创建，<code>0</code> 为允许创建，其余数值为阻止创建<br /><b>注：被阻挡的标点不会触发客户端的创建标点逻辑</b>
	 */
	int serverPre(LevelAccessor levelAccessor, Signal signal, ServerPlayer sourcePlayer, Team belongTeam);

	/**
	 * 服务端存在该标点时每<code>Tick</code>触发逻辑
	 *
	 * @param levelAccessor 存档访问器，参见 {@link LevelAccessor}
	 * @param signal        标点，参见 {@link Signal}
	 * @param sourcePlayer  创建标点的玩家，参见 {@link LocalPlayer}
	 */
	void serverTick(LevelAccessor levelAccessor, Signal signal, ServerPlayer sourcePlayer, Team belongTeam);

	/**
	 * 服务端删除标点时触发逻辑，注意在这里的逻辑只会运行一次
	 *
	 * @param levelAccessor 存档访问器，参见 {@link LevelAccessor}
	 * @param signal        标点，参见 {@link Signal}
	 * @param sourcePlayer  创建标点的玩家，参见 {@link LocalPlayer}
	 */
	void serverPost(LevelAccessor levelAccessor, Signal signal, ServerPlayer sourcePlayer, Team belongTeam);
}
