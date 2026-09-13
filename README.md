# Client-Flight-Mod
[![License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)](LICENSE)
[![GitHub stars](https://img.shields.io/github/stars/ReallyChooseC/Client-Flight-Mod?style=flat-square)](https://github.com/ReallyChooseC/Client-Flight-Mod)
[![Ask DeepWiki](https://deepwiki.com/badge.svg)](https://deepwiki.com/ReallyChooseC/Client-Flight-Mod)

Fabric 客户端模组，提供飞行、鞘翅加速和免摔功能。

## 安装

安装对应游戏版本的 Fabric Loader 和 Fabric API，将模组 JAR 放入客户端的 `mods` 文件夹。

## 用法

| 指令 | 功能 |
| --- | --- |
| `/cfly toggle` | 开关飞行能力，开启后双击跳跃键起飞 |
| `/cfly elytratoggle` | 开关鞘翅加速，默认开启 |
| `/cfly nofalltoggle` | 开关免摔，默认开启 |
| `/cfly forceflighttoggle` | 持续启用飞行能力，默认关闭；开启时不能用 `toggle` 关闭飞行 |
| `/cfly speed <数值>` | 设置鞘翅速度倍率，非负数，默认 `1.0` |

可在「选项 → 控制 → 按键绑定」中设置“切换飞行”快捷键，默认未绑定。鞘翅加速支持与 Tweakeroo 飞行速度设置联动。

服务器或反作弊可能限制这些功能，不保证在所有服务器上生效。
