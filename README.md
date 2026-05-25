# OffHand Attack

`OffHandAttack` 是一个基于 Minecraft Forge 1.20.1 的实验性近战模组，用来给玩家提供两类能力：

- 主副手交替攻击
- 双手武器占用副手

当前版本采用纯标签判定，不依赖配置界面或物品白名单代码。只要给物品打上对应标签，就可以接入功能。

# 参考MOD：
https://github.com/ZsoltMolnarrr/BetterCombat/tree/1.20.1
开源协议：https://github.com/ZsoltMolnarrr/BetterCombat/blob/1.20.1/LICENSE

https://github.com/BunnyCinnamon/OffHandCombat
开源协议：https://github.com/BunnyCinnamon/OffHandCombat/blob/master/LICENSE

## 环境

- Minecraft: `1.20.1`
- Forge: `47.4.6`
- Java: `17`
- Mod ID: `offhandattack`

## 功能说明

### 1. 主副手交替攻击

当玩家主手和副手的物品都带有 `offhandattack:is_duel` 标签时：

- 左键攻击会在主手与副手之间交替进行
- 轮到副手时，客户端会拦截原版攻击输入
- 模组通过网络包通知服务端执行一次副手攻击
- 服务端临时交换主副手物品，复用原版 `player.attack()` 逻辑完成伤害计算

这意味着：

- 双持短剑、匕首、拳套之类武器时，可以做出交替挥击效果
- 副手攻击仍然走原版攻击流程，兼容性通常比完全重写战斗逻辑更好

### 2. 双手武器逻辑

当主手物品带有 `offhandattack:is_hands` 标签时：

- 模组会暂存副手物品
- 玩家视觉和逻辑上都会进入“主手双手持武器”状态
- 切换回非双手武器后，之前暂存的副手物品会自动返还

如果副手物品本身带有 `offhandattack:is_hands` 标签，但没有 `offhandattack:can_hands_use` 标签，则：

- 禁止副手右键使用物品
- 禁止副手对方块交互
- 禁止副手对实体交互

适合用来限制大剑、长柄武器、重型武器等不应在副手正常使用的物品。

## 标签用法

本项目当前识别以下 3 个物品标签：

- `offhandattack:is_duel`
- `offhandattack:is_hands`
- `offhandattack:can_hands_use`

你可以把这些标签写在本模组内，也可以由数据包或别的模组提供。

### `offhandattack:is_duel`

用于声明“可参与主副手交替攻击”的物品。

示例：

```json
{
  "replace": false,
  "values": [
    "yourmod:iron_dagger",
    "yourmod:steel_dagger"
  ]
}
```

建议文件路径：

```text
data/offhandattack/tags/items/is_duel.json
```

### `offhandattack:is_hands`

用于声明“双手武器”或“占用副手”的物品。

示例：

```json
{
  "replace": false,
  "values": [
    "yourmod:greatsword",
    "yourmod:warhammer"
  ]
}
```

建议文件路径：

```text
data/offhandattack/tags/items/is_hands.json
```

### `offhandattack:can_hands_use`

用于声明“即使位于副手，也允许继续右键使用”的物品。一般只给特殊例外项使用。

示例：

```json
{
  "replace": false,
  "values": [
    "yourmod:special_tool"
  ]
}
```

建议文件路径：

```text
data/offhandattack/tags/items/can_hands_use.json
```

## 实际接入建议

常见搭配方式如下：

- 匕首、短刀、拳刃：加入 `offhandattack:is_duel`
- 大剑、战锤、长柄重武器：加入 `offhandattack:is_hands`
- 需要在副手保留右键能力的特殊物品：额外加入 `offhandattack:can_hands_use`

如果你要做“只能双持轻武器、重武器必须双手持”的玩法，通常只需要维护这 3 个标签即可。

## 开发与运行

### 运行客户端

```powershell
.\gradlew runClient
```

### 运行服务端

```powershell
.\gradlew runServer
```

### 构建产物

```powershell
.\gradlew build
```

构建完成后，Jar 通常位于：

```text
build/libs/
```

## 实现概要

项目当前的核心实现分为几部分：

- `MinecraftMixin`：在客户端拦截攻击输入，决定当前是否轮到副手攻击
- `OffHandAttackPacket` / `OffHandSwingPacket`：把副手攻击或挥空动作发给服务端
- `OffhandStateSyncPacket`：同步当前主副手轮转状态
- `OffHandData`：保存玩家当前轮转状态和被双手武器暂存的副手物品
- `ModEvents`：处理攻击切换、副手限制、玩家登录同步、双手武器副手暂存

当前版本没有启用可配置白名单逻辑，判定完全基于标签。

## 注意事项

- 只有主手和副手都命中 `offhandattack:is_duel` 时，才会触发交替攻击
- 双手武器逻辑会临时收起副手物品，请避免与其他强改玩家背包/副手槽的模组同时改同一时机
- 副手攻击通过临时交换主副手物品来复用原版逻辑，因此若其他模组强依赖“攻击瞬间的手部槽位”，需要额外做兼容验证

## 参考代码来源

为便于追溯，这个本地项目保留原始参考仓库来源：

- Git 远程 `origin`：<https://github.com/3128194922/OffHandAttack.git>

如果你后续继续修改本项目，建议保留这一节，避免参考来源丢失。

## License

项目配置中声明的许可证为 `MIT`。若后续补充正式 `LICENSE` 文件，请以仓库实际文件为准。
