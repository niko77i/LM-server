import os, re

SRC = r"D:\server\cc\LM-Server\src\main\java\com\lmserver\controller"

modules = {}
for root, dirs, files in os.walk(SRC):
    for f in sorted(files):
        if not f.endswith('.java'): continue
        path = os.path.join(root, f)
        with open(path, 'r', encoding='utf-8') as fp:
            content = fp.read()

        # 找 @RequestMapping prefix
        prefix = ''
        m = re.search(r'@RequestMapping\("([^"]+)"\)', content)
        if m: prefix = m.group(1)

        # 找类注释
        class_cmt = ''
        cm = re.search(r'/\*\*\s*\n\s*\*\s*(.+?)\s*\n\s*\*/', content)
        if cm: class_cmt = cm.group(1).strip()

        eps = []
        for method in ['GetMapping', 'PostMapping', 'PutMapping', 'DeleteMapping']:
            for m in re.finditer(r'@' + method + r'\(\"([^\"]+)\"\)', content):
                path_part = m.group(1)
                # 找方法注释
                prev_lines = content[:m.start()].split('\n')
                cmt = ''
                for i in range(len(prev_lines)-1, max(0, len(prev_lines)-6), -1):
                    line = prev_lines[i].strip()
                    if line.startswith('/**'):
                        cmt = line.replace('/**', '').replace('*/', '').strip()
                        break
                    if line.startswith('*'):
                        cmt = line.lstrip('* ').strip()
                        break
                method_name = method.replace('Mapping', '')
                full_path = prefix + path_part
                eps.append((method_name, full_path, cmt))

        if eps:
            name = f.replace('.java', '')
            modules[name] = (class_cmt, eps)

# 输出 Markdown
lines = []
lines.append('# LM-Server API 文档')
lines.append('')
lines.append('> 自动生成于 2026-08-03 | 基础路径: http://localhost:8080')
lines.append('')
lines.append('## 接口总览')
lines.append('')
lines.append('| 模块 | 接口数 |')
lines.append('|------|--------|')

for name, (cmt, eps) in sorted(modules.items()):
    lines.append(f'| {name} | {len(eps)} |')

lines.append('')
total = sum(len(eps) for _, (_, eps) in modules.items())
lines.append(f'**总计: {total} 个接口**')
lines.append('')

for name, (cmt, eps) in sorted(modules.items()):
    lines.append(f'---')
    lines.append(f'## {name}')
    if cmt:
        lines.append(f'> {cmt}')
    lines.append('')
    lines.append('| 方法 | 路径 | 说明 |')
    lines.append('|------|------|------|')
    for method, path, desc in eps:
        desc_text = desc if desc else '-'
        lines.append(f'| {method} | `{path}` | {desc_text} |')
    lines.append('')

with open(r"D:\server\cc\LM-Server\docs\api\api-reference.md", 'w', encoding='utf-8') as f:
    f.write('\n'.join(lines))

print(f'{len(modules)} modules, {total} endpoints')
PYEOF