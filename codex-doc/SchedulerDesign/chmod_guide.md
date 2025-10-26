# Linux chmod 数字权限速查（rwx 与特殊位）

## 1. 基本概念
- 对象：三类主体的权限分别设置，顺序为“用户(u/owner) – 组(g/group) – 其他(o/others)”。
- 权限位：
  - 读 r = 4
  - 写 w = 2
  - 执行 x = 1
- 数字表示法：每一位是三种权限位相加的和（r+w+x）。例如：
  - 7 = 4+2+1 = rwx
  - 6 = 4+2 = rw-
  - 5 = 4+1 = r-x
  - 4 = 4 = r--

## 2. 常见数字组合（文件/目录）
- 777 = rwx·rwx·rwx（所有人读写执行，风险高，不建议）
- 755 = rwx·r-x·r-x（可执行程序/目录的常见权限）
- 750 = rwx·r-x·---（仅用户与同组可读执行）
- 700 = rwx·---·---（仅属主可读写执行）
- 644 = rw-·r--·r--（普通文本/配置文件常见）
- 640 = rw-·r--·---（组可读，其他无权）
- 600 = rw-·---·---（仅属主可读写，含私钥等敏感文件）
- 664 = rw-·rw-·r--（协作编辑但限制其他）

说明：
- “文件”的 x 表示可执行；“目录”的 x 表示可进入/遍历（cd）。
- 列目录需要目录的 r（可读目录项）+ x（可进入）；创建/删除需要目录的 w + x。

## 3. 目录与可执行位的语义
- 目录 x：允许进入目录（cd）与访问其中文件（需配合文件自身权限）。
- 目录 r：允许列出目录清单（ls）。
- 目录 w：允许在该目录内创建/删除/重命名文件或子目录（通常还需要 x）。

## 4. 特殊权限位（千位/四位数写法）
- setuid（4xxx）
  - 作用：可执行文件以文件属主身份运行。
  - 示例：`chmod 4755 prog`（最高位 4），显示为 `-rwsr-xr-x`（用户位的 x 变为 s）。
- setgid（2xxx）
  - 文件：可执行文件以文件属组身份运行。
  - 目录：在该目录内创建的文件继承该目录的属组。
  - 示例：`chmod 2755 dir`，目录显示为 `drwxr-sr-x`。
- sticky（1xxx）
  - 作用：目录内文件仅文件属主或 root 可删除（常见于 /tmp）。
  - 示例：`chmod 1777 /tmp`，显示为 `drwxrwxrwt`（其他位的 x 显示为 t）。

组合示例：
- 4755 = setuid + 755
- 2755 = setgid + 755
- 1777 = sticky + 777（公共可写但防互删）

## 5. 符号表示法（与数字等价）
- `chmod u=rwx,g=rx,o=rx file` 等价于 `chmod 755 file`
- `chmod u+x,g-w,o= file`
- `u/g/o/a` 分别对应 用户/组/其他/全部；操作符 `+ - =` 表示 增/减/赋值。

## 6. 查看与变更属主/属组
- 查看：`ls -l file` → 形如 `-rw-r--r-- 1 alice staff 1234 filename`
- 改属主/组：`chown alice:staff file`（需要权限）

## 7. 速查表
| 数值 | rwx 三位 | 含义 |
|---:|:---:|---|
| 0 | --- | 无任何权限 |
| 1 | --x | 仅执行 |
| 2 | -w- | 仅写入 |
| 3 | -wx | 写+执行 |
| 4 | r-- | 仅读取 |
| 5 | r-x | 读+执行 |
| 6 | rw- | 读+写 |
| 7 | rwx | 读+写+执行 |

## 8. 常用命令示例
- 递归修改目录及其内容：`chmod -R 755 /opt/myapp`
- 保护私钥：`chmod 600 ~/.ssh/id_rsa`
- 共享目录（同组可写）：`chmod 2775 /data/shared`（setgid + 775）

## 9. 风险与建议
- 避免 `777`：带来安全与供应链风险；按需最小授权。
- 目录写入：创建/删除通常需要 `w` 与 `x` 同时具备。
- 可执行位：仅对真正的可执行文件（脚本需有 shebang 并具可执行位）。
- 审计：对公共可写目录使用 sticky；对团队共享目录使用 setgid。

---
参考：
- 文件与目录权限判定是多因素叠加（对象权限 + umask + ACL + SELinux/AppArmor）。本文仅覆盖经典 UNIX 权限位与常见特殊位。

## 10. 文件/目录操作与所需权限对照表

| 对象 | 操作 | 需要的权限 |
|---|---|---|
| 文件 | 读取内容 | 文件的 r |
| 文件 | 写入/修改 | 文件的 w（且所在目录具备 w+x 以便创建临时/替换） |
| 文件 | 执行 | 文件的 x（脚本还需正确 shebang 与可执行解释器） |
| 目录 | 进入目录（cd）/访问子路径 | 目录的 x |
| 目录 | 列出文件（ls） | 目录的 r + x |
| 目录 | 创建/删除/重命名文件 | 目录的 w + x（与文件自身权限无关） |

提示：删除文件的权限取决于“目录”的 w+x，而非目标文件的 w。

## 11. s/S 与 t/T 的显示规则（ls -l）

- setuid（用户位）
  - 若用户位含 x 且设置了 setuid，则显示为 `s`：如 `-rwsr-xr-x`
  - 若无 x 但设置了 setuid，则显示为 `S`：如 `-rwSr-xr-x`
- setgid（组位）
  - 组位含 x → `s`；无 x → `S`：如 `drwxr-sr-x` / `drwxr-Sr-x`
- sticky（其他位）
  - 其他位含 x → `t`；无 x → `T`：如 `drwxrwxrwt`（/tmp 常见）

## 12. umask 速查（新建默认权限）

- 规则：新建文件/目录权限 = 默认权限 − umask（文件默认 666，目录默认 777）
- 常见 umask 与结果：

| umask | 新文件 | 新目录 |
|---:|---:|---:|
| 022 | 644 | 755 |
| 027 | 640 | 750 |
| 077 | 600 | 700 |

查看/设置：`umask` / `umask 027`（对当前 shell 生效；系统级通常写入 profile 或 service unit）

## 13. ACL 快速入门（可选进阶）

- 查看：`getfacl file`
- 赋权：`setfacl -m u:bob:rw file`（给 bob 读写）
- 目录继承：`setfacl -Rdm g:dev:rwX /data/shared`（默认 ACL 让新文件继承）
  - `X` 表示“对目录或已有 x 的文件赋 x”，避免普通文件批量变可执行

注意：ACL 与传统 u/g/o 权限叠加生效；优先使用最小授权，ACL 用于细分例外。

## 14. 常见场景配方（实操）

- 应用安装目录 `/opt/myapp`
  - 属主：`appuser:app`
  - 目录：`750`（其他人不可读）；文件：`640`
  - 命令：
    ```bash
    chown -R appuser:app /opt/myapp
    find /opt/myapp -type d -exec chmod 750 {} +
    find /opt/myapp -type f -exec chmod 640 {} +
    ```
- 可执行脚本 `bin/*.sh`
  - 确保 LF 行尾 + shebang（如 `#!/usr/bin/env bash`）
  - `chmod 755 bin/*.sh`
- 日志目录 `/var/log/myapp`
  - 属主：`appuser:adm`；目录使用 setgid 便于组内共享：`chmod 2775 /var/log/myapp`
  - 日志文件：`640`；目录：`2755` 或 `2750`（按是否允许其他读取）
- 公共临时目录
  - `/tmp` → `1777`；项目私有临时目录 `/var/tmp/myapp` → `1770`（sticky + 组写）

## 15. 安全检查清单

- 查找 world-writable（公共可写）路径：
  ```bash
  find /path -xdev -type d -perm -0002 -print
  find /path -xdev -type f -perm -0002 -print
  ```
- 查找 suid/sgid 可执行文件：
  ```bash
  find / -xdev -perm -4000 -o -perm -2000 -type f -print 2>/dev/null
  ```
- 仅在明确需要的场景使用 suid/sgid；避免对可被覆盖或可被非特权用户写入的位置设置可执行位。

## 16. 递归变更属主/属组的注意

```bash
chown -R appuser:app /srv/myapp
# 保持符号链接本身的属主属组（不跟随）：
chown -h appuser:app symlink
```

## 17. SELinux/AppArmor 提示

- SELinux 开启时，权限通过上下文进一步约束；查看：`sestatus`；临时宽松：`setenforce 0`（不建议长期）
- 审计日志通常位于 `/var/log/audit/audit.log`；结合 `ausearch`/`sealert` 分析拒绝原因
