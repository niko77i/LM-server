import os, re

SRC = r"D:\server\cc\LM-Server\src\main\java\com\lmserver"
count = 0

for root, dirs, files in os.walk(SRC):
    for f in files:
        if not f.endswith('.java'): continue
        path = os.path.join(root, f)
        with open(path, 'r', encoding='utf-8') as fp:
            lines = fp.readlines()

        # Find class line
        class_line = -1
        for i, line in enumerate(lines):
            if re.search(r'public (?:class|interface|enum) \w+', line):
                class_line = i
                break
        if class_line < 0: continue

        # Find class javadoc: the last /** block before class
        # Look backwards from class_line for /** ... */
        javadoc_start = -1
        for i in range(class_line - 1, max(0, class_line - 15), -1):
            if lines[i].strip().startswith('/**'):
                javadoc_start = i
                break
        if javadoc_start < 0: continue

        # Find javadoc end
        javadoc_end = -1
        for i in range(javadoc_start, class_line):
            if '*/' in lines[i]:
                javadoc_end = i
                break
        if javadoc_end < 0: continue

        # Find ALL other /** ... */ blocks between class_line-15 and class_line
        # and remove all except the one at javadoc_start
        to_remove = []
        for i in range(max(0, class_line - 20), class_line):
            if i >= javadoc_start and i <= javadoc_end: continue
            if lines[i].strip().startswith('/**') and i != javadoc_start:
                # Find end of this comment block
                end = i
                for j in range(i, class_line):
                    if '*/' in lines[j]:
                        end = j
                        break
                to_remove.append((i, end))

        # Remove duplicate javadocs (from end to start to preserve indices)
        removed = False
        for start, end in sorted(to_remove, reverse=True):
            for k in range(start, end + 1):
                lines[k] = ''  # Empty out
            removed = True

        # Also fix: method javadocs placed inside class body on wrong lines
        # (comment between @Xxx and method signature)
        # No action needed for now

        if removed:
            # Remove consecutive empty lines (more than 2)
            cleaned = []
            empty_count = 0
            for line in lines:
                if line.strip() == '':
                    empty_count += 1
                    if empty_count <= 2:
                        cleaned.append(line)
                else:
                    empty_count = 0
                    cleaned.append(line)

            with open(path, 'w', encoding='utf-8') as fp:
                fp.writelines(cleaned)
            count += 1
            print(f'  Cleaned: {os.path.relpath(path, SRC)}')

print(f'\nCleaned {count} files')
PYEOF