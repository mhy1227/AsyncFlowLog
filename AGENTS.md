# AGENTS.md - 核心工作规则

---

## 0. 必读要点（MUST）

- 必写“前置说明”，并在答复末尾附“工具调用简报”（若发生外呼）。
- 遵循退避策略：429 → 退避 20s；5xx/超时 → 退避 2s 且最多一次重试，如仍失败需给出保守离线答案并说明局限。
- 严格网络只读与合规要求：避免上传敏感信息，优先引用官方及权威来源。
- 对话统一使用中文；生成与编辑文件需采用 UTF-8（无 BOM）。
- 编码前需输出“Sequential-Thinking 分析”；代码或文档改动应控制在最小必要范围。
- 禁止执行危险命令（如 `rm -rf`）、泄露密钥/令牌/内部链接。
- 涉及“任务编排/待办清单/模板/里程碑与状态跟踪”时，必须调用 `shrimp-task-manager`。
- 变更策略：正确性优先，不强求向后兼容；必要时进行颠覆性改动，清理过时内容，并在 PR 中提供迁移指引或声明“无迁移、直接替换”。

---

## 1. MCP 选择规则

- `SequentialThinking`：用于规划、分解、里程碑设计；仅输出可执行计划，不暴露中间推理。
- `Context7`：查询官方文档/API/版本差异；先执行 `resolve-library-id`，再 `get-library-docs`；必须提供 `topic`，`tokens` ≤ 5000；回复需注明库 ID、版本与出处。
- `DuckDuckGo`：检索最新网页/公告/官方链接；使用 12 个精准关键词与限定词（如 `site:`、`filetype:`、`after:YYYY-MM`）；`safesearch=moderate`，`maxResults≤35`，`timeout=5s`，去重域名并剔除内容农场。
- `Serena`（可选）：跨文档语义检索/总结/规划；需指定数据域/路径；`retrieve.top_k=5`，`answer.max_tokens=700`，`citations=true`。
- `shrimp-task-manager`：当需要任务编排、待办模板或状态看板时必须使用（本地目录 `.shrimp`，中文模板可用，`ENABLE_GUI=true`）。
- 降级策略：`Context7` 无法使用时转向 `DuckDuckGo`；`DuckDuckGo` 不可用时给出保守离线答案；`SequentialThinking` 或 `Serena` 受限时仅输出最小可行计划。

---

## 2. 最小工作流（R → P → I → V → D）

1. **Research**：使用 `rg -n` 分析代码或文档，记录约束与未知。
2. **Plan**：调用 `update_plan` 维护步骤状态，明确验证标准（简单任务可跳过计划工具）。
3. **Implement**：通过 `apply_patch` 或等效方式小步提交，保持最小变更；必要时添加少量中文注释提升可读性。
4. **Verify**：运行构建或测试；评估边界与性能风险，确保无破坏性影响。
5. **Deliver**：总结变更、风险与验证结果；若调用外部 MCP，必须附“工具调用简报”。

---

## 3. MCP 执行命令（以 `.codex/config.toml` 为准）

- `sequential-thinking`（stdio）：`npx -y @modelcontextprotocol/server-sequential-thinking`
- `context7`（stdio）：`npx -y @upstash/context7-mcp`
- `memory`（stdio）：`npx -y @modelcontextprotocol/server-memory`
- `shrimp-task-manager`（stdio）：`npx -y mcp-shrimp-task-manager`（`DATA_DIR=.shrimp`，`TEMPLATES_USE=zh`，`ENABLE_GUI=true`）
- `duckduckgo-search`（stdio）：`uvx duckduckgo-mcp-server`
- `fetch`（stdio）：`uvx mcp-server-fetch`
- `serena`（stdio）：`uvx --from git+https://github.com/oraios/serena serena start-mcp-server --context codex`

---

## 4. 工具调用简报模板

```
工具: <SequentialThinking|Context7|DuckDuckGo|Serena|shrimp-task-manager>
触发原因: <为何需要该工具>
输入摘要: <关键词/库ID/Topic/查询意图>
参数: <tokens/结果数/时间窗 等>
结果概览: <条数/命中/主要来源域名 或 库ID>
重试/退避: <无|20s|2s + 重试1次>
时间: <UTC 时间戳>
来源: <Context7 的库ID/版本；DuckDuckGo 的来源域名清单；shrimp 的任务模板/清单 ID/路径>
```

---

## 5. 违规处理要求

- 若发现未遵守“最小必要变更 / 简报 / 退避 / UTF-8 / 中文”等 MUST 项，需立即停止外呼并回到“研究/计划”阶段补齐缺项。
- 在回复中明确指出修正动作与理由，并提供保守离线答案、说明局限及下一步建议。
- 若多次违规，需在 PR 中补充“回退策略/开关”，必要时分拆任务以降低风险。

---

> 本文件用于指导在 `AsyncFlowLog` 项目中的协作流程与工具使用，所有贡献者应严格遵守上述约束。
